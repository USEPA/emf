package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.data.GeoRegion;

public interface CaseEditorTabPresenter {

    void doSave() throws EmfException;
    
    Case getCaseObj();
    
    void checkIfLockedByCurrentUser() throws EmfException;
    
    String isGeoRegionInSummary(int selectedCaseId, GeoRegion[] georegions) throws EmfException;
    
}
