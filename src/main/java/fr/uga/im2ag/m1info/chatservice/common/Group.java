package fr.uga.im2ag.m1info.chatservice.common;

import java.util.HashSet;
import java.util.Set;

public class Group {
    private int id;
    private String title;
    private int adminId;
    private Set<User> members;

    private Group(){}

    public Group(int id, String title, int adminId) {
        this.id = id;
        this.title = title;
        this.adminId = adminId;
        this.members = new HashSet<>();
        members.add(new User(adminId, null)); // Might need a username for the admin
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public int getAdminId() { return adminId; }
    public Set<User> getMembers() { return members; }

    // Setters
    public void setTitle(String title) { this.title = title; }

    public boolean addMember(User user) {
        if(!members.contains(user)) return false;
        members.add(user);
        return true;
    }

    public boolean removeMember(User user) {
        if(user.getUserId() == adminId || !members.contains(user)) return false;
        members.remove(user);
        return true;
    }

    public boolean hasMember(User user){
        return members.contains(user);
    }
}
