package application.messages.Group;

import com.fasterxml.jackson.annotation.JsonCreator;

public class GroupCreate extends GroupMessage{

    @JsonCreator
    public GroupCreate(String groupName, String sourceSender) {
        super(groupName, sourceSender);
    }

    @Override
    public boolean equals(Object o) {
        return (o != null && getClass() == o.getClass() &&
                ((GroupCreate) o).getGroupName().equals(getGroupName()));
    }
}
