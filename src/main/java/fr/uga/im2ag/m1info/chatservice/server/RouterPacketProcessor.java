package fr.uga.im2ag.m1info.chatservice.server;

import java.util.HashMap;
import java.util.Map;

import fr.uga.im2ag.m1info.chatservice.common.PacketProcessor;
import fr.uga.im2ag.m1info.chatservice.common.Packet;

public class RouterPacketProcessor implements PacketProcessor {
    // virtualProcessors only contains (0 -> AdminProcessor)
    // We use a map in case we ever want to add more virtualProcessors (ex: notificationProcessor, authProcessor,...)
    private Map<Integer, PacketProcessor> virtualProcessors = new HashMap<>(); // "virtual" in the sense that it doesn’t represent a real user or a real group.
    private UserRegistry users;
    private GroupRegistry groups;

    // Handlers for TEXT packets (normal text messages) to other users and groups
    private PacketProcessor textDirectHandler;
    private PacketProcessor textGroupHandler; // We can have an "infinite" amount of groups so we don't add them to the virtualProcessors Map

    public RouterPacketProcessor(UserRegistry users, GroupRegistry groups) {
        this.users = users;
        this.groups = groups;
    }

    public void register(int destId, PacketProcessor p) {
        if (p == null) virtualProcessors.remove(destId);
        else virtualProcessors.put(destId, p);
    }

    public void setTextHandlers(PacketProcessor direct, PacketProcessor group) {
        this.textDirectHandler = direct;
        this.textGroupHandler = group;
    }

    @Override
    public void process(Packet pkt) {
        int to = pkt.to();

        // 1) Admin (to = 0)
        PacketProcessor vp = virtualProcessors.get(to); // REMINDER: processors only contains (0 -> AdminProcessor)
        if (vp != null) {
            vp.process(pkt);
            return;
        }

        // 2) Group messages
        if (groups.exists(to)) {
            if (textGroupHandler != null) textGroupHandler.process(pkt);
            return;
        }

        // 3) Direct messages
        if (users.exists(to)) {
            if (textDirectHandler != null) textDirectHandler.process(pkt);
            return;
        }
    }
}
