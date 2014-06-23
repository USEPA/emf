package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.Serializable;
import java.util.Date;

public class ControlProgram implements Lockable, Serializable {

    private int id;

    private String name;

    private String description;

    private Date startDate;

    private Date endDate;

    private ControlProgramType controlProgramType;

    private User creator;

    private Date lastModifiedDate;

    private Date completionDate;

    private EmfDataset dataset;

    private Integer datasetVersion;

    private Mutex lock;

    private ControlTechnology[] technologies = new ControlTechnology[] {};

    private ControlMeasure[] controlMeasures = new ControlMeasure[] {};

    public ControlProgram() {
        this.lock = new Mutex();
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof ControlProgram))
            return false;

        final ControlProgram cs = (ControlProgram) other;

        return cs.name.equals(name) || cs.id == id;
    }

    public int hashCode() {
        return name.hashCode();
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public ControlProgramType getControlProgramType() {
        return controlProgramType;
    }

    public void setControlProgramType(ControlProgramType controlProgramType) {
        this.controlProgramType = controlProgramType;
    }

    public void setDataset(EmfDataset dataset) {
        this.dataset = dataset;
    }

    public EmfDataset getDataset() {
        return dataset;
    }

    public void setDatasetVersion(Integer datasetVersion) {
        this.datasetVersion = datasetVersion;
    }

    public Integer getDatasetVersion() {
        return datasetVersion;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
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

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public String toString() {
        return name;
    }

    public void setTechnologies(ControlTechnology[] technologies) {
        this.technologies = technologies;
    }

    public ControlTechnology[] getTechnologies() {
        return technologies;
    }

    public void setControlMeasures(ControlMeasure[] controlMeasures) {
        this.controlMeasures = controlMeasures;
    }

    public ControlMeasure[] getControlMeasures() {
        return controlMeasures;
    }
}
