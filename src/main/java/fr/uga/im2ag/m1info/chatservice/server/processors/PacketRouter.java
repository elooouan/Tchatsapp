package fr.uga.im2ag.m1info.chatservice.server.processors;

import fr.uga.im2ag.m1info.chatservice.common.PacketProcessor;
import fr.uga.im2ag.m1info.chatservice.server.GroupRegistry;
import fr.uga.im2ag.m1info.chatservice.server.ServerState;
import fr.uga.im2ag.m1info.chatservice.server.UserRegistry;
import fr.uga.im2ag.m1info.chatservice.common.Packet;

public class PacketRouter {
    // virtualProcessors only contains (0 -> AdminProcessor)
    // We use a map in case we ever want to add more virtualProcessors (ex: notificationProcessor, authProcessor,...)
    private ServerState serverState;
    private UserRegistry users;
    private GroupRegistry groups;

    // Handlers for TEXT packets (normal text messages) to other users and groups
    private PacketRouter(ServerState server) {
        this.serverState = server;
        this.users = serverState.getUserRegistry();
        this.groups = serverState.getGroupRegistry();
    }

    public void route(Packet pkt) {
        int to = pkt.to();
        int from = pkt.from(); // A changer ici, je vois juste pas comment récup le sender

        PacketProcessor strategy = null;

        // 1) Admin (to = 0)
        if (to == 0) {
            strategy = new AdminProcessor(serverState);
        }

        // 2) Group messages
        if (groups.exists(to)) {
            strategy = new GroupMessageProcessor(from, serverState);
        }

        // 3) Direct messages
        if (users.exists(to)) {
            strategy = new DirectMessageProcessor(from, serverState);
        }
        if (strategy == null) {
            // erreur interne, pas réussi a interprêter le packet
            strategy = new ErrorProcessor();
        }
        strategy.process(pkt);
    }
}
