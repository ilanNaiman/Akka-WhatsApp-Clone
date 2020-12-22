package application.messages.Group;

import java.util.Date;

public class GroupTextualMessage extends GroupMessage {
    private final Date date;
    private final String textualMessageContent;

    public GroupTextualMessage(Date date, String groupName, String sourceSender, String textualMessageContent) {
        super(groupName, sourceSender);
        this.date = date;
        this.textualMessageContent = textualMessageContent;
    }

    public Date getDate() {
        return date;
    }

    public String getTextualMessageContent() {
        return textualMessageContent;
    }

    @Override
    public boolean equals(Object o) {
        return (o != null && getClass() == o.getClass() &&
                ((GroupTextualMessage) o).getGroupName().equals(getGroupName()) &&
                ((GroupTextualMessage) o).getSourceSender().equals(getSourceSender()) &&
                ((GroupTextualMessage) o).textualMessageContent.equals(this.textualMessageContent));
    }
}
