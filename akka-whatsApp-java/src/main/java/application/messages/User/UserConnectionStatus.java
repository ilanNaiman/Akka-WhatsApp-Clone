package application.messages.User;

public class UserConnectionStatus {

    private final String groupName;
    private final String targetUserName;

    public UserConnectionStatus(String groupName, String targetUserName) {
        this.groupName = groupName;
        this.targetUserName = targetUserName;
    }


    public String getGroupName() {
        return groupName;
    }

    public String getTargetUserName() {
        return targetUserName;
    }
}

