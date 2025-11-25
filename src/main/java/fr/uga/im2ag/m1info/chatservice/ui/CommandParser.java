package fr.uga.im2ag.m1info.chatservice.ui;

import fr.uga.im2ag.m1info.chatservice.client.ClientAPI;

import javax.swing.*;
import java.util.Scanner;

/**
 * Legacy name kept for compatibility.
 * Instead of parsing /commands on stdin, this now just launches the Swing UI.
 */
public class CommandParser {
    private final ClientAPI api;

    public CommandParser(ClientAPI api, Scanner in) {
        this.api = api;
    }

    /**
     * Instead of reading commands from stdin, just opens the Swing window.
     */
    public void run() {
        SwingUtilities.invokeLater(() -> {
            SwingChatWindow ui = new SwingChatWindow();
            ui.setApi(api);
            ui.setVisible(true);
        });
    }
}
