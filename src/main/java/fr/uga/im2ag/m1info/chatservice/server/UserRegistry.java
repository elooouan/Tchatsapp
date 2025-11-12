package fr.uga.im2ag.m1info.chatservice.server;

import fr.uga.im2ag.m1info.chatservice.common.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRegistry {
    private IdGenerator idGenerator;

    private Map<Integer,User> users;

    public UserRegistry() {
        this.users = new HashMap<>();
    }

    public boolean existUser(int userId) {
        return users.containsKey(userId);
    }

    public int createUser() {
        return 0;
    }

    public int createUser(int userId) {
        return userId;
    }
}
