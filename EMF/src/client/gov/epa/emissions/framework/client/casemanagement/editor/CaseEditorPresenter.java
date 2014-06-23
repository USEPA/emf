package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.client.casemanagement.history.ShowHistoryTabView;
import gov.epa.emissions.framework.client.casemanagement.inputs.EditInputsTabView;
import gov.epa.emissions.framework.client.casemanagement.jobs.EditJobsTabView;
import gov.epa.emissions.framework.client.casemanagement.outputs.EditOutputsTabView;
import gov.epa.emissions.framework.client.casemanagement.parameters.EditCaseParametersTabView;
import gov.epa.emissions.framework.client.swingworker.HeavySwingWorkerPresenter;
import gov.epa.emissions.framework.client.swingworker.LightSwingWorkerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;

public interface CaseEditorPresenter extends LightSwingWorkerPresenter {

    void doDisplay() throws EmfException;

    void doClose() throws EmfException;

    void doSave() throws EmfException;
    
    void doSaveWithoutClose() throws EmfException;
    
    void set(EditableCaseSummaryTabView summaryView);

    void set(EditInputsTabView inputsView) throws EmfException;

    void set(EditJobsTabView jobsView) throws EmfException;

    void set(EditOutputsTabView inputsView) throws EmfException;

    void set(EditCaseParametersTabView parameterview);

    void set(ShowHistoryTabView caseHistoryView);

    void doLoad(String tabTitle) throws EmfException;
    
    void checkIfLockedByCurrentUser() throws EmfException;
    
    Case getCaseFromName(String caseName) throws EmfException;
    
    void doView(CaseViewerView caseView, Case caseObj) throws EmfException;
    
    Case[] getCasesThatInputToOtherCases()throws EmfException;

    Case[] getCasesThatOutputToOtherCases()throws EmfException;

    void doViewRelated(RelatedCaseView view, Case[] produceInputCases, Case[] useAsOutputCases)throws EmfException;

}