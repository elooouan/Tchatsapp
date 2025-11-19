package fr.uga.im2ag.m1info.chatservice.client;

import fr.uga.im2ag.m1info.chatservice.common.PacketProcessor;

import java.nio.ByteBuffer;

import fr.uga.im2ag.m1info.chatservice.common.Packet;

public class IncomingPacketProcessor implements PacketProcessor {
    private ClientState state;

    public IncomingPacketProcessor(ClientState state) { this.state = state; }

    @Override
    public void process(Packet pkt) {
        ByteBuffer payload = pkt.getPayload();
        if (payload == null || payload.remaining() < Integer.BYTES) return;

        int type = payload.getInt();

        switch (type) {
            
        }
    }

    // ====================================================================
    // Handlers
    // ====================================================================

    // At this point [type:int] has already been consumed 

    private void handleText(int fromId, ByteBuffer payload) {
        byte[] msgBytes = new byte[payload.remaining()];
        payload.get(msgBytes);

    }
}
