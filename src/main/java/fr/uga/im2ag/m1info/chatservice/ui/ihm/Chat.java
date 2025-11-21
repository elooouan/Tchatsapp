package fr.uga.im2ag.m1info.chatservice.ui.ihm;

import javax.swing.*;
import java.awt.*;

public class Chat extends JScrollPane {

    public Chat() {
        super();
        setPreferredSize(new Dimension(100, 200));

        JPanel listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.add(new ButtonMessage("Moi", "Message 1"));
        listContainer.add(new ButtonMessage("Moi", "Message 2"));
        listContainer.add(new ButtonMessage("Moi", "Message 3"));
        setViewportView(listContainer);
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }
}
