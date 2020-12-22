package application.messages;

import akka.actor.ActorRef;
import com.fasterxml.jackson.annotation.JsonCreator;

public class RemoveSession implements Message {
    private final ActorRef actorRef;

    @JsonCreator
    public RemoveSession (ActorRef actorRef) {
        this.actorRef = actorRef;
    }

    public ActorRef getActorRef() {
        return actorRef;
    }

    @Override
    public boolean equals(Object o) {
        return (o != null && getClass() == o.getClass() && ((RemoveSession) o).actorRef == actorRef);
    }
}
