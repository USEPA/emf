package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorTabPresenter;
import gov.epa.emissions.framework.client.casemanagement.editor.RelatedCaseView;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface EditOutputsTabPresenter extends CaseEditorTabPresenter {

    void display() throws EmfException;
    
    CaseOutput[] getCaseOutputs(int caseId, int jobId) throws EmfException;
    

    CaseJob[] getCaseJobs()throws EmfException;
    void doRemove(CaseOutput[] outputs, boolean deleteDataset) throws EmfException;

    void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException;
    
    EmfDataset getDataset(int id) throws EmfException;
    
    void addNewOutputDialog(NewOutputView view, CaseOutput output) throws EmfException;

    void editOutput(CaseOutput output, EditCaseOutputView outputEditor) throws EmfException;
    
    Case[] getCasesByInputDataset(int datasetId) throws EmfException;
    
    Case[] getCasesByOutputDatasets(int[] datasetIds) throws EmfException;
    
    void doViewRelated(RelatedCaseView view, Case[] casesByInputDataset, Case[] casesByOutputDataset);
}   
