package fr.uga.im2ag.m1info.chatservice.server;

import fr.uga.im2ag.m1info.chatservice.common.Group;
import fr.uga.im2ag.m1info.chatservice.common.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserGroupRegistry {
    private Map<Integer, List<Integer>> userGroups;

    UserGroupRegistry() {
        userGroups = new HashMap<>();
    }

    public void addGroupToUser(User user, Group group){
        if(!userGroups.containsKey(user.getUserId())){
            userGroups.put(user.getUserId(),new ArrayList<>());
        }
        if(!userGroups.get(user.getUserId()).contains(group.getId())) {
            userGroups.get(user.getUserId()).add(group.getId());
        }
    }

    public void removeGroupFromUser(User user, Group group){
        if(userGroups.containsKey(user.getUserId()) && userGroups.get(user.getUserId()).contains(group.getId())){
            userGroups.get(user.getUserId()).remove(group.getId());
        }
    }

    public List<Group> getGroupsOf(User user){
        if(userGroups.containsKey(user.getUserId())){
            List<Integer> groups = userGroups.get(user.getUserId());
            List<Group> groupList = new ArrayList<>();
            for(Integer groupId : groups){

            }
        }
    }
}
