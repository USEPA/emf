package gov.epa.emissions.framework.client.casemanagement.sensitivity;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorTabPresenter;
import gov.epa.emissions.framework.client.casemanagement.inputs.SetInputFieldsPanel;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;

import javax.swing.JPanel;

public interface SetCasePresenter extends CaseEditorTabPresenter {

    void display() throws EmfException;
    
    void doClose() throws EmfException;
    
    void doSaveInput(CaseInput input) throws EmfException;
    
    void doSaveParam(CaseParameter param) throws EmfException;
    
    CaseInput[] getCaseInput(int caseId, Sector sector, boolean showAll) throws EmfException;
    
    String validateNLInputs(int caseId) throws EmfException;
    
    String validateNLParameters(int caseId) throws EmfException;
    
    CaseParameter[] getCaseParameters(int caseId, Sector sector, boolean showAll) throws EmfException; 
   
    EmfSession getSession();

    void doAddInputFields(CaseInput input, JPanel container, SetInputFieldsPanel setInputFieldsPanel) throws EmfException;

    String getJobName(int jobId) throws EmfException;
    
}