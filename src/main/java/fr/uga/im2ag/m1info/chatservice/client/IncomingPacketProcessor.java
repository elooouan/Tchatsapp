package fr.uga.im2ag.m1info.chatservice.client;

import fr.uga.im2ag.m1info.chatservice.common.Packet;
import fr.uga.im2ag.m1info.chatservice.common.PacketType;
import fr.uga.im2ag.m1info.chatservice.common.PacketProcessor;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Processes packets received by the client from the server.
 * It decodes the payload and forwards "events" to a listener
 * (typically your UI / client model).
 */
public class IncomingPacketProcessor implements PacketProcessor {

    /**
     * Callbacks for the client UI / model.
     * Plug this into whatever you already have on the client side.
     */
    public interface Listener {
        void onDirectText(int fromUserId, String message);
        void onGroupText(int groupId, int fromUserId, String message);
        void onError(String message);
        void onUnknownPacket(Packet pkt);
    }

    private final Listener listener;

    public IncomingPacketProcessor(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void process(Packet pkt) {
        PacketType type = pkt.type();
        ByteBuffer payload = pkt.getPayload();

        if (payload == null) {
            // No payload: treat as malformed / error
            listener.onError("Recu paquet sans payload, type=" + type);
            return;
        }

        switch (type) {
            case TEXT_USER -> handleDirectText(pkt, payload);
            case TEXT_GROUP -> handleGroupText(pkt, payload);
            case ERROR -> handleError(pkt, payload);
            default -> listener.onUnknownPacket(pkt);
        }
    }

    /**
     * TEXT_USER payload format:
     *   [int msgLength][msgLength bytes UTF-8]
     * Sender id = pkt.from()
     * Recipient id = pkt.to() (this client)
     */
    private void handleDirectText(Packet pkt, ByteBuffer payload) {
        String message = readString(payload);
        int fromUserId = pkt.from();
        listener.onDirectText(fromUserId, message);
    }

    /**
     * TEXT_GROUP payload format:
     *   [int msgLength][msgLength bytes UTF-8]
     * Sender id   = pkt.from()
     * Group id    = pkt.to()
     */
    private void handleGroupText(Packet pkt, ByteBuffer payload) {
        String message = readString(payload);
        int fromUserId = pkt.from();
        int groupId = pkt.to();
        listener.onGroupText(groupId, fromUserId, message);
    }

    /**
     * ERROR payload format:
     *   [int msgLength][msgLength bytes UTF-8]
     * (message is a human-readable description sent by the server)
     */
    private void handleError(Packet pkt, ByteBuffer payload) {
        String message = readString(payload);
        listener.onError(message);
    }

    /**
     * Utility: read a single UTF-8 string encoded as:
     *   [int length][length bytes]
     */
    private static String readString(ByteBuffer buffer) {
        // Use a duplicate to avoid impacting any shared buffer state
        ByteBuffer buf = buffer.slice();

        if (buf.remaining() < Integer.BYTES) {
            return "";
        }

        int len = buf.getInt();
        if (len < 0 || buf.remaining() < len) {
            return "";
        }

        byte[] data = new byte[len];
        buf.get(data);
        return new String(data, StandardCharsets.UTF_8);
    }
}
