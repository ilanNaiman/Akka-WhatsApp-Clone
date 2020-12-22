package application.messages.Group;

public class GroupCoAdminAdd extends GroupMessage {

    private final String targetUserName;

    public GroupCoAdminAdd(String groupName, String sourceSender, String targetUserName) {
        super(groupName, sourceSender);
        this.targetUserName =targetUserName;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    @Override
    public boolean equals(Object o) {
        return (o != null && getClass() == o.getClass() &&
                ((GroupCoAdminAdd) o).getGroupName().equals(getGroupName()) &&
                ((GroupCoAdminAdd) o).getSourceSender().equals(getSourceSender()) &&
                ((GroupCoAdminAdd) o).targetUserName.equals(this.targetUserName));
    }
}
