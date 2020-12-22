package application.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import application.messages.Ack;
import application.messages.ErrorMessage;
import application.messages.Group.*;
import application.messages.Message;
import application.messages.User.UserConnectionRequest;
import application.messages.User.GetUserRef;
import application.messages.User.UserDisconnectionRequest;


public class ManagingServer extends AbstractActor {

    private final ActorRef usersManager = getContext().actorOf(Props.create(UserConnection.class), "UserConnection");
    private final ActorRef groupsManager = getContext().actorOf(GroupsManager.props(usersManager), "GroupsManager");


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(UserConnectionRequest.class, this::handleConnectionRequest)
				.match(UserDisconnectionRequest.class, this::handleDisconnectionRequest)
                .match(GetUserRef.class, this::handleGetUserRef)
                .match(GroupMessage.class, this::handleGroup)
                .match(ErrorMessage.class, this::handleGroup)
                .build();
    }


    private void handleGroup(Message msg) {
        groupsManager.forward(msg, getContext());
    }

    private void handleGetUserRef(GetUserRef getUserRefMessage) {
        // TODO: implement sending a message to the Manager to get the ref of GetUserRef.username
        // then send the actorRef to the father with new message
        // Looking up Remote Actors
        //  actorSelection(path) will obtain an ActorSelection to an Actor on a remote node, e.g.:
        // ActorSelection selection =
        //  context.actorSelection("akka://actorSystemName@10.0.0.1:25520/user/actorName");
        // get it by printing the Manager ActorRef, currently couldn't complete
        usersManager.forward(getUserRefMessage, getContext());
    }

    private void handleConnectionRequest(UserConnectionRequest connectionRequestMessage) {
        getSender().tell(new Ack(), getSelf());
        usersManager.forward(connectionRequestMessage, getContext());
    }

    private void handleDisconnectionRequest(UserDisconnectionRequest userDisconnectionRequest) {
        groupsManager.forward(userDisconnectionRequest, getContext());
        usersManager.forward(userDisconnectionRequest, getContext());
    }
}