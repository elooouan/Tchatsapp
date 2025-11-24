package fr.uga.im2ag.m1info.chatservice.client.registries;

import fr.uga.im2ag.m1info.chatservice.common.Message;

import java.io.Serializable;
import java.util.*;

public class MessageRegistry implements Serializable {
    private Map<Integer, List<Message>> convMessages = new HashMap<>();

    public List<Message> getMessages(int conversationId){
        return Collections.unmodifiableList(convMessages.get(conversationId));
    }

    public void addMessage(int conversationId, int from ,String message){
        if(!convMessages.containsKey(conversationId)){convMessages.put(conversationId,new ArrayList<Message>());}

        convMessages.get(conversationId).add(new Message(from,message));
    }
}
