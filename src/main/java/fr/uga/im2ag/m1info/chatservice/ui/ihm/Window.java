package fr.uga.im2ag.m1info.chatservice.ui.ihm;

import javax.swing.*;
import java.awt.*;

public class Window extends JFrame {

    public Window() {
        setLayout(new BorderLayout());
        add(new ChatWindow(), BorderLayout.CENTER);
        add(new ChatWindow.ButtonsBar(), BorderLayout.SOUTH);
        pack();
        setSize(new Dimension(800, 600));
        setVisible(true);
    }
}
