package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.data.GeoRegion;

public interface CaseEditorTabPresenter extends CaseViewerTabPresenter{

    void doSave() throws EmfException;
    
    Case checkIfLockedByCurrentUser() throws EmfException;
    
    String isGeoRegionInSummary(int selectedCaseId, GeoRegion[] georegions) throws EmfException;
    
}
