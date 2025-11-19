package fr.uga.im2ag.m1info.chatservice.server.routage;

import fr.uga.im2ag.m1info.chatservice.common.*;
import fr.uga.im2ag.m1info.chatservice.server.ServerState;
import fr.uga.im2ag.m1info.chatservice.server.routage.processors.*;

import java.util.HashMap;
import java.util.Map;

/*
    Unique responsabilité de cette classe : trouver la bonne stratégie à appliquer
 */

public class PacketRouter {
    private PacketSender sender;
    private ServerState serverState;
    private final Map<Integer, PacketProcessor> strategies;

    StrategyContext context;
    // Envoie les paquets au bon processor
    public PacketRouter(StrategyContext context) {
        this.context = context;
        strategies = new HashMap<>();

        strategies.put(PacketType.TEXT_USER, new DirectMessageProcessor(context));
        strategies.put(PacketType.TEXT_GROUP, new GroupMessageProcessor(context));
        strategies.put(PacketType.CREATE_GROUP, new AdminProcessor(context));
        strategies.put(PacketType.ADD_MEMBER, new AdminProcessor(context));
        strategies.put(PacketType.REMOVE_MEMBER, new AdminProcessor(context));
        strategies.put(PacketType.RENAME_GROUP, new AdminProcessor(context));
        strategies.put(PacketType.DELETE_GROUP, new AdminProcessor(context));
        strategies.put(PacketType.SET_PSEUDO, new UserProcessor(context));
        strategies.put(PacketType.ADD_CONTACT, new UserProcessor(context));
        strategies.put(PacketType.ERROR, new ErrorProcessor(context)); // Simple reply (ex: sendError...)
    }

    /*
    public void route(Packet pkt) {
        PacketProcessor strategy;
        int type = pkt.getType();

        switch (type) {
            // l'id est un id d'user
            case PacketType.TEXT_USER:
                strategy = new DirectMessageProcessor(sender, serverState);
                // l'id est un id de groupe
            case PacketType.TEXT_GROUP:
                strategy = new GroupMessageProcessor(sender, serverState);
            case PacketType.CREATE_GROUP:
            case PacketType.ADD_MEMBER:
            case PacketType.REMOVE_MEMBER:
            case PacketType.RENAME_GROUP:
            case PacketType.DELETE_GROUP:
                strategy = new AdminProcessor(serverState);
            case PacketType.SET_PSEUDO:
            case PacketType.ADD_CONTACT:
                strategy = new UserProcessor();
            case PacketType.ERROR:
            default:
                // Meme pas de type correct dans le paquet, erreur interne, errorProcessor aussi ?
                strategy = new ErrorProcessor(sender);
        }
        strategy.process(pkt);
    }
     */

    public PacketProcessor resolve(Packet p) {
        int type = p.type();
        return strategies.getOrDefault(type, new ErrorProcessor(context));
    }
}
