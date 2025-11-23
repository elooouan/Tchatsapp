package fr.uga.im2ag.m1info.chatservice.client.registries;

import java.io.Serializable;
import java.util.*;

/**
 * Stores contact relations between users.
 * On the client, this is simply a map userId -> pseudo (string).
 * The server keeps the full symmetric contact structure.
 */
public class ContactRegistry implements Serializable {

    /** contactId -> pseudo */
    private final Map<Integer, String> contacts = new HashMap<>();

    /**
     * Add or update a contact's pseudo.
     */
    public void addContact(int userId, String pseudo) {
        if (pseudo == null) pseudo = "";
        contacts.put(userId, pseudo);
    }

    /**
     * Clear all contacts (used when we receive a fresh CONTACTS_LIST).
     */
    public void clear() {
        contacts.clear();
    }

    /**
     * Return the set of contact IDs.
     * (Client only knows IDs + pseudos, not full User objects)
     */
    public Set<Integer> getContacts() {
        return Collections.unmodifiableSet(contacts.keySet());
    }

    /**
     * Resolve a userId to a pseudo if known, else return the id as a string.
     */
    public String resolveUserName(int id) {
        String name = contacts.get(id);
        return (name == null || name.isBlank()) ? "" + id : name;
    }
}
