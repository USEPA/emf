package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.data.GeoRegion;

import java.util.Date;

public class CompareCasePresenter {
    private CompareCaseView view;

    private EmfSession session;

    private CaseManagerPresenter managerPresenter;
    
    private CaseObjectManager caseObjectManager = null;
    
    private int[] ids;

    public CompareCasePresenter(EmfSession session, int[] ids, CompareCaseView view, CaseManagerPresenter managerPresenter) {
        this.session = session;
        this.ids = ids;
        this.view = view;
        this.managerPresenter = managerPresenter;
        this.caseObjectManager = CaseObjectManager.getCaseObjectManager(session);
    }

    public void doDisplay() {
        view.observe(this);
        view.display();
    }

    public void doClose() {
        closeView();
    }

    private void closeView() {
        view.disposeView();
    }

    public Sector[] getAllSectors() throws EmfException {
        return caseObjectManager.getSectors();
    }
    
    public GeoRegion[] getAllRegions() throws EmfException {
        return caseObjectManager.getGeoRegions();
    }

    public CaseCategory getSelectedCategory() {
        return managerPresenter.getSelectedCategory();
    }
    
    public String showCaseQA(String gridName, Sector[] sectors, String[] repDims, 
            String whereClause, String serverDir) throws EmfException{
        //System.out.println("sector name: " + sector + " gridname: " + gridName);
        return managerPresenter.viewCaseQaReports(session.user(), ids, gridName, sectors, 
                repDims, whereClause, serverDir);
    }

}
