package gov.epa.emissions.framework.client.casemanagement.parameters;

import java.util.List;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorTabPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.data.GeoRegion;

public interface EditParametersTabPresenter extends CaseEditorTabPresenter {

    void display() throws EmfException;
    
    void addNewParameterDialog(NewCaseParameterView view, CaseParameter param) throws EmfException;
    
    void addNewParameter(CaseParameter para) throws EmfException;

    void editParameter(CaseParameter para, EditCaseParameterView inputEditor) throws EmfException;

   // void doCheckDuplicate(CaseInput input, CaseInput[] existingInputs) throws EmfException;

    CaseParameter[] getCaseParameters(int caseId, Sector sector, String envNameContains, boolean showAll) throws EmfException;

    void removeParameters(CaseParameter[] params) throws EmfException;

    void copyParameter(int caseId, List<CaseParameter> params) throws Exception;
    
    void copyParameter(NewCaseParameterDialog view, CaseParameter param) throws Exception;

    public Object[] getAllCaseNameIDs() throws EmfException;
    
    GeoRegion[] getGeoregion(List<CaseParameter> parms);
}