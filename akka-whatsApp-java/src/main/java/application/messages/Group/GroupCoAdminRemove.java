package application.messages.Group;

public class GroupCoAdminRemove extends GroupMessage {

    private final String targetUserName;

    public GroupCoAdminRemove(String groupName, String sourceSender, String targetUserName) {
        super(groupName, sourceSender);
        this.targetUserName =targetUserName;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    @Override
    public boolean equals(Object o) {
        return (o != null && getClass() == o.getClass() &&
                ((GroupCoAdminRemove) o).getGroupName().equals(getGroupName()) &&
                ((GroupCoAdminRemove) o).getSourceSender().equals(getSourceSender()) &&
                ((GroupCoAdminRemove) o).targetUserName.equals(this.targetUserName));
    }
}
