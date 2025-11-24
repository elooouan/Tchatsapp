package fr.uga.im2ag.m1info.chatservice.ui;

import fr.uga.im2ag.m1info.chatservice.client.ClientAPI;
import fr.uga.im2ag.m1info.chatservice.client.IncomingPacketProcessor;
import fr.uga.im2ag.m1info.chatservice.client.registries.ContactRegistry;

import java.util.Set;

/**
 * Simple implementation of IncomingPacketProcessor.Listener
 * that prints events to the console.
 */
public class ConsoleClientListener implements IncomingPacketProcessor.Listener {

    private ClientAPI api;

    public void setApi(ClientAPI api) {
        this.api = api;
    }

    @Override
    public void onDirectText(int fromUserId, String message) {
        System.out.println("[DM from user " + api.resolveUserName(fromUserId) + "] " + message);
    }

    @Override
    public void onGroupText(int groupId, int fromUserId, String message) {
        System.out.println("[Group " + groupId + " | from user " + api.resolveUserName(fromUserId) + "] " + message);
    }

    @Override
    public void onMemberAdded(int groupId, int addedMember) {
        System.out.println("[Group " + groupId + " ] " + api.resolveUserName(addedMember) + " was added");
    }

    @Override
    public void onMemberRemoved(int groupId, int removedMember) {
        System.out.println("[Group " + groupId + " ] " + api.resolveUserName(removedMember) + " was removed");
    }

    @Override
    public void onGroupRenamed(int groupId, String title) {
        System.out.println("[Group " + groupId + " ] was renamed to " + title);
    }

    @Override
    public void onGroupDeleted(int groupId) {
        System.out.println("[Group " + groupId + " ] was deleted");
    }

    @Override
    public void onGroupCreated(int groupId, String title, int adminId, Set<Integer> membersIds) {
        StringBuilder memberList = new StringBuilder();
        for(int member : membersIds){
            memberList.append(api.resolveUserName(member));
            memberList.append(" ");
        }

        System.out.println("[Group " + groupId + " ] " + title + " was created with " + memberList.toString());
    }

    @Override
    public void onACK(String message) {
        System.err.println("[ACK] " + message);
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
