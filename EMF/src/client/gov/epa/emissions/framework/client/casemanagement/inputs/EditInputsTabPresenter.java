package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorTabPresenter;
import gov.epa.emissions.framework.client.casemanagement.editor.RelatedCaseView;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.swingworker.LightSwingWorkerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.GeoRegion;

import java.util.List;

public interface EditInputsTabPresenter extends CaseEditorTabPresenter{

    void display() throws EmfException;
    
    void addNewInputDialog(NewInputView view, CaseInput input) throws EmfException;
    
    void addNewInput(CaseInput input) throws EmfException;

    void doEditInput(CaseInput input, EditCaseInputView inputEditor) throws EmfException;
    
    Sector[] getAllSetcors();
    
    Case[] getCasesByInputDataset(int datasetId) throws EmfException;
    
    void doViewRelated(RelatedCaseView view, Case[] casesByInputDataset, Case[] casesByOutputDataset);
    
    Case[] getCasesByOutputDatasets(int[] datasetIds) throws EmfException;

   // void doCheckDuplicate(CaseInput input, CaseInput[] existingInputs) throws EmfException;

    void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException;
    
//    void doExportWithOverwrite(EmfDataset[] datasets, Version[] versions, String[] folders, String purpose) throws EmfException;
//
//    void doExport(EmfDataset[] datasets, Version[] versions, String[] folders, String purpose) throws EmfException;

    void exportCaseInputs(List<CaseInput> inputList, String purpose) throws EmfException;
    void exportCaseInputsWithOverwrite(List<CaseInput> inputList, String purpose) throws EmfException;
    
    void removeInputs(CaseInput[] inputs) throws EmfException;
    
    CaseInput[] getCaseInput(int caseId, Sector sector, String nameContains, boolean showAll) throws EmfException;

    void copyInput(CaseInput input, NewInputView dialog) throws Exception;
    
    void copyInput(int caseId, List<CaseInput> inputs) throws Exception;
    
    public Object[] getAllCaseNameIDs() throws EmfException;
    
    GeoRegion[] getGeoregion(List<CaseInput> inputs); 
}