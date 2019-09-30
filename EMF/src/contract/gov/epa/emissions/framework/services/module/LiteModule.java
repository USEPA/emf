package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.security.User;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class LiteModule implements Serializable, Lockable, Comparable<LiteModule> {

    private int id;

    private String name;

    private String description;

    private LiteModuleTypeVersion liteModuleTypeVersion;

    private Project project;

    private User creator;

    private Date creationDate;

    private Date lastModifiedDate;

    private boolean isFinal;

    private Mutex lock;

    private Set<Tag> tags;

    public LiteModule() {
        lock = new Mutex();
        tags = new HashSet<Tag>();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LiteModuleTypeVersion getLiteModuleTypeVersion() {
        return liteModuleTypeVersion;
    }

    public void setLiteModuleTypeVersion(LiteModuleTypeVersion liteModuleTypeVersion) {
        this.liteModuleTypeVersion = liteModuleTypeVersion;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
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

    public boolean getIsFinal() {
        return isFinal;
    }

    public void setIsFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public Mutex getLock() {
        return lock;
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

    public int compareTo(LiteModule o) {
        return name.compareTo(o.getName());
    }

    public String toString() {
        return getName();
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof LiteModule && ((LiteModule) other).getId() == id);
    }
}
