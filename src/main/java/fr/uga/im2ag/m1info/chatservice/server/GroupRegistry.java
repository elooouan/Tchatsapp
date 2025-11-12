package fr.uga.im2ag.m1info.chatservice.server;

import fr.uga.im2ag.m1info.chatservice.common.Group;
import fr.uga.im2ag.m1info.chatservice.common.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GroupRegistry {
    private AdminProcessor adminProcessor;
    private Map<Integer, Group> groups;
    GroupRegistry(AdminProcessor adminProcessor){
        this.adminProcessor = adminProcessor;
        groups = new HashMap<>();
    }

    public int createGroup(String title, int adminId){
        int groupId = adminProcessor.generateId();
        Group group = new Group(groupId,title,adminId);
        groups.put(groupId,group);
        return groupId;
    }

    public boolean existsGroup(int groupId){
        return groups.containsKey(groupId);
    }

    public Group getGroupById(Integer groupId){
        return groups.get(groupId);
    }

    public boolean isGroupAdmin(int groupId, int userId){
        if(!existsGroup(groupId)){return false;}
        return groups.get(groupId).getAdminId() == userId;
    }

    public Set<User> membersOfGroup(int groupId){
        if(!existsGroup(groupId)){return null;}
        return groups.get(groupId).getMembers();
    }

    public boolean addGroupMember(int groupId, int userId){
        if(!existsGroup(groupId)){return false;}

        Group group = groups.get(groupId);
        User user = adminProcessor.getUserById(userId);

        if(user == null || group.hasMember(user)){return false;}

        group.addMember(user);
        return true;
    }

    public boolean removeGroupMember(int groupId, int userId){
        if(!existsGroup(groupId)){return false;}

        Group group = groups.get(groupId);
        User user = adminProcessor.getUserById(userId);

        if(user == null || !group.hasMember(user) || group.getAdminId() == userId){return false;}

        group.removeMember(user);
        return true;
    }

    public boolean renameGroup(int groupId, String title){
        if(!existsGroup(groupId)){return false;}

        Group group = groups.get(groupId);
        group.setTitle(title);
        return true;
    }

    public boolean deleteGroup(int groupId){
        if(!existsGroup(groupId)){return false;}

        groups.remove(groupId);
        return true;
    }
}
