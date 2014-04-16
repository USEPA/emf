package gov.epa.emissions.commons.db.version;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.db.intendeduse.IntendedUse;
import gov.epa.emissions.commons.security.User;

import java.io.Serializable;
import java.util.Date;

public class Version implements Lockable, Serializable {

    private int id;

    private int datasetId;

    private int version;

    /* parent versions */
    private String path;

    private boolean finalVersion = false;

    private String name;

    private String description = "";

    private Date lastModifiedDate;

    private User creator;

    private int numberRecords;
    
    private IntendedUse intendedUse;
    
    private Mutex lock;

    public Version() {
        lock = new Mutex();
    }

    public Version(int v) {
        this();
        version = v;
    }

    public boolean isFinalVersion() {
        return finalVersion;
    }

    public void markFinal() {
        this.finalVersion = true;
    }

    public int getVersion() {
        return version;
    }

    public void setDatasetId(int datasetId) {
        this.datasetId = datasetId;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getDatasetId() {
        return datasetId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setFinalVersion(boolean finalVersion) {
        this.finalVersion = finalVersion;
    }

    public void setName(String name) {
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

    public IntendedUse getIntendedUse() {
        return intendedUse;
    }

    public void setIntendedUse(IntendedUse intendedUse) {
        this.intendedUse = intendedUse;
    }

    /**
     * create path that includes 'me'
     */
    public String createCompletePath() {
        return (path == null || path.length() == 0) ? (version + "") : (path + "," + version);
    }

    public long getBase() {
        if (version == 0)// i.e. root
            return 0;

        int start = path.lastIndexOf(",") + 1;
        return Long.parseLong(path.substring(start));
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public void setNumberRecords(int numberRecords) {
        this.numberRecords = numberRecords;
    }

    public int getNumberRecords() {
        return numberRecords;
    }

    public User getCreator() {
        return creator;
    }

    public String toString() {
        return version + " (" + name + ")";
    }

    public Date getLockDate() {
        return lock.getLockDate();
    }

    public void setLockDate(Date lockDate) {
        lock.setLockDate(lockDate);
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

    public String getLockOwner() {
        return lock.getLockOwner();
    }

    public void setLockOwner(String username) {
        lock.setLockOwner(username);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean equals(Object other) {
        return (other instanceof Version) && (((Version) other).id == id);
    }

    public int hashCode() {
        return id;
    }
}
