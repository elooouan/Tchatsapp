package fr.uga.im2ag.m1info.chatservice.client.ihm;

import javax.swing.*;
import java.awt.*;

public class Chat extends JScrollPane {

    private JList<String> langages;

    public Chat()
    {
        super(new JTextArea(5, 30));
        setPreferredSize(new Dimension(100, 200));
        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement("Message 1");
        model.addElement("Message 2");
        model.addElement("Message 3");
        langages = new JList<>(model);
        add(langages);
        setBackground(Color.ORANGE);

    }
}
