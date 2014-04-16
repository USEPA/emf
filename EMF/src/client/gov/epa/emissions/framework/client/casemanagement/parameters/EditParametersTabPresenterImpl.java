package gov.epa.emissions.framework.client.casemanagement.parameters;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.io.DeepCopy;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorPresenter;
import gov.epa.emissions.framework.client.preference.DefaultUserPreferences;
import gov.epa.emissions.framework.client.preference.UserPreference;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.data.GeoRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;

public class EditParametersTabPresenterImpl implements EditParametersTabPresenter {

    private Case caseObj;

    private EditCaseParametersTabView view;

    private EmfSession session;

    private int defaultPageSize = 20;
    
    public EditParametersTabPresenterImpl(EmfSession session, EditCaseParametersTabView view, Case caseObj, CaseEditorPresenter caseEditorPresenterImpl) {
        this.caseObj = caseObj;
        this.view = view;
        this.session = session;
        try {
            UserPreference pref = new DefaultUserPreferences();
            defaultPageSize = Integer.parseInt(pref.sortFilterPageSize());
        } catch (Exception e) {
            // NOTE: pass silently
        }
    }

    public void display() {
        view.display(session, caseObj, this);
    }

    public void doSave() {
        view.refresh();
    }

    public void addNewParameterDialog(NewCaseParameterView dialog, CaseParameter newParam) {
        dialog.register(this);
        dialog.display(newParam.getCaseID(), newParam);
    }

    public void addNewParameter(CaseParameter param) throws EmfException {
        CaseParameter loaded = service().addCaseParameter(session.user(), param);

        if (param.getCaseID() == caseObj.getId()) {
            view.addParameter(loaded);
        }
    }

    private CaseService service() {
        return session.caseService();
    }

    public void refreshView() {
        view.refresh();
    }

    public void editParameter(CaseParameter param, EditCaseParameterView parameterEditor) throws EmfException {
        EditCaseParameterPresenter editParaPresenter = new EditCaseParameterPresenterImpl(caseObj,
                parameterEditor, view, session);
        editParaPresenter.display(param, caseObj.getModel().getId());
    }
    
    public void copyParameter(NewCaseParameterDialog dialog, CaseParameter param) throws Exception {
        CaseParameter newParam = (CaseParameter) DeepCopy.copy(param);
        addNewParameterDialog(dialog, newParam);
    }

    // NOTE: used for copying into different case
    public void copyParameter(int caseID, List<CaseParameter> params) throws Exception {
        CaseParameter[] paramsArray = params.toArray(new CaseParameter[0]);
        
        for (int i = 0; i < params.size(); i++)
            paramsArray[i].setParentCaseId(this.caseObj.getId());
        
        service().addCaseParameters(session.user(), caseID, paramsArray);
    }

    public void addParameterFields(CaseParameter newParameter, JComponent container,
            ParameterFieldsPanelView parameterFields) throws EmfException {
        ParameterFieldsPanelPresenter parameterFieldsPresenter = new ParameterFieldsPanelPresenter(caseObj,
                parameterFields, session);
        parameterFieldsPresenter.display(newParameter, caseObj.getModel().getId(), container);
    }

    public CaseParameter[] getCaseParameters(int caseId, Sector sector, String envNameContains, boolean showAll) throws EmfException {
        return service().getCaseParameters(defaultPageSize, caseId, sector, envNameContains, showAll);
    }

    public void removeParameters(CaseParameter[] params) throws EmfException {
        service().removeCaseParameters(session.user(), params);
    }

    public Sector[] getAllSetcors() {
        Sector total = new Sector("All", "All");
        total.setId(-1); //NOTE: to differentiate from allNull
        Sector allNull = new Sector("All Sectors", "All Sectors");
        allNull.setId(-2); //NOTE: to differentiate from total
        
        List<Sector> all = new ArrayList<Sector>();
        all.add(total);
        all.add(allNull);
        all.addAll(Arrays.asList(caseObj.getSectors()));
        
        return all.toArray(new Sector[0]);
    }

    public Case reloadCaseObj() throws EmfException {
        return session.caseService().reloadCase(caseObj.getId());
    }
    
    public Case getCaseObj() {
        return this.caseObj;
    }

    public void checkIfLockedByCurrentUser() throws EmfException {
        Case reloaded = service().reloadCase(caseObj.getId());

        if (reloaded.isLocked() && !reloaded.isLocked(session.user()))
            throw new EmfException("Lock on current case object expired. User " + reloaded.getLockOwner()
                    + " has it now.");
    }

    public Object[] getAllCaseNameIDs() throws EmfException {
        return service().getAllCaseNameIDs();
    }

    public int getPageSize() {
        return this.defaultPageSize;
    }

    public String isGeoRegionInSummary(int selectedCaseId, GeoRegion[] georegions) throws EmfException {
        return service().isGeoRegionInSummary(selectedCaseId, georegions);
    }
    
    public GeoRegion[] getGeoregion(List<CaseParameter> parms){
        
        List<GeoRegion>  regions = new ArrayList<GeoRegion>();

        for (int i = 0; i < parms.size(); i++){
            GeoRegion region = parms.get(i).getRegion();
            if (region != null && !(regions.contains(region)))
                regions.add(region);
        }
        return regions.toArray(new GeoRegion[0]);
    } 

}
