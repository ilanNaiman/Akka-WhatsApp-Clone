package application.messages.User;

public class UserConnectionDenied {
    private final String message;

    public UserConnectionDenied() {
        this.message = "server is offline!";
    }

    public String getMessage() {
        return message;
    }
}
