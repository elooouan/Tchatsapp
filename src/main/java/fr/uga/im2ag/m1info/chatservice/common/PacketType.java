package fr.uga.im2ag.m1info.chatservice.common;

public final class PacketType {
    public static final int TEXT_USER = 1; // DirectMessageProcessor
    public static final int TEXT_GROUP = 2; // GroupMessageProcessor
    public static final int CREATE_GROUP = 3; // AdminProcessor
    public static final int ADD_MEMBER = 4; // AdminProcessor
    public static final int REMOVE_MEMBER = 5; // AdminProcessor
    public static final int RENAME_GROUP = 6; // AdminProcessor
    public static final int DELETE_GROUP = 7; // AdminProcessor
    public static final int SET_PSEUDO = 8; // UserProcessor
    public static final int ADD_CONTACT = 9; // UserProcessor
    public static final int ERROR = -1; // Simple reply (ex: sendError...)
}