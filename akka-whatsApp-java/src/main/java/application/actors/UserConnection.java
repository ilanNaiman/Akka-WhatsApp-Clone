package application.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import application.messages.Ack;
import application.messages.ErrorMessage;
import application.messages.User.*;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

// TODO: disconnect user should use other functionalities when availiabels.
//       Change actor ref to remote application.actors communication
public class UserConnection extends AbstractLoggingActor {
    private final BidiMap<String, ActorRef> userNameToReceiverActorRefMap;
    private final BidiMap<String, ActorRef> userNameToSenderActorRefMap;

    private UserConnection() {
        this.userNameToReceiverActorRefMap = new DualHashBidiMap<>();
        this.userNameToSenderActorRefMap = new DualHashBidiMap<>();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(UserConnectionRequest.class, this::handleConnectionRequest)
                .match(UserDisconnectionRequest.class, this::handleDisconnectionRequest)
                .match(GetUserRef.class, this::handleGetUserRef)
                .build();
    }

    private void handleConnectionRequest(UserConnectionRequest connectionRequest) {
        ActorRef actorRef = connectionRequest.getReceiver();

        if (this.userNameToReceiverActorRefMap.containsKey(connectionRequest.getUserName())) {
			final String USER_IN_USE_ERR = "{} is in use!";
            actorRef.tell(new ErrorMessage(USER_IN_USE_ERR.replace("{}", connectionRequest.getUserName())), getSelf());
        } else {
            this.userNameToReceiverActorRefMap.put(connectionRequest.getUserName(), connectionRequest.getReceiver());
            this.userNameToSenderActorRefMap.put(connectionRequest.getUserName(), connectionRequest.getSender());
            actorRef.tell(new AcceptedConnection(connectionRequest.getUserName()), getSelf());
        }
    }

    private void handleDisconnectionRequest(UserDisconnectionRequest disconnectionRequest) {
        ActorRef receiver = disconnectionRequest.getReceiver();
        this.userNameToSenderActorRefMap.removeValue(getSender());
        String userName = this.userNameToReceiverActorRefMap.removeValue(receiver);

        if (userName == null) {
            receiver.tell(new ErrorMessage(), getSelf());
        } else {
            getSender().tell(new Ack(), getSelf());
            receiver.tell(new UserDisconnectionConfirmed(userName), getSelf());
        }
    }

    private void handleGetUserRef(GetUserRef getUserRef){
        String user = getUserRef.getUserName();
        if (this.userNameToReceiverActorRefMap.containsKey(user)) {
            getSender().tell(new ReceivedUserRef(user,
                    getUserRef.getSourceUser(), this.userNameToReceiverActorRefMap.get(user)), self());
        } else {
            getSender().tell(new UserNotExists(getUserRef.getUserName()), getSelf());
            getUserRef.getSourceUser()
                    .tell(new ErrorMessage(String.format("%s does not exist!",
                    getUserRef.getUserName())), getSelf());
        }
    }
}
