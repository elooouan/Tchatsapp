package fr.uga.im2ag.m1info.chatservice.common;

import java.io.Serializable;

public class User implements Serializable {
    private final int userId;
    private String pseudo;

    public User(int userId, String pseudo) {
        this.userId = userId;
        this.pseudo = pseudo;
    }

    public String getPseudo() { return pseudo; }

    public int getUserId() { return userId; }

    public void setPseudo(String pseudo) { this.pseudo = pseudo; }

    @Override
    public boolean equals(Object o) { return (o instanceof User user) && user.userId == userId; }

    @Override
    public int hashCode() { return userId; }
}
