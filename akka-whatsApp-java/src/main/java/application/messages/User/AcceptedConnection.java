package application.messages.User;

import application.messages.Message;
import com.fasterxml.jackson.annotation.JsonCreator;

public class AcceptedConnection implements Message {

    private final String userName;
    private final String successMessage;

    @JsonCreator
    public AcceptedConnection(String userName) {
        this.userName = userName;
        this.successMessage = this.userName + " has connected successfully!";
    }

    public String getUserName() {
        return userName;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    @Override
    public boolean equals(Object o) {
        return (o != null && getClass() == o.getClass() &&
                ((AcceptedConnection) o).userName.equals(userName) &&
                ((AcceptedConnection) o).successMessage.equals(successMessage));
    }
}
