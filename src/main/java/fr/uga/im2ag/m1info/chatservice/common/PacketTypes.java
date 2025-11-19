package fr.uga.im2ag.m1info.chatservice.common;

public final class PacketTypes {
    public static final int TEXT = 1; // DirectMessageProcessor and GroupMessageProcessor
    public static final int CREATE_GROUP = 2; // AdminProcessor
    public static final int ADD_MEMBER = 3; // AdminProcessor
    public static final int REMOVE_MEMBER = 4; // AdminProcessor
    public static final int RENAME_GROUP = 5; // AdminProcessor
    public static final int DELETE_GROUP = 6; // AdminProcessor
    public static final int SET_PSEUDO = 7; // AdminProcessor
    public static final int ADD_CONTACT = 8; // AdminProcessor
    public static final int ERROR = 9; // Simple reply (ex: sendError...) 
}