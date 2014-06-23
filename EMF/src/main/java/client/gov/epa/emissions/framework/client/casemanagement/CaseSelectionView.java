package gov.epa.emissions.framework.client.casemanagement;

public interface CaseSelectionView {
    
    void display(String title, boolean selectSingle);
    boolean shouldCopy();
    
}
