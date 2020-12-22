package application.messages;

import application.messages.Message;

public class ErrorMessage implements Message {
    private String errorMessage;

    public ErrorMessage() {
        this.errorMessage = "";
    }

    public ErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        return (o != null && getClass() == o.getClass());
    }
}
