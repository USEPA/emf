package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.GeoRegion;

import java.io.Serializable;
import java.util.Date;

public class CaseInput implements Serializable, Comparable {
    
    private int id;

    private int caseID;
    
    private int parentCaseId;
    
    private String parentCase;

    private int caseJobID;
    
    private String jobName;
    
    private InputName inputName;
    
    private Sector sector;
    
    private GeoRegion region;
    
    private CaseProgram program;
    
    private InputEnvtVar envtVars;
    
    private EmfDataset dataset;
    
    private Version version;
    
    private DatasetType datasetType;
    
    private boolean required;
    
    private boolean local;
    
    private String subdir;
    
    private SubDir subdirObj;
    
    private Date lastModifiedDate;

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public CaseInput() {
        super();
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof CaseInput))
            return false;
        
        boolean bool1 = evalToEqual(((CaseInput) other).inputName, this.inputName);
        boolean bool2 = evalToEqual(((CaseInput) other).sector, this.sector);
        boolean bool3 = evalToEqual(((CaseInput) other).program, this.program);
        boolean bool4 = evalToEqual(((CaseInput) other).caseJobID, this.caseJobID);
        boolean bool5 = evalToEqual(((CaseInput) other).region, this.region);
        return (bool1 && bool2 && bool3 && bool4 && bool5) || (this.id == ((CaseInput) other).getId());
    }
    
    private boolean evalToEqual(Object obj, Object current) {
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

    public int hashCode() {
        return getName().hashCode() + sector.hashCode() + program.hashCode();
    }

    public String toString() {
        return getName();
    }

    public int compareTo(Object other) {
        return getName().compareToIgnoreCase(((CaseInput) other).getName());
    }

    public EmfDataset getDataset() {
        return dataset;
    }

    public void setDataset(EmfDataset dataset) {
        this.dataset = dataset;
    }

    public DatasetType getDatasetType() {
        return datasetType;
    }

    public void setDatasetType(DatasetType datasetType) {
        this.datasetType = datasetType;
    }

    public InputEnvtVar getEnvtVars() {
        return envtVars;
    }

    public void setEnvtVars(InputEnvtVar envtVars) {
        this.envtVars = envtVars;
    }

    public CaseProgram getProgram() {
        return program;
    }

    public void setProgram(CaseProgram program) {
        this.program = program;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public String getSubdir() {
        return subdir;
    }

    public void setSubdir(String subdir) {
        this.subdir = subdir;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public InputName getInputName() {
        return inputName;
    }

    public void setInputName(InputName inputName) {
        this.inputName = inputName;
    }
    
    public int getModelToRunId(){
        return inputName.getModelToRunId();
    }

    public String getName() {
        return inputName == null ? null : inputName.getName();
    }

    public SubDir getSubdirObj() {
        return subdirObj;
    }

    public void setSubdirObj(SubDir subdirObj) {
        this.subdirObj = subdirObj;
    }

    public int getCaseID() {
        return caseID;
    }

    public void setCaseID(int caseID) {
        this.caseID = caseID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCaseJobID() {
        return caseJobID;
    }

    public void setCaseJobID(int caseJobID) {
        this.caseJobID = caseJobID;
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

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setParentCase(String parent) {
        this.parentCase = parent;
    }
    
    public String getParentCase() {
        return this.parentCase;
    }

    public GeoRegion getRegion() {
        return region;
    }

    public void setRegion(GeoRegion region) {
        this.region = region;
    }

}
