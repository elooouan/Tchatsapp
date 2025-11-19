package fr.uga.im2ag.m1info.chatservice.server.routage.processors;

import fr.uga.im2ag.m1info.chatservice.common.*;
import fr.uga.im2ag.m1info.chatservice.server.ContactRegistry;
import fr.uga.im2ag.m1info.chatservice.server.GroupRegistry;
import fr.uga.im2ag.m1info.chatservice.server.UserRegistry;
import fr.uga.im2ag.m1info.chatservice.server.routage.StrategyContext;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;


public class AdminProcessor implements PacketProcessor {
    private StrategyContext context;
    private UserRegistry users;
    private GroupRegistry groups;
    private ContactRegistry contacts;

    public AdminProcessor(StrategyContext context) {
        this.context = context;
        this.users = context.serverState().getUserRegistry();
        this.groups = context.serverState().getGroupRegistry();
        this.contacts = context.serverState().getContactRegistry();
    }

    @Override
    public void process(Packet pkt) {
        ByteBuffer payload = pkt.getPayload();

        // If the payload or its size is invalid -> error
        if (payload == null || payload.remaining() < Integer.BYTES) { // Integer.BYTES has a value of 4
            throw new IllegalArgumentException("Empty or invalid Admin payload.");
        }

        // Payload header [first byte of the payload] = PacketType.*
        // [PacketType: int][Rest of the payload...]
        int type = pkt.type();

        switch (type) {
            case PacketType.CREATE_GROUP:
                handleCreateGroup(pkt.from(), payload);
                break;
            case PacketType.ADD_MEMBER:
                handleAddMember(pkt.from(), payload);
                break;
            case PacketType.REMOVE_MEMBER:
                handleRemoveMember(pkt.from(), payload);
                break;
            case PacketType.RENAME_GROUP:
                handleRenameGroup(pkt.from(), payload);
                break;
            case PacketType.DELETE_GROUP:
                handleDeleteGroup(pkt.from(), payload);
                break;
            case PacketType.SET_PSEUDO:
                handleSetPseudo(pkt.from(), payload);
                break;
            case PacketType.ADD_CONTACT:
                handleAddContact(pkt.from(), payload);
                break;
            default:
                throw new IllegalArgumentException("Unknown admin packet type: " + type);
        }
    }

    // ====================================================================
    // Handlers
    // ====================================================================

    // At this point [type:int] has already been consumed 

    /*
     * CREATE_GROUP payload:
     *   [type:int][titleLen:int][title:bytes]
     * The creator/admin is implicitly the callerId (pkt.from()).
     */
    private void handleCreateGroup(int callerId, ByteBuffer payload) {
        String title = readString(payload);
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
     *   [type:int][groupId:int][memberId:int]
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
     *   [type:int][groupId:int][memberId:int]
     */
    private void handleRemoveMember(int callerId, ByteBuffer payload) {
        if (payload.remaining() < 2 * Integer.BYTES) {
            throw new IllegalArgumentException("Invalid REMOVE_MEMBER payload");
        }

        int groupId = payload.getInt();
        int memberId = payload.getInt();

        if (!groups.exists(groupId)) {
            throw new IllegalArgumentException("unknown group " + groupId);
        }

        groups.removeMember(groupId, memberId);
        context.sendOk(memberId, "memberId " + memberId + " removed from groupId " + groupId);
    }

    /*
     * RENAME_GROUP payload:
     *   [type:int][groupId:int][titleLen:int][title:bytes]
     */
    private void handleRenameGroup(int callerId, ByteBuffer payload) {
        // We only need to check for a single byte -> groupeId, because the rest is handled by readString
        if (payload.remaining() < Integer.BYTES) {
            throw new IllegalArgumentException("Invalid RENAME_GROUP payload.");
        }

        int groupeId = payload.getInt();
        if (!groups.exists(groupeId)) {
            throw new IllegalArgumentException("Unknown groupeId " + groupeId);
        }

        String newTitle = readString(payload);
        if (newTitle == null || newTitle.isEmpty()) {
            throw new IllegalArgumentException("Group title is either empty or invalid.");
        }

        groups.rename(groupeId, newTitle);
        context.sendOk(callerId, "groupId " + groupeId + " has been renamed to " + newTitle);
    }

    /*
     * DELETE_GROUP payload:
     *   [type:int][groupId:int]
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

    /*
     * SET_PSEUDO payload:
     *   [type:int][pseudoLen:int][pseudo:bytes]
     * The user whose pseudo is changed is callerId (pkt.from()).
     */
    private void handleSetPseudo(int callerId, ByteBuffer payload) {
        String newPseudo = readString(payload);
        if (newPseudo == null || newPseudo.isEmpty()) {
            throw new IllegalArgumentException("Invalid SET_PSEUDO payload.");
        }

        // Safety net -> we still check it regardless just in case
        if (!users.exists(callerId)) {
            throw new IllegalArgumentException("Unknown callerid " + callerId);
        }

        users.setPseudo(callerId, newPseudo);
        context.sendOk(callerId, "New pseudo set: " + newPseudo);
    }

    /*
     * ADD_CONTACT payload:
     *   [type:int][contactId:int]
     * The owner of the contact list is callerId.
     */
    private void handleAddContact(int callerId, ByteBuffer payload) {
        if (payload.remaining() < Integer.BYTES) {
            throw new IllegalArgumentException("Invalid ADD_CONTACT payload.");
        }

        int contactId = payload.getInt();
        if (!users.exists(callerId) || !users.exists(contactId)) {
            throw new IllegalArgumentException("Uknown ID.");
        }

        // Because contacts uses User not userId
        User caller = users.getUser(callerId);
        User newContact = users.getUser(callerId);

        contacts.addContact(caller, newContact);
        context.sendOk(contactId, "New contactId " + contactId + " added to userId " + callerId +  "'s contacts list");
    }


    // ====================================================================
    // Helpers (same as DirectMessageProcessor and GroupMessageProcessor)
    // ====================================================================

    // Helper to read bytes from the payload and convert them into a String
    // [len:int][len bytes]
    private String readString(ByteBuffer buf) {
        if (buf.remaining() < Integer.BYTES) return null;

        int len = buf.getInt(); // Consume [len:int]
        if (len < 0 || buf.remaining() < len) return null;

        byte[] data = new byte[len];
        buf.get(data);
        
        return new String(data, StandardCharsets.UTF_8); // UTF-8 is the standard for network protocols (UTF-16 is used for java objects)
    }
}