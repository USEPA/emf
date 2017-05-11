package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.security.User;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class LiteModuleType implements Serializable, Lockable, Comparable<LiteModuleType> {

    private int id;

    private String name;

    private String description;

    private Mutex lock;

    private Date creationDate;

    private Date lastModifiedDate;

    private User creator;

    private int defaultVersion;

    private boolean isComposite;

    private Set<Tag> tags;

    public LiteModuleType() {
        lock = new Mutex();
        isComposite = false;
        tags = new HashSet<Tag>();
    }

    public LiteModuleType(int id, String name) {
        this();
        this.id = id;
        this.name = name;
    }

    public LiteModuleType(String name) {
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

    public int compareTo(LiteModuleType o) {
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
    
    // tags

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public void clearTags() {
        this.tags.clear();
    }

    public String getTagsText() {
        StringBuilder tagsText = new StringBuilder();
        for(Tag tag : tags) {
            if (tagsText.length() > 0)
                tagsText.append(", ");
            tagsText.append(tag.name);
        }
        return tagsText.toString();
    }

    // standard methods

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof LiteModuleType && ((LiteModuleType) other).getId() == id);
    }
}
