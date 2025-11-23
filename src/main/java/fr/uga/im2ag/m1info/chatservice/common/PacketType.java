package fr.uga.im2ag.m1info.chatservice.common;

public enum PacketType {
    // DirectMessageProcessor
    TEXT_USER,
    
    // GroupMessageProcessor
    TEXT_GROUP,
    
    // AdminProcessor
    CREATE_GROUP,
    ADD_MEMBER,
    REMOVE_MEMBER,
    RENAME_GROUP,
    DELETE_GROUP,
    GROUP_EVENT, // has subtype: GroupEventType
    
    // UserProcessor
    SET_PSEUDO,
    ADD_CONTACT,
    CREATE_USER,
    LIST_CONTACTS,

    // IncomingPacketProcessor -> ClientSide ONLY
    ACK,
    ERROR;

    static public PacketType convertIntToPacketType(int type){
        return PacketType.values()[type];
    }
}