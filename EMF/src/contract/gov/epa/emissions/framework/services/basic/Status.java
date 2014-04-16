package gov.epa.emissions.framework.services.basic;

import java.io.Serializable;
import java.util.Date;

public class Status implements Serializable {

    private int id;

    private String username;

    private String type;

    private String message;

    private boolean isRead;

    private Date timestamp;

    public Status() {// needed for serialization
    }

    public Status(String username, String msgType, String message, Date timestamp) {
        this.username = username;
        this.type = msgType;
        this.message = message;
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void markRead() {
        this.isRead = true;
    }

    public void setRead(boolean read) {
        this.isRead = read;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String messageType) {
        this.type = messageType;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String toString() {
        return "Message : " + message + " for user: " + username;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
