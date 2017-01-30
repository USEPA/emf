package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.security.User;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

public class ModuleType implements Serializable, Lockable, Comparable<ModuleType> {

    private int id;

    private String name;

    private String description;

    private Mutex lock;

    private Date creationDate;

    private Date lastModifiedDate;

    private User creator;

    private int defaultVersion;

    private boolean isComposite;

    private Map<Integer, ModuleTypeVersion> moduleTypeVersions;

    public ModuleType() {
        lock = new Mutex();
        isComposite = false;
        moduleTypeVersions = new HashMap<Integer, ModuleTypeVersion>();
    }

    public ModuleType(int id, String name) {
        this();
        this.id = id;
        this.name = name;
    }

    public ModuleType(String name) {
        this();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return getName();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLockOwner() {
        return lock.getLockOwner();
    }

    public void setLockOwner(String username) {
        lock.setLockOwner(username);
    }

    public Date getLockDate() {
        return lock.getLockDate();
    }

    public void setLockDate(Date lockDate) {
        this.lock.setLockDate(lockDate);
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

    public int compareTo(ModuleType o) {
        return name.compareTo(o.getName());
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public int getDefaultVersion() {
        return defaultVersion;
    }

    public void setDefaultVersion(int defaultVersion) {
        this.defaultVersion = defaultVersion;
    }

    public boolean getIsComposite() {
        return isComposite;
    }

    public void setIsComposite(boolean isComposite) {
        this.isComposite = isComposite;
    }

    public boolean isComposite() {
        return getIsComposite();
    }
    
    public void setComposite(boolean isComposite) {
        setIsComposite(isComposite);
    }
    
    // moduleTypeVersions

    public Map<Integer, ModuleTypeVersion> getModuleTypeVersions() {
        return this.moduleTypeVersions;
    }

    public void setModuleTypeVersions(Map<Integer, ModuleTypeVersion> moduleTypeVersions) {
        this.moduleTypeVersions = moduleTypeVersions;
    }

    public void addModuleTypeVersion(ModuleTypeVersion moduleTypeVersion) {
        moduleTypeVersion.setModuleType(this);
        moduleTypeVersion.setVersion(this.moduleTypeVersions.size());
        this.moduleTypeVersions.put(moduleTypeVersion.getVersion(), moduleTypeVersion);
    }

    public void removeModuleTypeVersion(ModuleTypeVersion moduleTypeVersion) {
        removeModuleTypeVersion(moduleTypeVersion.getVersion());
    }

    public void removeModuleTypeVersion(Integer version) {
        this.moduleTypeVersions.remove(version);
    }

    // standard methods

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof ModuleType && ((ModuleType) other).getId() == id);
    }
}
