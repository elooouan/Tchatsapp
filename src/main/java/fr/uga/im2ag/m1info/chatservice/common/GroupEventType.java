package fr.uga.im2ag.m1info.chatservice.common;

public final class GroupEventType {
    public static final byte MEMBER_ADDED   = 1;
    public static final byte MEMBER_REMOVED = 2;
    public static final byte RENAMED        = 3;
    public static final byte DELETED        = 4;

    // No instance possible
    private GroupEventType() {
    }
}
