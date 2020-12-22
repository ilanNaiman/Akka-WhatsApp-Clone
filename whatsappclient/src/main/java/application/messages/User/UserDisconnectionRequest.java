package application.messages.User;

import akka.actor.ActorRef;
import application.messages.Message;
import com.fasterxml.jackson.annotation.JsonCreator;

public class UserDisconnectionRequest implements Message {
    private final String userName;
    private final ActorRef receiver;

    @JsonCreator
    public UserDisconnectionRequest(ActorRef receiver, String userName) {
        this.receiver = receiver;
        this.userName = userName;
    }

    public ActorRef getReceiver() {
        return receiver;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public boolean equals(Object o) {
        return (o != null && getClass() == o.getClass() &&
                ((UserDisconnectionRequest) o).receiver.equals(this.receiver));
    }
}
