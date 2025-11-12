package fr.uga.im2ag.m1info.chatservice.client.ihm;

import javax.swing.*;
import java.awt.*;

public class ContactsList extends JScrollPane {
    public ContactsList() {
        super();
        setPreferredSize(new Dimension(150, 200));
        JPanel listContainer = new JPanel();

        GridBagLayout layout = new GridBagLayout();
        listContainer.setLayout(layout);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        listContainer.add(new ButtonContact("Contact 1"), gbc);
        gbc.gridy++;
        listContainer.add(new ButtonContact("Contact 2"), gbc);
        gbc.gridy++;
        listContainer.add(new ButtonContact("Contact 3"), gbc);

        gbc.gridy++;
        gbc.weighty = 1.0;
        listContainer.add(Box.createVerticalGlue(), gbc);

        setViewportView(listContainer);
        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }
}
