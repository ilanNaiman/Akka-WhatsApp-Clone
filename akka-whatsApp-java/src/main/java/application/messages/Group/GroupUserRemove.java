package application.messages.Group;

public class GroupUserRemove extends GroupMessage {

    private final String targetUserName;

    public GroupUserRemove(String groupName, String sourceSender, String targetUserName) {
        super(groupName, sourceSender);
        this.targetUserName = targetUserName;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    @Override
    public boolean equals(Object o) {
        return (o != null && getClass() == o.getClass() &&
                ((GroupUserRemove) o).getGroupName().equals(getGroupName()) &&
                ((GroupUserRemove) o).getSourceSender().equals(getSourceSender()) &&
                ((GroupUserRemove) o).targetUserName.equals(this.targetUserName));
    }
}
