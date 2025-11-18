package fr.uga.im2ag.m1info.chatservice.common;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Group implements Serializable {
    private int id;
    private String title;
    private int adminId;
    private Set<Integer> members = new HashSet<>();

    public Group(int id, String title, int adminId) {
        this.id = id;
        this.title = title;
        this.adminId = adminId;
        members.add(adminId);
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getAdminId() { return adminId; }
    public Set<Integer> getMembers() { return Collections.unmodifiableSet(members); }

    public boolean addMember(int userId) { return members.add(userId); }
    public boolean removeMember(int userId) { return members.remove(userId); }
    public boolean hasMember(int userId) { return members.contains(userId); }
}
