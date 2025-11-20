package fr.uga.im2ag.m1info.chatservice.client.ihm;

import javax.swing.*;
import java.awt.*;

public class ChatWindow extends JPanel {
    public ChatWindow() {
        setLayout(new BorderLayout());
        add(new Chat(), BorderLayout.CENTER);
        add(new ContactsList(), BorderLayout.WEST);
    }

    public static class ButtonsBar extends JPanel {

        JButton envoyer = new JButton("Envoyer");
        JTextField barre = new JTextField();

        public ButtonsBar() {
            setLayout(new BorderLayout());
            add(envoyer, BorderLayout.EAST);
            add(barre, BorderLayout.CENTER);
        }
    }
}
