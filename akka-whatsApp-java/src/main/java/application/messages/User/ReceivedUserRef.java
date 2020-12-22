package application.messages.User;

import akka.actor.ActorRef;
import application.messages.Message;

public class ReceivedUserRef implements Message {

    private final String userName;
    private final ActorRef sourceUserRef;
    private final ActorRef targetUserRef;

    public ReceivedUserRef(String userName, ActorRef sourceUserRef, ActorRef targetUserRef) {
        this.userName = userName;
        this.sourceUserRef = sourceUserRef;
        this.targetUserRef = targetUserRef;
    }

    public String getUserName() {
        return userName;
    }

    public ActorRef getSourceUserRef() {
        return sourceUserRef;
    }

    public ActorRef getTargetUserRef() {
        return targetUserRef;
    }
}
