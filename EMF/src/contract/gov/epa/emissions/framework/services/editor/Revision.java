package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.security.User;

import java.util.Date;

/**
 * This class keeps track of the date/time a user initiated an export of a particular version of a dataset to a
 * repository (location).
 * 
 * @author Conrad F. D'Cruz
 * 
 */
public class Revision implements Lockable {

    private int id;

    private int datasetId;

    private int version;

    private User creator;

    private Date date;

    private String what;

    private String why;

    private String references;

    private Mutex lock;

    public Revision() {// No argument constructor needed for hibernate mapping
        lock = new Mutex();
    }

    public Revision(User creator, int datasetId, Date date, int version, String what, String why, String references) {

        this();
        this.creator = creator;
        this.datasetId = datasetId;
        this.date = date;
        this.version = version;
        this.what = what;
        this.why = why;
        this.references = references;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public int getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(int datasetId) {
        this.datasetId = datasetId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getWhat() {
        return what;
    }

    public void setWhat(String what) {
        this.what = what;
    }

    public String getWhy() {
        return why;
    }

    public void setWhy(String why) {
        this.why = why;
    }

    public String getReferences() {
        return references;
    }

    public void setReferences(String references) {
        this.references = references;
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

    @Override
    public String toString() {
        return "Revision (" + super.toString() + ") with id " + this.getId() + " is locked by " + this.getLockOwner()
                + " at " + this.getLockDate() + " (what: " + what + ", why: " + why + ", references: " + references
                + ")";
    }
}
