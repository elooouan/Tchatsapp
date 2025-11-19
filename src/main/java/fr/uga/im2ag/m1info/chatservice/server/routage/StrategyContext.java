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
            server.sendPacket(p);
        }

        public void sendOk(String message) {
            // PRINT CONSOLE
        }

        public void sendError(String error) {
            // PRINT CONSOLE
        }

        public void sendOk(int cible, String message){
            // build un packet d'erreur puis le send
            //cible.sendPacket();
        }

        public void sendError(int cible, String message){
            // pareil
            //cible.sendPacket();
        }

        public ServerState serverState() {
            return server.getServerState();
        }
}
