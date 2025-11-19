package fr.uga.im2ag.m1info.chatservice.server.routage.processors;

import fr.uga.im2ag.m1info.chatservice.common.Packet;
import fr.uga.im2ag.m1info.chatservice.common.Packet.PacketBuilder;

import java.nio.ByteBuffer;

import fr.uga.im2ag.m1info.chatservice.common.Group;
import fr.uga.im2ag.m1info.chatservice.common.PacketProcessor;
import fr.uga.im2ag.m1info.chatservice.common.PacketSender;
import fr.uga.im2ag.m1info.chatservice.server.GroupRegistry;
import fr.uga.im2ag.m1info.chatservice.server.ServerState;
import fr.uga.im2ag.m1info.chatservice.server.UserRegistry;
import fr.uga.im2ag.m1info.chatservice.server.routage.StrategyContext;

/*
 * Handles group text messages.
 */
public class GroupMessageProcessor implements PacketProcessor {
    private StrategyContext context;
    private UserRegistry users;
    private GroupRegistry groups;

    public GroupMessageProcessor(StrategyContext context) {
        this.context = context;
        this.users = context.serverState().getUserRegistry();
        this.groups = context.serverState().getGroupRegistry();
    }

    @Override
    public void process(Packet pkt) {
        int from = pkt.from();
        int groupId = pkt.to(); // msg to group

        if (!users.exists(from)) {
            context.sendError("Unknown sender: " + from);
            return;
        }

        if (!groups.exists(groupId)) {
            context.sendError("Unknown group: " + groupId);
            return;
        }
        
        Group group = groups.getGroupById(groupId);

        // Enforce membership -> we can change this later if the group is public
        if (!group.getMembers().contains(from)) {
            context.sendError("User " + from + " is not a member of group " + groupId);
            return;
        }

        ByteBuffer payload = pkt.getPayload();
        if (payload == null || payload.remaining() == 0) {
            context.sendError("Empty group message payload.");
            return;
        }

        ByteBuffer readOnly = payload.asReadOnlyBuffer(); // Read Only duplicate of the payload to avoid interfering with the original buffer
        byte[] payloadBytes = new byte[readOnly.remaining()]; // Allocate a byte array of size duplicate buffer
        readOnly.get(payloadBytes); // Read the entire payload into the byte array: payloadBytes 
        int payloadSize = payloadBytes.length;

        // Broadcast to all members of the group except the sender
        for (int memberId : group.getMembers()) {
            if (memberId == from) continue;
            if (!users.exists(memberId)) continue; // Safety net, deleted users should be automatically removed from all groups and contacts
            
            PacketBuilder pb = new PacketBuilder(payloadSize, from, memberId);
            pb.setPayload(payloadBytes);

            Packet out = pb.build();
            context.send(out);
        }

        // Comment/Uncomment this ACK - use this for debugging (on the sender side)
        context.sendOk("Message sent to group " + groupId);
    }
}
