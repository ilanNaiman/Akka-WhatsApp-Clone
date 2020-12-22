package application.messages.User;

import application.messages.Message;

public class UserDisconnectionDenied implements Message {
    private final String message;

    public UserDisconnectionDenied() {
        this.message = "server is offline! try again later!";
    }

    public String getMessage() {
        return message;
    }
}
