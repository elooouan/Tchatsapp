package fr.uga.im2ag.m1info.chatservice.client;

import fr.uga.im2ag.m1info.chatservice.common.Packet;
import fr.uga.im2ag.m1info.chatservice.common.PacketType;
import fr.uga.im2ag.m1info.chatservice.common.PacketProcessor;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Processes packets received by the client from the server.
 * It decodes the payload and forwards "events" to a listener
 */
public class IncomingPacketProcessor implements PacketProcessor {

    /**
     * Callbacks for the client UI / model.
     */
    public interface Listener {
        void onDirectText(int fromUserId, String message);
        void onGroupText(int groupId, int fromUserId, String message);
        void onUnknownPacket(Packet pkt);
        void onACK(String message);
        void onError(String message);
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
            listener.onError("Received packet with no payload, type=" + type);
            return;
        }

        switch (type) {
            case TEXT_USER -> handleDirectText(pkt, payload);
            case TEXT_GROUP -> handleGroupText(pkt, payload);
            case ACK -> handleACK(pkt, payload);
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
        String message = pkt.payloadAsString();
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
        String message = pkt.payloadAsString();
        int fromUserId = pkt.from();
        int groupId = pkt.to();
        listener.onGroupText(groupId, fromUserId, message);
    }

    /**
     * ACK payload format:
     *   [int msgLength][msgLength bytes UTF-8]
     * (message is a human-readable description sent by the server)
     */
    private void handleACK(Packet pkt, ByteBuffer payload) {
        listener.onACK(pkt.payloadAsString());
    }

    /**
     * ERROR payload format:
     *   [int msgLength][msgLength bytes UTF-8]
     * (message is a human-readable description sent by the server)
     */
    private void handleError(Packet pkt, ByteBuffer payload) {
        listener.onError(pkt.payloadAsString());
    }
}
