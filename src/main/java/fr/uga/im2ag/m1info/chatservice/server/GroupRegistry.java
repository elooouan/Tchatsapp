package fr.uga.im2ag.m1info.chatservice.server;

import fr.uga.im2ag.m1info.chatservice.common.Group;

import java.util.HashMap;
import java.util.Map;

public class GroupRegistry {
    private AdminProcessor adminProcessor;
    private Map<Integer, Group> groups;
    GroupRegistry(AdminProcessor adminProcessor){
        this.adminProcessor = adminProcessor;
        groups = new HashMap<>();
    }

    public Group getGroupById(Integer groupId){
        return groups.get(groupId);
    }
}
