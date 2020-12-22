package application.messages.Group;

import akka.actor.ActorRef;

public class Unmute {
    private final String groupName;
    private final String targetUserName;
    private final ActorRef sender;
    private final String message;

    public Unmute(String groupName, String targetUserName, ActorRef sender, String message) {
        this.groupName = groupName;
        this.targetUserName = targetUserName;
        this.sender = sender;
        this.message = message;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    public ActorRef getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        return (o != null && getClass() == o.getClass() &&
                ((Unmute) o).groupName.equals(this.groupName) &&
                ((Unmute) o).targetUserName.equals(this.targetUserName));
    }
}
