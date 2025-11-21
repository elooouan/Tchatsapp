package fr.uga.im2ag.m1info.chatservice.server;

import fr.uga.im2ag.m1info.chatservice.server.registries.ContactRegistry;
import fr.uga.im2ag.m1info.chatservice.server.registries.GroupRegistry;
import fr.uga.im2ag.m1info.chatservice.server.registries.PacketMissingRegistry;
import fr.uga.im2ag.m1info.chatservice.server.registries.UserRegistry;

import java.io.Serializable;

public class ServerState implements Serializable {
    private UserRegistry userRegistry;
    private GroupRegistry groupRegistry;
    private ContactRegistry contactRegistry;
    private PacketMissingRegistry packetMissingRegistry;
    private IdGenerator idGenerator;

    private class IdIntGenerator implements IdGenerator {
        int id = 1; // id 0 is reserved for the server

        @Override
        public synchronized int generateId() {
            return id++;
        }
    }

    public ServerState() {
        idGenerator = new IdIntGenerator();
        userRegistry = new UserRegistry(idGenerator);
        groupRegistry = new GroupRegistry(idGenerator);
        packetMissingRegistry = new PacketMissingRegistry();
        contactRegistry = new ContactRegistry();
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

    public PacketMissingRegistry getPacketMissingRegistry(){
        return packetMissingRegistry;
    }

    public IdGenerator getIdGenerator(){
        return idGenerator;
    }
}
