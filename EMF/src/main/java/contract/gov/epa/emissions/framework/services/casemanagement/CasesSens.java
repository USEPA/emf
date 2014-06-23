package gov.epa.emissions.framework.services.casemanagement;

import java.io.Serializable;

public class CasesSens implements Serializable {
    
    private int id;
    
    private int parentCaseid;
    
    private int sensCaseId;
    
    public CasesSens() {
        //;
    }
    
    public CasesSens(int parentCaseid, int sensCaseId) {
        this.parentCaseid = parentCaseid;
        this.sensCaseId = sensCaseId;
    }
    
    public int getParentCaseid() {
        return parentCaseid;
    }

    public void setParentCaseid(int parentCaseid) {
        this.parentCaseid = parentCaseid;
    }

    public int getSensCaseId() {
        return sensCaseId;
    }

    public void setSensCaseId(int sensCaseId) {
        this.sensCaseId = sensCaseId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
