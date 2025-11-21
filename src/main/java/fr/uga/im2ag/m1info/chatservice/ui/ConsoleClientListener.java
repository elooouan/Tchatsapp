package fr.uga.im2ag.m1info.chatservice.ui;

import fr.uga.im2ag.m1info.chatservice.client.IncomingPacketProcessor;

/**
 * Simple implementation of IncomingPacketProcessor.Listener
 * that prints events to the console.
 */
public class ConsoleClientListener implements IncomingPacketProcessor.Listener {

    @Override
    public void onDirectText(int fromUserId, String message) {
        // You can later replace "user <id>" with a pseudo lookup if you have one
        System.out.println("[DM from user " + fromUserId + "] " + message);
    }

    @Override
    public void onGroupText(int groupId, int fromUserId, String message) {
        System.out.println("[Group " + groupId + " | from user " + fromUserId + "] " + message);
    }

    @Override
    public void onError(String message) {
        System.err.println("[ERROR] " + message);
    }

    @Override
    public void onUnknownPacket(fr.uga.im2ag.m1info.chatservice.common.Packet pkt) {
        System.err.println("[UNKNOWN PACKET] type=" + pkt.type()
                + " from=" + pkt.from()
                + " to=" + pkt.to()
                + " (payload size=" + (pkt.getPayload() != null ? pkt.getPayload().remaining() : 0) + ")");
    }
}
