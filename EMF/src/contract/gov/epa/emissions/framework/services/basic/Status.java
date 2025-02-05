package gov.epa.emissions.framework.services.basic;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="status", schema="emf")
public class Status implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "status_generator")
    @SequenceGenerator(name="status_generator", sequenceName = "status_id_seq", allocationSize=1)
    @Column(name = "id")
    private Integer id;

    @Column(name="username", nullable=false, unique=false)
    private String username;

    @Column(name="type", nullable=false, unique=false, length = 255)
    private String type;

    @Column(name="message", nullable=true, unique=false)
    private String message;

    @Column(name="is_read", nullable=false, unique=false)
    private boolean read;

    @Column(name="date", nullable=false, unique=false)
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
        return read;
    }

    public void markRead() {
        this.read = true;
    }

    public void setRead(boolean read) {
        this.read = read;
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Status that = (Status) o;

        return !(id != null ? !id.equals(that.id) : that.id != null);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
