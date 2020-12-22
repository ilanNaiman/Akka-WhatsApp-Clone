package application.messages;

import akka.actor.ActorRef;

public class AddSession implements Message {
    private final ActorRef actorRef;
    private final String userName;

    public AddSession (ActorRef actorRef, String userName) {
        this.actorRef = actorRef;
        this.userName = userName;
    }

    public ActorRef getActorRef() {
        return actorRef;
    }

    public String getUserName() {
        return userName;
    }
}
