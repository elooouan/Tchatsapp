package fr.uga.im2ag.m1info.chatservice.server.packets;

import fr.uga.im2ag.m1info.chatservice.common.Packet;
import fr.uga.im2ag.m1info.chatservice.common.PacketType;
import fr.uga.im2ag.m1info.chatservice.server.ServerState;

import java.nio.ByteBuffer;

public class MainPacketProcesor {
    /* Classe implémentant le pattern builder Strategy et selon le type de paquet utilise le bon processor pour le traiter */

    /* Singleton ? */
    ServerState serverState;

    public MainPacketProcesor(ServerState serverState) {
        this.serverState = serverState;
    }

    public void route(Packet pkt) {
        ByteBuffer payload = pkt.getPayload();
        int type = payload.get();

        PacketStrategy strategy;

        switch (type) {
            case PacketType.TEXT:
                strategy = new DirectMessageProcessor();
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
                strategy = new ErrorProcessor();
        }
        strategy.process(pkt);
    }
}
