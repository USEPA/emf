package gov.epa.emissions.framework.client.casemanagement.parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.data.GeoRegion;

import javax.swing.JComponent;

public class ViewableParametersTabPresenterImpl extends EditParametersTabPresenterImpl{

    private ViewableParametersTab view;
    
    public ViewableParametersTabPresenterImpl(EmfSession session, ViewableParametersTab view, Case caseObj) {
        super(session, caseObj);       
        this.view = view;
    }

    public void display() {
        view.display(session, caseObj, this);
    }


//    public Case getCaseObj() {
//        return this.caseObj;
//    }
//    
//    public Object[] getAllCaseNameIDs() throws EmfException {
//        return service().getAllCaseNameIDs();
//    }
//    
//    public String isGeoRegionInSummary(int selectedCaseId, GeoRegion[] georegions) throws EmfException {
//        return service().isGeoRegionInSummary(selectedCaseId, georegions);
//    }
//    

    public Object[] refreshProcessData() throws EmfException {
        if ( view.getSelectedSector() == null )
            return null;
        CaseParameter[] freshList = getCaseParameters(caseObj.getId(), view.getSelectedSector(), 
                view.nameContains(), view.isShowAll());

        return freshList;
    }

    @Override
    public void refreshDisplay(Object[] objs) throws EmfException {
        view.refresh((CaseParameter[]) objs);        
    } 


}
