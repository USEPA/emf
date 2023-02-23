package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.DependentJob;
import gov.epa.emissions.framework.ui.RowSource;

public class CaseJobsRowSource implements RowSource {

    private CaseJob job;

    public CaseJobsRowSource(CaseJob source) {
        this.job = source;
    }

    public Object[] values() {
        return new Object[] { getJobName(), getJobOrder(job), getRegionName(job),
                getSectorName(job), getRunStatus(job), getRunningUser(job), getRunLog(job), 
                getStartDate(job), getCompleteDate(job), getExecutableName(job),  
                getArgs(job), getVersion(job), getJobId(job),
                getPath(job), getQOpt(job), getJobAbbrev(), isLocal(job), getIDInQ(job), 
                getUser(job), getHost(job), getPurpose(job), getDependOn() };
    }
    
    private String getJobName() {
        return (job.getName() == null) ? "" : job.getName();
    }
    
    //NOTE: order is being stored in the job number field
    private Float getJobOrder(CaseJob job) {
        return Float.valueOf(job.getJobNo()); 
    }
    
    private String getRegionName(CaseJob job) {
        return (job.getRegion() == null) ? "" : job.getRegion().toString();
    }
    
    private String getSectorName(CaseJob job) {
        return (job.getSector() == null) ? "All sectors" : job.getSector().getName();
    }
    
    private String getExecutableName(CaseJob job) {
        return (job.getExecutable() == null) ? "" : job.getExecutable().getName();
    }
    
    private Integer getVersion(CaseJob job) {
        return Integer.valueOf(job.getVersion());
    }
    
    private String getArgs(CaseJob job) {
        return job.getArgs();
    }
    
    private Integer getJobId(CaseJob job) {
        return Integer.valueOf(job.getId());
    }
    
    private String getRunStatus(CaseJob job) {
        return (job.getRunstatus() == null) ? "" : job.getRunstatus().getName();
    }
    
    private String getPath(CaseJob job) {
        return job.getPath();
    }
    
    private String getIDInQ(CaseJob job) {
        return job.getIdInQueue();
    }

    private String getQOpt(CaseJob job) {
        return job.getQueOptions();
    }
    
    private String getJobAbbrev(){
        return (job.getJobGroup() == null)? "": job.getJobGroup();
    }
    
    private String getStartDate(CaseJob job) {
        return (job.getRunStartDate() == null) ? "" : CustomDateFormat.format_YYYY_MM_DD_HH_MM(job.getRunStartDate());
    }

    private String getCompleteDate(CaseJob job) {
        return (job.getRunCompletionDate() == null) ? "" : CustomDateFormat.format_YYYY_MM_DD_HH_MM(job.getRunCompletionDate());
    }
    
    private String getPurpose(CaseJob job) {
        return job.getPurpose();
    }

    private String getHost(CaseJob job) {
        return (job.getHost() == null) ? "" : job.getHost().getName();
    }

    private String getRunLog(CaseJob job) {
        return job.getRunLog();
    }

    private String getUser(CaseJob job) {
        return (job.getUser() == null) ? "" : job.getUser().getName();
    }
    
    private String getRunningUser(CaseJob job) {
        return (job.getRunJobUser() == null) ? "" : job.getRunJobUser().getName();
    }
    
    private String isLocal(CaseJob job) {
        return (job == null) ? "" : job.isLocal() + "";
    }
    
    private String getDependOn(){
        String d="";
        DependentJob [] dependJobs =job.getDependentJobs();
        if (dependJobs != null || dependJobs.length >0){
            for (DependentJob dJob : dependJobs)
                d += dJob.getJobId() + ", ";
        }
        return d; 
    }
    
    public Object source() {
        return job;
    }

    public void validate(int rowNumber) {// No Op
    }

    public void setValueAt(int column, Object val) {// No Op
    }
}