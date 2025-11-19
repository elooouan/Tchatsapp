package fr.uga.im2ag.m1info.chatservice.server.routage.processors;

import fr.uga.im2ag.m1info.chatservice.common.*;
import fr.uga.im2ag.m1info.chatservice.server.ContactRegistry;
import fr.uga.im2ag.m1info.chatservice.server.GroupRegistry;
import fr.uga.im2ag.m1info.chatservice.server.ServerState;
import fr.uga.im2ag.m1info.chatservice.server.UserRegistry;
import fr.uga.im2ag.m1info.chatservice.server.routage.StrategyContext;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/*
 * Handles commands of type "admin" (to == 0)
 */
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
        //int type = pkt.getType(); // type propre a venir
        int type = pkt.type();

        switch (type) {
            case PacketType.CREATE_GROUP:
                handleCreateGroup(pkt);
                break;
            case PacketType.ADD_MEMBER:
                handleAddMember(pkt);
                break;
            case PacketType.REMOVE_MEMBER:
                handleRemoveMember(pkt);
                break;
            case PacketType.RENAME_GROUP:
                handleRenameGroup(pkt);
                break;
            case PacketType.DELETE_GROUP:
                handleDeleteGroup(pkt);
                break;
            case PacketType.SET_PSEUDO:
                handleSetPseudo(pkt);
                break;
            default:
                throw new IllegalArgumentException("Unknown admin packet type: " + type);
        }
    }

    // ====================================================================
    // Handlers
    // ====================================================================

    private void handleCreateGroup(Packet pkt) {
        /*
        String title = readString(pkt.getPayload());
        int from = pkt.from();

        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("invalid title.");
        }

        // We don't know how this could happen but we still handle this error just in case
        if (!users.exists(from)) {
            throw new IllegalArgumentException("unknown user " + from);
        }

        // Create the group
        int groupId = groups.createGroup(title, from);
        
        context.sendOk(from, "GROUP_CREATED with groupId: " + groupId);
         */
    }

    private void handleAddMember(Packet pkt) {
        /*
        int groupId = pkt.to();
        int memberId = pkt.from();

        if (!groups.exists(groupId)) {
            throw new IllegalArgumentException("Unknown groupId: " + groupId);
        }

        if (!users.exists(memberId)) {
            throw new IllegalArgumentException("Unknown memberId: " + memberId);
        }

        groups.addMember(groupId, memberId);
        context.sendOk(memberId, "memberId " + memberId + " added to groupId " + groupId);
         */
    }

    private void handleRemoveMember(Packet pkt) {
        /*
        int groupId = pkt.to();
        int memberId = pkt.from();

        if (!groups.exists(groupId)) {
            throw new IllegalArgumentException("unknown group " + groupId);
        }

        groups.removeMember(groupId, memberId);
        context.sendOk(memberId, "memberId " + memberId + " removed from groupId " + groupId);
         */
    }

    private void handleRenameGroup(Packet pkt) {
        /*
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
         */
    }

    private void handleDeleteGroup(Packet pkt) {
        /*
        if (payload.remaining() < Integer.BYTES) {
            throw new IllegalArgumentException("Invalid DELETE_GROUP payload.");
        }

        int groupeId = payload.getInt();
        if (!groups.exists(groupeId)) {
            throw new IllegalArgumentException("Unknown groupId " + groupeId);
        }

        groups.delete(groupeId);
        context.sendOk(groupeId, "Delete groupId " + groupeId);
         */
    }

    private void handleSetPseudo(Packet pkt) {
        /*
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
         */
    }

    private void handleAddContact(Packet pkt) {
        /*
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
         */
    }
}