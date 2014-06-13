package gov.epa.emissions.framework.client.casemanagement.sensitivity;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.CaseManagerPresenter;
import gov.epa.emissions.framework.client.casemanagement.inputs.InputFieldsPanelPresenter;
import gov.epa.emissions.framework.client.casemanagement.inputs.SetInputFieldsPanel;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.ModelToRun;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.data.GeoRegion;

import java.util.Date;

import javax.swing.JPanel;

public class SetCasePresenterImpl implements SetCasePresenter {
    
    private SetCaseView view;
    private CaseManagerPresenter managerPresenter;
    private EmfSession session;
    private int defaultPageSize = 20;
    
    private Case caseObj; 
    
    public SetCasePresenterImpl(Case caseObj, SetCaseView view, 
           EmfSession session, CaseManagerPresenter managerPresenter) {
        this.view = view;
        this.session = session;
        this.caseObj = caseObj;

        this.managerPresenter = managerPresenter;    }

    public void display() throws EmfException {
        view.observe(this, managerPresenter);
        //Case b4locked = service().reloadCase(caseObj.getId());
        caseObj = service().obtainLocked(session.user(), caseObj);

        if (!caseObj.isLocked(session.user())) {// view mode, locked by another user
            view.notifyLockFailure(caseObj);
            return;
        }
        view.display(caseObj); //display(caseObj, jobSummaryMsg);

//        if (b4locked.isLocked() && !b4locked.isLocked(session.user()))
//            view.showLockingMsg("Lock acquired from an expired one (by user " + b4locked.getLockOwner() + ").");
   
    }

    public void doAddInputFields(CaseInput input, JPanel container, SetInputFieldsPanel setInputFieldsPanel) throws EmfException {
        ModelToRun model = caseObj.getModel();
        InputFieldsPanelPresenter inputFieldsPresenter = new InputFieldsPanelPresenter(caseObj, setInputFieldsPanel, session);
        inputFieldsPresenter.display(input, container, (model == null ? 0 : model.getId()));
    }

    public CaseInput[] getCaseInput(int caseId, Sector sector, boolean showAll) throws EmfException {
        return service().getCaseInputs(defaultPageSize, caseId, sector, "", showAll);
    }
    
    public String validateNLInputs(int caseId) throws EmfException{
        return service().validateNLInputs(caseId);
    }
    
    public String validateNLParameters(int caseId) throws EmfException{
        return service().validateNLParameters(caseId);
    }
    
    public Case checkIfLockedByCurrentUser() throws EmfException {
        Case reloaded = service().reloadCase(caseObj.getId());

        if (reloaded.isLocked() && !reloaded.isLocked(session.user()))
            throw new EmfException("Lock on current case object expired. User " + reloaded.getLockOwner() + " has it now.");
        return reloaded;
    }

    public void doSaveParam(CaseParameter param) throws EmfException {
        session.caseService().updateCaseParameter(session.user(), param);
    }
    
    public void doSaveInput(CaseInput input) throws EmfException {
        session.caseService().updateCaseInput(session.user(), input);
    }

    public Case getCaseObj() {
        return caseObj;
    }

    public CaseParameter[] getCaseParameters(int caseId, Sector sector, boolean showAll) throws EmfException {
        //return service().getCaseParameters(defaultPageSize, caseId, sector, showAll);
        if (sector == null)
            return new CaseParameter[0];

        if (sector.compareTo(new Sector("All", "All")) == 0)
            sector = null; // to trigger select all on the server side

        //return service().getcasgetCaseParameters(caseId, sector, showAll);
        return service().getCaseParameters(defaultPageSize, caseId, sector, "", showAll);

    }
    
    public void doClose() throws EmfException {
        service().releaseLocked(session.user(), caseObj);
        closeView();
    }
    
    private void closeView() {
        view.disposeView();
    }
    
    private CaseService service() {
        return session.caseService();
    }

    public EmfSession getSession() {
        return this.session;
    }

    public String getJobName(int jobId) throws EmfException {
        if (jobId == 0)
            return "All jobs for sector";
        
        CaseJob job = session.caseService().getCaseJob(jobId);
        if (job == null)
            throw new EmfException("Cannot retrieve job (id = " + jobId + ").");
        return job.getName();
    }

    public void doSave() throws EmfException {
        checkIfLockedByCurrentUser();

        caseObj.setLastModifiedBy(session.user());
        caseObj.setLastModifiedDate(new Date());
        service().updateCaseWithLock(caseObj);
    }

    public String isGeoRegionInSummary(int selectedCaseId, GeoRegion[] georegions) {
        // NOTE Auto-generated method stub
        return null;
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
    public Object[] refreshProcessData() throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }

    @Override
    public void refreshDisplay(Object[] objs) throws EmfException {
        // NOTE Auto-generated method stub
        
    }
}
