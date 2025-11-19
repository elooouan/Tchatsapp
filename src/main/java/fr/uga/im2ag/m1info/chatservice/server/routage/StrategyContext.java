package fr.uga.im2ag.m1info.chatservice.server.routage;

import fr.uga.im2ag.m1info.chatservice.common.Packet;
import fr.uga.im2ag.m1info.chatservice.common.PacketSender;
import fr.uga.im2ag.m1info.chatservice.server.ServerState;
import fr.uga.im2ag.m1info.chatservice.server.TchatsAppServer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

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

    private String readString(ByteBuffer buf) {
        if (buf.remaining() < Integer.BYTES) return null;

        int len = buf.getInt(); // Consume [len:int]
        if (len < 0 || buf.remaining() < len) return null;

        byte[] data = new byte[len];
        buf.get(data);

        return new String(data, StandardCharsets.UTF_8); // UTF-8 is the standard for network protocols (UTF-16 is used for java objects)
    }

        public ServerState serverState() {
            return server.getServerState();
        }
}
