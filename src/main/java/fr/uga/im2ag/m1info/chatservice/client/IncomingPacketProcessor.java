package fr.uga.im2ag.m1info.chatservice.client;

import fr.uga.im2ag.m1info.chatservice.client.registries.ContactRegistry;
import fr.uga.im2ag.m1info.chatservice.client.registries.GroupRegistry;
import fr.uga.im2ag.m1info.chatservice.common.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * Processes packets received by the client from the server.
 * It decodes the payload and forwards "events" to a listener
 */
public class IncomingPacketProcessor implements PacketProcessor {
    private ContactRegistry contacts;
    private GroupRegistry groups;
    // private MessageRegistry messages;

    /**
     * Callbacks for the client UI / model.
     */
    public interface Listener {
        void onDirectText(int fromUserId, String message);
        void onGroupText(int groupId, int fromUserId, String message);
        void onMemberAdded(int groupId, int addedMember);
        void onMemberRemoved(int groupId, int removedMember);
        void onGroupRenamed(int groupId, String title);
        void onGroupDeleted(int groupId);
        void onGroupCreated(int groupId, String title, int adminId, Set<Integer> membersIds);
        void onContactsUpdated();
        void onACK(String message);
        void onError(String message);
        void onUnknownPacket(Packet pkt);
    }

    private final Listener listener;

    public IncomingPacketProcessor(Listener listener) {
        contacts = Client.getClientState().getContactRegistry();
        groups = Client.getClientState().getGroupRegistry();
        // messages = Client.getClientState().getMessageRegistry();
        this.listener = listener;
    }

    @Override
    public void process(Packet pkt) {
        PacketType type = pkt.type();
        ByteBuffer payload = pkt.getPayload();

        if (payload == null) {
            // No payload: treat as malformed / error
            listener.onError("Received packet with no payload, type=" + type);
            return;
        }

        // Works with certain java versions -> double check before running
        switch (type) {
            case TEXT_USER -> handleDirectText(pkt, payload);
            case TEXT_GROUP -> handleGroupText(pkt, payload);
            case GROUP_EVENT -> handleGroupEvent(payload);
            case LIST_CONTACTS -> handleListContacts(payload);
            case ACK -> handleACK(pkt, payload);
            case ERROR -> handleError(pkt, payload);
            default -> listener.onUnknownPacket(pkt);
        }
    }

    /**
     * TEXT_USER payload format:
     *   [int msgLength][msgLength bytes UTF-8]
     * Sender id = pkt.from()
     * Recipient id = pkt.to() (this client)
     */
    private void handleDirectText(Packet pkt, ByteBuffer payload) {
        String message = pkt.payloadAsString();
        int fromUserId = pkt.from();
        listener.onDirectText(fromUserId, message);
    }

    /**
     * TEXT_GROUP payload format:
     *   [int msgLength][msgLength bytes UTF-8]
     * Sender id   = pkt.from()
     * Group id    = pkt.to()
     */
    private void handleGroupText(Packet pkt, ByteBuffer payload) {
        String message = pkt.payloadAsString();
        int fromUserId = pkt.from();
        int groupId = pkt.to();
        listener.onGroupText(groupId, fromUserId, message);
    }

    /**
     * GROUP_EVENT payload format:
     *   [byte subType]...
     *  Redistribute to subtype handlers
     */
    private void handleGroupEvent(ByteBuffer payload) {
        byte subType = payload.get();

        switch (subType) {
            case GroupEventType.MEMBER_ADDED -> handleMemberAdded(payload);
            case GroupEventType.MEMBER_REMOVED -> handleMemberRemoved(payload);
            case GroupEventType.RENAMED -> handleGroupRenamed(payload);
            case GroupEventType.DELETED -> handleGroupDeleted(payload);
            case GroupEventType.CREATED -> handleGroupCreated(payload);
        }
    }

    /**
     * MEMBER_ADDED payload format:
     *   [int groupId][addedMember int]
     *  add member to an existing group
     */
    private void handleMemberAdded(ByteBuffer payload) {
        int groupId = payload.getInt();
        int addedMember = payload.getInt();

        groups.addMember(groupId, addedMember);

        listener.onMemberAdded(groupId, addedMember);
    }

    /**
     * MEMBER_REMOVED payload format:
     *   [int groupId][removedMember int]
     *  remove member from an existing group
     */
    private void handleMemberRemoved(ByteBuffer payload) {
        int groupId = payload.getInt();
        int addedMember = payload.getInt();

        groups.removeMember(groupId, addedMember);

        listener.onMemberRemoved(groupId, addedMember);
    }

    /**
     * RENAMED payload format:
     *   [int groupId][int titleLen][bytes title]
     *  rename an existing group
     */
    private void handleGroupRenamed(ByteBuffer payload) {
        int groupId = payload.getInt();
        String title = readString(payload);

        groups.rename(groupId, title);

        listener.onGroupRenamed(groupId, title);
    }

    /**
     * DELETED payload format:
     *   [int groupId]
     *  remove an existing group
     */
    private void handleGroupDeleted(ByteBuffer payload) {
        int groupId = payload.getInt();

        groups.delete(groupId);

        listener.onGroupDeleted(groupId);
    }

    /**
     * CREATED payload format:
     *   [groupId:int]
     *   [titleLen:int][title:bytes]
     *   [adminId:int]
     *   [memberCount:int][memberId1:int]...[memberIdN:int]
     *  create a droup with a snapshot
     */
    private void handleGroupCreated(ByteBuffer payload) {
        int groupId = payload.getInt();
        String title = readString(payload);
        int adminId = payload.getInt();

        int memberCount = payload.getInt();

        Set<Integer> memberIds = new HashSet<>();
        for (int i = 0; i < memberCount; i++) {
            int memberId = payload.getInt();
            memberIds.add(memberId);
        }

        groups.createGroup(groupId,title, adminId, memberIds);

        listener.onGroupCreated(groupId,title, adminId, memberIds);
    }


    /**
     * LIST_CONTACTS reply payload format:
     *   [int count]
     *   repeated count times:
     *     [int userId]
     *     [int nameLen]
     *     [nameLen bytes UTF-8]
     */
    private void handleListContacts(ByteBuffer payload) {
        contacts.clear();

        int count = payload.getInt();
        for (int i = 0; i < count; i++) {
            int userId   = payload.getInt();
            int nameLen  = payload.getInt();
            byte[] nameBytes = new byte[nameLen];
            payload.get(nameBytes);

            String pseudo = new String(nameBytes, StandardCharsets.UTF_8);
            contacts.addContact(userId, pseudo);
        }

        listener.onContactsUpdated();
    }

    

    /**
     * ACK payload format:
     *   [int msgLength][msgLength bytes UTF-8]
     * (message is a human-readable description sent by the server)
     */
    private void handleACK(Packet pkt, ByteBuffer payload) {
        listener.onACK(pkt.payloadAsString());
    }

    /**
     * ERROR payload format:
     *   [int msgLength][msgLength bytes UTF-8]
     * (message is a human-readable description sent by the server)
     */
    private void handleError(Packet pkt, ByteBuffer payload) {
        listener.onError(pkt.payloadAsString());
    }

    public String readString(ByteBuffer buf) {
        if (buf.remaining() < Integer.BYTES) return null;

        int len = buf.getInt();
        if (len < 0 || buf.remaining() < len) return null;

        byte[] data = new byte[len];
        buf.get(data);

        return new String(data, StandardCharsets.UTF_8);
    }
}
