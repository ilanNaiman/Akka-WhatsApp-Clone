package application.messages.Group;

import akka.actor.ActorRef;

public class GroupAddUser extends GroupMessage {

    private final String targetUserName;
    private final ActorRef targetRef;

    public GroupAddUser(String groupName, String sourceSender, String targetUserName, ActorRef targetRef) {
        super(groupName, sourceSender);
        this.targetUserName = targetUserName;
        this.targetRef = targetRef;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    public ActorRef getTargetRef() {
        return targetRef;
    }
}
