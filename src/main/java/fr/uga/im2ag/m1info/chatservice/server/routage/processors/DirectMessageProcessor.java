package fr.uga.im2ag.m1info.chatservice.server.routage.processors;

import java.nio.ByteBuffer;

import fr.uga.im2ag.m1info.chatservice.common.Packet;
import fr.uga.im2ag.m1info.chatservice.common.PacketProcessor;
import fr.uga.im2ag.m1info.chatservice.common.PacketSender;
import fr.uga.im2ag.m1info.chatservice.server.ServerState;
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
        int from = pkt.from(); // a fix avec une structure meilleure
        int to = pkt.to();

        // Safety net
        if (!users.exists(from)) {
            context.sendError("Unknown sender: " + from);
            return;
        }

        // Safety net: Server should have already checked this 
        if (!users.exists(to)) {
            context.sendError("Unknown recipient: " + to);
            return;
        }

        ByteBuffer payload = pkt.getPayload();
        if (payload == null || payload.remaining() == 0) {
            context.sendError("Empty direct message payload.");
            return;
        }

        //sender.sendPacket(pkt);

        // Comment/Uncomment this ACK - use this for debugging (on the sender side)
        context.sendOk("Message sent to group " + to);
    }
}
