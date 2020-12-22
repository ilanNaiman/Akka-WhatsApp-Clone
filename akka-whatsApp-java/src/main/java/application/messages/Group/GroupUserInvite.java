package application.messages.Group;

import akka.actor.ActorRef;

public class GroupUserInvite extends GroupMessage {
    private final String targetUserName;
    private final ActorRef sourceUser;

    public GroupUserInvite(String groupName, String sourceSender, String targetUserName, ActorRef sourceUser) {
        super(groupName, sourceSender);
        this.targetUserName =targetUserName;
        this.sourceUser = sourceUser;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    @Override
    public boolean equals(Object o) {
        return (o != null && getClass() == o.getClass() &&
                ((GroupUserInvite) o).getGroupName().equals(getGroupName()) &&
                ((GroupUserInvite) o).getSourceUser().equals(getSourceUser()) &&
                ((GroupUserInvite) o).targetUserName.equals(this.targetUserName) &&
                ((GroupUserInvite) o).sourceUser.equals(this.sourceUser));
    }

    public ActorRef getSourceUser() {
        return sourceUser;
    }
}
