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

public class ViewableOutputsTabPresenterImpl extends EditOutputsTabPresenterImpl {

    private ViewableOutputsTab view;
      
    public ViewableOutputsTabPresenterImpl(EmfSession session, ViewableOutputsTab view, Case caseObj) {
        super(session, caseObj);
        this.view = view;
    }

    public void display() {
        view.doDisplay(this, caseObj);
    }

    public CaseOutput[] getCaseOutputs(int caseId, int jobId) throws EmfException {
        return service().getCaseOutputs(caseId, jobId);
    }

    private CaseService service() {
        return session.caseService();
    }

//    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException {
//        view.clearMessage();
//
//        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
//        presenter.doDisplay(propertiesView);
//    }
    
    public void viewOutput(CaseOutput output, EditCaseOutputView outputEditor) throws EmfException {
        EditOutputPresenter editOutputPresenter = new EditCaseOutputPresenterImpl(caseObj.getId(), outputEditor,
                session);
        editOutputPresenter.display(output);
    }
    
 
//    public void doViewRelated(RelatedCaseView view, Case[] casesByInputDataset, Case[] casesByOutputDataset) {
//        RelatedCasePresenter presenter = new RelatedCasePresenter(view, session);
//        presenter.doDisplay(casesByInputDataset, casesByOutputDataset);
//    }
    
    @Override
    public Object[] refreshProcessData() throws EmfException {
        //view.refreshJobList(getCaseJobs());
        Integer jobId = view.getSelectedJobId();
         
        if (jobId != null ){        
            return getCaseOutputs(caseObj.getId(), jobId);
        }
        return new CaseOutput[0];
    }

    @Override
    public void refreshDisplay(Object[] objs) throws EmfException {
        view.refresh((CaseOutput[]) objs);      
    }
    
    @Override
    public Object[] swProcessData() throws EmfException {
        return getCaseJobs();
    }

    @Override
    public void swDisplay(Object[] objs) throws EmfException {
        view.display( (CaseJob[]) objs);     
    }
    
}
