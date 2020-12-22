package application.messages.User;

import application.messages.Message;
import com.fasterxml.jackson.annotation.JsonCreator;

public class UserNotExists implements Message {
    private final String userName;

    @JsonCreator
    public UserNotExists(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}
