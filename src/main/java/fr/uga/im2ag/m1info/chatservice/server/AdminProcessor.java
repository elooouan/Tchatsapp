package fr.uga.im2ag.m1info.chatservice.server;

public class AdminProcessor {
    private TchatsAppServer server;
    private UserRegistry userRegistry;
    private GroupRegistry groupRegistry;
    private UserGroupRegistry userGroupRegistry;
    private IdGenerator idGenerator;

    public AdminProcessor(TchatsAppServer server){
        this.server = server;
        userRegistry = new UserRegistry(this);
        groupRegistry = new GroupRegistry(this);
        userGroupRegistry = new UserGroupRegistry(this);
    }

    public UserRegistry getUserRegistry() {
        return userRegistry;
    }
    public GroupRegistry getGroupRegistry() {
        return groupRegistry;
    }
    public UserGroupRegistry getUserGroupRegistry() {
        return userGroupRegistry;
    }

}
