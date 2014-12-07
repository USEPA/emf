package gov.epa.emissions.framework.services.tempalloc;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class TemporalAllocation implements Lockable, Serializable {

    private int id;

    private String name;

    private String description;

    private Project project;

    private User creator;
    
    private String filter;
    
    private TemporalAllocationResolution resolution;
    
    private Date startDay, endDay;

    private Date lastModifiedDate;

    private Date startDate;

    private Date completionDate;

    private TemporalAllocationInputDataset[] temporalAllocationInputDatasets = new TemporalAllocationInputDataset[] {};
    
    private EmfDataset xrefDataset, monthlyProfileDataset, weeklyProfileDataset, dailyProfileDataset;
    
    private Integer xrefDatasetVersion, monthlyProfileDatasetVersion, weeklyProfileDatasetVersion, dailyProfileDatasetVersion;
        
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

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
    
    public TemporalAllocationResolution getResolution() {
        return resolution;
    }
    
    public void setResolution(TemporalAllocationResolution resolution) {
        this.resolution = resolution;
    }
    
    public Date getStartDay() {
        return startDay;
    }
    
    public void setStartDay(Date startDay) {
        this.startDay = startDay;
    }
    
    public Date getEndDay() {
        return endDay;
    }
    
    public void setEndDay(Date endDay) {
        this.endDay = endDay;
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
    
    public EmfDataset getXrefDataset() {
        return xrefDataset;
    }
    
    public void setXrefDataset(EmfDataset xrefDataset) {
        this.xrefDataset = xrefDataset;
    }

    public void setXrefDatasetVersion(Integer xrefDatasetVersion) {
        this.xrefDatasetVersion = xrefDatasetVersion;
    }

    public Integer getXrefDatasetVersion() {
        return xrefDatasetVersion;
    }
    
    public EmfDataset getMonthlyProfileDataset() {
        return monthlyProfileDataset;
    }
    
    public void setMonthlyProfileDataset(EmfDataset monthlyProfileDataset) {
        this.monthlyProfileDataset = monthlyProfileDataset;
    }

    public void setMonthlyProfileDatasetVersion(Integer monthlyProfileDatasetVersion) {
        this.monthlyProfileDatasetVersion = monthlyProfileDatasetVersion;
    }

    public Integer getMonthlyProfileDatasetVersion() {
        return monthlyProfileDatasetVersion;
    }
    
    public EmfDataset getWeeklyProfileDataset() {
        return weeklyProfileDataset;
    }
    
    public void setWeeklyProfileDataset(EmfDataset weeklyProfileDataset) {
        this.weeklyProfileDataset = weeklyProfileDataset;
    }

    public void setWeeklyProfileDatasetVersion(Integer weeklyProfileDatasetVersion) {
        this.weeklyProfileDatasetVersion = weeklyProfileDatasetVersion;
    }

    public Integer getWeeklyProfileDatasetVersion() {
        return weeklyProfileDatasetVersion;
    }
    
    public EmfDataset getDailyProfileDataset() {
        return dailyProfileDataset;
    }
    
    public void setDailyProfileDataset(EmfDataset dailyProfileDataset) {
        this.dailyProfileDataset = dailyProfileDataset;
    }

    public void setDailyProfileDatasetVersion(Integer dailyProfileDatasetVersion) {
        this.dailyProfileDatasetVersion = dailyProfileDatasetVersion;
    }

    public Integer getDailyProfileDatasetVersion() {
        return dailyProfileDatasetVersion;
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

    // When the client displays the temporal allocation start or end date, differences
    // in the server and client time zones can cause the dates to be displayed 
    // incorrectly. For example, if the server's local time zone is GMT and the client
    // is Eastern, a date like 08/01/2011 gets displayed as 07/31/2011. To fix this,
    // we check if the hour of the day is not midnight and adjust it to midnight. If
    // the hour is less than 12, we assume the server is behind the client (e.g., server 
    // is Pacific, client is Eastern) and don't bother adjusting the time since the date 
    // will be displayed correctly.
    private Date adjustDay(Date date) {
        if (date == null) return null;
        GregorianCalendar calDate = new GregorianCalendar();
        calDate.setTime(date);
        int hour = calDate.get(Calendar.HOUR_OF_DAY);
        if (hour != 0 && hour > 12) {
            calDate.add(Calendar.HOUR_OF_DAY, 24 - hour);
        }
        return calDate.getTime();
    }
    
    public Date adjustedStartDay() {
        return adjustDay(startDay);
    }
    
    public Date adjustedEndDay() {
        return adjustDay(endDay);
    }
}
