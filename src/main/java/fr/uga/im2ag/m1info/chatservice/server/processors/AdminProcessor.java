package fr.uga.im2ag.m1info.chatservice.server.processors;

import fr.uga.im2ag.m1info.chatservice.common.*;
import fr.uga.im2ag.m1info.chatservice.server.ContactRegistry;
import fr.uga.im2ag.m1info.chatservice.server.GroupRegistry;
import fr.uga.im2ag.m1info.chatservice.server.ServerState;
import fr.uga.im2ag.m1info.chatservice.server.UserRegistry;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/*
 * Handles commands of type "admin" (to == 0)
 */
public class AdminProcessor implements PacketProcessor {
    private UserRegistry users;
    private GroupRegistry groups;
    private ContactRegistry contacts;

    public AdminProcessor(ServerState serverState) {
        this.users = serverState.getUserRegistry();
        this.groups = serverState.getGroupRegistry();
        this.contacts = serverState.getContactRegistry();
    }

    @Override
    public void process(Packet pkt) {
        ByteBuffer payload = pkt.getPayload();

        // If the payload or its size is invalid -> error
        if (payload == null || payload.remaining() < Integer.BYTES) { // Integer.BYTES has a value of 4
            sendError(pkt.from(), "Empty or invalid Admin payload."); // Reply to the sender with an ERROR
            return;
        }

        // Payload header [first byte of the payload] = PacketType.*
        // [PacketType: int][Rest of the payload...]
        int type = payload.get();

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
                sendError(pkt.from(), "Unknown admin packet type: " + type);
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
            sendError(callerId, "invalid CREATE_GROUP payload.");
            return;
        }

        // We don't know how this could happen but we still handle this error just in case
        if (!users.exists(callerId)) {
            sendError(callerId, "unknown user " + callerId);
            return;
        }

        // Create the group
        int groupId = groups.createGroup(title, callerId);
        
        sendOk(callerId, "GROUP_CREATED with groupId: " + groupId);
    }
    
    /*
     * ADD_MEMBER payload:
     *   [type:int][groupId:int][memberId:int]
     */
    private void handleAddMember(int callerId, ByteBuffer payload) {
        if (payload.remaining() < 2 * Integer.BYTES) {
            sendError(callerId, "Invalid ADD_MEMBER payload");
            return;
        }

        int groupId = payload.getInt();
        int memberId = payload.getInt();

        if (!groups.exists(groupId)) {
            sendError(groupId, "Unknown groupId: " + groupId);
            return;
        }

        if (!users.exists(memberId)) {
            sendError(memberId, "Unknown memberId: " + memberId);
            return;
        }

        groups.addMember(groupId, memberId);
        sendOk(callerId, "memberId " + memberId + " added to groupId " + groupId);
    }

    /*
     * REMOVE_MEMBER payload:
     *   [type:int][groupId:int][memberId:int]
     */
    private void handleRemoveMember(int callerId, ByteBuffer payload) {
        if (payload.remaining() < 2 * Integer.BYTES) {
            sendError(callerId, "Invalid REMOVE_MEMBER payload");
            return;
        }

        int groupId = payload.getInt();
        int memberId = payload.getInt();

        if (!groups.exists(groupId)) {
            sendError(callerId, "unknown group " + groupId);
            return;
        }

        groups.removeMember(groupId, memberId);
        sendOk(memberId, "memberId " + memberId + " removed from groupId " + groupId);
    }

    /*
     * RENAME_GROUP payload:
     *   [type:int][groupId:int][titleLen:int][title:bytes]
     */
    private void handleRenameGroup(int callerId, ByteBuffer payload) {
        // We only need to check for a single byte -> groupeId, because the rest is handled by readString
        if (payload.remaining() < Integer.BYTES) {
            sendError(callerId, "Invalid RENAME_GROUP payload.");
            return;
        }

        int groupeId = payload.getInt();
        if (!groups.exists(groupeId)) {
            sendError(groupeId, "Unknown groupeId " + groupeId);
            return;
        }

        String newTitle = readString(payload);
        if (newTitle == null || newTitle.isEmpty()) {
            sendError(groupeId, "Group title is either empty or invalid.");
            return;
        }

        groups.rename(groupeId, newTitle);
        sendOk(callerId, "groupId " + groupeId + " has been renamed to " + newTitle);
    }

    /*
     * DELETE_GROUP payload:
     *   [type:int][groupId:int]
     */
    private void handleDeleteGroup(int callerId, ByteBuffer payload) {
        if (payload.remaining() < Integer.BYTES) {
            sendError(callerId, "Invalid DELETE_GROUP payload.");
            return;
        }

        int groupeId = payload.getInt();
        if (!groups.exists(groupeId)) {
            sendError(groupeId, "Unknown groupId " + groupeId);
            return;
        }

        groups.delete(groupeId);
        sendOk(groupeId, "Delete groupId " + groupeId);
    }

    /*
     * SET_PSEUDO payload:
     *   [type:int][pseudoLen:int][pseudo:bytes]
     * The user whose pseudo is changed is callerId (pkt.from()).
     */
    private void handleSetPseudo(int callerId, ByteBuffer payload) {
        String newPseudo = readString(payload);
        if (newPseudo == null || newPseudo.isEmpty()) {
            sendError(callerId, "Invalid SET_PSEUDO payload.");
            return;
        }

        // Safety net -> we still check it regardless just in case
        if (!users.exists(callerId)) {
            sendError(callerId, "Unknown callerid " + callerId);
            return;
        }

        users.setPseudo(callerId, newPseudo);
        sendOk(callerId, "New pseudo set: " + newPseudo);
    }

    /*
     * ADD_CONTACT payload:
     *   [type:int][contactId:int]
     * The owner of the contact list is callerId.
     */
    private void handleAddContact(int callerId, ByteBuffer payload) {
        if (payload.remaining() < Integer.BYTES) {
            sendError(callerId, "Invalid ADD_CONTACT payload.");
            return;
        }

        int contactId = payload.getInt();
        if (!users.exists(callerId) || !users.exists(contactId)) {
            sendError(callerId, "Uknown ID.");
            return;
        }

        // Because contacts uses User not userId
        User caller = users.getUser(callerId);
        User newContact = users.getUser(callerId);

        contacts.addContact(caller, newContact);
        sendOk(contactId, "New contactId " + contactId + " added to userId " + callerId +  "'s contacts list");
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

    // For now we just send a TEXT packet with "OK ..." / "ERROR ...".
    // We can later switch to real PacketTypes.ERROR
    private void sendOk(int to, String msg) {
        Packet p = Packet.createTextMessage(0, to, "OK " + msg);
        //server.sendPacket(p);
    }

    private void sendError(int to, String msg) {
        Packet p = Packet.createTextMessage(0, to, "ERROR " + msg);
        //server.sendPacket(p);
    }
}