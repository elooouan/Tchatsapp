package fr.uga.im2ag.m1info.chatservice.client;

import fr.uga.im2ag.m1info.chatservice.client.registries.ContactRegistry;
import fr.uga.im2ag.m1info.chatservice.common.Message;
import fr.uga.im2ag.m1info.chatservice.common.Packet;
import fr.uga.im2ag.m1info.chatservice.common.Packet.PacketBuilder;
import fr.uga.im2ag.m1info.chatservice.common.PacketSender;
import fr.uga.im2ag.m1info.chatservice.common.PacketType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * High-level client API used by the UI / command parser.
 * - Outgoing: exposes methods (sendDirectMessage, createGroup, ...)
 *   that build Packets and send them via PacketSender.
 * - Incoming: forwards packets to IncomingPacketProcessor, which calls the Listener.
 */
public class ClientAPI {

    /** Special destination id for admin commands (server-side AdminProcessor). */
    public static final int ADMIN_ID = 0;

    private final PacketSender sender;
    private final IncomingPacketProcessor incoming;
    
    /** This client's ... as known by the server. */
    private volatile int clientId;

    /** This client's pseudo as known by the server. */
    private volatile String pseudo;

    public ClientAPI(PacketSender sender,
                     int initialClientId,
                     IncomingPacketProcessor.Listener listener) {
        this.sender = sender;
        this.clientId = initialClientId;
        this.incoming = new IncomingPacketProcessor(listener);
    }

    /*********************************** SETTERS ************************************/ 

    /**
     * If the server later tells us our real client id, we can update it.
     */
    public void setClientId(int clientId) { this.clientId = clientId; }

    /*********************************** GETTERS ************************************/ 

    /**
     * Getter for the user's username
     */
    public int getClientId() { return clientId; }
    
    public String getPseudo() { return Client.getClientState().getPseudo(); }

    public List<Message> getConversation(int conversationId) {
        return Client.getClientState().getMessageRegistry().getMessages(conversationId);
    }

    // -------------------------------------------------------------------------
    // Incoming side: called by the TCP reader thread (In client)
    // -------------------------------------------------------------------------

    /**
     * Called by the TCP reader thread whenever a Packet is received.
     */
    public void handleIncoming(Packet pkt) {
        incoming.process(pkt);
    }

    // -------------------------------------------------------------------------
    // Outgoing side: high-level methods used by the UI / command parser
    // -------------------------------------------------------------------------

    /**
     * Send a direct text message to another user.
     * Payload format:
     *   [int msgLen][msgLen bytes UTF-8]
     * This matches IncomingPacketProcessor.readString().
     */
    public void sendDirectMessage(int destUserId, String message) {
        byte[] payload = encodeString(message);

        Packet pkt = new PacketBuilder(payload.length,
                clientId,
                destUserId,
                PacketType.TEXT_USER.ordinal())
                .setPayload(payload)
                .build();

        sender.sendPacket(pkt);
    }

    /**
     * Send a text message to a group.
     * Payload format:
     *   [int msgLen][msgLen bytes UTF-8]
     */
    public void sendGroupMessage(int groupId, String message) {
        byte[] payload = encodeString(message);

        Packet pkt = new PacketBuilder(payload.length,
                clientId,
                groupId,
                PacketType.TEXT_GROUP.ordinal())
                .setPayload(payload)
                .build();

        sender.sendPacket(pkt);
    }

    // ----------------------- Admin / user commands ---------------------------

    /**
     * CREATE_GROUP
     * Payload:
     *   [int nameLen][nameLen bytes UTF-8]
     */
    public void createGroup(String name, int[] memberIds) {
        if (memberIds == null) {
            memberIds = new int[0];
        }

        byte[] nameBytes = name.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        int nameLen = nameBytes.length;
    
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(
                Integer.BYTES +                    // titleLen
                nameLen +                          // title
                Integer.BYTES +                    // memberCount
                Integer.BYTES * memberIds.length   // member IDs
        );
    
        buf.putInt(nameLen);
        buf.put(nameBytes);
        buf.putInt(memberIds.length);             // 0 when no userIds were specified
        
        for (int id : memberIds) {
            buf.putInt(id);
        }
    
        byte[] payload = buf.array();
        Packet pkt = Packet.createPacket(
            clientId,
            0,
            PacketType.CREATE_GROUP,
            payload
        );

        sender.sendPacket(pkt);
    }
    

    /**
     * ADD_MEMBER
     * Payload:
     *   [int groupId][int memberId]
     */
    public void addMember(int groupId, int memberId) {
        ByteBuffer buf = ByteBuffer.allocate(2 * Integer.BYTES);
        buf.putInt(groupId);
        buf.putInt(memberId);
        byte[] payload = buf.array();

        Packet pkt = new PacketBuilder(payload.length,
                clientId,
                ADMIN_ID,
                PacketType.ADD_MEMBER.ordinal())
                .setPayload(payload)
                .build();

        sender.sendPacket(pkt);
    }

    /**
     * REMOVE_MEMBER
     * Payload:
     *   [int groupId][int memberId]
     */
    public void removeMember(int groupId, int memberId) {
        ByteBuffer buf = ByteBuffer.allocate(2 * Integer.BYTES);
        buf.putInt(groupId);
        buf.putInt(memberId);
        byte[] payload = buf.array();

        Packet pkt = new PacketBuilder(payload.length,
                clientId,
                ADMIN_ID,
                PacketType.REMOVE_MEMBER.ordinal())
                .setPayload(payload)
                .build();

        sender.sendPacket(pkt);
    }

    /**
     * RENAME_GROUP
     * Payload:
     *   [int groupId][int nameLen][nameLen bytes UTF-8]
     */
    public void renameGroup(int groupId, String newName) {
        byte[] nameBytes = newName.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate(
                Integer.BYTES + Integer.BYTES + nameBytes.length);
        buf.putInt(groupId);
        buf.putInt(nameBytes.length);
        buf.put(nameBytes);
        byte[] payload = buf.array();

        Packet pkt = new PacketBuilder(payload.length,
                clientId,
                ADMIN_ID,
                PacketType.RENAME_GROUP.ordinal())
                .setPayload(payload)
                .build();

        sender.sendPacket(pkt);
    }

    /**
     * DELETE_GROUP
     * Payload:
     *   [int groupId]
     */
    public void deleteGroup(int groupId) {
        ByteBuffer buf = ByteBuffer.allocate(Integer.BYTES);
        buf.putInt(groupId);
        byte[] payload = buf.array();

        Packet pkt = new PacketBuilder(payload.length,
                clientId,
                ADMIN_ID,
                PacketType.DELETE_GROUP.ordinal())
                .setPayload(payload)
                .build();

        sender.sendPacket(pkt);
    }

    /**
     * SET_PSEUDO
     * Payload:
     *   [int nameLen][nameLen bytes UTF-8]
     */
    public void setPseudo(String pseudo) {
        byte[] payload = encodeString(pseudo);

        Packet pkt = new PacketBuilder(payload.length,
                clientId,
                ADMIN_ID,
                PacketType.SET_PSEUDO.ordinal())
                .setPayload(payload)
                .build();

        sender.sendPacket(pkt);
    }

    /**
     * ADD_CONTACT
     * Payload:
     *   [int contactId]
     */
    public void addContact(int contactId) {
        ByteBuffer buf = ByteBuffer.allocate(Integer.BYTES);
        buf.putInt(contactId);
        byte[] payload = buf.array();

        Packet pkt = new PacketBuilder(payload.length,
                clientId,
                ADMIN_ID,
                PacketType.ADD_CONTACT.ordinal())
                .setPayload(payload)
                .build();

        sender.sendPacket(pkt);
    }

    /**
     * LIST_CONTACTS
     * payload: empty
     *
     * Ask server to send the contact list
     */
    public Set<Integer> requestContacts() {
        ContactRegistry contactRegistry = Client.getClientState().getContactRegistry();
        Set<Integer> contactIds = contactRegistry.getContacts();
        return contactIds;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Encode a single UTF-8 string as:
     *   [int length][length bytes]
     * This matches IncomingPacketProcessor.readString() on the receiving side.
     */
    private static byte[] encodeString(String s) {
        byte[] data = s.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate(Integer.BYTES + data.length);
        buf.putInt(data.length);
        buf.put(data);
        return buf.array();
    }

    public String resolveUserName(int userId) {
        return Client.getClientState()
                     .getContactRegistry()
                     .resolveUserName(userId);
    }    
}
