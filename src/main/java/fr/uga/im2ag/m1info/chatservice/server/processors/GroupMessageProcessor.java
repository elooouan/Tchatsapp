package fr.uga.im2ag.m1info.chatservice.server.processors;

import fr.uga.im2ag.m1info.chatservice.common.Packet;
import fr.uga.im2ag.m1info.chatservice.common.Packet.PacketBuilder;

import java.nio.ByteBuffer;

import fr.uga.im2ag.m1info.chatservice.common.Group;
import fr.uga.im2ag.m1info.chatservice.common.PacketProcessor;
import fr.uga.im2ag.m1info.chatservice.server.GroupRegistry;
import fr.uga.im2ag.m1info.chatservice.server.ServerState;
import fr.uga.im2ag.m1info.chatservice.server.UserRegistry;

/*
 * Handles group text messages.
 */
public class GroupMessageProcessor implements PacketProcessor {
    private int sender;
    private UserRegistry users;
    private GroupRegistry groups;

    public GroupMessageProcessor(int sender, ServerState serverState) {
        this.sender = sender;
        this.users = serverState.getUserRegistry();
        this.groups = serverState.getGroupRegistry();
    }

    @Override
    public void process(Packet pkt) {
        int from = pkt.from();
        int groupId = pkt.to(); // msg to group

        if (!users.exists(from)) {
            sendError(from, "Unknown sender: " + from);
            return;
        }

        if (!groups.exists(groupId)) {
            sendError(from, "Unknown group: " + groupId);
            return;
        }
        
        Group group = groups.getGroupById(groupId);

        // Enforce membership -> we can change this later if the group is public
        if (!group.getMembers().contains(from)) {
            sendError(from, "User " + from + " is not a member of group " + groupId);
            return;
        }

        ByteBuffer payload = pkt.getPayload();
        if (payload == null || payload.remaining() == 0) {
            sendError(from, "Empty group message payload.");
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
            //sender.sendPacket(out);
        }

        // Comment/Uncomment this ACK - use this for debugging (on the sender side)
        sendOk(from, "Message sent to group " + groupId);
    }


    // ====================================================================
    // Helpers (same as AdminProcessor)
    // ====================================================================

    private void sendOk(int to, String msg) {
        Packet p = Packet.createTextMessage(0, to, "OK " + msg);
        //sender.sendPacket(p);
    }

    private void sendError(int to, String msg) {
        Packet p = Packet.createTextMessage(0, to, "ERROR " + msg);
        //sender.sendPacket(p);
    }
}
