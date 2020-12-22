package application.messages.User;

import akka.actor.ActorRef;

import java.util.Date;

public class UserTextualMessage extends UserMessage {
    private final String textualMessageContent;

    public UserTextualMessage(Date date, String targetUserName, String sourceUserName, ActorRef receiver, String textualMessageContent) {
        super(date, targetUserName, receiver, sourceUserName);
        this.textualMessageContent = textualMessageContent;
    }

    public String getTextualMessageContent() {
        return textualMessageContent;
    }

    @Override
    public boolean equals(Object o) {
        return (o != null && getClass() == o.getClass() &&
                ((UserTextualMessage) o).targetUserName.equals(this.targetUserName) &&
                ((UserTextualMessage) o).textualMessageContent.equals(this.textualMessageContent));
    }
}
