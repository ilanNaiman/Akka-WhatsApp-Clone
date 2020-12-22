package application.messages.User;

import akka.actor.ActorRef;
import application.messages.BinaryMessage;

import java.util.Date;

public class UserBinaryMessage extends UserMessage implements BinaryMessage {
    private final String fileName;
    private final byte[] fileBytes;

    public UserBinaryMessage(Date date, String username, ActorRef receiver, String fileName, byte[] fileBytes, String sourceUserName) {
        super(date, username, receiver, sourceUserName);
        this.fileName = fileName;
        this.fileBytes = fileBytes;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public String getSourceName() {
        return targetUserName;
    }

    @Override
    public boolean equals(Object o) {
        return (o != null && getClass() == o.getClass() &&
                ((UserBinaryMessage) o).targetUserName.equals(this.targetUserName) &&
                ((UserBinaryMessage) o).fileName.equals(this.fileName));
    }
}
