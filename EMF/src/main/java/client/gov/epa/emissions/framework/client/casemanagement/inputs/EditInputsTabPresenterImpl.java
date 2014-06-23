package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.io.DeepCopy;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.editor.RelatedCasePresenter;
import gov.epa.emissions.framework.client.casemanagement.editor.RelatedCaseView;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.client.preference.DefaultUserPreferences;
import gov.epa.emissions.framework.client.preference.UserPreference;
import gov.epa.emissions.framework.client.swingworker.SwingWorkerTasks;
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

public class EditInputsTabPresenterImpl implements EditInputsTabPresenter {

    private Case caseObj;

    private EditInputsTabView view;
    
    private EmfSession session;
    
    private int defaultPageSize = 20;

    public EditInputsTabPresenterImpl(EmfSession session, EditInputsTabView view, Case caseObj) {
        this.caseObj = caseObj;
        this.view = view;
        this.session = session;
        try {
            UserPreference pref = new DefaultUserPreferences();
            defaultPageSize = Integer.parseInt(pref.sortFilterPageSize());
        } catch (Exception e) {
            //NOTE: pass silently
        }
    }

    public void display() {
        view.doDisplay(this);
    }
    
    @Override
    public Object[] swProcessData() throws EmfException {
        return new CaseInput[0];
    }

    @Override
    public void swDisplay(Object[] objs) throws EmfException {
        view.display(session, caseObj);     
    }
    

    public void doSave() {
        String caseInputDir = view.getCaseInputFileDir();
        if (caseInputDir != null)
            caseObj.setInputFileDir(caseInputDir);
        //view.refresh();
    }

    public void addNewInputDialog(NewInputView dialog, CaseInput newInput) {
        dialog.register(this);
        dialog.display(newInput.getCaseID(), newInput);
    }
    
//    public void displayCaseSelectionDialog(CaseSelectionView dialog) {
//        dialog.register(this);
//        dialog.display(newInput.getCaseID(), newInput);
//    }

    public void addNewInput(CaseInput input) throws EmfException {
        CaseInput loaded = service().addCaseInput(session.user(), input);

        if (input.getCaseID() == caseObj.getId()) {
            view.addInput(loaded);
        }
    }

    private CaseService service() {
        return session.caseService();
    }

    public void removeInputs(CaseInput[] inputs) throws EmfException {
        service().removeCaseInputs(session.user(), inputs);
    }

    public void doEditInput(CaseInput input, EditCaseInputView inputEditor) throws EmfException {
        EditInputPresenter editInputPresenter = new EditCaseInputPresenterImpl(caseObj, inputEditor, view,
                session);
        editInputPresenter.display(input, caseObj.getModel().getId());
    }
    
    public void copyInput(CaseInput input, NewInputView dialog) throws Exception {
        CaseInput newInput = (CaseInput) DeepCopy.copy(input);
        addNewInputDialog(dialog, newInput);
    }

    //NOTE: used for copying into different case
    public void copyInput(int caseId, List<CaseInput> inputs) throws Exception {
        CaseInput[] inputsArray = inputs.toArray(new CaseInput[0]);
        
        for (int i = 0; i < inputs.size(); i++)
            inputsArray[i].setParentCaseId(this.caseObj.getId());
        
        service().addCaseInputs(session.user(), caseId, inputsArray);
    }

    public void copyInput(int caseId, CaseInput input) throws Exception {
        CaseInput newInput = (CaseInput) DeepCopy.copy(input);
        newInput.setCaseID(caseId);
        addNewInput(newInput);
    }

    public void doAddInputFields(JComponent container, InputFieldsPanelView inputFields, CaseInput newInput)
            throws EmfException {
        newInput.setId(view.numberOfRecord());

        InputFieldsPanelPresenter inputFieldsPresenter = new InputFieldsPanelPresenter(caseObj, inputFields,
                session);
        inputFieldsPresenter.display(newInput, container, caseObj.getModel().getId());
    }

    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException {
        view.clearMessage();

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }

    public CaseInput[] getCaseInput(int caseId, Sector sector, String envNameContains, boolean showAll) throws EmfException {
        return service().getCaseInputs(defaultPageSize, caseId, sector, envNameContains, showAll);
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
    
    public Case reloadCaseObj() throws EmfException {
        return session.caseService().reloadCase(caseObj.getId());
    }

    public void checkIfLockedByCurrentUser() throws EmfException {
        Case reloaded = session.caseService().reloadCase(caseObj.getId());

        if (reloaded.isLocked() && !reloaded.isLocked(session.user()))
            throw new EmfException("Lock on current case object expired. User " + reloaded.getLockOwner()
                    + " has it now.");
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

    public Object[] getAllCaseNameIDs() throws EmfException {
        return service().getAllCaseNameIDs();
    }
    
    public int getPageSize() {
        return this.defaultPageSize;
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
    public void saveData(Object[] objs) throws EmfException {
        // NOTE Auto-generated method stub
        
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
