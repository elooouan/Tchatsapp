package fr.uga.im2ag.m1info.chatservice.ui;

import fr.uga.im2ag.m1info.chatservice.client.ClientAPI;
import fr.uga.im2ag.m1info.chatservice.client.IncomingPacketProcessor;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Basic Swing UI similar to WhatsApp/Discord:
 * - Left: list of conversations (users + groups).
 * - Right: messages + input field + Send button.
 */
public class SwingChatWindow extends JFrame implements IncomingPacketProcessor.Listener {

    private ClientAPI api;

    // Conversation item: user or group
    private static class ConversationItem {
        final boolean isGroup;
        final int id;
        String name;

        ConversationItem(boolean isGroup, int id, String name) {
            this.isGroup = isGroup;
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            String prefix = isGroup ? "[G] " : "[U] ";
            return prefix + (name != null ? name : ("#" + id));
        }
    }

    // UI components
    private final DefaultListModel<ConversationItem> conversationModel = new DefaultListModel<>();
    private final JList<ConversationItem> conversationList = new JList<>(conversationModel);

    private final JTextArea chatArea = new JTextArea();
    private final JTextField inputField = new JTextField();
    private final JButton sendButton = new JButton("Send");
    private final JLabel statusLabel = new JLabel("Disconnected");

    // top-right help button
    private final JButton helpButton = new JButton("?");

    // conversation key -> history text
    private final Map<String, StringBuilder> histories = new HashMap<>();

    public SwingChatWindow() {
        super("TchatsApp");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        initUI();
    }

    public void setApi(ClientAPI api) {
        this.api = api;
        statusLabel.setText("Connected as: " + Optional.ofNullable(api.getPseudo()).orElse("unknown"));
    }

    private void initUI() {
        // Left: conversations
        conversationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        conversationList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateChatAreaForSelection();
            }
        });
        JScrollPane leftScroll = new JScrollPane(conversationList);

        // Right: chat area + input
        chatArea.setEditable(false);
        JScrollPane chatScroll = new JScrollPane(chatArea);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        sendButton.addActionListener(e -> sendCurrentMessage());
        inputField.addActionListener(e -> sendCurrentMessage());

        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.add(chatScroll, BorderLayout.CENTER);
        rightPanel.add(inputPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightPanel);
        splitPane.setDividerLocation(250);

        // Bottom status bar
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(statusLabel, BorderLayout.WEST);

        // Top right help button
        helpButton.setMargin(new Insets(2, 6, 2, 6));
        helpButton.addActionListener(e -> showHelpDialog());
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 3));
        topRight.add(helpButton);

        // Menu bar for actions
        setJMenuBar(buildMenuBar());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topRight, BorderLayout.NORTH);
        getContentPane().add(splitPane, BorderLayout.CENTER);
        getContentPane().add(bottom, BorderLayout.SOUTH);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        // Account
        JMenu account = new JMenu("Account");
        JMenuItem changePseudo = new JMenuItem("Change pseudo...");
        changePseudo.addActionListener(e -> {
            if (api == null) return;
            String pseudo = JOptionPane.showInputDialog(this, "New pseudo:", api.getPseudo());
            if (pseudo != null && !pseudo.isBlank()) {
                api.setPseudo(pseudo.trim());
                statusLabel.setText("Connected as: " + pseudo.trim());
            }
        });
        account.add(changePseudo);

        // Direct messages (no need to add as contact)
        JMenu direct = new JMenu("Direct");
        JMenuItem startDm = new JMenuItem("Start DM by id...");
        startDm.addActionListener(e -> startDirectChatById());
        direct.add(startDm);

        // Contacts
        JMenu contacts = new JMenu("Contacts");
        JMenuItem addContact = new JMenuItem("Add contact by id...");
        addContact.addActionListener(e -> {
            if (api == null) return;
            String str = JOptionPane.showInputDialog(this, "User id to add:");
            if (str == null || str.isBlank()) return;
            try {
                int userId = Integer.parseInt(str.trim());
                api.addContact(userId);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid user id", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        JMenuItem listContacts = new JMenuItem("List contacts");
        listContacts.addActionListener(e -> loadContacts());
        contacts.add(addContact);
        contacts.add(listContacts);

        // Groups
        JMenu groups = new JMenu("Groups");
        JMenuItem createGroup = new JMenuItem("Create group...");
        createGroup.addActionListener(e -> createGroupDialog());

        JMenuItem addMember = new JMenuItem("Add member to selected group...");
        addMember.addActionListener(e -> addMemberToSelectedGroup());

        JMenuItem renameGroup = new JMenuItem("Rename selected group...");
        renameGroup.addActionListener(e -> renameSelectedGroup());

        JMenuItem removeMember = new JMenuItem("Remove member from selected group...");
        removeMember.addActionListener(e -> removeMemberFromSelectedGroup());

        groups.add(createGroup);
        groups.add(addMember);
        groups.add(renameGroup);
        groups.add(removeMember);

        // Help menu (same as ? button)
        JMenu helpMenu = new JMenu("Help");
        JMenuItem showHelp = new JMenuItem("Show help");
        showHelp.addActionListener(e -> showHelpDialog());
        helpMenu.add(showHelp);

        bar.add(account);
        bar.add(direct);
        bar.add(contacts);
        bar.add(groups);
        bar.add(helpMenu);

        return bar;
    }

    // ===== Utilities for conversations =====

    private static String key(boolean isGroup, int id) {
        return (isGroup ? "G:" : "U:") + id;
    }

    private ConversationItem findConversation(boolean isGroup, int id) {
        for (int i = 0; i < conversationModel.size(); i++) {
            ConversationItem it = conversationModel.get(i);
            if (it.isGroup == isGroup && it.id == id) {
                return it;
            }
        }
        return null;
    }

    private ConversationItem ensureConversation(boolean isGroup, int id, String name) {
        ConversationItem it = findConversation(isGroup, id);
        if (it == null) {
            it = new ConversationItem(isGroup, id, name);
            conversationModel.addElement(it);
        } else if (name != null && !name.isBlank()) {
            it.name = name;
            conversationList.repaint();
        }
        return it;
    }

    private void updateChatAreaForSelection() {
        ConversationItem sel = conversationList.getSelectedValue();
        if (sel == null) {
            chatArea.setText("");
            return;
        }
        String k = key(sel.isGroup, sel.id);
        StringBuilder sb = histories.get(k);
        chatArea.setText(sb != null ? sb.toString() : "");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void appendToHistory(boolean isGroup, int id, String line) {
        String k = key(isGroup, id);
        StringBuilder sb = histories.computeIfAbsent(k, kk -> new StringBuilder());
        if (sb.length() > 0) sb.append("\n");
        sb.append(line);

        ConversationItem sel = conversationList.getSelectedValue();
        if (sel != null && sel.isGroup == isGroup && sel.id == id) {
            chatArea.setText(sb.toString());
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        }
    }

    // ===== Sending =====

    private void sendCurrentMessage() {
        if (api == null) return;
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
    
        ConversationItem sel = conversationList.getSelectedValue();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Select a conversation first", "No conversation", JOptionPane.WARNING_MESSAGE);
            return;
        }
    
        try {
            String selfName = Optional.ofNullable(api.getPseudo()).orElse("Me");
    
            if (sel.isGroup) {
                // For groups: let the server echo the message back via onGroupText().
                api.sendGroupMessage(sel.id, text);
            } else {
                // For direct messages: append locally, since server usually does not echo.
                api.sendDirectMessage(sel.id, text);
                appendToHistory(false, sel.id, selfName + ": " + text);
            }
    
            inputField.setText("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to send: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    

    // ===== Actions replacing old commands =====

    // /contacts -> show list + add them on the left
    private void loadContacts() {
        if (api == null) return;
        Set<Integer> contacts = api.requestContacts();
        if (contacts == null || contacts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No contacts.", "Contacts", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int id : contacts) {
            String name = api.resolveUserName(id);
            ensureConversation(false, id, name);
            sb.append(id).append(" -> ").append(name).append("\n");
        }

        JOptionPane.showMessageDialog(this, sb.toString(), "Contacts", JOptionPane.INFORMATION_MESSAGE);
    }

    // /creategroup
    private void createGroupDialog() {
        if (api == null) return;

        JTextField nameField = new JTextField();
        JTextField membersField = new JTextField();
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Group name:"));
        panel.add(nameField);
        panel.add(new JLabel("Member ids (space separated, optional):"));
        panel.add(membersField);

        int res = JOptionPane.showConfirmDialog(this, panel, "Create group",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String membersText = membersField.getText().trim();
        String[] parts = membersText.isEmpty() ? new String[0] : membersText.split("\\s+");
        int[] memberIds = new int[parts.length];
        try {
            for (int i = 0; i < parts.length; i++) {
                memberIds[i] = Integer.parseInt(parts[i]);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid member ids", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        api.createGroup(name, memberIds);
        // confirmation via onGroupCreated/onACK
    }

    // /groupadd <groupId> <userId> -> here: add member to selected group
    private void addMemberToSelectedGroup() {
        if (api == null) return;
        ConversationItem sel = conversationList.getSelectedValue();
        if (sel == null || !sel.isGroup) {
            JOptionPane.showMessageDialog(this, "Select a group conversation first.", "No group selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userStr = JOptionPane.showInputDialog(this, "User id to add to group " + sel.id + ":");
        if (userStr == null || userStr.isBlank()) return;

        int userId;
        try {
            userId = Integer.parseInt(userStr.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid user id.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        api.addMember(sel.id, userId);
        appendToHistory(true, sel.id, "[Local] Add user #" + userId + " requested.");
    }

    // /grouprename
    private void renameSelectedGroup() {
        if (api == null) return;
        ConversationItem sel = conversationList.getSelectedValue();
        if (sel == null || !sel.isGroup) {
            JOptionPane.showMessageDialog(this, "Select a group conversation first.", "No group selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String newName = JOptionPane.showInputDialog(this, "New name for group " + sel.id + ":", sel.name);
        if (newName == null || newName.isBlank()) return;

        api.renameGroup(sel.id, newName.trim());
        // server will confirm via onGroupRenamed; we can also update locally
        ensureConversation(true, sel.id, newName.trim());
        appendToHistory(true, sel.id, "[Local] Rename requested: " + newName.trim());
    }

    // /groupremove
    private void removeMemberFromSelectedGroup() {
        if (api == null) return;
        ConversationItem sel = conversationList.getSelectedValue();
        if (sel == null || !sel.isGroup) {
            JOptionPane.showMessageDialog(this, "Select a group conversation first.", "No group selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userStr = JOptionPane.showInputDialog(this, "User id to remove from group " + sel.id + ":");
        if (userStr == null || userStr.isBlank()) return;

        int userId;
        try {
            userId = Integer.parseInt(userStr.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid user id.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        api.removeMember(sel.id, userId);
        appendToHistory(true, sel.id, "[Local] Remove user #" + userId + " requested.");
    }

    // Start DM without adding contact
    private void startDirectChatById() {
        if (api == null) return;
        String str = JOptionPane.showInputDialog(this, "User id to DM:");
        if (str == null || str.isBlank()) return;

        int userId;
        try {
            userId = Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid user id.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String name = api.resolveUserName(userId);
        ConversationItem it = ensureConversation(false, userId, name);
        conversationList.setSelectedValue(it, true);
    }

    // Help dialog / "?" button
    private void showHelpDialog() {
        String msg =
                "Main features:\n" +
                "\n" +
                "- Left list: conversations (users / groups).\n" +
                "- Click a conversation, type a message at the bottom, press Enter or Send.\n" +
                "\n" +
                "Menus:\n" +
                "- Account > Change pseudo: change your display name.\n" +
                "- Direct > Start DM by id: open a DM without adding a contact.\n" +
                "- Contacts > Add contact by id: add a user to your contacts.\n" +
                "- Contacts > List contacts: load and show your contacts and add them to the left list.\n" +
                "- Groups > Create group: create a group (optional member ids).\n" +
                "- Groups > Add member to selected group: add a user by id to the selected group.\n" +
                "- Groups > Rename selected group: rename the currently selected group.\n" +
                "- Groups > Remove member from selected group: remove a user by id from the selected group.\n" +
                "\n" +
                "Incoming messages, group events and errors are shown in the chat area and status bar.";
        JOptionPane.showMessageDialog(this, msg, "Help", JOptionPane.INFORMATION_MESSAGE);
    }

    // ===== IncomingPacketProcessor.Listener implementation =====

    @Override
    public void onDirectText(int fromUserId, String message) {
        SwingUtilities.invokeLater(() -> {
            String name = resolveDisplayName(fromUserId);
            ensureConversation(false, fromUserId, name);
            appendToHistory(false, fromUserId, name + ": " + message);
        });
    }

    @Override
    public void onGroupText(int groupId, int fromUserId, String message) {
        SwingUtilities.invokeLater(() -> {
            String fromName = resolveDisplayName(fromUserId);
            ensureConversation(true, groupId, "Group " + groupId);
            appendToHistory(true, groupId, fromName + ": " + message);
        });
    }


    @Override
    public void onMemberAdded(int groupId, int addedMember) {
        SwingUtilities.invokeLater(() -> {
            String name = (api != null) ? api.resolveUserName(addedMember) : ("#" + addedMember);
            ensureConversation(true, groupId, "Group " + groupId);
            appendToHistory(true, groupId, "[System] " + name + " joined the group.");
        });
    }

    @Override
    public void onMemberRemoved(int groupId, int removedMember) {
        SwingUtilities.invokeLater(() -> {
            String name = (api != null) ? api.resolveUserName(removedMember) : ("#" + removedMember);
            ensureConversation(true, groupId, "Group " + groupId);
            appendToHistory(true, groupId, "[System] " + name + " left the group.");
        });
    }

    @Override
    public void onGroupRenamed(int groupId, String title) {
        SwingUtilities.invokeLater(() -> {
            ensureConversation(true, groupId, title);
            appendToHistory(true, groupId, "[System] Group renamed to '" + title + "'");
            conversationList.repaint();
        });
    }

    @Override
    public void onGroupDeleted(int groupId) {
        SwingUtilities.invokeLater(() -> {
            ensureConversation(true, groupId, "Group " + groupId);
            appendToHistory(true, groupId, "[System] Group deleted.");
        });
    }

    @Override
    public void onGroupCreated(int groupId, String title, int adminId, Set<Integer> membersIds) {
        SwingUtilities.invokeLater(() -> {
            ConversationItem it = ensureConversation(true, groupId, title);
            StringBuilder memberNames = new StringBuilder();
            if (api != null) {
                for (int m : membersIds) {
                    if (memberNames.length() > 0) memberNames.append(", ");
                    memberNames.append(api.resolveUserName(m));
                }
            }
            appendToHistory(true, groupId,
                    "[System] Group '" + title + "' created. Members: " + memberNames);
            conversationList.setSelectedValue(it, true);
        });
    }

    @Override
    public void onContactAdded(int contactId, String pseudo) {
        SwingUtilities.invokeLater(() -> {
            ensureConversation(false, contactId, pseudo);
            statusLabel.setText("Contact added: " + pseudo + " (#" + contactId + ")");
        });
    }

    @Override
    public void onACK(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText("[ACK] " + message));
    }

    @Override
    public void onError(String message) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("[ERROR] " + message);
            JOptionPane.showMessageDialog(this, message, "Server error", JOptionPane.ERROR_MESSAGE);
        });
    }

    @Override
    public void onUnknownPacket(fr.uga.im2ag.m1info.chatservice.common.Packet pkt) {
        SwingUtilities.invokeLater(() -> {
            String txt = "[UNKNOWN PACKET] type=" + pkt.type()
                    + " from=" + pkt.from()
                    + " to=" + pkt.to();
            statusLabel.setText(txt);
        });
    }

    // ----- name resolution helper -----
    private String resolveDisplayName(int userId) {
        if (api == null) {
            return "#" + userId;
        }

        int selfId;
        try {
            // assuming ClientAPI has getClientId()
            selfId = api.getClientId();
        } catch (NoSuchMethodError | UnsupportedOperationException e) {
            // fallback: just use resolveUserName for everyone
            String name = api.resolveUserName(userId);
            return (name == null || name.isBlank()) ? "#" + userId : name;
        }

        // Myself
        if (userId == selfId) {
            String pseudo = api.getPseudo();
            if (pseudo != null && !pseudo.isBlank()) {
                return pseudo;
            }
            return "Me";
        }

        // Others
        String name = api.resolveUserName(userId);
        return (name == null || name.isBlank()) ? "#" + userId : name;
    }
    
}
