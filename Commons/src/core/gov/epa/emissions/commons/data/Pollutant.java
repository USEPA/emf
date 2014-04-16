package gov.epa.emissions.commons.data;

import gov.epa.emissions.commons.security.User;

import java.io.Serializable;
import java.util.Date;

public class Pollutant implements Serializable, Lockable, Comparable {
    private int id;

    private String name;

    private String description;

    private Mutex lock;

    public Pollutant() {
        this.lock = new Mutex();
    }

    public Pollutant(String name) {
        this();
        this.name = name.toUpperCase();
    }

    public Pollutant(int id, String name) {
        this();
        this.id = id;
        this.name = name.toUpperCase();
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
        this.name = name.toUpperCase();
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

    public boolean equals(Object other) {
        if (other == null || !(other instanceof Pollutant))
            return false;

        Pollutant pollutant = (Pollutant) other;
        return (pollutant.id == id || (name != null ? name : "").equalsIgnoreCase((pollutant.getName() != null ? pollutant.getName() : "")));
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return name;
    }

    public int compareTo(Object other) {
        if (other == null || !(other instanceof Pollutant))
            return -1;

        Pollutant pollutant = (Pollutant) other;
        return (pollutant.id == id || pollutant.getName().equalsIgnoreCase(name) ? 0 : -1);
    }
}
