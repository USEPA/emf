package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.editor.RelatedCasePresenter;
import gov.epa.emissions.framework.client.casemanagement.editor.RelatedCaseView;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.client.swingworker.LightSwingWorkerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.GeoRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;

public interface ViewableInputsTabPresenter extends LightSwingWorkerPresenter {
 
    void display();
    void doEditInput(CaseInput input, EditCaseInputView inputEditor) throws EmfException;
    void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException;
    CaseInput[] getCaseInput(int caseId) throws EmfException;
    
    void exportCaseInputs(List<CaseInput> inputList, String purpose) throws EmfException;
    //void doExport(List<CaseInput> caseInputs, boolean overwrite, String purpose) throws EmfException;  
    void exportCaseInputsWithOverwrite(List<CaseInput> inputList, String purpose) throws EmfException;
    Case getCaseObj();
    int getPageSize();

    CaseInput[] getCaseInput(int caseId, Sector sector, String envNameContains, boolean showAll) throws EmfException;
    Sector[] getAllSetcors();
    Case[] getCasesByInputDataset(int datasetId) throws EmfException;
    
    Case[] getCasesByOutputDatasets(int[] datasetIds) throws EmfException;   
    void doViewRelated(RelatedCaseView view, Case[] casesByInputDataset, Case[] casesByOutputDataset);

    Object[] getAllCaseNameIDs() throws EmfException;
    
    //NOTE: used for copying into different case
    void copyInput(int caseId, List<CaseInput> inputs) throws Exception;
    
    String isGeoRegionInSummary(int selectedCaseId, GeoRegion[] georegions) throws EmfException;
    
    GeoRegion[] getGeoregion(List<CaseInput> inputs);
    
}
