package gov.epa.emissions.commons.data;

import gov.epa.emissions.commons.security.User;

import java.io.Serializable;
import java.util.Date;

public class Mutex implements Serializable {

    private String owner;

    private Date lockDate;

    public Date getLockDate() {
        return lockDate;
    }

    public void setLockDate(Date lockDate) {
        this.lockDate = lockDate;
    }

    public String getLockOwner() {
        return owner;
    }

    public void setLockOwner(String username) {
        this.owner = username;
    }

    public boolean isLocked(String owner) {
        return this.owner != null && owner != null && this.owner.equals(owner);
    }

    public boolean isLocked(User user) {
        return (user.getUsername().equals(owner));
    }

    public boolean isLocked() {
        return owner != null && lockDate != null;
    }

}
