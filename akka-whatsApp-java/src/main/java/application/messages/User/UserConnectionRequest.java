package application.messages.User;

import akka.actor.ActorRef;
import application.messages.Message;

public class UserConnectionRequest implements Message {
    private final ActorRef receiver;
    private final ActorRef sender;
    private final String userName;

    public UserConnectionRequest(ActorRef receiver, ActorRef sender, String userName) {
        this.receiver = receiver;
        this.sender = sender;
        this.userName = userName;
    }

    public ActorRef getReceiver() {
        return receiver;
    }

    public ActorRef getSender() {
        return sender;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public boolean equals(Object o) {
        return (o != null && getClass() == o.getClass() &&
                ((UserConnectionRequest) o).userName.equals(this.userName) &&
                ((UserConnectionRequest) o).receiver.equals(this.receiver) &&
                ((UserConnectionRequest) o).sender.equals(this.sender));
    }
}
