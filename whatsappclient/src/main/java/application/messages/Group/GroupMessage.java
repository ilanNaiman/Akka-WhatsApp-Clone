package application.messages.Group;

import application.messages.Message;
import com.fasterxml.jackson.annotation.JsonCreator;

public abstract class GroupMessage implements Message {

    protected final String groupName;
    protected final String sourceSender;

    @JsonCreator
    protected GroupMessage(String groupName, String sourceSender) {
        this.groupName = groupName;
        this.sourceSender = sourceSender;
    }


    public String getGroupName() {
        return groupName;
    }

    public String getSourceSender() {
        return sourceSender;
    }
}
