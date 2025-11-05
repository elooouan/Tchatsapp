package fr.uga.im2ag.m1info.chatservice.client.ihm;

import javax.swing.*;
import java.awt.*;

public class ButtonsBar extends JPanel {

    JButton envoyer = new JButton("Envoyer");
    JTextField barre = new JTextField();

    public ButtonsBar() {
        setLayout(new BorderLayout());
        add(envoyer, BorderLayout.EAST);
        add(barre, BorderLayout.CENTER);
    }
}
