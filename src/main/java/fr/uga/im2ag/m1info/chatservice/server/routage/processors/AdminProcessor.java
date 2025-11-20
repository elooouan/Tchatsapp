package fr.uga.im2ag.m1info.chatservice.server.routage.processors;

import fr.uga.im2ag.m1info.chatservice.common.*;
import fr.uga.im2ag.m1info.chatservice.server.GroupRegistry;
import fr.uga.im2ag.m1info.chatservice.server.ServerState;
import fr.uga.im2ag.m1info.chatservice.server.UserRegistry;
import fr.uga.im2ag.m1info.chatservice.server.routage.StrategyContext;

import java.nio.ByteBuffer;

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
        int type = pkt.type();
        PacketType typeComparison = PacketType.convertIntToPacketType(type);

        switch (typeComparison) {
            case CREATE_GROUP:
                handleCreateGroup(pkt.from(), payload);
                break;
            case ADD_MEMBER:
                handleAddMember(pkt.from(), payload);
                break;
            case REMOVE_MEMBER:
                handleRemoveMember(pkt.from(), payload);
                break;
            case RENAME_GROUP:
                handleRenameGroup(pkt.from(), payload);
                break;
            case DELETE_GROUP:
                handleDeleteGroup(pkt.from(), payload);
                break;
            default:
                throw new IllegalArgumentException("Unknown group admin packet type: " + type);
        }
    }

    // ====================================================================
    // Handlers
    // ====================================================================

    // At this point [type:int] has already been consumed 

    /*
     * CREATE_GROUP payload:
     *   [titleLen:int][title:bytes]
     */
    private void handleCreateGroup(int callerId, ByteBuffer payload) {
        String title = context.readString(payload);
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("invalid CREATE_GROUP payload.");
        }

        // We don't know how this could happen but we still handle this error just in case
        if (!users.exists(callerId)) {
            throw new IllegalArgumentException("unknown user " + callerId);
        }

        // Create the group
        int groupId = groups.createGroup(title, callerId);
        
        context.sendOk(callerId, "GROUP_CREATED with groupId: " + groupId);
    }
    
    /*
     * ADD_MEMBER payload:
     *   [groupId:int][memberId:int]
     */
    private void handleAddMember(int callerId, ByteBuffer payload) {
        if (payload.remaining() < 2 * Integer.BYTES) {
            throw new IllegalArgumentException("Invalid ADD_MEMBER payload");
        }

        int groupId = payload.getInt();
        int memberId = payload.getInt();

        if (!groups.exists(groupId)) {
            throw new IllegalArgumentException("Unknown groupId: " + groupId);
        }

        if (!users.exists(memberId)) {
            throw new IllegalArgumentException("Unknown memberId: " + memberId);
        }

        groups.addMember(groupId, memberId);
        context.sendOk(callerId, "memberId " + memberId + " added to groupId " + groupId);
    }

    /*
     * REMOVE_MEMBER payload:
     *   [groupId:int][memberId:int]
     */
    private void handleRemoveMember(int callerId, ByteBuffer payload) {
        if (payload.remaining() < 2 * Integer.BYTES) {
            throw new IllegalArgumentException("Invalid REMOVE_MEMBER payload");
        }

        int groupId = payload.getInt();
        int memberId = payload.getInt();

        if (!groups.exists(groupId)) {
            throw new IllegalArgumentException("Unknown group " + groupId);
        }

        groups.removeMember(groupId, memberId);
        context.sendOk(memberId, "memberId " + memberId + " removed from groupId " + groupId);
    }

    /*
     * RENAME_GROUP payload:
     *   [groupId:int][titleLen:int][title:bytes]
     */
    private void handleRenameGroup(int callerId, ByteBuffer payload) {
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
        context.sendOk(callerId, "groupId " + groupId + " has been renamed to " + newTitle);
    }

    /*
     * DELETE_GROUP payload:
     *   [groupId:int]
     */
    private void handleDeleteGroup(int callerId, ByteBuffer payload) {
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