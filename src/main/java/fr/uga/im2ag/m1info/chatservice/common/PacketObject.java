package fr.uga.im2ag.m1info.chatservice.common;

import java.io.Serializable;

public class PacketObject implements Serializable {
    int from;
    int to;
    PacketType type;
    String content;

    public PacketObject(int from, int to, PacketType type, String content) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.content = content;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public PacketType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }
}
