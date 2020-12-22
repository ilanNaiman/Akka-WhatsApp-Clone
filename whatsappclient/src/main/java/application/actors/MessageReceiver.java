package application.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import application.messages.Ack;
import application.messages.ErrorMessage;
import application.messages.Group.*;
import application.messages.RemoveSession;
import akka.actor.*;
import application.messages.*;
import application.messages.User.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class MessageReceiver extends AbstractActor {
    private final ActorRef messageSender;

    private final Stack<GroupUserInvite> pendingInvitation;
    private final ActorRef commandParser;
    private final String MANAGER_REMOTE = "akka://whatsApp-chat-system@127.0.0.1:3553/user/ManagingServer";
    private final ActorSelection managerRef = getContext().actorSelection(MANAGER_REMOTE);
    private final Set<String> usersAlreadyCached;


    private MessageReceiver(ActorRef commandParser, ActorRef messageSender) {
        this.pendingInvitation = new Stack<>();
        this.commandParser = commandParser;
        this.messageSender = messageSender;
        this.usersAlreadyCached = new HashSet<>();
    }

    public static Props props(ActorRef commandParser, ActorRef messageSender) {
        return Props.create(MessageReceiver.class, commandParser, messageSender);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(UserTextualMessage.class, this::handleReceiveTextualMessage)
                .match(UserBinaryMessage.class, this::handleReceiveUserBinaryMessage)
                .match(GroupBinaryMessage.class, this::handleReceiveGroupBinaryMessage)
                .match(UserDisconnectionRequest.class, this::handleDisconnectionRequest)
                .match(GroupUserInvite.class, this::handleReceiveUserInvite)
				.match(GroupTextualMessage.class, this::handleGroupTextualMessage)
				.match(GroupUserMute.class, this::handleGroupUserMute)
                .match(GroupUserUnmute.class, this::handleGroupUserUnmute)
                .match(AcceptedConnection.class, this::handleAcceptedConnection)
                .match(UserConnectionDenied.class, this::handleUserConnectionDenied)
                .match(UserDisconnectionDenied.class, this::handleUserDisconnectionDenied)
                .match(UserDisconnectionConfirmed.class, this::handleUserDisconnectionConfirmed)
                .match(InformTarget.class, this::handleInformTarget)
                .match(UserResponse.class, this::handleUserResponse)
                .match(Ack.class, this::handleAckReply)
                .match(ErrorMessage.class, this::handleErrorReply)
                .build();
    }


    private void handleInformTarget(InformTarget informTarget) {
        informTarget.getTargetUser().tell(new Ack(informTarget.getAnnouncement()), getSelf());
    }

    private void handleGroupUserMute(GroupUserMute groupUserMute){
        System.out.println(String.format("[%s][%s][%s] You have been muted for %s seconds in %s by %s", new Date(),
                groupUserMute.getGroupName(), groupUserMute.getSourceSender(),
                groupUserMute.getDuration().getSeconds(), groupUserMute.getGroupName(), groupUserMute.getSourceSender()));
        this.messageSender.tell(new Mute(groupUserMute.getGroupName(),
                groupUserMute.getTargetUserName(), groupUserMute.getDuration(), getSender()), getSelf());
    }

    private void handleGroupUserUnmute(GroupUserUnmute groupUserUnmute){
        this.messageSender.tell(new Unmute(groupUserUnmute.getGroupName(),
                groupUserUnmute.getTargetUserName(), getSender(),
                String.format("[%s][%s][%s] You have been unmuted in %s by %s!",
                        new Date(), groupUserUnmute.getGroupName(),
                        groupUserUnmute.getSourceSender(), groupUserUnmute.getGroupName(),
                        groupUserUnmute.getSourceSender())), getSelf());
    }

    private void handleAcceptedConnection(AcceptedConnection acceptedConnection) {
        System.out.println(acceptedConnection.getSuccessMessage());
        commandParser.forward(acceptedConnection, getContext());
    }

    private void handleUserConnectionDenied(UserConnectionDenied ucd) {
        System.out.println(ucd.getMessage());
    }

    private void handleUserResponse(UserResponse userResponse) {
        GroupUserInvite userInvite = pendingInvitation.pop();
        if (userResponse.getAnswer().equals("Yes")) {
            managerRef.tell(new GroupAddUser(userInvite.getGroupName(), userInvite.getSourceSender(),
                    userInvite.getTargetUserName(), getSelf()), userInvite.getSourceUser());
        }
    }

    private void handleReceiveTextualMessage(UserTextualMessage textualMessage) {
        if (usersAlreadyCached.add(textualMessage.getSourceUserName())) {
            messageSender.tell(new AddSession(textualMessage.getReceiver(), textualMessage.getSourceUserName()),
                    getSelf());
        }
        System.out.println(String.format("[%s][user][%s] %s", textualMessage.getDate(), textualMessage.getSourceUserName(),
                textualMessage.getTextualMessageContent()));
    }

    private void handleReceiveBinaryMessage(BinaryMessage binaryMessage, String message) {
        final Path downloadDir = Paths.get("src/Downloads");
        final Path downloadTo = Paths.get(downloadDir.toString(), binaryMessage.getSourceName(), binaryMessage.getFileName());

        try {
            if (Files.notExists(downloadDir.getParent())) Files.createDirectory(downloadDir.getParent());
            if (Files.notExists(downloadDir)) Files.createDirectory(downloadDir);
            if (Files.notExists(downloadTo.getParent())) Files.createDirectory(downloadTo.getParent());
            Files.write(downloadTo, binaryMessage.getFileBytes());
            System.out.println(message);
        } catch (IOException e) {
            System.out.println(String.format("Failed downloading binary message from: %s, ERROR: %s", binaryMessage.getSourceName(), e));
        }
    }

    private void handleReceiveUserBinaryMessage(UserBinaryMessage userBinaryMessage) {
        handleReceiveBinaryMessage(userBinaryMessage, String.format("[%s][user][%s] File received: %s", userBinaryMessage.getDate(),
                userBinaryMessage.getSourceName(), userBinaryMessage.getFileName()));
    }

    private void handleReceiveGroupBinaryMessage(GroupBinaryMessage groupBinaryMessage) {
        handleReceiveBinaryMessage(groupBinaryMessage, String.format("[%s][%s][%s] File received: %s", groupBinaryMessage.getDate(),
                groupBinaryMessage.getGroupName(), groupBinaryMessage.getSourceName(), groupBinaryMessage.getFileName()));
    }

    private void handleReceiveUserInvite (GroupUserInvite userInvite) {
        pendingInvitation.push(userInvite);
        System.out.println(String.format("You have been invited to %s, Accept?", userInvite.getGroupName()));
    }

    private void handleUserDisconnectionConfirmed(UserDisconnectionConfirmed udf) {
        System.out.println(udf.getMessage());
        this.getContext().getParent().tell(PoisonPill.getInstance(), ActorRef.noSender());

        // Initiate coordinated shutdown
        this.getContext().getSystem().terminate();
    }

    private void handleUserDisconnectionDenied(UserDisconnectionDenied udd) {
        System.out.println(udd.getMessage());
    }

    private void handleAckReply(Ack reply) {
        System.out.println(reply.getSuccessMessage());
    }

    private void handleErrorReply(ErrorMessage reply) {
        System.out.println(reply.getErrorMessage());
    }

    private void handleDisconnectionRequest(UserDisconnectionRequest userDisconnectionRequest) {
        this.usersAlreadyCached.remove(userDisconnectionRequest.getUserName());
        this.messageSender.tell(new RemoveSession(userDisconnectionRequest.getReceiver()), self());
    }

    private void handleGroupTextualMessage(GroupTextualMessage groupTextualMessage) {
        System.out.println(String.format("[%s][%s][%s] %s",
                groupTextualMessage.getDate(),
                groupTextualMessage.getGroupName(),
                groupTextualMessage.getSourceSender(),
                groupTextualMessage.getTextualMessageContent()));
    }
}
