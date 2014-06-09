package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.data.GeoRegion;

import java.util.Date;

public class EditableCaseSummaryTabPresenterImpl extends CaseSummaryTabPresenterImpl implements EditableCaseSummaryTabPresenter {

    private EditableCaseSummaryTabView view;
    private EmfSession session;

    public EditableCaseSummaryTabPresenterImpl(EmfSession session, Case caseObj, EditableCaseSummaryTabView view) {
        super(session, caseObj);
        this.view = view;
        this.session = session;
    }

    public void doSave() throws EmfException {
        caseObj.setLastModifiedDate(new Date());
        view.save(caseObj);
    }
    
    
    public String[] isGeoRegionUsed(GeoRegion[] grids)throws EmfException {
        Integer caseId = caseObj.getId();
        if ( (caseId ==0) || (grids == null) ){
            throw new EmfException("Incorrect case Id or georegion values. ");
        }       
         return caseObjectManager.isGeoRegionUsed(caseId, grids);
    }

    @Override
    public String isGeoRegionInSummary(int selectedCaseId, GeoRegion[] georegions) throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }

    @Override
    public Object[] refreshProcessData() throws EmfException {
        refreshObjectManager(); 
        this.caseObj = checkIfLockedByCurrentUser();
        return new Case[]{this.caseObj};
    }

    @Override
    public void refreshDisplay(Object[] objs) throws EmfException {
        view.refresh(this.caseObj);
    }

    @Override
    public Object[] swProcessData() throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }

    @Override
    public void swDisplay(Object[] objs) throws EmfException {
        // NOTE Auto-generated method stub
        
    }

    @Override
    public Case checkIfLockedByCurrentUser() throws EmfException{
        Case reloaded = session.caseService().reloadCase(caseObj.getId());

        if (reloaded.isLocked() && !reloaded.isLocked(session.user()))
            throw new EmfException("Lock on current case object expired. User " + reloaded.getLockOwner()
                    + " has it now.");

        if (!reloaded.isLocked())
            reloaded = session.caseService().obtainLocked(session.user(), caseObj);

        if (reloaded == null)
            throw new EmfException("Acquire lock on case failed. Please exit editing the case.");
        return reloaded;
    }

    @Override
    public Object[] saveProcessData() throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }

    @Override
    public void saveData(Object[] objs) throws EmfException {
        // NOTE Auto-generated method stub
        
    }

}
