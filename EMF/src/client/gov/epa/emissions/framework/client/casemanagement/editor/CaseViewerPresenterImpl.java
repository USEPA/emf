package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.CaseManagerPresenter;
import gov.epa.emissions.framework.client.casemanagement.history.ViewableHistoryTab;
import gov.epa.emissions.framework.client.casemanagement.history.ViewableHistoryTabPresenter;
import gov.epa.emissions.framework.client.casemanagement.inputs.ViewableInputsTab;
import gov.epa.emissions.framework.client.casemanagement.inputs.ViewableInputsTabPresenterImpl;
import gov.epa.emissions.framework.client.casemanagement.jobs.ViewableJobsTab;
import gov.epa.emissions.framework.client.casemanagement.jobs.ViewableJobsTabPresenterImpl;
import gov.epa.emissions.framework.client.casemanagement.outputs.ViewableOutputsTab;
import gov.epa.emissions.framework.client.casemanagement.outputs.ViewableOutputsTabPresenterImpl;
import gov.epa.emissions.framework.client.casemanagement.parameters.ViewableParametersTab;
import gov.epa.emissions.framework.client.casemanagement.parameters.ViewableParametersTabPresenterImpl;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;

public class CaseViewerPresenterImpl implements CaseViewerPresenter {
    private CaseViewerView view;

    private EmfSession session;

    private Case caseObj;

    private ViewableInputsTabPresenterImpl inputPresenter;

    private ViewableJobsTabPresenterImpl jobsPresenter;

    private ViewableParametersTabPresenterImpl parametersPresenter;

    private ViewableOutputsTabPresenterImpl outputPresenter;

    private ViewableHistoryTabPresenter historyPresenter;

    private boolean inputsLoaded = false;

    private boolean jobsLoaded = false;

    private boolean parameterLoaded = false;

    private boolean historyLoaded = false;

    private boolean outputsLoaded = false;

    public CaseViewerPresenterImpl(Case caseObj, EmfSession session, CaseViewerView view,
            CaseManagerPresenter managerPresenter) {
        this.caseObj = caseObj;
        this.session = session;
        this.view = view;
    }
    
    public CaseViewerPresenterImpl(Case caseObj, EmfSession session, CaseViewerView view) {
        this.caseObj = caseObj;
        this.session = session;
        this.view = view;
    }

    public void doDisplay() throws EmfException {
        view.observe(this, caseObj);
        CaseService service = session.caseService();
        
        String jobSummaryMsg = service.getJobStatusMessage(caseObj.getId());
        view.display(jobSummaryMsg);
    }
    
    

    public void doClose(){
        closeView();
    }

    private void closeView() {
        view.disposeView();
    }

    public void set(ViewableInputsTab inputsView) {
        inputPresenter = new ViewableInputsTabPresenterImpl(session, inputsView, caseObj);
    }

    public void set(ViewableJobsTab jobsView) {
        jobsPresenter = new ViewableJobsTabPresenterImpl(session, jobsView, caseObj);
    }

    public void set(ViewableOutputsTab OutputsView) {
        outputPresenter = new ViewableOutputsTabPresenterImpl(session, OutputsView, caseObj);
    }

    public void set(ViewableParametersTab parameterview) {
        parametersPresenter = new ViewableParametersTabPresenterImpl(session, parameterview, caseObj);
    }

    public void set(ViewableHistoryTab caseHistoryView) {
        historyPresenter = new ViewableHistoryTabPresenter(session, caseHistoryView, caseObj);
    }

    public void doLoad(String tabTitle) {
        if (!inputsLoaded && tabTitle.equalsIgnoreCase("Inputs")) {
            inputPresenter.display();
            inputsLoaded = true;
        }

        if (!jobsLoaded && tabTitle.equalsIgnoreCase("Jobs")) {
            jobsPresenter.display();
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

    public void set(ViewableCaseSummaryTab summaryView) {
        // NOTE Auto-generated method stub
        
    }
    
    public Case getCaseFromName(String caseName) throws EmfException{
        return service().getCaseFromName(caseName);
    }
    
    private CaseService service() {
        return session.caseService();
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
