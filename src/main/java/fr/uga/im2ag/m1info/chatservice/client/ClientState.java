package fr.uga.im2ag.m1info.chatservice.client;



import fr.uga.im2ag.m1info.chatservice.client.registries.ContactRegistry;
import fr.uga.im2ag.m1info.chatservice.client.registries.GroupRegistry;
import fr.uga.im2ag.m1info.chatservice.client.registries.MessageRegistry;

import java.io.Serializable;

public class ClientState implements Serializable {
    int clientId;
    private GroupRegistry groupRegistry;
    private ContactRegistry contactRegistry;
    private MessageRegistry messageRegistry;

    public ClientState(int clientId){
        this.clientId = clientId;
        groupRegistry = new GroupRegistry();
        contactRegistry = new ContactRegistry();
        messageRegistry = new MessageRegistry();
    }

    public int getClientId(){
        return clientId;
    }

    public ContactRegistry getContactRegistry() {
        return contactRegistry;
    }

    public GroupRegistry getGroupRegistry() {
        return groupRegistry;
    }

    public MessageRegistry getMessageRegistry() {
        return messageRegistry;
    }
}
