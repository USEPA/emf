package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.editor.RelatedCasePresenter;
import gov.epa.emissions.framework.client.casemanagement.editor.RelatedCaseView;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.GeoRegion;

import javax.swing.JComponent;

public class EditOutputsTabPresenterImpl implements EditOutputsTabPresenter {

    private Case caseObj;

    private EditOutputsTabView view;
    
    private EmfSession session;
    
    public EditOutputsTabPresenterImpl(EmfSession session, EditOutputsTabView view, Case caseObj) {
        this.caseObj = caseObj;
        this.view = view;
        this.session = session;
    }

    public void display() {
        view.observe(this);
        view.display();
    }

    public void doSave() throws EmfException {
        // NOTE Auto-generated method stub
        try{
            view.refresh();
        }catch (Exception e) {
            throw new EmfException("Cannot save output tab");
        }
    }

    public CaseOutput[] getCaseOutputs(int caseId, int jobId) throws EmfException {
        return service().getCaseOutputs(caseId, jobId);
    }

    private CaseService service() {
        return session.caseService();
    }

    public CaseJob[] getCaseJobs() throws EmfException {
        return service().getCaseJobs(caseObj.getId());
    }
    
    public Case getCaseObj() {
        return this.caseObj;
    }


    public void doRemove(CaseOutput[] outputs, boolean deleteDataset) throws EmfException {
       try{
        service().removeCaseOutputs(session.user(), outputs, deleteDataset);
       }catch (EmfException e) {
           throw new EmfException(e.getMessage());
       }
    }
    
    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException {
        view.clearMessage();

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }
    
    public EmfDataset getDataset(int id) throws EmfException{
        return session.dataService().getDataset(id);
        
    }

    public void editOutput(CaseOutput output, EditCaseOutputView outputEditor) throws EmfException {
        EditOutputPresenter editOutputPresenter = new EditCaseOutputPresenterImpl(caseObj.getId(), outputEditor, view,
                session);
        editOutputPresenter.display(output);
    }
    
    public void addNewOutputDialog(NewOutputView dialog, CaseOutput newOutput) {
        dialog.observe(this);
        dialog.display(caseObj.getId(), newOutput);
    }

    public void addNewOutput(CaseOutput output) throws EmfException {
        output.setCaseId(caseObj.getId());
        view.addOutput(service().addCaseOutput(session.user(), output));
    }
    
    public void doAddOutputFields(JComponent container, OutputFieldsPanelView outputFields, CaseOutput newOutput) throws EmfException {
 //       newOutput.setId(view.numberOfRecord());
        
        OutputFieldsPanelPresenter outputFieldsPresenter = new OutputFieldsPanelPresenter(caseObj.getId(), outputFields,
                session);
        outputFieldsPresenter.display(newOutput, container);
    }
    
    public void checkIfLockedByCurrentUser() throws EmfException {
        Case reloaded = session.caseService().reloadCase(caseObj.getId());

        if (reloaded.isLocked() && !reloaded.isLocked(session.user()))
            throw new EmfException("Lock on current case object expired. User " + reloaded.getLockOwner()
                    + " has it now.");
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

    public String isGeoRegionInSummary(int selectedCaseId, GeoRegion[] georegions) {
        // NOTE Auto-generated method stub
        return null;
    }
}
