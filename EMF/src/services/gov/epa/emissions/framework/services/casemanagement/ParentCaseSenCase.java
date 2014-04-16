package gov.epa.emissions.framework.services.casemanagement;

public class ParentCaseSenCase {
    
    private int parentCaseId;
    
    private int senCaseId;
    
    public ParentCaseSenCase(){
        super();
        // needed for hibernate mapping
    }
    
    public ParentCaseSenCase(int parentCaseId, int senCaseId) {
        this.parentCaseId = parentCaseId ;
        this.senCaseId = senCaseId ;
    }
    
    public int getParentCaseId() {
        return parentCaseId;
    }

    public void setParentCaseId(int id) {
        this.parentCaseId = id;
    }

    public int getSenCaseId() {
        return senCaseId;
    }

    public void setSenCaseId(int id) {
        this.senCaseId = id;
    }

}
