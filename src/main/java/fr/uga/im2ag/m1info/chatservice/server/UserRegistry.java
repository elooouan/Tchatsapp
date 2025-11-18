package fr.uga.im2ag.m1info.chatservice.server;

import fr.uga.im2ag.m1info.chatservice.common.User;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class UserRegistry implements Serializable {
    private Map<Integer,User> users = new HashMap<>();

    public boolean exists(int userId) {
        return users.containsKey(userId);
    }
    
    /* Called AFTER the server generates a userId with IdGenerator -> AdmindProcessor calls UserRegistry not the other way around */
    public void createUser(int userId) {
        users.put(userId, new User(userId, null));
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
