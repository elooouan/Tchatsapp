package fr.uga.im2ag.m1info.chatservice.client;

import fr.uga.im2ag.m1info.chatservice.common.Group;

import java.util.*;


public class ClientState {
    private int id;

    private Map<Integer, String> contacts = new HashMap<>(); // We can use Set<User> instead but it's more of a pain
    private Map<Integer, Group> groups = new HashMap<>();
    private Map<Integer, Set<Integer>> groupMembers= new HashMap<>();

    ClientState(int id) {
        this.id = id;
        contacts = new HashMap<>();
        groups = new HashMap<>();
        groupMembers = new HashMap<>();
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }
    public void setContact(int userId, String username) {
        contacts.put(userId, username);
    }
    public void setGroup(int groupId, Group group) {
        groups.put(groupId, group);
        groupMembers.put(groupId, group.getMembers());
    }

    // Getters
    public int getId() { return id; }
    public String getContact(int userId) {
        return contacts.get(userId);
    }
    public Group getGroup(int groupId) {
        return groups.get(groupId);
    }
    public Set<Integer> getGroupMembers(int groupId) {
        Set<Integer> members = groupMembers.get(groupId);
        if (members == null) return Collections.emptySet(); // to avoid NullPointerException
        return Collections.unmodifiableSet(members);
    }

    // Add/Remove
    public void removeGroup(int groupId) {
        groups.remove(groupId);
        groupMembers.remove(groupId);
    } 

    public void addMember(int groupId, Integer userId) {
        groupMembers.get(groupId).add(userId);
    }

    public void removeMember(int groupId, Integer userId) {
        groupMembers.get(groupId).remove(userId);
        // Delete the group if it's empty
        if (groupMembers.get(groupId).isEmpty()) {
            removeGroup(groupId);
        }
    }

    // Used for disconnection -> reset memory
    public void reset() {
        id = 0; // might not be needed if we just increment every Id regardless of disconnections (if ids are never re-usable)
        contacts.clear();
        groups.clear();
        groupMembers.clear();
    }
}
