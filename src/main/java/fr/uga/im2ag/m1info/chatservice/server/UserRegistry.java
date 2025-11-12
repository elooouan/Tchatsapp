package fr.uga.im2ag.m1info.chatservice.server;

import fr.uga.im2ag.m1info.chatservice.common.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRegistry {
    private IdGenerator idGenerator;
    private AdminProcessor adminProcessor;
    private Map<Integer,User> users;

    public UserRegistry(AdminProcessor adminProcessor) {
        this.users = new HashMap<>();
        this.adminProcessor = adminProcessor;
    }

    public boolean existUser(int userId) {
        return users.containsKey(userId);
    }

    public int createUser() {
        int userId = adminProcessor.genId();
        User user = new User(userId,null);
        users.put(userId,user);
        return userId;
    }

    public int createUser(int userId) {
        return userId;
    }
}
