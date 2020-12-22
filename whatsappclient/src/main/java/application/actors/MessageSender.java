package application.actors;

import akka.actor.*;
import application.messages.*;
import application.messages.Group.*;
import akka.pattern.Patterns;
import akka.util.Timeout;
import application.messages.Group.GroupCreate;
import application.messages.Group.GroupMessage;
import application.messages.User.*;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.time.Duration;
import java.util.*;

public class MessageSender extends AbstractActor {
    private final BidiMap<String, ActorRef> cachedSessionsMap;
    private final Map<String, List<Message>> pendingSendingMessages;
    private final Map<String, Cancellable> userMutedGroupsToTimer;

    // TODO: remove hard coded manager ref
    private final String MANAGER_REMOTE = "akka://whatsApp-chat-system@127.0.0.1:3553/user/ManagingServer";
    private final ActorSelection managerRef = getContext().actorSelection(MANAGER_REMOTE);

    private MessageSender() {
        this.cachedSessionsMap = new DualHashBidiMap<>();
        this.pendingSendingMessages = new HashMap<>();
        this.userMutedGroupsToTimer = new HashMap<>();
    }

    public static Props props() {
        return Props.create(MessageSender.class, MessageSender::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(UserConnectionRequest.class, this::handleConnectionRequest)
                .match(UserDisconnectionRequest.class, this::handleDisconnectionRequest)
                .match(AddSession.class, this::handleAddSession)
                .match(RemoveSession.class, this::handleRemoveSession)
                .match(UserMessage.class, this::handleSendUserMessage)
                .match(UserNotExists.class, this::handleUserNotExists)
                .match(ReceivedUserRef.class, this::handleReceivedUserRef)
                .match(GroupCreate.class, this::handleGroupCreate)
                .match(GroupUserInvite.class, this::handleGroupUserInvite)
				.match(GroupUserRemove.class, this::handleGroupUserRemove)
                .match(GroupCoAdminRemove.class, this::handleCoAdminRemove)
                .match(GroupMessage.class, this::handleGroupMessage)
                .match(GroupCoAdminAdd.class, this::handleCoAdminReq)
                .match(Mute.class, this::handleMute)
                .match(Unmute.class, this::handleUnmute)
                .match(GroupUserMute.class, this::handleGroupUserMute)
                .match(GroupUserUnmute.class, this::handleGroupUserUnmute)
                .build();
    }

    private void handleUserNotExists(UserNotExists userNotExists) {
        this.pendingSendingMessages.remove(userNotExists.getUserName());
    }

    private void handleConnectionRequest(UserConnectionRequest connectionRequest) {
        Timeout timeout = Timeout.create(Duration.ofSeconds(5));
        Future<Object> future = Patterns.ask(managerRef, connectionRequest, timeout);

        try {
            Await.result(future, timeout.duration());
        } catch (Exception e) {
            this.getContext().getParent().tell(new UserConnectionDenied(), getSelf());
        }
    }

    private void handleDisconnectionRequest(UserDisconnectionRequest disconnectionRequest) {
        Timeout timeout = Timeout.create(Duration.ofSeconds(5));
        Future<Object> future = Patterns.ask(managerRef, disconnectionRequest, timeout);

        try {
            Await.result(future, timeout.duration());

            for (ActorRef clientReceiver : this.cachedSessionsMap.values()) {
                clientReceiver.tell(disconnectionRequest, self());
            }

            this.cachedSessionsMap.clear();
        } catch (Exception e) {
            this.getContext().getParent().tell(new UserDisconnectionDenied(), getSelf());
            this.getContext().getParent().tell(new StartListener(), getSelf());
        }
    }

    private void handleSendUserMessage(UserMessage userMessage) {
        String addressee = userMessage.getTargetUserName();

        // Verify whether the client receiver is cached, otherwise get its ref from the manager and add the message to the
        // Pending user messages
        if (this.cachedSessionsMap.containsKey(addressee)) {
            this.cachedSessionsMap.get(addressee).tell(userMessage, self());
        } else {
            if (!this.pendingSendingMessages.containsKey(addressee)) {
                this.pendingSendingMessages.put(addressee, new LinkedList<Message>() {{
                    add(userMessage);
                }});
                managerRef.tell(new GetUserRef(addressee, getSender()), self());
            } else {
                this.pendingSendingMessages.get(addressee).add(userMessage);
            }
        }
    }

    private void handleAddSession(AddSession addSession) {
        if (!this.cachedSessionsMap.containsKey(addSession.getUserName()))
            this.cachedSessionsMap.put(addSession.getUserName(), addSession.getActorRef());
    }

    private void handleRemoveSession(RemoveSession removeSession) {
        this.cachedSessionsMap.removeValue(removeSession.getActorRef());
    }

    private void handleReceivedUserRef(ReceivedUserRef receivedUserRef) {
        /*
          A new session can be added in two ways:
          1- NOT IMPLEMENTED (the trade off is better) - By a remote client sending message to this client so the message
             receiver sending to the sender receiver a add session message.
          2- By trying to send a message to an uncached user, so the message is kept in the pendingSendingMessages while
             the message sender get the user ref and then cache it.
         */

        this.cachedSessionsMap.put(receivedUserRef.getUserName(), receivedUserRef.getTargetUserRef());

        // For each pending message we send it to the addressee client receiver
        for (Message message: this.pendingSendingMessages.get(receivedUserRef.getUserName())) {
            receivedUserRef.getTargetUserRef().tell(message, self());
        }

        // Clearing the user key in the pending sending application.messages map
        this.pendingSendingMessages.remove(receivedUserRef.getUserName());
    }


    // @TODO: Refactor all this messages into 1 method
    private void handleGroupCreate(GroupMessage groupCreateMessage) {
        managerRef.tell(groupCreateMessage, getSender());
    }


    private void handleGroupUserInvite(GroupUserInvite groupUserInvite) {
        managerRef.tell(groupUserInvite, groupUserInvite.getSourceUser());
    }

    private void handleGroupUserRemove(GroupUserRemove groupUserRemove) {
        managerRef.tell(groupUserRemove, getSender());
    }

    private void handleGroupMessage(GroupMessage groupMessage) {
        if (!userMutedGroupsToTimer.containsKey(groupMessage.getGroupName())) managerRef.tell(groupMessage, getSender());
    }

    private void handleCoAdminReq(GroupCoAdminAdd groupCoAdminAdd) {
        managerRef.tell(groupCoAdminAdd, getSender());
    }

    private void handleGroupUserMute(GroupUserMute groupUserMute) {
        managerRef.tell(groupUserMute, getSelf());
    }

    private void handleGroupUserUnmute(GroupUserUnmute groupUserUnmute) {
        managerRef.tell(groupUserUnmute, getSelf());
    }

    private void handleMute(Mute mute) {
        // If the user is already muted canceling the previous timer
        if (userMutedGroupsToTimer.containsKey(mute.getGroupName())) {
            userMutedGroupsToTimer.get(mute.getGroupName()).cancel();
        }

        Cancellable cancellable =
                getContext().getSystem()
                        .scheduler()
                        .scheduleOnce(
                                mute.getDuration(),
                                getSelf(),
                                new Unmute(mute.getGroupName(), mute.getTargetUserName(), mute.getSender(),
                                        "You have been unmuted! Muting time is up!"),
                                getContext().getSystem().dispatcher(),
                                getSender());
        userMutedGroupsToTimer.put(mute.getGroupName(), cancellable);
    }

    private void handleUnmute(Unmute unmute) {
        if (userMutedGroupsToTimer.containsKey(unmute.getGroupName())) {
            userMutedGroupsToTimer.get(unmute.getGroupName()).cancel();
            userMutedGroupsToTimer.remove(unmute.getGroupName());
            getSender().tell(new Ack(unmute.getMessage()), getSelf());
        } else {
            unmute.getSender().tell(new ErrorMessage(unmute.getTargetUserName() + " is not muted!"), getSelf());
        }
    }
	
	private void handleCoAdminRemove(GroupCoAdminRemove groupCoAdminRemove) {
        managerRef.tell(groupCoAdminRemove, getSender());
    }
}
