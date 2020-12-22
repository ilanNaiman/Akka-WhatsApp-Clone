package application.messages;

import akka.actor.ActorRef;

public class InformTarget implements Message {

    private final ActorRef targetUser;
    private final String announcement;


    public InformTarget(ActorRef targetUser, String announcement) {
        this.targetUser = targetUser;
        this.announcement = announcement;
    }

    public ActorRef getTargetUser() {
        return targetUser;
    }

    public String getAnnouncement() {
        return announcement;
    }

    @Override
    public boolean equals(Object o) {
        return (o != null && getClass() == o.getClass() && ((InformTarget) o).targetUser == targetUser);
    }
}
