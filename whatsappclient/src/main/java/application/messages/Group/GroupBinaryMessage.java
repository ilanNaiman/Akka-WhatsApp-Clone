package application.messages.Group;

import application.messages.BinaryMessage;
import application.messages.Message;

import java.util.Date;

public class GroupBinaryMessage extends GroupMessage implements Message, BinaryMessage {
    private final Date date;
    private final String fileName;
    private final byte[] fileBytes;

    public GroupBinaryMessage(Date date, String groupName, String fileName, String senderName, byte[] fileBytes) {
        super(groupName, senderName);
        this.date = date;
        this.fileName = fileName;
        this.fileBytes = fileBytes;
    }

    public Date getDate() {
        return date;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public String getSourceName() {
        return sourceSender;
    }

    @Override
    public boolean equals(Object o) {
        return (o != null && getClass() == o.getClass() &&
                ((GroupBinaryMessage) o).groupName.equals(this.groupName) &&
                ((GroupBinaryMessage) o).fileName.equals(this.fileName));
    }
}
