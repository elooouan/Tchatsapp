package fr.uga.im2ag.m1info.chatservice.ui;
/*
 * Command-line parser / REPL for the TchatsApp client.
 */
import fr.uga.im2ag.m1info.chatservice.client.ClientAPI;

import java.util.Scanner;

public class CommandParser {

    private final ClientAPI api;
    private final Scanner in;

    private volatile boolean running = true;

    public CommandParser(ClientAPI api, Scanner in) {
        this.api = api;
        this.in = in;
    }

    /**
     * Main loop: reads lines from stdin and executes commands
     */
    public void run() {
        printHelp();
        while (running && in.hasNextLine()) {
            System.out.print("> ");
            String line = in.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            try {
                handleLine(line);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private void handleLine(String line) {
        if (!line.startsWith("/")) {
            // Default: send to last-used user or reject.
            System.out.println("Commands must start with '/'. Type /help for help.");
            return;
        }

        String[] parts = line.split("\\s+", 2);
        String cmd = parts[0].toLowerCase();

        String args = parts.length > 1 ? parts[1] : "";

        switch (cmd) {
            case "/help" -> printHelp();
            case "/quit", "/exit" -> handleQuit();
            case "/msg" -> handleMsg(args);
            case "/gmsg" -> handleGroupMsg(args);
            case "/pseudo" -> handlePseudo(args);
            case "/creategroup" -> handleCreateGroup(args);
            case "/groupadd" -> handleGroupAdd(args);
            case "/groupremove" -> handleGroupRemove(args);
            case "/grouprename" -> handleGroupRename(args);
            case "/groupdelete" -> handleGroupDelete(args);
            case "/addcontact" -> handleAddContact(args);
            default -> System.out.println("Unknown command: " + cmd + " (try /help)");
        }
    }

    private void handleQuit() {
        running = false;
        System.out.println("Bye.");
        // Optionally: api.disconnect();
    }

    /**
     * /msg <userId> <message...>
     */
    private void handleMsg(String args) {
        String[] parts = args.trim().split("\\s+", 2);
        if (parts.length < 2) {
            System.out.println("Usage: /msg <userId> <message>");
            return;
        }
        int userId;
        try {
            userId = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid user id: " + parts[0]);
            return;
        }
        String message = parts[1];
        api.sendDirectMessage(userId, message);
    }

    /**
     * /gmsg <groupId> <message...>
     */
    private void handleGroupMsg(String args) {
        String[] parts = args.trim().split("\\s+", 2);
        if (parts.length < 2) {
            System.out.println("Usage: /gmsg <groupId> <message>");
            return;
        }
        int groupId;
        try {
            groupId = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid group id: " + parts[0]);
            return;
        }
        String message = parts[1];
        api.sendGroupMessage(groupId, message);
    }

    /**
     * /pseudo <newPseudo>
     */
    private void handlePseudo(String args) {
        String pseudo = args.trim();
        if (pseudo.isEmpty()) {
            System.out.println("Usage: /pseudo <newPseudo>");
            return;
        }
        api.setPseudo(pseudo);
    }

    /**
     * /creategroup <name...>
     */
    private void handleCreateGroup(String args) {
        String name = args.trim();
        if (name.isEmpty()) {
            System.out.println("Usage: /creategroup <name>");
            return;
        }
        api.createGroup(name);
        System.out.println("Group creation requested for \"" + name + "\"");
    }

    /**
     * /groupadd <groupId> <userId>
     */
    private void handleGroupAdd(String args) {
        String[] parts = args.trim().split("\\s+");
        if (parts.length != 2) {
            System.out.println("Usage: /groupadd <groupId> <userId>");
            return;
        }
        try {
            int groupId = Integer.parseInt(parts[0]);
            int userId = Integer.parseInt(parts[1]);
            api.addMember(groupId, userId);
        } catch (NumberFormatException e) {
            System.out.println("Invalid id(s).");
        }
    }

    /**
     * /groupremove <groupId> <userId>
     */
    private void handleGroupRemove(String args) {
        String[] parts = args.trim().split("\\s+");
        if (parts.length != 2) {
            System.out.println("Usage: /groupremove <groupId> <userId>");
            return;
        }
        try {
            int groupId = Integer.parseInt(parts[0]);
            int userId = Integer.parseInt(parts[1]);
            api.removeMember(groupId, userId);
        } catch (NumberFormatException e) {
            System.out.println("Invalid id(s).");
        }
    }

    /**
     * /grouprename <groupId> <newName...>
     */
    private void handleGroupRename(String args) {
        String[] parts = args.trim().split("\\s+", 2);
        if (parts.length < 2) {
            System.out.println("Usage: /grouprename <groupId> <newName>");
            return;
        }
        try {
            int groupId = Integer.parseInt(parts[0]);
            String newName = parts[1];
            api.renameGroup(groupId, newName);
        } catch (NumberFormatException e) {
            System.out.println("Invalid group id: " + parts[0]);
        }
    }

    /**
     * /groupdelete <groupId>
     */
    private void handleGroupDelete(String args) {
        String[] parts = args.trim().split("\\s+");
        if (parts.length != 1) {
            System.out.println("Usage: /groupdelete <groupId>");
            return;
        }
        try {
            int groupId = Integer.parseInt(parts[0]);
            api.deleteGroup(groupId);
        } catch (NumberFormatException e) {
            System.out.println("Invalid group id: " + parts[0]);
        }
    }

    /**
     * /addcontact <userId>
     */
    private void handleAddContact(String args) {
        String[] parts = args.trim().split("\\s+");
        if (parts.length != 1) {
            System.out.println("Usage: /addcontact <userId>");
            return;
        }
        try {
            int userId = Integer.parseInt(parts[0]);
            api.addContact(userId);
        } catch (NumberFormatException e) {
            System.out.println("Invalid user id: " + parts[0]);
        }
    }

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  /help                         - show this help");
        System.out.println("  /quit                         - exit");
        System.out.println("  /msg <userId> <message>       - send direct message");
        System.out.println("  /gmsg <groupId> <message>     - send message to group");
        System.out.println("  /pseudo <name>                - change your pseudo");
        System.out.println("  /creategroup <name>           - create a new group");
        System.out.println("  /groupadd <groupId> <userId>  - add member to group");
        System.out.println("  /groupremove <groupId> <userId> - remove member from group");
        System.out.println("  /grouprename <groupId> <name> - rename group");
        System.out.println("  /groupdelete <groupId>        - delete group");
        System.out.println("  /addcontact <userId>          - add a contact");
    }
}
