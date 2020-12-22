package application.messages;

import java.util.Date;

public interface BinaryMessage {
    Date getDate();
    String getSourceName();
    String getFileName();
    byte[] getFileBytes();
}
