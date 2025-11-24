package fr.uga.im2ag.m1info.chatservice.ui;
/*
 * Command-line parser / REPL for the TchatsApp client.
 */
import fr.uga.im2ag.m1info.chatservice.client.ClientAPI;
import fr.uga.im2ag.m1info.chatservice.common.Message;

import java.util.List;
import java.util.Scanner;

public class CommandParser {

    private final ClientAPI api;
    private final Scanner in;

    private volatile boolean running = true;
    private Integer lastDirectTargetId = null;

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
            System.out.print(buildPrompt());
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

    /**
     * Builds the user's prompt
     */
    private String buildPrompt() {
        String self = api.getPseudo();
        if (self == null || self.isBlank()) {
            self = "";
        }

        String peerPart = "";
        if (lastDirectTargetId != null) {
            // use contacts mapping: name if in contacts, else "#id"
            String peerName = api.resolveUserName(lastDirectTargetId);
            peerPart = " -> " + peerName;
        }

        return self + peerPart + "> ";
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
            case "/showmsgs" -> handleShowMsgs(args);
            case "/pseudo" -> handlePseudo(args);
            case "/creategroup" -> handleCreateGroup(args);
            case "/groupadd" -> handleGroupAdd(args);
            case "/groupremove" -> handleGroupRemove(args);
            case "/grouprename" -> handleGroupRename(args);
            case "/groupdelete" -> handleGroupDelete(args);
            case "/addcontact" -> handleAddContact(args);
            case "/contacts" -> handleContacts();
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

        lastDirectTargetId = userId;        // remember for the prompt
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

    private void handleShowMsgs(String args) {
        String[] parts = args.trim().split("\\s+");
        if (parts.length != 1) {
            System.out.println("Usage: /showmsgs <convId>");
        }
        String convId = parts[0];
        List<Message> messages = api.getConversation(Integer.parseInt(convId));
        if(messages == null){
            System.out.println("No convesation");
        }
        for(int i = 0; i < messages.size(); i++){
            Message msg = messages.get(i);
            System.out.println(api.resolveUserName(msg.getFrom()) + ": " + msg.getMessage());
        }
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
 * /creategroup <name> [userId1 ... userIdN]
 */
private void handleCreateGroup(String args) {
    String trimmed = args.trim();
    if (trimmed.isEmpty()) {
        System.out.println("Usage: /creategroup <name> [userId1 ... userIdN]");
        return;
    }

    String[] parts = trimmed.split("\\s+");
    if (parts.length < 1) {
        System.out.println("Usage: /creategroup <name> [userId1 ... userIdN]");
        return;
    }

    // First token = group name (no spaces)
    String name = parts[0];

    // Remaining tokens = member IDs (optional)
    int[] memberIds;
    if (parts.length == 1) {
        // No user specified -> memberCount must be 0 in the packet
        memberIds = new int[0];
    } else {
        memberIds = new int[parts.length - 1];
        for (int i = 1; i < parts.length; i++) {
            try {
                memberIds[i - 1] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid user id: \"" + parts[i] + "\" (must be an integer)");
                return;
            }
        }
    }

    // Just send the request; success/failure will appear via ConsoleClientListener.onACK/onError
    api.createGroup(name, memberIds);
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

    /**
     * /contacts
     */
    private void handleContacts() { return;}
    

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  /help                             - show this help");
        System.out.println("  /quit                             - exit");
        System.out.println("  /msg <userId> <message>           - send direct message");
        System.out.println("  /gmsg <groupId> <message>         - send message to group");
        System.out.println("  /pseudo <name>                    - change your pseudo");
        System.out.println("  /creategroup <name>               - create a new group");
        System.out.println("  /groupadd <groupId> <userId>      - add member to group");
        System.out.println("  /groupremove <groupId> <userId>   - remove member from group");
        System.out.println("  /grouprename <groupId> <name>     - rename group");
        System.out.println("  /groupdelete <groupId>            - delete group");
        System.out.println("  /addcontact <userId>              - add a contact");
        System.out.println("  /contacts                         - display all contacts");
    }
}
