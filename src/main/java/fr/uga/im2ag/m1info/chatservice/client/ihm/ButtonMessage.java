package fr.uga.im2ag.m1info.chatservice.client.ihm;

import javax.swing.*;

public class ButtonMessage extends JLabel {

    public ButtonMessage(String msg, String sender) {
        super(sender + " : " + msg);
    }
}
