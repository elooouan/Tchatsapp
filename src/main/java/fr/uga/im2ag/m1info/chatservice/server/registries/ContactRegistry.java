package fr.uga.im2ag.m1info.chatservice.server.registries;

import fr.uga.im2ag.m1info.chatservice.common.User;

import java.io.Serializable;
import java.util.*;

/**
 * Stores contact relations between users.
 * Contacts are symmetric: if A has B, then B also has A.
 */
public class ContactRegistry implements Serializable {
    private Map<User, Set<User>> contacts = new HashMap<>();
    private Map<User, Set<User>> usersWhoHaveYourContact = new HashMap<>();

    public void addContact(User user, User newContact) {
        if (user == null || newContact == null || user == newContact) return;

        contacts.computeIfAbsent(user, k -> new HashSet<>()).add(newContact); // Add u2 to u1's contacts
        usersWhoHaveYourContact.computeIfAbsent(newContact, k -> new HashSet<>()).add(user);
    }

    public void removeContact(User user, User toRemove) {
        if (user == null || toRemove == null || user == toRemove) return;

        Set<User> userContacts = contacts.get(user); // Fetch the user's contacts
        userContacts.remove(toRemove); // Remove toRemove from his contacts
        
        if (contacts.get(user).isEmpty()) contacts.remove(user); // If he has no contacts remove him from the contacts HashMap
    }

    public Set<User> listContacts(User user) { 
        Set<User> userContacts = contacts.get(user);
        if (userContacts == null) return Collections.emptySet();
        return Collections.unmodifiableSet(userContacts); // Avoid bugs and preserve encapsulation 
    }

    public Set<User> getUserWhoHaveYourContact(User caller) { return usersWhoHaveYourContact.get(caller); }
}

