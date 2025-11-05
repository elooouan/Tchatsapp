package fr.uga.im2ag.m1info.chatservice.client.ihm;

import javax.swing.*;
import java.awt.*;

public class Window extends JFrame {

    public Window() {
        setLayout(new BorderLayout());
        add(new Chat(), BorderLayout.CENTER);
        add(new ButtonsBar(), BorderLayout.SOUTH);
        pack();
        setSize(new Dimension(800, 600));
        setVisible(true);
    }
}
