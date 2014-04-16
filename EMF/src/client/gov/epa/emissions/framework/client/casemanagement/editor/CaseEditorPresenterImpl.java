package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.CaseManagerPresenter;
import gov.epa.emissions.framework.client.casemanagement.history.ShowHistoryTabPresenter;
import gov.epa.emissions.framework.client.casemanagement.history.ShowHistoryTabView;
import gov.epa.emissions.framework.client.casemanagement.inputs.EditInputsTabPresenter;
import gov.epa.emissions.framework.client.casemanagement.inputs.EditInputsTabPresenterImpl;
import gov.epa.emissions.framework.client.casemanagement.inputs.EditInputsTabView;
import gov.epa.emissions.framework.client.casemanagement.jobs.EditJobsTabPresenter;
import gov.epa.emissions.framework.client.casemanagement.jobs.EditJobsTabPresenterImpl;
import gov.epa.emissions.framework.client.casemanagement.jobs.EditJobsTabView;
import gov.epa.emissions.framework.client.casemanagement.outputs.EditOutputsTabPresenter;
import gov.epa.emissions.framework.client.casemanagement.outputs.EditOutputsTabPresenterImpl;
import gov.epa.emissions.framework.client.casemanagement.outputs.EditOutputsTabView;
import gov.epa.emissions.framework.client.casemanagement.parameters.EditCaseParametersTabView;
import gov.epa.emissions.framework.client.casemanagement.parameters.EditParametersTabPresenter;
import gov.epa.emissions.framework.client.casemanagement.parameters.EditParametersTabPresenterImpl;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class CaseEditorPresenterImpl implements CaseEditorPresenter {
    private CaseEditorView view;

    private EmfSession session;

    //private CaseManagerPresenter managerPresenter;

    private Case caseObj;

    private List<CaseEditorTabPresenter> presenters;

    private EditInputsTabPresenter inputPresenter;

    private EditJobsTabPresenter jobsPresenter;

    private EditParametersTabPresenter parametersPresenter;

    private EditOutputsTabPresenter outputPresenter;

    private EditableCaseSummaryTabPresenter summaryPresenter;

    private ShowHistoryTabPresenter historyPresenter;

    private boolean inputsLoaded = false;

    private boolean jobsLoaded = false;

    private boolean parameterLoaded = false;

    private boolean historyLoaded = false;

    private boolean outputsLoaded = false;
    
    public CaseEditorPresenterImpl(Case caseObj, EmfSession session, CaseEditorView view,
            CaseManagerPresenter managerPresenter) {
        this.caseObj = caseObj;
        this.session = session;
        this.view = view;
        //this.managerPresenter = managerPresenter;
        presenters = new ArrayList<CaseEditorTabPresenter>();
    }

    public void doDisplay() throws EmfException {
        view.observe(this);

        Case b4locked = service().reloadCase(caseObj.getId());
        caseObj = service().obtainLocked(session.user(), caseObj);

        if (!caseObj.isLocked(session.user())) {// view mode, locked by another user
            view.notifyLockFailure(caseObj);
            return;
        }
        
        String jobSummaryMsg = service().getJobStatusMessage(caseObj.getId());
        view.display(caseObj, jobSummaryMsg);

        if (b4locked.isLocked() && !b4locked.isLocked(session.user()))
            view.showLockingMsg("Lock acquired from an expired one (by user " + b4locked.getLockOwner() + ").");
    }

    public void doClose() throws EmfException {
        service().releaseLocked(session.user(), caseObj);
        closeView();
    }

    private void closeView() {
        view.disposeView();
    }

    public void doSave() throws EmfException {
        updateCase();
    }

    void updateCase() throws EmfException {
        checkIfLockedByCurrentUser();
        saveTabs();

        if (isDuplicate(caseObj))
            throw new EmfException("Duplicate name - '" + caseObj.getName() + "'.");

        caseObj.setLastModifiedBy(session.user());
        caseObj.setLastModifiedDate(new Date());
        service().updateCaseWithLock(caseObj);
    }

    private void saveTabs() throws EmfException {
        for (Iterator<CaseEditorTabPresenter> iter = presenters.iterator(); iter.hasNext();) {
            CaseEditorTabPresenter element = iter.next();
            element.doSave();
        }
    }

    private boolean isDuplicate(Case newCase) throws EmfException {
        Case[] cases = service().getCases();
        for (int i = 0; i < cases.length; i++) {
            if (cases[i].getName().equals(newCase.getName()) && cases[i].getId() != newCase.getId())
                return true;
        }

        return false;
    }

    public void checkIfLockedByCurrentUser() throws EmfException {
        Case reloaded = service().reloadCase(caseObj.getId());
        
        if (reloaded.isLocked() && !reloaded.isLocked(session.user()))
            throw new EmfException("Lock on current case object expired. User " + reloaded.getLockOwner() + " has it now.");
    }

    private CaseService service() {
        return session.caseService();
    }

    public void set(EditableCaseSummaryTabView summaryView) {
        summaryPresenter = new EditableCaseSummaryTabPresenterImpl(caseObj, summaryView);
        presenters.add(summaryPresenter);
    }

    public void set(EditInputsTabView inputsView) {
        inputPresenter = new EditInputsTabPresenterImpl(session, inputsView, caseObj);
        presenters.add(inputPresenter);
    }

    public void set(EditJobsTabView jobsView) {
        jobsPresenter = new EditJobsTabPresenterImpl(session, jobsView, caseObj);
        presenters.add(jobsPresenter);
    }

    public void set(EditOutputsTabView OutputsView) {
        outputPresenter = new EditOutputsTabPresenterImpl(session, OutputsView, caseObj);
        presenters.add(outputPresenter);
    }

    public void doSaveWithoutClose() throws EmfException {
        updateCase();
        //managerPresenter.doRefresh();

        caseObj = service().obtainLocked(session.user(), caseObj); // get lock after release it
        if (!caseObj.isLocked(session.user())) {// view mode, locked by another user
            closeView();
            return;
        }
    }

    public void set(EditCaseParametersTabView parameterview) {
        parametersPresenter = new EditParametersTabPresenterImpl(session, parameterview, caseObj, this);
        presenters.add(parametersPresenter);
    }

    public void set(ShowHistoryTabView caseHistoryView) {
        historyPresenter = new ShowHistoryTabPresenter(session, caseHistoryView, caseObj);
    }

    public void doLoad(String tabTitle) throws EmfException {
        if (!inputsLoaded && tabTitle.equalsIgnoreCase("Inputs")) {
            inputPresenter.display();
            inputsLoaded = true;
        }

        if (!jobsLoaded && tabTitle.equalsIgnoreCase("Jobs")) {
            jobsPresenter.display(this);
            jobsLoaded = true;
        }

        if (!parameterLoaded && tabTitle.equalsIgnoreCase("Parameters")) {
            parametersPresenter.display();
            parameterLoaded = true;
        }

        if (!historyLoaded && tabTitle.equalsIgnoreCase("History")) {
            historyPresenter.display();
            historyLoaded = true;
        }

        if (!outputsLoaded && tabTitle.equalsIgnoreCase("Outputs")) {
            outputPresenter.display();
            outputsLoaded = true;
        }
    }
    
    public Case getCaseFromName(String caseName) throws EmfException{
        return service().getCaseFromName(caseName);
    }
    
    public void doView(CaseViewerView caseView, Case caseObj) throws EmfException {
        CaseViewerPresenter presenter = new CaseViewerPresenterImpl(caseObj, session, caseView);
        presenter.doDisplay();
    }
    
    public Case[] getCasesThatInputToOtherCases() throws EmfException{
        return service().getCasesThatInputToOtherCases(caseObj.getId());
    }
    
    public Case[] getCasesThatOutputToOtherCases() throws EmfException{
        return service().getCasesThatOutputToOtherCases(caseObj.getId());
    }

    public void doViewRelated(RelatedCaseView view, Case[] inputCases, Case[] outputCases) {
        RelatedCasePresenter presenter = new RelatedCasePresenter(view, session);
        presenter.doDisplay(inputCases, outputCases);
    }
    
}
