package fr.uga.im2ag.m1info.chatservice.server;

import fr.uga.im2ag.m1info.chatservice.common.User;

import java.util.HashMap;
import java.util.Map;

public class UserRegistry {
    private IdGenerator idGenerator;
    private AdminProcessor adminProcessor;
    private Map<Integer,User> users;

    public UserRegistry(AdminProcessor adminProcessor) {
        this.users = new HashMap<>();
        this.adminProcessor = adminProcessor;
    }

    public boolean existsUser(int userId) {
        return users.containsKey(userId);
    }

    public int createUser() {
        int userId = adminProcessor.generateId();
        User user = new User(userId,null);
        users.put(userId,user);
        return userId;
    }

    public User getUserById(int userId){
        if(!existsUser(userId)){return null;}
        return users.get(userId);
    }

    public boolean setPseudoUser(int userId, String pseudo){
        if(!existsUser(userId)){return false;}
        User user = getUserById(userId);
        user.setPseudo(pseudo);
        return true;
    }

    public String getPseudoUser(int userId){
        if(!existsUser(userId)){return null;}
        User user = getUserById(userId);
        return user.getPseudo();
    }
}
