package application.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import application.messages.Ack;
import application.messages.Group.*;
import application.messages.ErrorMessage;
import application.messages.InformTarget;
import akka.actor.*;
import akka.routing.*;
import application.messages.User.GetUserRef;
import application.messages.User.ReceivedUserRef;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.*;

public class Group extends AbstractLoggingActor {

    private static final String ADMINISTRATIVE_ERROR = "You are neither an admin nor a co-admin of %s!";
    private static final String GROUP_MEMBERSHIP_ERROR = "You are not part of %s!";
    private static final String GROUP_MESSAGE_FORMAT = "[%s][%s][%s]%s";

    private final String groupName;
    private BidiMap<ActorRef, String> actorRefToUserName;
    private ActorRef groupAdminRef;
    private final ActorRef userConnection;
    private Router router;
    private final Set<String> coAdmins;

    public Group(String groupName, ActorRef groupAdminRef, String adminName, ActorRef userConnection) {
        this.groupName = groupName;
        this.groupAdminRef = groupAdminRef;
        List<Routee> routees = new ArrayList<>();
        routees.add(new ActorRefRoutee(groupAdminRef));
        this.router = new Router(new BroadcastRoutingLogic(), routees);
        this.coAdmins = new HashSet<>();             // will hold the group users strings
        this.actorRefToUserName = new DualHashBidiMap<>(); // contain dual hashMap of user and their actorRef
        actorRefToUserName.put(groupAdminRef, adminName);
        this.userConnection = userConnection;
    }

    public static Props props(String groupName, ActorRef groupAdmin, String adminName, ActorRef userConnection) {
        return Props.create(Group.class, groupName, groupAdmin, adminName, userConnection);
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GroupTextualMessage.class, this::handleGroupMessage)
                .match(GroupBinaryMessage.class, this::handleGroupMessage)
                .match(GroupUserInvite.class, this::handleUserInvite)
                .match(GroupUserRemove.class, this::handleUserRemove)
                .match(GroupAddUser.class, this::handleGroupAddUser)
                .match(ReceivedUserRef.class, this::handleReceivedUserRef)
                .match(GroupCoAdminAdd.class, this::handleCoAdminReq)
				.match(GroupMessageToTarget.class, this::handleGroupMessageToTarget)
                .match(GroupCoAdminRemove.class, this::handleCoAdminRemove)
                .match(GroupLeave.class, this::handleGroupLeave)
                .build();
    }


    private void handleGroupMessageToTarget(GroupMessageToTarget groupMessageToTarget) {
        if (canSendAdministrativeCommandToTarget(groupMessageToTarget)) {
            this.actorRefToUserName.inverseBidiMap().get(groupMessageToTarget.getTargetUserName()).tell(
                    groupMessageToTarget, getSender()
            );
        }
    }

    private void handleGroupMessage(GroupMessage msg) {
        if (isNotAGroupMember(msg.getSourceSender())) {
            getSender().tell(new ErrorMessage(String.format(GROUP_MEMBERSHIP_ERROR, groupName)), getSender());
        } else {
        router.route(msg, getSender());
        }
    }


    private void handleUserInvite(GroupUserInvite msg) {
        if (notAdminOrCoAdmin(msg.getSourceSender())) {
            getSender().tell(new ErrorMessage(String.format(
                    ADMINISTRATIVE_ERROR, msg.getGroupName())), getSelf());
            return;
        }
        // sending a message to the user if he is connected, of yes or no (answer for invitation)
        userConnection.tell(new GetUserRef(msg.getTargetUserName(), msg.getSourceUser()), getSelf());
    }


    private void handleReceivedUserRef(ReceivedUserRef receivedUserRef) {
        if (actorRefToUserName.containsKey(receivedUserRef.getTargetUserRef())) {
            receivedUserRef.getSourceUserRef().tell(
                    new ErrorMessage(String.format(
                    "%s is already in %s", receivedUserRef.getUserName(), getGroupName())), getSelf());
            return;
        }
        receivedUserRef.getTargetUserRef().tell(
                new GroupUserInvite(getGroupName(),
                actorRefToUserName.get(receivedUserRef.getSourceUserRef()),
                receivedUserRef.getUserName(),
                receivedUserRef.getSourceUserRef()), getSelf());
    }

    private void handleGroupAddUser(GroupAddUser groupAddUser) {
        // this message is a result of a user accepting a group invitation
        this.router = router.addRoutee(groupAddUser.getTargetRef());
        actorRefToUserName.put(groupAddUser.getTargetRef(), groupAddUser.getTargetUserName());
        getSender().tell(
                new InformTarget(groupAddUser.getTargetRef(),
                                 String.format("Welcome to %s!", groupAddUser.getGroupName())),
                getSelf());
    }

    private void handleUserRemove(GroupUserRemove userRemove) {
        if (notValidSourceAndTarget(userRemove.getSourceSender(), userRemove.getGroupName())) return;
        ActorRef targetActorRef = actorRefToUserName.inverseBidiMap().get(userRemove.getTargetUserName());
        removeUserData(userRemove.getTargetUserName(), targetActorRef);
        getSender().tell(new InformTarget(targetActorRef,
                String.format(GROUP_MESSAGE_FORMAT,
                        new Date(),
                        userRemove.getGroupName(),
                        userRemove.getSourceSender(),
                        String.format("You have been removed from %s by %s!",
                                userRemove.getGroupName(),
                                userRemove.getSourceSender()))), getSelf());
    }

    private void handleGroupLeave(GroupLeave groupLeave) {
        if (targetUserDoesNotExist(groupLeave.getSourceSender()) && !groupLeave.quietly()) {
            getSender().tell(new ErrorMessage(String.format(
                    "%s is not in %s!",
                    groupLeave.getSourceSender(),
                    groupName)),
                    getSender());
        } else {
            ActorRef sourceRef = actorRefToUserName.inverseBidiMap().get(groupLeave.getSourceSender());
            // if user is the admin, broadcast message to every one and close the group.
            if (groupAdminRef.equals(sourceRef)) {
                router.route(new Ack(String.format("%s admin has closed %s!",
                        groupLeave.getSourceSender(),
                        groupLeave.getGroupName())), getSender());
                this.getContext().getParent().tell(new GroupClose(groupLeave.getGroupName()), getSelf());
            } else {
                // else if regular user, remove content from group and if also co-admin, remove from this list too.
                removeUserData(groupLeave.getSourceSender(), sourceRef);
                this.router.route(new Ack(String.format(GROUP_MESSAGE_FORMAT,
                        new Date(),
                        groupName,
                        groupLeave.getSourceSender(),
                        String.format("%s has left %s!",
                                groupLeave.getSourceSender(),
                                groupName))), getSender());
                // if the user is co-admin he will be removed
                coAdmins.remove(groupLeave.getSourceSender());
            }
        }
    }


    private void handleCoAdminReq(GroupCoAdminAdd groupCoAdminAdd) {
        final ActorRef targetUserRef= actorRefToUserName.inverseBidiMap().get(groupCoAdminAdd.getTargetUserName());
        if (notValidSourceAndTarget(groupCoAdminAdd.getTargetUserName(), groupCoAdminAdd.getGroupName())) return;
        coAdmins.add(groupCoAdminAdd.getTargetUserName());
        getSender().tell(
                new InformTarget(targetUserRef,
                        String.format("You have been promoted to co-admin in %s!", groupCoAdminAdd.getGroupName())),
                getSelf());
    }

    private void handleCoAdminRemove(GroupCoAdminRemove groupCoAdminRemove) {
        final ActorRef targetUserRef = actorRefToUserName.inverseBidiMap().get(groupCoAdminRemove.getTargetUserName());
        if (notValidSourceAndTarget(groupCoAdminRemove.getTargetUserName(), groupCoAdminRemove.getGroupName())) return;
        coAdmins.remove(groupCoAdminRemove.getTargetUserName());
        targetUserRef.tell(
                new InformTarget(targetUserRef,
                        String.format("You have been demoted to user in in %s!",
                        groupCoAdminRemove.getGroupName())),
                getSelf());
    }

    private boolean notValidSourceAndTarget(String targetUserName, String groupName) {
        if (targetUserDoesNotExist(targetUserName)) {
            getSender().tell(new ErrorMessage(String.format("%s does not exist!",
                    targetUserName)),
                    getSelf());
            return true;
        }
        if (notAdminOrCoAdmin(targetUserName)) {
            getSender().tell(new ErrorMessage(String.format(
                    ADMINISTRATIVE_ERROR, groupName)), getSelf());
            return true;
        }
        return false;
    }

    private boolean notAdminOrCoAdmin(String targetUserName) {
        return !groupAdminRef.equals(getSender()) && !coAdmins.contains(targetUserName);
    }

    private boolean targetUserDoesNotExist(String targetUserName) {
        return !this.actorRefToUserName.inverseBidiMap().containsKey(targetUserName);
    }

    private void removeUserData(String userName ,ActorRef userRef) {
        actorRefToUserName.removeValue(userRef);
        actorRefToUserName.inverseBidiMap().removeValue(userRef);
        this.router = router.removeRoutee(userRef);
        coAdmins.remove(userName);
    }

    private boolean hasAdministrativePermissions(String sourceName) {
        return (actorRefToUserName.get(groupAdminRef).equals(sourceName) || coAdmins.contains(sourceName));
    }

    private boolean isNotAGroupMember(String sourceName) {
        return !this.actorRefToUserName.inverseBidiMap().containsKey(sourceName);
    }

    private boolean canSendAdministrativeCommandToTarget(GroupMessageToTarget messageToTarget) {
        if (isNotAGroupMember(messageToTarget.getTargetUserName())) {
            getSender().tell(new ErrorMessage(
                    String.format(GROUP_MEMBERSHIP_ERROR, this.groupName)), getSelf());
            return false;
        }
        else if (!hasAdministrativePermissions(messageToTarget.getSourceSender())) {
            getSender().tell(new ErrorMessage(
                    String.format(ADMINISTRATIVE_ERROR, this.groupName)), getSelf());
            return false;
        }

        return true;
    }

}