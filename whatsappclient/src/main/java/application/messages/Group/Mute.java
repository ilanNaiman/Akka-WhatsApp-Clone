package application.messages.Group;

import akka.actor.ActorRef;

import java.time.Duration;

public class Mute {
    private final String groupName;
    private final String targetUserName;
    private final Duration duration;
    private final ActorRef sender;

    public Mute(String groupName, String targetUserName, Duration duration, ActorRef sender) {
        this.groupName = groupName;
        this.targetUserName =targetUserName;
        this.duration = duration;
        this.sender = sender;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    public Duration getDuration() {
        return duration;
    }

    public ActorRef getSender() {
        return sender;
    }

    @Override
    public boolean equals(Object o) {
        return (o != null && getClass() == o.getClass() &&
                ((Mute) o).groupName.equals(this.groupName) &&
                ((Mute) o).targetUserName.equals(this.targetUserName) &&
                ((Mute) o).duration.toNanos() == this.duration.toNanos());
    }
}
