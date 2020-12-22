package application;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import application.actors.CommandParser;
import application.actors.MessageReceiver;
import application.actors.MessageSender;
import application.messages.Ack;
import application.messages.ClientKeystrokes;
import application.messages.StartListener;
import application.messages.User.UserConnectionDenied;
import application.messages.User.UserDisconnectionDenied;

import java.util.Scanner;

public class UserClient extends AbstractActor {
    private final ActorRef messageSender;
    private final ActorRef commandParser;
    private final ActorRef messageReceiver;

    public UserClient() {
        this.messageSender = getContext().actorOf(Props.create(MessageSender.class),
                "MessageSender");
        this.commandParser = getContext().actorOf(Props.create(CommandParser.class),
                "CommandParser");
        this.messageReceiver = getContext().actorOf(MessageReceiver.props(commandParser, messageSender),
                "MessageReceiver");
    }

    public static Props props() {
        return Props.create(UserClient.class);
    }

    @Override
    public Receive createReceive() {
        // for any message that given to user client is sent by the client, thus should be parsed
        // also send messageSender as the sender of this message in order to have its reference.
        return receiveBuilder()
                .match(StartListener.class, this::handleStartListener)
                .match(UserDisconnectionDenied.class, this::handleUserDisconnectionDenied)
                .match(UserConnectionDenied.class, this::handleUserConnectionDenied)
                .match(Ack.class, this::handleAckMessage)
                .match(String.class, this::handleDirectInput)
                .build();
    }

    private void handleAckMessage(Ack ack) {
        messageReceiver.forward(ack, getContext());
    }

    private void handleUserDisconnectionDenied(UserDisconnectionDenied udd) {
        messageReceiver.forward(udd, getContext());
    }

    private void handleUserConnectionDenied(UserConnectionDenied ucd) {
        messageReceiver.forward(ucd, getContext());
    }

    private void handleDirectInput(String userInput) {
        commandParser.forward(new ClientKeystrokes(userInput, messageReceiver, messageSender), getContext());
    }

    private void handleStartListener(StartListener startListener) {
        Scanner myObj = new Scanner(System.in);
        String userInput;

        do {
            userInput = myObj.nextLine();
            commandParser.tell(new ClientKeystrokes(userInput, messageReceiver, messageSender), self());
        }
        while (!userInput.equals("/user disconnect"));
    }
}
