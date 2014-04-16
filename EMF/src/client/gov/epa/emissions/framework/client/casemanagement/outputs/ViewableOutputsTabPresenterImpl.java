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

import javax.swing.JComponent;

public class ViewableOutputsTabPresenterImpl{

    private Case caseObj;

    private ViewableOutputsTab view;
    
    private EmfSession session;
    
    public ViewableOutputsTabPresenterImpl(EmfSession session, ViewableOutputsTab view, Case caseObj) {
        this.caseObj = caseObj;
        this.view = view;
        this.session = session;
    }

    public void display() {
        view.observe(this);
        view.display();
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


    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException {
        view.clearMessage();

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }
    
    public EmfDataset getDataset(int id) throws EmfException{
        return session.dataService().getDataset(id);
        
    }

    public void viewOutput(CaseOutput output, EditCaseOutputView outputEditor) throws EmfException {
        EditOutputPresenter editOutputPresenter = new EditCaseOutputPresenterImpl(caseObj.getId(), outputEditor,
                session);
        editOutputPresenter.display(output);
    }
    
    public void doAddOutputFields(JComponent container, OutputFieldsPanelView outputFields, CaseOutput newOutput) throws EmfException {
 //       newOutput.setId(view.numberOfRecord());
        
        OutputFieldsPanelPresenter outputFieldsPresenter = new OutputFieldsPanelPresenter(caseObj.getId(), outputFields,
                session);
        outputFieldsPresenter.display(newOutput, container);
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
}
