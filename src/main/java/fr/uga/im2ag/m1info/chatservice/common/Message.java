package fr.uga.im2ag.m1info.chatservice.common;

import java.io.Serializable;

public class Message implements Serializable {
    int from;
    String message;

    public Message(int from,int to,String message){
        this.from = from;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public int getFrom() {
        return from;
    }
}
