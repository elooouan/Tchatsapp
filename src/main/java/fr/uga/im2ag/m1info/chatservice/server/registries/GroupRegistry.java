package fr.uga.im2ag.m1info.chatservice.server.registries;

import fr.uga.im2ag.m1info.chatservice.common.Group;
import fr.uga.im2ag.m1info.chatservice.server.IdGenerator;

import java.io.Serializable;
import java.util.*;

public class GroupRegistry implements Serializable {
    private Map<Integer, Group> groups = new HashMap<>();
    IdGenerator idGenerator;


    private GroupRegistry(){}

    public GroupRegistry(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public int createGroup(String title, int adminId){
        int groupId = idGenerator.generateId();
        groups.put(groupId, new Group(groupId, title, adminId)); // Adds the admin
        
        return groupId;
    }

    public boolean exists(int groupId){ return groups.containsKey(groupId); }

    public Group getGroupById(Integer groupId) { return groups.get(groupId); }

    public boolean isAdmin(int groupId, int userId){
        Group g = groups.get(groupId);
        return g != null && g.getAdminId() == userId;
    }

    public Set<Integer> membersOf(int groupId){
        Group g = groups.get(groupId);
        return g == null ? Collections.emptySet() : g.getMembers();
    }

    public boolean addMember(int groupId, int userId){
        Group g = groups.get(groupId);
        if (g == null) return false;
        return g.addMember(userId);
    }

    public boolean removeMember(int groupId, int userId){
        Group g = groups.get(groupId);
        if (g == null || g.getAdminId() == userId) return false; // Can't remove admin
        return g.removeMember(userId);
    }

    public boolean rename(int groupId, String title){
        Group g = groups.get(groupId);
        if (g == null) return false;
        g.setTitle(title);
        return true;
    }

    public boolean delete(int groupId) { return groups.remove(groupId) != null; }
}
