package gov.epa.emissions.commons.data;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.security.User;

import java.io.Serializable;
import java.util.Date;

public class SourceGroup implements Serializable, Lockable {
    private int id;

    private String name;

    private String description;

    private Mutex lock;

    public SourceGroup() {
        this.lock = new Mutex();
    }

    public SourceGroup(String description, String name) {
        this();
        this.description = description;
        this.name = name;
    }

    public SourceGroup(String name) {
        this();
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLockDate() {
        return lock.getLockDate();
    }

    public void setLockDate(Date lockDate) {
        lock.setLockDate(lockDate);
    }

    public String getLockOwner() {
        return lock.getLockOwner();
    }

    public void setLockOwner(String owner) {
        lock.setLockOwner(owner);
    }

    public boolean isLocked(String owner) {
        return lock.isLocked(owner);
    }

    public boolean isLocked(User owner) {
        return lock.isLocked(owner);
    }

    public boolean isLocked() {
        return lock.isLocked();
    }

    public boolean equals(Object object) {
        if (object == null || !(object instanceof SourceGroup))
            return false;

        SourceGroup other = (SourceGroup) object;
        return id == other.getId() || (name.equals(other.getName()));
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return name;
    }
}
