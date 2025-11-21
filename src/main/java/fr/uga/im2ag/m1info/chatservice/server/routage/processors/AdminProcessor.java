package fr.uga.im2ag.m1info.chatservice.server.routage.processors;

import fr.uga.im2ag.m1info.chatservice.common.*;
import fr.uga.im2ag.m1info.chatservice.server.registries.GroupRegistry;
import fr.uga.im2ag.m1info.chatservice.server.registries.UserRegistry;
import fr.uga.im2ag.m1info.chatservice.server.routage.StrategyContext;

import java.nio.ByteBuffer;
import java.util.*;

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

        // Payload header [first byte of the payload] = PacketType.*
        // [PacketType: int][Rest of the payload...]
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

    // At this point [type:int] has already been consumed 

    /*
     * CREATE_GROUP payload:
     *   [subType(CREATED):1 byte][titleLen:int][title:bytes][memberCount:int][memberId1:int]...[memberIdCount:int]
     */
    private void handleCreateGroup(int adminId, ByteBuffer payload) {
        // Handle this error just in case
        if (!users.exists(adminId)) { context.sendError(adminId, "Unknown user " + adminId); }

        String title = context.readString(payload);
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("invalid CREATE_GROUP payload.");
        }

        // Get the number of members to add to the group
        int memberCount = payload.getInt();

        Set<Integer> memberIds = new HashSet<>();
        
        // Loop over the payload and if the user exists, add them to the group
        for (int i = 0; i < memberCount; i++) {
            int memberId = payload.getInt();
            if (!users.exists(memberId)); 
            memberIds.add(memberId);
        }
        
        // Create the group
        Group g = groups.createGroup(title, adminId, memberIds);

        // ACK to admin -> just for the admin to know the group creation was successful
        context.sendOk(adminId, "GROUP_CREATED with groupId: " + g.getId());

        // Broadcast to all members of the group -> for them to update their Group Registries
        broadcastGroupCreated(g);
    }
    

    /**
     * Create a group snapshot and creates the corresponding packet payload -> byte[]
     */
    private byte[] createGroupSnapshot(Group g) {
        Set<Integer> memberIds = g.getMembers();
        String title = g.getTitle();
        
        // Allocate payload
        ByteBuffer buf = ByteBuffer.allocate(
            1 + Integer.BYTES +
            title.length() + Integer.BYTES +
            memberIds.size() * Integer.BYTES
        );

        // Build payload
        buf.put(GroupEventType.CREATED); // subtype
        buf.putInt(title.length());
        buf.put(title.getBytes());
        buf.putInt(memberIds.size());
        for (int member : memberIds) buf.putInt(member);

        return buf.array();
    }

    /*
     * Broadcast the Group creation to every member of the newly created group
     * Used to update the members GroupRegistry
     */
    private void broadcastGroupCreated(Group g) {
        byte[] payload = createGroupSnapshot(g);
        // For each member of the group, send them a snapshot of all the current group members
        for (int memberId : g.getMembers()) {
            context.send(
                Packet.createPacket(0, memberId, PacketType.GROUP_EVENT, payload)
            );
        }
    }

    /*
     * ADD_MEMBER payload:
     *   [groupId:int][memberId:int]
     */
    private void handleAddMember(int adminId, ByteBuffer payload) {
        if (payload.remaining() < 2 * Integer.BYTES) { context.sendError(adminId, "Invalid ADD_MEMBER payload"); }

        int groupId = payload.getInt();
        int memberId = payload.getInt();

        if (groups.exists(groupId) && !groups.hasMember(groupId, adminId)) { context.sendError(adminId, "You are not in this group "); return; }
        if (!groups.exists(groupId)) { context.sendError(adminId, "Unknown groupId: " + groupId); return; }
        if (!users.exists(memberId)) { context.sendError(adminId, "Unknown memberId: " + memberId); return; }
        if (!groups.isAdmin(groupId, adminId)) { context.sendError(adminId, "Can't add member, you are not the admin: " + adminId); return; }

        Group g = groups.getGroupById(groupId);
        
        if (g == null) { context.sendError(adminId, "Unknown Group."); return; }
        if (!g.addMember(memberId)) { context.sendError(adminId, "User already in group"); return; }

        // Send "silent" ACK to admin
        context.sendOk(adminId, "memberId " + memberId + " added to groupId " + groupId);

        // Broadcast added member to rest of group
        broadcastMemberAdded(g, memberId);
    }


    /*
     * Broadcast the added member to every member of the newly created group EXCEPT the added member himself
     * The added member receives a group snapshot
     * Used to update the members GroupRegistry
     */
    public void broadcastMemberAdded(Group g, int addedMember) {

        // addedMember's payload creation + send
        byte[] addedMemberPayload = createGroupSnapshot(g);

        context.send(
            Packet.createPacket(0, addedMember, PacketType.GROUP_EVENT, addedMemberPayload)
        );


        // "Old" group members (already there before addedMember)
        // Payload format: [subType(MEMBER_ADDED):1 byte][groupId:int][addedMember:int]
        ByteBuffer buf = ByteBuffer.allocate(1 + Integer.BYTES * 2);
        
        buf.put(GroupEventType.MEMBER_ADDED);
        buf.putInt(g.getId());
        buf.putInt(addedMember);

        byte[] oldMemberPayload = buf.array();

        for (int member : g.getMembers()) {
            if (member == addedMember) continue;
            context.send(
                Packet.createPacket(0, member, PacketType.GROUP_EVENT, oldMemberPayload)
            );
        }
    }

    /*
     * REMOVE_MEMBER payload:
     *   [groupId:int][memberId:int]
     */
    private void handleRemoveMember(int adminId, ByteBuffer payload) {
        if (payload.remaining() < 2 * Integer.BYTES) {
            throw new IllegalArgumentException("Invalid REMOVE_MEMBER payload");
        }

        int groupId = payload.getInt();
        int memberId = payload.getInt();

        if (!groups.exists(groupId)) { context.sendError(adminId, "Unknown groupId: " + groupId); return; }
        if (!groups.hasMember(groupId, memberId)) { context.sendError(adminId, "Member " + memberId + " not in group " + groupId); return; }
        if (!groups.isAdmin(groupId, adminId)) { context.sendError(adminId, "Can't add member, you are not the admin"); return; }

        groups.removeMember(groupId, memberId);
        context.sendOk(memberId, "memberId " + memberId + " removed from groupId " + groupId);
    }

    /*
     * RENAME_GROUP payload:
     *   [groupId:int][titleLen:int][title:bytes]
     */
    private void handleRenameGroup(int adminId, ByteBuffer payload) {
        // We only need to check for a single byte -> groupeId, because the rest is handled by readString
        if (payload.remaining() < Integer.BYTES) {
            throw new IllegalArgumentException("Invalid RENAME_GROUP payload.");
        }

        int groupId = payload.getInt();
        if (!groups.exists(groupId)) {
            throw new IllegalArgumentException("Unknown groupId " + groupId);
        }

        String newTitle = context.readString(payload);
        if (newTitle == null || newTitle.isEmpty()) {
            throw new IllegalArgumentException("Group title is either empty or invalid.");
        }

        groups.rename(groupId, newTitle);
        context.sendOk(adminId, "groupId " + groupId + " has been renamed to " + newTitle);
    }

    /*
     * DELETE_GROUP payload:
     *   [groupId:int]
     */
    private void handleDeleteGroup(int adminId, ByteBuffer payload) {
        if (payload.remaining() < Integer.BYTES) {
            throw new IllegalArgumentException("Invalid DELETE_GROUP payload.");
        }

        int groupeId = payload.getInt();
        if (!groups.exists(groupeId)) {
            throw new IllegalArgumentException("Unknown groupId " + groupeId);
        }

        groups.delete(groupeId);
        context.sendOk(groupeId, "Delete groupId " + groupeId);
    }
}