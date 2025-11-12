package fr.uga.im2ag.m1info.chatservice.client;

import fr.uga.im2ag.m1info.chatservice.common.Group;
import fr.uga.im2ag.m1info.chatservice.common.User;

import java.util.*;


public class ClientState {
    private int id;

    private Map<Integer, String> contacts = new HashMap<>(); // We can use Set<User> instead but it's more of a pain
    private Map<Integer, Group> groups = new HashMap<>();
    private Map<Integer, Set<User>> groupMembers= new HashMap<>();

    ClientState(int id) {
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
    public Set<User> getGroupMembers(int groupId) {
        return groupMembers.get(groupId);
    }

    // Add/Remove
    public void removeGroup(int groupId) {
        groups.remove(groupId);
        groupMembers.remove(groupId);
    } 

    public void addMember(int groupId, User user) {
        groupMembers.get(groupId).add(user);
    }

    public void removeMember(int groupId, User user) {
        groupMembers.get(groupId).remove(user);
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
