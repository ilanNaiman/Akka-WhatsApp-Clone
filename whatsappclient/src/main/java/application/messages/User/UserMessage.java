package application.messages.User;

import akka.actor.ActorRef;
import application.messages.Message;

import java.util.Date;

public abstract class UserMessage implements Message {
    protected final Date date;
    protected final String sourceUserName;
    protected final String targetUserName;
    protected final ActorRef receiver;

    public UserMessage(Date date, String targetUserName, ActorRef receiver, String sourceUserName) {
        this.date = date;
        this.targetUserName = targetUserName;
        this.receiver = receiver;
        this.sourceUserName = sourceUserName;
    }

    public Date getDate() {
        return date;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    public ActorRef getReceiver() {
        return receiver;
    }

    public String getSourceUserName() {
        return sourceUserName;
    }

    @Override
    public boolean equals(Object o) {
        return (o != null && getClass() == o.getClass() &&
                ((UserMessage) o).targetUserName.equals(this.targetUserName) &&
                ((UserMessage) o).receiver.equals(this.receiver));
    }
}
