package fr.uga.im2ag.m1info.chatservice.common;

public class User {
    private final int userId;
    private String pseudo;

    User(int userId, String pseudo) {
        this.userId = userId;
        this.pseudo = pseudo;
    }

    public String getPseudo() {
        return pseudo;
    }

    public int getUserId() {
        return userId;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

}
