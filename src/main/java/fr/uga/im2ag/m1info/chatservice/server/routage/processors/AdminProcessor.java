package fr.uga.im2ag.m1info.chatservice.server.routage.processors;

import fr.uga.im2ag.m1info.chatservice.common.*;
import fr.uga.im2ag.m1info.chatservice.server.registries.GroupRegistry;
import fr.uga.im2ag.m1info.chatservice.server.registries.UserRegistry;
import fr.uga.im2ag.m1info.chatservice.server.routage.StrategyContext;

import java.nio.ByteBuffer;
import java.util.Set;
import java.util.HashSet;

public class AdminProcessor implements PacketProcessor {
    private StrategyContext context;
    private UserRegistry users;
    private GroupRegistry groups;

    public AdminProcessor(StrategyContext context) {
        this.context = context;
        this.users   = context.serverState().getUserRegistry();
        this.groups  = context.serverState().getGroupRegistry();
    }

    @Override
    public void process(Packet pkt) {
        ByteBuffer payload = pkt.getPayload();

        if (payload == null || payload.remaining() < Integer.BYTES) {
            throw new IllegalArgumentException("Empty or invalid Admin payload.");
        }

        PacketType type = pkt.type();

        switch (type) {
            case CREATE_GROUP   -> handleCreateGroup(pkt.from(), payload);
            case ADD_MEMBER     -> handleAddMember(pkt.from(), payload);
            case REMOVE_MEMBER  -> handleRemoveMember(pkt.from(), payload);
            case RENAME_GROUP   -> handleRenameGroup(pkt.from(), payload);
            case DELETE_GROUP   -> handleDeleteGroup(pkt.from(), payload);
            default             -> context.sendError(pkt.from(), "Unknown group admin packet type: " + type);
        }
    }

    // ====================================================================
    // Handlers
    // ====================================================================

    /*
     * CREATE_GROUP payload (command from client):
     *   [titleLen:int][title:bytes][memberCount:int][memberId1:int]...[memberIdN:int]
     */
    private void handleCreateGroup(int adminId, ByteBuffer payload) {
        if (!users.exists(adminId)) {
            context.sendError(adminId, "Unknown user " + adminId);
            return;
        }

        String title = context.readString(payload);
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Invalid CREATE_GROUP payload.");
        }

        int memberCount = payload.getInt();

        Set<Integer> memberIds = new HashSet<>();
        for (int i = 0; i < memberCount; i++) {
            int memberId = payload.getInt();
            if (!users.exists(memberId)) {
                // Ignore unknown ids, just don't add them
                continue;
            }
            memberIds.add(memberId);
        }

        // Create the group (registry should add admin as member/admin)
        Group g = groups.createGroup(title, adminId, memberIds);

        // ACK to admin
        context.sendOk(adminId, "GROUP_CREATED with groupId: " + g.getId());

        // Broadcast to all members of the group (including admin)
        broadcastGroupCreated(g);
    }

    // ====================================================================
    // Broadcast / payload helpers
    // ====================================================================

    /**
     * Creates a full "snapshot" of the group used for:
     *  - newly added member
     *  - notification after group creation
     *
     * Snapshot payload:
     *   [subType(CREATED):1 byte]
     *   [groupId:int]
     *   [titleLen:int][title:bytes]
     *   [memberCount:int][memberId1:int]...[memberIdN:int]
     */
    private byte[] createGroupSnapshot(Group g) {
        Set<Integer> memberIds = g.getMembers();
        String title = g.getTitle();

        byte[] titleBytes = title.getBytes();
        int titleLen = titleBytes.length;

        ByteBuffer buf = ByteBuffer.allocate(
                1 +                       // subtype
                Integer.BYTES +           // groupId
                Integer.BYTES +           // titleLen
                titleLen +                // title
                Integer.BYTES +           // memberCount
                memberIds.size() * Integer.BYTES
        );

        buf.put(GroupEventType.CREATED);
        buf.putInt(g.getId());
        buf.putInt(titleLen);
        buf.put(titleBytes);
        buf.putInt(memberIds.size());
        for (int member : memberIds) buf.putInt(member);

        return buf.array();
    }

    /*
     * Broadcast the Group creation to every member of the newly created group
     * Used to update the members GroupRegistry
     *
     * Payload = snapshot (see createGroupSnapshot)
     */
    private void broadcastGroupCreated(Group g) {
        byte[] payload = createGroupSnapshot(g);
        for (int memberId : g.getMembers()) {
            context.send(Packet.createPacket(0, memberId, PacketType.GROUP_EVENT, payload));
        }
    }

    /*
     * ADD_MEMBER payload:
     *   [groupId:int][memberId:int]
     */
    private void handleAddMember(int adminId, ByteBuffer payload) {
        if (payload.remaining() < 2 * Integer.BYTES) {
            context.sendError(adminId, "Invalid ADD_MEMBER payload");
            return;
        }

        int groupId = payload.getInt();
        int memberId = payload.getInt();

        if (!groups.exists(groupId)) {
            context.sendError(adminId, "Unknown groupId: " + groupId);
            return;
        }
        if (!users.exists(memberId)) {
            context.sendError(adminId, "Unknown memberId: " + memberId);
            return;
        }
        if (!groups.hasMember(groupId, adminId)) {
            context.sendError(adminId, "You are not in this group");
            return;
        }
        if (!groups.isAdmin(groupId, adminId)) {
            context.sendError(adminId, "Can't add member, you are not the admin: " + adminId);
            return;
        }

        Group g = groups.getGroupById(groupId);
        if (g == null) {
            context.sendError(adminId, "Unknown Group.");
            return;
        }

        if (!g.addMember(memberId)) {
            context.sendError(adminId, "User already in group");
            return;
        }

        // ACK to admin
        context.sendOk(adminId, "memberId " + memberId + " added to groupId " + groupId);

        // Broadcast
        broadcastMemberAdded(g, memberId);
    }

    /*
     * Broadcast the added member:
     *
     * 1) To the newly added member:
     *       payload = snapshot (subType CREATED)
     *
     * 2) To the "old" members:
     *       payload:
     *           [subType(MEMBER_ADDED):1 byte]
     *           [groupId:int]
     *           [addedMember:int]
     */
    public void broadcastMemberAdded(Group g, int addedMember) {
        // 1) Added member: full snapshot
        byte[] addedMemberPayload = createGroupSnapshot(g);
        context.send(Packet.createPacket(0, addedMember, PacketType.GROUP_EVENT, addedMemberPayload));

        // 2) Existing members
        ByteBuffer buf = ByteBuffer.allocate(1 + 2 * Integer.BYTES);
        buf.put(GroupEventType.MEMBER_ADDED);
        buf.putInt(g.getId());
        buf.putInt(addedMember);

        byte[] oldMemberPayload = buf.array();

        for (int member : g.getMembers()) {
            if (member == addedMember) continue;
            context.send(Packet.createPacket(0, member, PacketType.GROUP_EVENT, oldMemberPayload));
        }
    }

    /*
     * REMOVE_MEMBER payload:
     *   [groupId:int][memberId:int]
     */
    private void handleRemoveMember(int adminId, ByteBuffer payload) {
        if (payload.remaining() < 2 * Integer.BYTES) {
            context.sendError(adminId, "Invalid REMOVE_MEMBER payload");
            return;
        }

        int groupId = payload.getInt();
        int memberId = payload.getInt();

        if (!groups.exists(groupId)) {
            context.sendError(adminId, "Unknown groupId: " + groupId);
            return;
        }
        if (!groups.hasMember(groupId, memberId)) {
            context.sendError(adminId, "Member " + memberId + " not in group " + groupId);
            return;
        }
        if (!groups.isAdmin(groupId, adminId)) {
            context.sendError(adminId, "Can't remove member, you are not the admin");
            return;
        }

        Group g = groups.getGroupById(groupId);
        if (g == null) {
            context.sendError(adminId, "Unknown Group.");
            return;
        }

        // snapshot of recipients before removal, so removed member also receives the event
        Set<Integer> recipients = g.getMembers();

        groups.removeMember(groupId, memberId);

        // ACK to admin (optional: also notify removed member directly, if you want)
        context.sendOk(adminId, "memberId " + memberId + " removed from groupId " + groupId);

        // Broadcast to all previous members (including removed)
        broadcastMemberRemoved(groupId, memberId, recipients);
    }

    /*
     * Broadcast MEMBER_REMOVED to all previous members of the group (including the removed one):
     *
     * Payload:
     *   [subType(MEMBER_REMOVED):1 byte]
     *   [groupId:int]
     *   [removedMember:int]
     */
    private void broadcastMemberRemoved(int groupId, int removedMemberId, Set<Integer> recipients) {
        ByteBuffer buf = ByteBuffer.allocate(1 + 2 * Integer.BYTES);
        buf.put(GroupEventType.MEMBER_REMOVED);
        buf.putInt(groupId);
        buf.putInt(removedMemberId);

        byte[] payload = buf.array();

        for (int memberId : recipients) {
            context.send(Packet.createPacket(0, memberId, PacketType.GROUP_EVENT, payload));
        }
    }

    /*
     * RENAME_GROUP payload:
     *   [groupId:int][titleLen:int][title:bytes]
     */
    private void handleRenameGroup(int adminId, ByteBuffer payload) {
        if (payload.remaining() < Integer.BYTES) {
            context.sendError(adminId, "Invalid RENAME_GROUP payload.");
            return;
        }

        int groupId = payload.getInt();
        if (!groups.exists(groupId)) {
            context.sendError(adminId, "Unknown groupId " + groupId);
            return;
        }
        if (!groups.isAdmin(groupId, adminId)) {
            context.sendError(adminId, "Can't rename group, you are not the admin");
            return;
        }

        String newTitle = context.readString(payload);
        if (newTitle == null || newTitle.isEmpty()) {
            context.sendError(adminId, "Group title is either empty or invalid.");
            return;
        }

        groups.rename(groupId, newTitle);

        // ACK to admin
        context.sendOk(adminId, "groupId " + groupId + " has been renamed to " + newTitle);

        // Broadcast rename
        broadcastGroupRenamed(groupId, newTitle);
    }

    /*
     * Broadcast RENAMED event:
     *
     * Payload:
     *   [subType(RENAMED):1 byte]
     *   [groupId:int]
     *   [titleLen:int][title:bytes]
     */
    private void broadcastGroupRenamed(int groupId, String newTitle) {
        Group g = groups.getGroupById(groupId);

        // Snapshot members before rename, but we do not need previous name
        Set<Integer> members = g.getMembers();

        byte[] titleBytes = newTitle.getBytes();
        int titleLen = titleBytes.length;

        ByteBuffer buf = ByteBuffer.allocate(1 + Integer.BYTES + Integer.BYTES + titleLen);
        buf.put(GroupEventType.RENAMED);
        buf.putInt(groupId);
        buf.putInt(titleLen);
        buf.put(titleBytes);

        byte[] payload = buf.array();

        for (int memberId : members) {
            context.send(Packet.createPacket(0, memberId, PacketType.GROUP_EVENT, payload));
        }
    }

    /*
     * DELETE_GROUP payload:
     *   [groupId:int]
     */
    private void handleDeleteGroup(int adminId, ByteBuffer payload) {
        if (payload.remaining() < Integer.BYTES) {
            context.sendError(adminId, "Invalid DELETE_GROUP payload.");
            return;
        }

        int groupId = payload.getInt();
        if (!groups.exists(groupId)) {
            context.sendError(adminId, "Unknown groupId " + groupId);
            return;
        }
        if (!groups.isAdmin(groupId, adminId)) {
            context.sendError(adminId, "Can't delete group, you are not the admin");
            return;
        }

        Group g = groups.getGroupById(groupId);
        if (g == null) {
            context.sendError(adminId, "Unknown Group.");
            return;
        }

        // Snapshot members before deletion
        Set<Integer> members = g.getMembers();

        groups.delete(groupId);

        // ACK to admin
        context.sendOk(adminId, "Deleted groupId " + groupId);

        // Broadcast delete
        broadcastGroupDeleted(groupId, members);
    }

    /*
     * Broadcast DELETED event:
     *
     * Payload:
     *   [subType(DELETED):1 byte]
     *   [groupId:int]
     */
    private void broadcastGroupDeleted(int groupId, Set<Integer> members) {
        ByteBuffer buf = ByteBuffer.allocate(1 + Integer.BYTES);
        buf.put(GroupEventType.DELETED);
        buf.putInt(groupId);

        byte[] payload = buf.array();

        for (int memberId : members) {
            context.send(Packet.createPacket(0, memberId, PacketType.GROUP_EVENT, payload));
        }
    }
}
