package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.security.User;

import java.io.Serializable;
import java.util.Date;

public class LiteModuleTypeVersion implements Serializable {

    private int id;

    private LiteModuleType liteModuleType;

    private int version;

    private String name;

    private String description;

    private Date creationDate;

    private Date lastModifiedDate;

    private User creator;

    private int baseVersion;

    private boolean isFinal;
    
    public LiteModuleTypeVersion()
    {
    }

    public boolean isComposite() {
        return liteModuleType.isComposite();
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LiteModuleType getLiteModuleType() {
        return liteModuleType;
    }

    public void setLiteModuleType(LiteModuleType liteModuleType) {
        this.liteModuleType = liteModuleType;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String versionAndName() {
        return version + " - " + name;
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

    public int getBaseVersion() {
        return baseVersion;
    }

    public void setBaseVersion(int baseVersion) {
        this.baseVersion = baseVersion;
    }

    public boolean getIsFinal() {
        return isFinal;
    }

    public void setIsFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    // standard methods

    public String toString() {
        return getName();
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof LiteModuleTypeVersion)) return false;
        return ((LiteModuleTypeVersion) other).getId() == id;
    }

    public int compareTo(LiteModuleTypeVersion o) {
        if (this == o) return 0;
        return Integer.valueOf(id).compareTo(o.getId());
    }
}
