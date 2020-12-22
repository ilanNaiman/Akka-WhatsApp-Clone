package application.messages.Group;

import com.fasterxml.jackson.annotation.JsonCreator;

public class GroupLeave extends GroupMessage {
    private final boolean quietly;

    @JsonCreator
    public GroupLeave(String groupName, String sourceSender) {
        super(groupName, sourceSender);
        this.quietly = false;
    }

    public GroupLeave(String groupName, String sourceSender, boolean quietly) {
        super(groupName, sourceSender);
        this.quietly = quietly;
    }

    public boolean quietly() {
        return quietly;
    }

    @Override
    public boolean equals(Object o) {
        return (o != null && getClass() == o.getClass() &&
                ((GroupLeave) o).getGroupName().equals(getGroupName()) &&
                ((GroupLeave) o).getSourceSender().equals(getSourceSender()));
    }
}
