package fr.uga.im2ag.m1info.chatservice.common;

public enum PacketType {
    TEXT_USER, // DirectMessageProcessor
    TEXT_GROUP, // GroupMessageProcessor
    CREATE_GROUP, // AdminProcessor
    ADD_MEMBER, // AdminProcessor
    REMOVE_MEMBER, // AdminProcessor
    RENAME_GROUP, // AdminProcessor
    DELETE_GROUP, // AdminProcessor
    SET_PSEUDO, // UserProcessor
    ADD_CONTACT, // UserProcessor
    CREATE_USER, // UserProcessor
    ERROR; // Simple reply (ex: sendError...)

    static public PacketType convertIntToPacketType(int type){
        return PacketType.values()[type];
    }
}