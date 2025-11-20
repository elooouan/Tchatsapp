package fr.uga.im2ag.m1info.chatservice.server.routage.processors;

import fr.uga.im2ag.m1info.chatservice.common.*;
import fr.uga.im2ag.m1info.chatservice.server.ContactRegistry;
import fr.uga.im2ag.m1info.chatservice.server.ServerState;
import fr.uga.im2ag.m1info.chatservice.server.UserRegistry;
import fr.uga.im2ag.m1info.chatservice.server.routage.StrategyContext;

import java.nio.ByteBuffer;

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

        if (payload == null || payload.remaining() < Integer.BYTES) {
            throw new IllegalArgumentException("Empty or invalid User payload.");
        }

        int type = pkt.type();

        PacketType typeComparison = PacketType.convertIntToPacketType(type);

        switch (typeComparison) {
            case SET_PSEUDO:
                handleSetPseudo(pkt.from(), payload);
                break;
            case ADD_CONTACT:
                handleAddContact(pkt.from(), payload);
                break;
            case PacketType.CREATE_USER:
                handleCreateUser(pkt.from(), payload);
                break;
            default:
                throw new IllegalArgumentException("Unknown user packet type: " + type);
        }
    }

    // ----------------------------------------------------------------
    // Handlers (user)
    // ----------------------------------------------------------------

    /*
     * SET_PSEUDO payload:
     *   [type:int][pseudoLen:int][pseudo:bytes]
     */
    private void handleSetPseudo(int callerId, ByteBuffer payload) {
        String newPseudo = context.readString(payload);
        if (newPseudo == null || newPseudo.isEmpty()) {
            throw new IllegalArgumentException("Invalid SET_PSEUDO payload.");
        }

        if (!users.exists(callerId)) {
            throw new IllegalArgumentException("Unknown callerId " + callerId);
        }

        users.setPseudo(callerId, newPseudo);
        context.sendOk(callerId, "New pseudo set: " + newPseudo);
    }

    /*
     * ADD_CONTACT payload:
     *   [type:int][contactId:int]
     */
    private void handleAddContact(int callerId, ByteBuffer payload) {
        if (payload.remaining() < Integer.BYTES) {
            throw new IllegalArgumentException("Invalid ADD_CONTACT payload.");
        }

        int contactId = payload.getInt();
        if (!users.exists(callerId) || !users.exists(contactId)) {
            throw new IllegalArgumentException("Unknown ID.");
        }

        User caller     = users.getUser(callerId);
        User newContact = users.getUser(contactId); // (fixed: contactId, not callerId)

        contacts.addContact(caller, newContact);
        context.sendOk(callerId,
                "New contactId " + contactId + " added to userId " + callerId + "'s contacts list");
    }

    private void handleCreateUser(int callerId, ByteBuffer payload) {
        if (payload.remaining() < Integer.BYTES) {
            throw new IllegalArgumentException("Invalid ADD_CONTACT payload.");
        }

        if (users.exists(callerId)) {
            throw new IllegalArgumentException("This user already have an account");
        }

        // très moche, TODO passer le idGenerator en singleton
        int id = users.createUser(context.serverState().getIdGenerator().generateId());
        context.sendOk(callerId, "New user " + id + " created");

    }
}
