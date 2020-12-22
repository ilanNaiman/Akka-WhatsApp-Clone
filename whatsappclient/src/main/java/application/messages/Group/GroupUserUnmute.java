package application.messages.Group;

public class GroupUserUnmute extends GroupMessageToTarget {

    public GroupUserUnmute(String groupName, String sourceSender, String targetUserName) {
        super(groupName, sourceSender, targetUserName);
    }

    @Override
    public boolean equals(Object o) {
        return (o != null && getClass() == o.getClass() &&
                ((GroupUserUnmute) o).groupName.equals(this.groupName) &&
                ((GroupUserUnmute) o).targetUserName.equals(this.targetUserName));
    }
}
