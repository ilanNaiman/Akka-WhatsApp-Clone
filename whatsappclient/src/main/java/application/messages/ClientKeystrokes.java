package application.messages;

import akka.actor.ActorRef;

public class ClientKeystrokes implements Message {
    private final String clientKeyStroke;
    private final ActorRef receiver;
    private final ActorRef messageSender;

    public ClientKeystrokes (String clientKeyStroke, ActorRef receiver, ActorRef messageSender) {
        this.clientKeyStroke = clientKeyStroke;
        this.receiver = receiver;
        this.messageSender = messageSender;
    }

    public String getClientKeyStroke() {
        return clientKeyStroke;
    }

    public ActorRef getReceiver() {
        return receiver;
    }

    public ActorRef getMessageSender() {
        return messageSender;
    }
}
