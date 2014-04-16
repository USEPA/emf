package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.data.GeoRegion;

import java.util.Date;

public class EditableCaseSummaryTabPresenterImpl implements EditableCaseSummaryTabPresenter {

    private EditableCaseSummaryTabView view;

    private Case caseObj;

    public EditableCaseSummaryTabPresenterImpl(Case caseObj, EditableCaseSummaryTabView view) {
        this.caseObj = caseObj;
        this.view = view;
    }

    public void doSave() throws EmfException {
        caseObj.setLastModifiedDate(new Date());
        view.save(caseObj);
    }
    
    public Case getCaseObj() {
        return this.caseObj;
    }
    
    public void checkIfLockedByCurrentUser() {
        //
    }

    public void addSector(Sector sector) {
        if (sector == null)
            return;
        
        view.addSector(sector);
    }
    
    public void addRegion(GeoRegion region) {
        if (region == null)
            return;
        
        view.addRegion(region);
    }

    public String isGeoRegionInSummary(int selectedCaseId, GeoRegion[] georegions) {
        // NOTE Auto-generated method stub
        return null;
    }

}
