package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.client.casemanagement.history.ViewableHistoryTab;
import gov.epa.emissions.framework.client.casemanagement.inputs.ViewableInputsTab;
import gov.epa.emissions.framework.client.casemanagement.jobs.ViewableJobsTab;
import gov.epa.emissions.framework.client.casemanagement.outputs.ViewableOutputsTab;
import gov.epa.emissions.framework.client.casemanagement.parameters.ViewableParametersTab;
import gov.epa.emissions.framework.client.swingworker.HeavySwingWorkerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;

public interface CaseViewerPresenter{

    void doDisplay() throws EmfException;

    void doClose() throws EmfException;

    void set(ViewableCaseSummaryTab summaryView);

    void set(ViewableInputsTab inputsView) throws EmfException;

    void set(ViewableJobsTab jobsView) throws EmfException;

    void set(ViewableOutputsTab inputsView) throws EmfException;

    void set(ViewableParametersTab parameterview);

    void set(ViewableHistoryTab caseHistoryView);

    void doLoad(String tabTitle) throws EmfException;
    
    Case getCaseFromName(String caseName) throws EmfException;
    
    void doView(CaseViewerView caseView, Case caseObj) throws EmfException;
    
    Case[] getCasesThatInputToOtherCases() throws EmfException;
    
    Case[] getCasesThatOutputToOtherCases() throws EmfException;
    
    void doViewRelated(RelatedCaseView view, Case[] inputCases, Case[] outputCases);

}