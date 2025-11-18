package fr.uga.im2ag.m1info.chatservice.server;

import java.io.Serializable;

public class ServerState implements Serializable {
    private UserRegistry userRegistry;
    private GroupRegistry groupRegistry;
    private ContactRegistry contactRegistry;
    private IdGenerator idGenerator;

    private class IdIntGenerator implements Serializable,IdGenerator {
        int id = 0;

        @Override
        public synchronized int generateId() {
            int value = id;
            id++;
            return value;
        }
    }

    public ServerState(){
         userRegistry = new UserRegistry();
         groupRegistry = new GroupRegistry();
         contactRegistry = new ContactRegistry();
         idGenerator = new IdIntGenerator();
    }

    public ContactRegistry getContactRegistry() {
        return contactRegistry;
    }

    public UserRegistry getUserRegistry() {
        return userRegistry;
    }

    public GroupRegistry getGroupRegistry() {
        return groupRegistry;
    }

    public IdGenerator getIdGenerator(){
        return idGenerator;
    }
}
