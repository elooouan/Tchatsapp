package fr.uga.im2ag.m1info.chatservice.server.routage.processors;

import java.nio.ByteBuffer;

import fr.uga.im2ag.m1info.chatservice.common.Packet;
import fr.uga.im2ag.m1info.chatservice.common.PacketProcessor;
import fr.uga.im2ag.m1info.chatservice.common.PacketType;
import fr.uga.im2ag.m1info.chatservice.server.UserRegistry;
import fr.uga.im2ag.m1info.chatservice.server.routage.StrategyContext;

public class DirectMessageProcessor implements PacketProcessor {
    StrategyContext context;
    private UserRegistry users;

    public DirectMessageProcessor(StrategyContext context) {
        this.context = context;
        this.users = context.serverState().getUserRegistry();
    }

    @Override
    public void process(Packet pkt) {
        // Sanity check: this processor should only handle TEXT_GROUP packets
        if (pkt.type() != PacketType.TEXT_USER.ordinal()) {
            throw new IllegalArgumentException(
                "DirectMessageProcessor received non TEXT_USER packet, type=" + pkt.type()
            );
        }

        int from = pkt.from(); // a fix avec une structure meilleure
        int to = pkt.to();

        // Safety net
        if (!users.exists(from)) {
            throw new IllegalArgumentException("Unknown sender: " + from);
        }

        // Safety net: Server should have already checked this 
        if (!users.exists(to)) {
            throw new IllegalArgumentException("Unknown recipient: " + to);
        }

        ByteBuffer payload = pkt.getPayload();
        if (payload == null || payload.remaining() == 0) {
            throw new IllegalArgumentException("Empty direct message payload.");
        }

        context.send(pkt);

        // Comment/Uncomment this ACK - use this for debugging (on the sender side)
        context.sendOk(from, "Message sent to group " + to);
    }
}
