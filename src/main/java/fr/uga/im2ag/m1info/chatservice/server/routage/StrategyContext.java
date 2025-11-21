package fr.uga.im2ag.m1info.chatservice.server.routage;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import fr.uga.im2ag.m1info.chatservice.common.Packet;
import fr.uga.im2ag.m1info.chatservice.common.PacketType;
import fr.uga.im2ag.m1info.chatservice.server.ServerState;
import fr.uga.im2ag.m1info.chatservice.server.TchatsAppServer;

public class StrategyContext {
    private final TchatsAppServer server;
    private final Packet packet;

    public StrategyContext(TchatsAppServer server, Packet packet) {
        this.server = server;
        this.packet = packet;
    }

    public Packet getPacket() {
        return packet;
    }

    public void send(Packet p) {
        // Send packet to users
        server.sendPacket(p);
    }

    public String readString(ByteBuffer buf) {
        if (buf.remaining() < Integer.BYTES) return null;

        int len = buf.getInt();
        if (len < 0 || buf.remaining() < len) return null;

        byte[] data = new byte[len];
        buf.get(data);

        return new String(data, StandardCharsets.UTF_8);
    }

    public void sendOk(int destId, String message) {
        Packet ack = Packet.createPacket(
            0,
            destId,
            PacketType.ACK,
            message
        );

        server.sendPacket(ack);
    }

    public void sendError(int destId, String message) {
        Packet error = Packet.createPacket(
            0,
            destId,
            PacketType.ERROR,
            message
        );
        
        server.sendPacket(error);
    }
    
    public ServerState serverState() {
        return TchatsAppServer.getServerState();
    }
}
