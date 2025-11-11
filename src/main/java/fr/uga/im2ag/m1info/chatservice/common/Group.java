package fr.uga.im2ag.m1info.chatservice.common;

import java.util.Set;

public class Group {
    private int id;
    private String title;
    private int adminId;
    private Set<User> members;

    public Group(int id, String title, int adminId) {
        this.id = id;
        this.title = title;
        this.adminId = adminId;
        members.add(new User(adminId, null)); // Might need a username for the admin
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public int getAdminId() { return adminId; }
    public Set<User> getMembers() { return members; }

    // Setters
    public void setTitle(String title) { this.title = title; }

    public void addMember(User user) { members.add(user); }
    public void removeMember(User user) { members.remove(user); }
}
