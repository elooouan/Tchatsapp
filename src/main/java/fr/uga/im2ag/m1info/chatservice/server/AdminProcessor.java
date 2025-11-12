package fr.uga.im2ag.m1info.chatservice.server;

import fr.uga.im2ag.m1info.chatservice.common.Group;
import fr.uga.im2ag.m1info.chatservice.common.User;

public class AdminProcessor {
    private TchatsAppServer server;
    private UserRegistry userRegistry;
    private GroupRegistry groupRegistry;
    private IdGenerator idGenerator;

    public AdminProcessor(TchatsAppServer server){
        this.server = server;
        userRegistry = new UserRegistry(this);
        groupRegistry = new GroupRegistry(this);
    }

    public int generateId(){
        return idGenerator.generateId();
    }

    public User getUserById(int userId){
        return userRegistry.getUserById(userId);
    }

    public Group getGroupById(int groupId){
        return groupRegistry.getGroupById(groupId);
    }

}
