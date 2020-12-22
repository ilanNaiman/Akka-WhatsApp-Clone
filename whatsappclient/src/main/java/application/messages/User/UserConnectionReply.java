package application.messages.User;

import application.messages.Message;

public class UserConnectionReply implements Message {
    private final String message;

    public UserConnectionReply(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
