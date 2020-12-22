package application.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import application.messages.Group.*;
import application.messages.Ack;
import application.messages.ErrorMessage;
import application.messages.User.UserDisconnectionRequest;

import java.util.HashMap;
import java.util.Map;

public class GroupsManager extends AbstractActor {

    private final Map<String, ActorRef> groupsNamesToActors;
    private final ActorRef userConnection;
    private Integer id = 0;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GroupCreate.class, this::handleGroupCreate)
                .match(GroupLeave.class, this::handleGroupMessage)
                .match(GroupClose.class, this::handleGroupClose)
                .match(GroupTextualMessage.class, this::handleGroupMessage)
                .match(GroupBinaryMessage.class, this::handleGroupMessage)
                .match(GroupUserRemove.class, this::handleGroupMessage)
                .match(GroupAddUser.class, this::handleGroupAddUser)
                .match(GroupCoAdminAdd.class, this::handleGroupMessage)
                .match(GroupCoAdminRemove.class, this::handleGroupMessage)
                .match(UserDisconnectionRequest.class, this::handleDisconnectionRequest)
                .match(GroupMessage.class, this::handleGroupMessage)
                .build();
    }

    public GroupsManager(ActorRef userConnection) {
        this.groupsNamesToActors = new HashMap<>();
        this.userConnection = userConnection;
    }

    static Props props(ActorRef userConnection) {
        return Props.create(GroupsManager.class, userConnection);
    }

    private void handleGroupCreate(GroupCreate groupCreationRequest) {
        if (groupDoesNotExist(groupCreationRequest.getGroupName())) {
            groupsNamesToActors.put(groupCreationRequest.getGroupName(),
                    getContext().actorOf(Group.props(
                            groupCreationRequest.getGroupName(),
                            getSender(),
                            groupCreationRequest.getSourceSender(),
                            userConnection
                            ),
                            "group-" + id));
            id++;

            // a success message will be printed: "<groupname> created successfully!"
            getSender().tell(
                    new Ack(String.format("%s created successfully!", groupCreationRequest.getGroupName())),
                    getSender());
        } else {
            getSender().tell(
                    new ErrorMessage(String.format("%s already exists!", groupCreationRequest.getGroupName())),
                    getSender());
        }
    }

    private void handleDisconnectionRequest(UserDisconnectionRequest userDisconnectionRequest) {
        for (String group: this.groupsNamesToActors.keySet()) {
            handleGroupMessage(new GroupLeave(group, userDisconnectionRequest.getUserName(), true));
        }
    }

    private void handleGroupClose(GroupClose groupClose) {
        groupsNamesToActors.remove(groupClose.getGroupName(), getSender());
        getSender().tell(PoisonPill.getInstance(), ActorRef.noSender());
    }

    private void handleGroupMessage(GroupMessage groupMessage) {
        // tried to make this check generic for all messages of kind Group but for
        if (groupDoesNotExist(groupMessage.getGroupName())) {
           final String GROUP_DOES_NOT_EXISTS = "%s does not exist!";
            getSender().tell(new ErrorMessage(String.format(GROUP_DOES_NOT_EXISTS, groupMessage.getGroupName())),
                    getSelf());
        } else {
            groupsNamesToActors.get(groupMessage.getGroupName()).forward(groupMessage, getContext());
        }
    }

    private void handleGroupAddUser(GroupMessage groupAddUser) {
        groupsNamesToActors.get(groupAddUser.getGroupName()).forward(groupAddUser, getContext());
    }

    private boolean groupDoesNotExist(String groupName) {
        return !groupsNamesToActors.containsKey(groupName);
    }
}
