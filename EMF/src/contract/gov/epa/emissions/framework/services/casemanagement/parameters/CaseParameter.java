package gov.epa.emissions.framework.services.casemanagement.parameters;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.services.casemanagement.CaseProgram;
import gov.epa.emissions.framework.services.data.GeoRegion;

import java.io.Serializable;
import java.util.Date;

public class CaseParameter implements Serializable, Comparable<CaseParameter> {

    private int id;
    
    private ParameterName parameterName;
    
    private ParameterEnvVar envVar;
    
    private ValueType type;
    
    private Sector sector;
    
    private GeoRegion region;
    
    private CaseProgram program;
    
    private String value;
    
    private String notes;
    
    private String purpose;
    
    private float order;
    
    private boolean required;
    
    private boolean local;
    
    private int jobId;
    
    private int caseID;
    
    private int parentCaseId;
    
    private Date lastModifiedDate;

    private String jobName;
    
    public CaseParameter() {
        // NOTE Auto-generated constructor stub
    }
    
    public int getCaseID() {
        return caseID;
    }

    public void setCaseID(int caseID) {
        this.caseID = caseID;
    }

    public ParameterEnvVar getEnvVar() {
        return envVar;
    }

    public void setEnvVar(ParameterEnvVar envVar) {
        this.envVar = envVar;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getName() {
        return this.parameterName.getName();
    }
    
    public int getModelToRunId() {
        return this.parameterName.getModelToRunId();
    }

    public ParameterName getParameterName() {
        return parameterName;
    }

    public void setParameterName(ParameterName name) {
        this.parameterName = name;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public ValueType getType() {
        return type;
    }

    public void setType(ValueType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    public CaseProgram getProgram() {
        return program;
    }

    public void setProgram(CaseProgram program) {
        this.program = program;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof CaseParameter))
            return false;
        
        boolean bool1 = isEqual(((CaseParameter) other).parameterName, this.parameterName);
        boolean bool2 = isEqual(((CaseParameter) other).sector, this.sector);
        boolean bool3 = isEqual(((CaseParameter) other).program, this.program);
        boolean bool4 = ((CaseParameter) other).jobId == this.jobId;
        boolean bool5 = isEqual(((CaseParameter) other).region, this.region);
        
        return (bool1 && bool2 && bool3 && bool4 && bool5) || (this.id == ((CaseParameter) other).getId());
    }
    
    private boolean isEqual(Object obj, Object current) {
        if (obj == null && current != null)
            return false;
        
        if (obj != null && current == null)
            return false;
        
        if (obj == null && current == null)
            return true;
        
        if (obj.equals(current))
            return true;
        
        return false;
    }
    
//    public int compareTo(Object other) {
//        return getName().compareToIgnoreCase(((CaseParameter) other).getName());
//    }
    
    public int compareTo(CaseParameter other) {
        Float otherOrder = new Float (other.getOrder());
        Float thisOrder = new Float(getOrder());
        
        return thisOrder.compareTo(otherOrder);
    }
    

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public float getOrder() {
        return order;
    }

    public void setOrder(float order) {
        this.order = order;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public String toString() {
        return getName();
    }
    
    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public int getParentCaseId() {
        return parentCaseId;
    }

    public void setParentCaseId(int parentCaseId) {
        this.parentCaseId = parentCaseId;
    }

    public void setJobName(String name) {
        this.jobName = name;
    }
    
    public String getJobName() {
        return this.jobName;
    }

    public GeoRegion getRegion() {
        return region;
    }

    public void setRegion(GeoRegion region) {
        this.region = region;
    }

}
