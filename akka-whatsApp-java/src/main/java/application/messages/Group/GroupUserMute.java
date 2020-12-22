package application.messages.Group;

import application.messages.Message;

import java.time.Duration;

public class GroupUserMute extends GroupMessageToTarget {
    private final String groupName;
    private final Duration duration;

    public GroupUserMute(String groupName, String targetUserName, String sourceSender, Duration duration) {
        super(groupName, sourceSender, targetUserName);
        this.groupName = groupName;
        this.duration = duration;
    }

    public Duration getDuration() {
        return duration;
    }

    @Override
    public boolean equals(Object o) {
        return (o != null && getClass() == o.getClass() &&
                ((GroupUserMute) o).groupName.equals(this.groupName) &&
                ((GroupUserMute) o).targetUserName.equals(this.targetUserName) &&
                ((GroupUserMute) o).duration.toNanos() == this.duration.toNanos());
    }
}
