package fr.uga.im2ag.m1info.chatservice.client.registries;

import fr.uga.im2ag.m1info.chatservice.common.Group;
import fr.uga.im2ag.m1info.chatservice.server.IdGenerator;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GroupRegistry implements Serializable {
    private Map<Integer, Group> groups;

    public GroupRegistry(){
        groups = new HashMap<>();
    }

    public int createGroup(int groupId, String title, int adminId){
        groups.put(groupId, new Group(groupId, title, adminId)); // Adds the admin
        return groupId;
    }

    // Create a group initially comprised of a list of members
    public Group createGroup(int groupId, String title, int adminId, Set<Integer> membersId) {
        groups.put(groupId, new Group(groupId, title, adminId));

        for (int member : membersId) {
            groups.get(groupId).addMember(member);
        }

        return groups.get(groupId);
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
