package application.messages.User;

import akka.actor.ActorRef;
import application.messages.Message;
import com.fasterxml.jackson.annotation.JsonCreator;

public class GetUserRef implements Message {
    private final String userName;
    private final ActorRef sourceUser;

    @JsonCreator
    public GetUserRef(String userName, ActorRef sourceUser) {
        this.userName = userName;
        this.sourceUser = sourceUser;
    }

    public String getUserName() {
        return userName;
    }

    public ActorRef getSourceUser() {
        return sourceUser;
    }
}
