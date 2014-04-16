package gov.epa.emissions.framework.client.casemanagement.parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.data.GeoRegion;

import javax.swing.JComponent;

public class ViewableParametersTabPresenterImpl {

    private Case caseObj;

    private ViewableParametersTab view;
    
    private int defaultPageSize = 20;

    private EmfSession session;

    public ViewableParametersTabPresenterImpl(EmfSession session, ViewableParametersTab view, Case caseObj) {
        this.caseObj = caseObj;
        this.view = view;
        this.session = session;
    }

    public void display() {
        view.display(session, caseObj, this);
    }

    // NOTE: used for copying into different case
    public void copyParameter(int caseID, List<CaseParameter> params) throws Exception {
        CaseParameter[] paramsArray = params.toArray(new CaseParameter[0]);
        
        for (int i = 0; i < params.size(); i++)
            paramsArray[i].setParentCaseId(this.caseObj.getId());
        
        service().addCaseParameters(session.user(), caseID, paramsArray);
    }

    private CaseService service() {
        return session.caseService();
    }
    
    public void editParameter(CaseParameter param, EditCaseParameterView parameterEditor) throws EmfException {
        EditCaseParameterPresenter editParaPresenter = new EditCaseParameterPresenterImpl(caseObj, parameterEditor, session);
        editParaPresenter.display(param, caseObj.getModel().getId());
    }

    public void addParameterFields(CaseParameter newParameter, JComponent container, ParameterFieldsPanelView parameterFields) throws EmfException {
        ParameterFieldsPanelPresenter parameterFieldsPresenter = new ParameterFieldsPanelPresenter(caseObj, parameterFields, session);
        parameterFieldsPresenter.display(newParameter,caseObj.getModel().getId(), container);
    }

    public CaseParameter[] getCaseParameters(int caseId) throws EmfException {
        return service().getCaseParameters(caseId);
    }
    
    public CaseParameter[] getCaseParameters(int caseId, Sector sector, String envNameContains, boolean showAll) throws EmfException {
        return service().getCaseParameters(defaultPageSize, caseId, sector, envNameContains, showAll);
    }

    public Sector[] getAllSetcors() {
        Sector total = new Sector("All", "All");
        total.setId(-1); //NOTE: to differentiate from allNull
        Sector allNull = new Sector("All Sectors", "All Sectors");
        allNull.setId(-2); //NOTE: to differentiate from total
        
        List<Sector> all = new ArrayList<Sector>();
        all.add(total);
        all.add(allNull);
        all.addAll(Arrays.asList(this.caseObj.getSectors()));

        return all.toArray(new Sector[0]);
    }
    
    public int getPageSize() {
        return this.defaultPageSize;
    }

    public Case getCaseObj() {
        return this.caseObj;
    }
    
    public Object[] getAllCaseNameIDs() throws EmfException {
        return service().getAllCaseNameIDs();
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
