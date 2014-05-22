package gov.epa.emissions.framework.services.tempalloc;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.security.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TemporalAllocation implements Lockable, Serializable {

    private int id;

    private String name;

    private String description;

    private Project project;

    private User creator;

    private Date lastModifiedDate;

    private Date startDate;

    private Date completionDate;

    private TemporalAllocationInputDataset[] temporalAllocationInputDatasets = new TemporalAllocationInputDataset[] {};
    
    private String runStatus;

    private Mutex lock;
    
    public TemporalAllocation() {
        this.lock = new Mutex();
    }

    public TemporalAllocation(String name) {
        this();
        this.name = name;
    }

    public TemporalAllocation(int id, String name) {
        this(name);
        this.id = id;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof TemporalAllocation))
            return false;

        final TemporalAllocation ta = (TemporalAllocation) other;

        return ta.name.equals(name) || ta.id == id;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return name;
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

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }

    public TemporalAllocationInputDataset[] getTemporalAllocationInputDatasets() {
        return temporalAllocationInputDatasets;
    }

    public void setTemporalAllocationInputDatasets(TemporalAllocationInputDataset[] inputDatasets) {
        this.temporalAllocationInputDatasets = inputDatasets;
    }

    public void addTemporalAllocationInputDatasets(TemporalAllocationInputDataset inputDataset) {
        List<TemporalAllocationInputDataset> inputDatasetList = new ArrayList<TemporalAllocationInputDataset>();
        inputDatasetList.addAll(Arrays.asList(temporalAllocationInputDatasets));
        inputDatasetList.add(inputDataset);
        this.temporalAllocationInputDatasets = inputDatasetList.toArray(new TemporalAllocationInputDataset[0]);
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
}
