package fr.uga.im2ag.m1info.chatservice.server.routage.processors;

import fr.uga.im2ag.m1info.chatservice.common.*;
import fr.uga.im2ag.m1info.chatservice.server.registries.GroupRegistry;
import fr.uga.im2ag.m1info.chatservice.server.registries.UserRegistry;
import fr.uga.im2ag.m1info.chatservice.server.routage.StrategyContext;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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
     *   [titleLen:int][title:bytes]
     */
    private void handleCreateGroup(int adminId, ByteBuffer payload) {
        // Handle this error just in case
        if (!users.exists(adminId)) { context.sendError(adminId, "Unknown user " + adminId); }

        String title = context.readString(payload);
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("invalid CREATE_GROUP payload.");
        }

        // Create the group
        int groupId = groups.createGroup(title, adminId);
        
        // ACK to admin -> just for the admin to know the group creation was successful
        context.sendOk(adminId, "GROUP_CREATED with groupId: " + groupId);

        // Response packet creation
        int payloadSize = title.length() + Integer.BYTES;
        byte[] titleBytes = title.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate(payloadSize);

        // Payload format : [groupId:int][title:bytes][]
        buf.putInt(groupId);
        buf.put(titleBytes);

        // Created packet
        Packet pkt = Packet.createPacket(
            0,
            adminId,
            PacketType.CREATE_GROUP,
            buf.array()
        );

        context.send(pkt);
    }
    

    
    /*
     * ADD_MEMBER payload:
     *   [groupId:int][memberId:int]
     */
    private void handleAddMember(int adminId, ByteBuffer payload) {
        if (payload.remaining() < 2 * Integer.BYTES) {
            throw new IllegalArgumentException("Invalid ADD_MEMBER payload");
        }

        int groupId = payload.getInt();
        int memberId = payload.getInt();

        if (groups.exists(groupId) && !groups.hasMember(groupId, adminId)) { context.sendError(adminId, "You are not in this group "); return; }
        if (!groups.exists(groupId)) { context.sendError(adminId, "Unknown groupId: " + groupId); return; }
        if (!users.exists(memberId)) { context.sendError(adminId, "Unknown memberId: " + memberId); return; }
        if (!groups.isAdmin(groupId, adminId)) { context.sendError(adminId, "Can't add member, you are not the admin: " + adminId); return; }

        groups.addMember(groupId, memberId);
        context.sendOk(adminId, "memberId " + memberId + " added to groupId " + groupId);
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