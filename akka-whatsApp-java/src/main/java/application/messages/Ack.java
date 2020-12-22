package application.messages;

import application.messages.Message;

public class Ack implements Message {
    private String successMessage;

    public Ack() {
        successMessage = "";
    }

    public Ack(String successMessage) {
        this.successMessage = successMessage;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    @Override
    public boolean equals(Object o) {
        return (o != null && getClass() == o.getClass());
    }
}
