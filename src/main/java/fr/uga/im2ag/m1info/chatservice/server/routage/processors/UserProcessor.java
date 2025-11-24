package fr.uga.im2ag.m1info.chatservice.server.routage.processors;

import fr.uga.im2ag.m1info.chatservice.common.*;
import fr.uga.im2ag.m1info.chatservice.server.registries.ContactRegistry;
import fr.uga.im2ag.m1info.chatservice.server.registries.UserRegistry;
import fr.uga.im2ag.m1info.chatservice.server.routage.StrategyContext;

import java.nio.ByteBuffer;
import java.util.Set;

public class UserProcessor implements PacketProcessor {
    private StrategyContext context;
    private UserRegistry users;
    private ContactRegistry contacts;

    public UserProcessor(StrategyContext context) {
        this.context  = context;
        this.users    = context.serverState().getUserRegistry();
        this.contacts = context.serverState().getContactRegistry();
    }

    @Override
    public void process(Packet pkt) {
        ByteBuffer payload = pkt.getPayload();
        int from = pkt.from();
        PacketType type = pkt.type();

        // We don't check if payload == null because of LIST_CONTACTS empty payload
        if (payload == null || payload.remaining() < Integer.BYTES) {
            throw new IllegalArgumentException("Empty or invalid User payload.");
        }


        switch (type) {
            case SET_PSEUDO -> handleSetPseudo(from, payload);
            case ADD_CONTACT -> handleAddContact(from, payload);
            case CREATE_USER -> handleCreateUser(from, payload);
            default -> context.sendError(from, "Unknown user packet type: " + type);
        }
    }

    // ----------------------------------------------------------------
    // Handlers (user)
    // ----------------------------------------------------------------

    /*
     * SET_PSEUDO payload:
     *   [pseudoLen:int][pseudo:bytes]
     */
    private void handleSetPseudo(int callerId, ByteBuffer payload) {
        String newPseudo = context.readString(payload);
        if (newPseudo == null || newPseudo.isEmpty()) {
            context.sendError(callerId, "Invalid SET_PSEUDO payload.");
        }

        if (!users.exists(callerId)) {
            context.sendError(callerId, "Unknown callerId " + callerId);
        }

        users.setPseudo(callerId, newPseudo);
        context.sendOk(callerId, "New pseudo set: " + newPseudo);
    }

    /*
     * ADD_CONTACT payload:
     *   [contactId:int]
     */
    private void handleAddContact(int callerId, ByteBuffer payload) {
        if (payload.remaining() < Integer.BYTES) {
            context.sendError(callerId, "Invalid ADD_CONTACT payload.");
        }

        int contactId = payload.getInt();

        if (contactId == callerId) return;

        if (!users.exists(callerId) || !users.exists(contactId)) {
            context.sendError(contactId, "Unknown ID.");
        }

        User caller     = users.getUser(callerId);
        User newContact = users.getUser(contactId);

        contacts.addContact(caller, newContact);
        context.sendOk(callerId,
                "New contactId " + contactId + " added to userId " + callerId + "'s contacts list");
    }


    /*
     * CREATE_USER payload:
     *   [subtype:byte][contactId:int]
     */
    private void handleCreateUser(int callerId, ByteBuffer payload) {
        if (payload.remaining() < Integer.BYTES) {
            context.sendError(callerId, "Invalid ADD_CONTACT payload.");
        }

        if (users.exists(callerId)) {
            context.sendError(callerId, "This user already have an account");
        }

        int id = users.createUser(context.serverState().getIdGenerator().generateId());
        context.sendOk(callerId, "New user " + id + " created");
        byte[] bytes = ByteBuffer.allocate(4).putInt(id).array();
        context.send(Packet.createPacket(0, callerId, PacketType.CREATE_USER, bytes));
    }

}
