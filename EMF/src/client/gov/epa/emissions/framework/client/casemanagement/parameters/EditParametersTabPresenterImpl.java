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

    protected Case caseObj;

    private EditCaseParametersTabView view;

    protected EmfSession session;

    protected int defaultPageSize = 20;
    
    public EditParametersTabPresenterImpl(EmfSession session, Case caseObj){
        this.caseObj = caseObj;
        this.session = session;
    }
    
    public EditParametersTabPresenterImpl(EmfSession session, EditCaseParametersTabView view, Case caseObj, CaseEditorPresenter caseEditorPresenterImpl) {
        this(session, caseObj);
        this.view = view;
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
        //view.refresh();
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

    protected CaseService service() {
        return session.caseService();
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

    @Override
    public Object[] saveProcessData() throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }

    @Override
    public void saveDisplay(Object[] objs) throws EmfException {
        // NOTE Auto-generated method stub
        
    }

    @Override
    public Object[] swProcessData() throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }

    @Override
    public void swDisplay(Object[] objs) throws EmfException {
        // NOTE Auto-generated method stub
        
    }

    @Override
    public Object[] refreshProcessData() throws EmfException {
        if ( view.getSelectedSector() == null )
            return new CaseParameter[0];
        CaseParameter[] freshList = getCaseParameters(caseObj.getId(), view.getSelectedSector(), 
                view.nameContains(), view.isShowAll());

        return freshList;
    }

    @Override
    public void refreshDisplay(Object[] objs) throws EmfException {
        view.refresh((CaseParameter[]) objs);        
    } 

}
