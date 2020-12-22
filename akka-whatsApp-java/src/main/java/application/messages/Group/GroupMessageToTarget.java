package application.messages.Group;

public abstract class GroupMessageToTarget extends GroupMessage {
    protected final String targetUserName;

    public GroupMessageToTarget(String groupName, String sourceSender, String targetUserName) {
        super(groupName, sourceSender);
        this.targetUserName = targetUserName;
    }

    public String getTargetUserName() {
        return targetUserName;
    }
}
