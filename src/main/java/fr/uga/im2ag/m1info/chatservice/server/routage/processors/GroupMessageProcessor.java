package fr.uga.im2ag.m1info.chatservice.server.routage.processors;

import fr.uga.im2ag.m1info.chatservice.common.*;
import fr.uga.im2ag.m1info.chatservice.common.Packet.PacketBuilder;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import fr.uga.im2ag.m1info.chatservice.server.registries.GroupRegistry;
import fr.uga.im2ag.m1info.chatservice.server.registries.UserRegistry;
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
        // Sanity check: this processor should only handle TEXT_GROUP packets
        if (pkt.type() != PacketType.TEXT_GROUP) {
            throw new IllegalArgumentException(
                "GroupMessageProcessor received non TEXT_GROUP packet, type=" + pkt.type()
            );
        }

        int from = pkt.from();
        int groupId = pkt.to(); // destination is a group

        if (!users.exists(from)) {
            throw new IllegalArgumentException("Unknown sender: " + from);
        }

        if (!groups.exists(groupId)) {
            throw new IllegalArgumentException("Unknown group: " + groupId);
        }
        
        Group group = groups.getGroupById(groupId);

        // Enforce membership -> we can change this later if the group is public
        if (!group.getMembers().contains(from)) {
            throw new IllegalArgumentException("User " + from + " is not a member of group " + groupId);
        }

        ByteBuffer payload = pkt.getPayload();
        if (payload == null || payload.remaining() == 0) {
            throw new IllegalArgumentException("Empty group message payload.");
        }

        String message = context.readString(payload);
        ByteBuffer bufferToSend = ByteBuffer.allocate(1+ // GROUP_EVENT subtype
                                                        Integer.BYTES+ //groupId
                                                        Integer.BYTES+ //message length
                                                        message.length() // message
        );
        bufferToSend.put(GroupEventType.MESSAGE_RECIEVED);
        bufferToSend.putInt(groupId);
        bufferToSend.putInt(message.length());
        bufferToSend.put(message.getBytes(StandardCharsets.UTF_8));
        byte[] payloadToSend = bufferToSend.array();

        // Broadcast to all members of the group
        for (int memberId : group.getMembers()) {
            if (!users.exists(memberId)) continue; // Safety net, deleted users should be automatically removed from all groups and contacts

            Packet out = Packet.createPacket(from,memberId,PacketType.GROUP_EVENT,payloadToSend);
            context.send(out);
        }

        // Comment/Uncomment this ACK - use this for debugging (on the sender side)
        context.sendOk(pkt.from(), "Message sent to group " + groupId);
    }
}
