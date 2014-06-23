package gov.epa.emissions.framework.services.casemanagement.outputs;

import java.io.Serializable;
import java.util.Date;

public class QueueCaseOutput implements Serializable, Comparable<QueueCaseOutput> {

    private int id;
    
    private int caseId;
    
    private int jobId;
    
    private String name = ""; // make sure it's empty, not null
    
    private String datasetFile;
    
    private String path;
    
    private String pattern;
    
    private String datasetType;
    
    private String datasetName;
    
    private String message;
    
    private String execName;
    
    private String remoteUser;
    
    private String status;
    
    private Date createDate;
    
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public QueueCaseOutput() {
        //
    }

    public QueueCaseOutput(String name) {
        this.name = name;
    }
    
    public boolean equals(Object obj)
    {
        // overriding equals 
        
        // normal tests for equal
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        
        // additional tests specific to outputs
        QueueCaseOutput localOther = (QueueCaseOutput)obj;
        if (!this.name.equalsIgnoreCase(localOther.getName()))
            return false;   
        if (!this.datasetFile.equalsIgnoreCase(localOther.datasetFile))
            return false;    
        if (!this.path.equalsIgnoreCase(localOther.path))
            return false;
        if (!this.pattern.equalsIgnoreCase(localOther.pattern))
            return false;
        if (!this.datasetName.equalsIgnoreCase(localOther.datasetName))
            return false;
        if (!this.datasetType.equals(localOther.datasetType))
            return false;
       
        // if got to hear return true
        return true;
    }

    public int compareTo(QueueCaseOutput other) {
        return this.name.compareToIgnoreCase(other.getName());
    }

    public String getDatasetFile() {
        return datasetFile;
    }

    public void setDatasetFile(String datasetFile) {
        this.datasetFile = datasetFile;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public String getDatasetType() {
        return datasetType;
    }

    public void setDatasetType(String datasetType) {
        this.datasetType = datasetType;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public int getCaseId() {
        return caseId;
    }

    public void setCaseId(int caseId) {
        this.caseId = caseId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }
    
    public String getExecName() {
        return execName;
    }

    public void setExecName(String execName) {
        this.execName = execName;
    }
    
    public String getRemoteUser() {
        return remoteUser;
    }

    public void setRemoteUser(String remoteUser) {
        this.remoteUser = remoteUser;
    }
    
    public void poputlate(CaseOutput output) {
        this.createDate = new Date();
        this.caseId = output.getCaseId();
        this.jobId = output.getJobId();
        this.path = output.getPath();
        this.pattern = output.getPattern();
        this.datasetFile = output.getDatasetFile();
        this.datasetName = output.getDatasetName();
        this.datasetType = output.getDatasetType();
        this.execName = output.getExecName();
        this.message = output.getMessage();
        this.name = output.getName();
        this.remoteUser = output.getRemoteUser();
        this.status = output.getStatus();
    }
    
    public CaseOutput convert2CaseOutput() {
        CaseOutput newOutput = new CaseOutput(this.name);
        newOutput.setCaseId(this.caseId);
        newOutput.setJobId(this.jobId);
        newOutput.setPath(this.path);
        newOutput.setPattern(this.pattern);
        newOutput.setDatasetFile(this.datasetFile);
        newOutput.setDatasetName(this.datasetName);
        newOutput.setDatasetType(this.datasetType);
        newOutput.setExecName(this.execName);
        newOutput.setMessage(this.message);
        newOutput.setRemoteUser(this.remoteUser);
        newOutput.setStatus(this.status);
        
        return newOutput;
    }
    
    public boolean isEmpty() {
        boolean result = true;
        String[] fields = new String[] {
                datasetType,
                datasetName,
                path,
                datasetFile,
                pattern,
                name
        };
        
        for (String field : fields) {
            if (field != null && !field.trim().isEmpty()) {
                result = false;
                break;
            }
        }
        return result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
