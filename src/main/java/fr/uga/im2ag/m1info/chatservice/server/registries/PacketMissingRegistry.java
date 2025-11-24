package fr.uga.im2ag.m1info.chatservice.server.registries;

import fr.uga.im2ag.m1info.chatservice.common.Packet;
import fr.uga.im2ag.m1info.chatservice.common.PacketObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PacketMissingRegistry implements Serializable {
    Map<Integer, List<PacketObject>> userPackets = new HashMap<>();

    public boolean hasPackets(int to){
        return userPackets.containsKey(to);
    }

    public List<Packet> getPackets(int to){
        List<Packet> packets = new ArrayList<>();
        for(PacketObject p : userPackets.get(to)){
            packets.add(Packet.createPacket(p.getFrom(),p.getTo(),p.getType(),p.getContent()));
        }
        return packets;
    }
}
