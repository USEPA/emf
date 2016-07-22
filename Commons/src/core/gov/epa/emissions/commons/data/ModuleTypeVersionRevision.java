package gov.epa.emissions.commons.data;

import java.io.Serializable;
import java.util.Date;

import gov.epa.emissions.commons.security.User;

public class ModuleTypeVersionRevision implements Serializable {

    private int id;

    private ModuleTypeVersion moduleTypeVersion;

    private int revision;

    private String description;

    private Date creationDate;

    private User creator;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ModuleTypeVersion getModuleTypeVersion() {
        return moduleTypeVersion;
    }

    public void setModuleTypeVersion(ModuleTypeVersion moduleTypeVersion) {
        this.moduleTypeVersion = moduleTypeVersion;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public String toString() {
        return String.format("%d", revision);
    }

    public int hashCode() {
        return revision;
    }

    public boolean equals(Object other) {
        return (other instanceof ModuleTypeVersionRevision && ((ModuleTypeVersionRevision) other).getRevision() == revision);
    }

    public int compareTo(ModuleTypeVersionRevision o) {
        return revision - o.getRevision();
    }
}
