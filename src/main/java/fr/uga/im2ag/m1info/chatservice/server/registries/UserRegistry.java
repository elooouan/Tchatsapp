package fr.uga.im2ag.m1info.chatservice.server.registries;

import fr.uga.im2ag.m1info.chatservice.common.User;
import fr.uga.im2ag.m1info.chatservice.server.IdGenerator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class UserRegistry implements Serializable {
    private Map<Integer,User> users = new HashMap<>();  //holds the corresponding Ids to Users
    private IdGenerator idGenerator;

    private UserRegistry(){}

    public UserRegistry(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public boolean exists(int userId) {
        return users.containsKey(userId);
    }
    
    /* Called AFTER the server generates a userId with IdGenerator -> AdmindProcessor calls UserRegistry not the other way around */
    public int createUser(int userId) {
        int id = idGenerator.generateId();
        users.put(id, new User(userId, null));
        return id;
    }

    public User getUser(int userId) {
        return users.get(userId);
    }

    public void setPseudo(int userId, String pseudo) {
        if (exists(userId)) users.get(userId).setPseudo(pseudo);
    }

    public String getPseudo(int userId) {
        return exists(userId) ? users.get(userId).getPseudo() : null;
    }
}
