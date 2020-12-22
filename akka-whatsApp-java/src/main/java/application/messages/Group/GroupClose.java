package application.messages.Group;

import application.messages.Message;

public class GroupClose implements Message {

    private final String groupName;


    public GroupClose(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }
}
