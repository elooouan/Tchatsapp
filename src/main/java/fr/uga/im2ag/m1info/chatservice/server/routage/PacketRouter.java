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
    private final Map<PacketType, PacketProcessor> strategies;

    StrategyContext context;
    // Envoie les paquets au bon processor
    public PacketRouter(StrategyContext context) {
        this.context = context;
        strategies = new HashMap<>();

        AdminProcessor admin = new AdminProcessor(context);
        UserProcessor user = new UserProcessor(context);
        ErrorProcessor error = new ErrorProcessor(context);

        strategies.put(PacketType.TEXT_USER,   new DirectMessageProcessor(context));
        strategies.put(PacketType.TEXT_GROUP,  new GroupMessageProcessor(context));

        strategies.put(PacketType.CREATE_GROUP, admin);
        strategies.put(PacketType.ADD_MEMBER,   admin);
        strategies.put(PacketType.REMOVE_MEMBER,admin);
        strategies.put(PacketType.RENAME_GROUP, admin);
        strategies.put(PacketType.DELETE_GROUP, admin);

        strategies.put(PacketType.SET_PSEUDO,  user);
        strategies.put(PacketType.ADD_CONTACT, user);
        strategies.put(PacketType.CREATE_USER, user);

        strategies.put(PacketType.ERROR,       error);
    }

    public PacketProcessor resolve(Packet p) {
        PacketType type = p.type();
        return strategies.getOrDefault(type, new ErrorProcessor(context));
    }
}
