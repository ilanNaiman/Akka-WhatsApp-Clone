package application.messages.User;

import akka.actor.ActorRef;

public class UserInvite {

    private final String groupName;
    private final ActorRef sourceUser;

    public UserInvite(String groupName, ActorRef sourceUser) {
        this.groupName = groupName;
        this.sourceUser = sourceUser;
    }

    public String getGroupName() {
        return groupName;
    }

    public ActorRef getSourceUser() {
        return sourceUser;
    }
}
