package fr.uga.im2ag.m1info.chatservice.server;

import fr.uga.im2ag.m1info.chatservice.common.Group;
import fr.uga.im2ag.m1info.chatservice.common.Packet;
import fr.uga.im2ag.m1info.chatservice.common.PacketProcessor;
import fr.uga.im2ag.m1info.chatservice.common.PacketTypes;
import fr.uga.im2ag.m1info.chatservice.common.User;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/*
 * Handles commands of type "admin" (destId == 0)
 */
public class AdminProcessor implements PacketProcessor {
    private TchatsAppServer server;
    private UserRegistry users;
    private GroupRegistry groups;
    private ContactRegistry contacts;
    private IdGenerator idGenerator;

    public AdminProcessor(TchatsAppServer server,
                          UserRegistry users,
                          GroupRegistry groups,
                          ContactRegistry contacts) {
        this.server = server;
        this.users = users;
        this.groups = groups;
        this.contacts = contacts;
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
            case PacketTypes.CREATE_GROUP:
                handleCreateGroup(pkt.from(), payload);
                break;
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
    
    // ====================================================================
    // Helpers
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
        server.sendPacket(p);
    }

    private void sendError(int to, String msg) {
        Packet p = Packet.createTextMessage(0, to, "ERROR " + msg);
        server.sendPacket(p);
    }
}
