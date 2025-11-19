package fr.uga.im2ag.m1info.chatservice.server;

import fr.uga.im2ag.m1info.chatservice.common.PacketProcessor;
import fr.uga.im2ag.m1info.chatservice.common.PacketSender;

import java.nio.ByteBuffer;

import fr.uga.im2ag.m1info.chatservice.common.Packet;

public class DirectMessageProcessor implements PacketProcessor {
    private PacketSender sender;
    private UserRegistry users;

    public DirectMessageProcessor(PacketSender sender, UserRegistry users) {
        this.sender = sender;
        this.users = users;
    }

    @Override
    public void process(Packet pkt) {
        int from = pkt.from();
        int to = pkt.to();

        // Safety net
        if (!users.exists(from)) {
            sendError(from, "Unknown sender: " + from);
            return;
        }

        // Safety net: Server should have already checked this 
        if (!users.exists(to)) {
            sendError(from, "Unknown recipient: " + to);
            return;
        }

        ByteBuffer payload = pkt.getPayload();
        if (payload == null || payload.remaining() == 0) {
            sendError(from, "Empty direct message payload.");
            return;
        }

        sender.sendPacket(pkt);

        // Comment/Uncomment this ACK - use this for debugging (on the sender side)
        sendOk(from, "Message sent to group " + to);
    }

    // ====================================================================
    // Helpers (same as AdminProcessor)
    // ====================================================================

    private void sendOk(int to, String msg) {
        Packet p = Packet.createTextMessage(0, to, "OK " + msg);
        sender.sendPacket(p);
    }

    private void sendError(int to, String msg) {
        Packet p = Packet.createTextMessage(0, to, "ERROR " + msg);
        sender.sendPacket(p);
    }
}
