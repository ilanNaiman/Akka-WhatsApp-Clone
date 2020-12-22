package application.messages.User;

import application.messages.Message;
import com.fasterxml.jackson.annotation.JsonCreator;

public class UserDisconnectionConfirmed implements Message {
    private final String userName;
    private String message;

    @JsonCreator
    public UserDisconnectionConfirmed(String userName) {
        this.userName = userName;
        this.message = String.format("%s has been disconnected successfully!", userName);
    }

    public String getUserName() {
        return userName;
    }

    public String getMessage() {
        return message;
    }
}
