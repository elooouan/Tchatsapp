package fr.uga.im2ag.m1info.chatservice.server.routage;

import fr.uga.im2ag.m1info.chatservice.common.Packet;
import fr.uga.im2ag.m1info.chatservice.common.PacketSender;
import fr.uga.im2ag.m1info.chatservice.server.ServerState;
import fr.uga.im2ag.m1info.chatservice.server.TchatsAppServer;

public class StrategyContext {
        private final TchatsAppServer server;
        private final Packet packet;

        public StrategyContext(TchatsAppServer server, Packet packet) {
            this.server = server;
            this.packet = packet;
        }

        public Packet getPacket() { return packet; }

        public void send(Packet p) {
            // Envoyer des paquets aux users
            server.sendPacket(p);
        }


    public void sendOk(int cible, String message) {
        //Packet ack = Packet.createAck(msg.to(), msg.from(), message);
        //server.sendPacket(ack);
        System.out.println("ACK");
    }

        /*
        public void sendError(int cible, String errorMessage) {
            //Packet errPacket = Packet.createError(msg.to(), msg.from(), errorMessage);
            //server.sendPacket(errPacket);
            System.out.println("ERREUR :" + errorMessage);
        }
        */

        public ServerState serverState() {
            return server.getServerState();
        }
}
