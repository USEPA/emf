package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.cost.ControlStrategy;

import java.io.Serializable;
import java.util.Date;

public class StrategyGroup implements Lockable, Serializable {

    private int id;
    
    private String name;
    
    private String notes;
    
    private ControlStrategy[] controlStrategies = new ControlStrategy[] {};
    
    private Mutex lock;
    
    public StrategyGroup() {
        this.lock = new Mutex();
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public ControlStrategy[] getControlStrategies() {
        return controlStrategies;
    }
    
    public void setControlStrategies(ControlStrategy[] controlStrategies) {
        this.controlStrategies = controlStrategies;
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

    public String toString() {
        return name;
    }

    public int hashCode() {
        return name.hashCode();
    }
    
    public boolean equals(Object other) {
        if (other == null || !(other instanceof StrategyGroup))
            return false;
        
        final StrategyGroup group = (StrategyGroup) other;
        
        return group.name.equals(name) || group.id == id;
    }
}
