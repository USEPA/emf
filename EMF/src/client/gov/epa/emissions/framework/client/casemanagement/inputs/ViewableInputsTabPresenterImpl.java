package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.editor.RelatedCasePresenter;
import gov.epa.emissions.framework.client.casemanagement.editor.RelatedCaseView;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
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

public class ViewableInputsTabPresenterImpl extends EditInputsTabPresenterImpl {
 
    private ViewableInputsTab view;
  
    public ViewableInputsTabPresenterImpl(EmfSession session, ViewableInputsTab view, Case caseObj) {
        super(session, caseObj);     
        this.view = view;
    }

    public void display() {
        view.doDisplay(this);
    }

    public void doEditInput(CaseInput input, EditCaseInputView inputEditor) throws EmfException {
        EditInputPresenter editInputPresenter = new EditCaseInputPresenterImpl(caseObj, inputEditor,
                session);
        editInputPresenter.display(input, caseObj.getModel().getId());
    }
    
    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException {
        view.clearMessage();

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }

    public CaseInput[] getCaseInput(int caseId) throws EmfException {
        return service().getCaseInputs(caseId);
    }

    private void doExport(List<CaseInput> caseInputs, boolean overwrite, String purpose) throws EmfException {
        CaseService services = session.caseService();
        Integer[] caseInputIds = new Integer[caseInputs.size()];

        for (int i = 0; i < caseInputIds.length; i++) {
            caseInputIds[i] = new Integer(caseInputs.get(i).getId());
        }

        if (overwrite)
            services.exportCaseInputsWithOverwrite(session.user(), caseInputIds, purpose);

        else
            services.exportCaseInputs(session.user(), caseInputIds, purpose);
    }

    public void exportCaseInputs(List<CaseInput> inputList, String purpose) throws EmfException {
        doExport(inputList, false, purpose);
    }

    public void exportCaseInputsWithOverwrite(List<CaseInput> inputList, String purpose) throws EmfException {
        doExport(inputList, true, purpose);
    }
    
    public Case getCaseObj() {
        return this.caseObj;
    }
    
    public int getPageSize() {
        return this.defaultPageSize;
    }

    public CaseInput[] getCaseInput(int caseId, Sector sector, String envNameContains, boolean showAll) throws EmfException {
        return service().getCaseInputs(defaultPageSize, caseId, sector, envNameContains, showAll);
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
    
    public Case[] getCasesByInputDataset(int datasetId) throws EmfException{
        return service().getCasesByInputDataset(datasetId);
    }
    
    public Case[] getCasesByOutputDatasets(int[] datasetIds) throws EmfException{
        return service().getCasesByOutputDatasets(datasetIds);
    }
    
    public void doViewRelated(RelatedCaseView view, Case[] casesByInputDataset, Case[] casesByOutputDataset) {
        RelatedCasePresenter presenter = new RelatedCasePresenter(view, session);
        presenter.doDisplay(casesByInputDataset, casesByOutputDataset);
    }

    public Object[] getAllCaseNameIDs() throws EmfException {
        return service().getAllCaseNameIDs();
    }
    
    //NOTE: used for copying into different case
    public void copyInput(int caseId, List<CaseInput> inputs) throws Exception {
        CaseInput[] inputsArray = inputs.toArray(new CaseInput[0]);
        
        for (int i = 0; i < inputs.size(); i++)
            inputsArray[i].setParentCaseId(this.caseObj.getId());
        
        service().addCaseInputs(session.user(), caseId, inputsArray);
    }
    
    public String isGeoRegionInSummary(int selectedCaseId, GeoRegion[] georegions) throws EmfException {
        return service().isGeoRegionInSummary(selectedCaseId, georegions);
    }
    
    public GeoRegion[] getGeoregion(List<CaseInput> inputs){
        
        List<GeoRegion>  regions = new ArrayList<GeoRegion>();

        for (int i = 0; i < inputs.size(); i++){
            GeoRegion region = inputs.get(i).getRegion();
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
        return new CaseInput[0];
    }

    @Override
    public void swDisplay(Object[] objs) throws EmfException {
        view.display(session, caseObj);      
    }

    @Override
    public Object[] refreshProcessData() throws EmfException {
        if (view.getSelectedSector() == null )
            return null;
        CaseInput[] freshList = getCaseInput(caseObj.getId(), view.getSelectedSector(), 
                view.nameContains(), view.isShowAll() );
        return freshList;
    }

    @Override
    public void refreshDisplay(Object[] objs) throws EmfException {
        view.refresh((CaseInput[]) objs);   
    }

}
