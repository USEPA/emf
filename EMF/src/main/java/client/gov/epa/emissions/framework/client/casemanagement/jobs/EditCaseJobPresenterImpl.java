package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;

import javax.swing.JComponent;

public class EditCaseJobPresenterImpl implements EditJobPresenter {

    private EditCaseJobView view;
    
    private EmfSession session;
    
    private JobFieldsPanelPresenter jobFieldsPresenter;
    
    private CaseJob job;
    
    private EditJobsTabPresenter parentPresenter;
    
    private EditJobsTabView parentView;

    public EditCaseJobPresenterImpl(EditCaseJobView view, 
            EditJobsTabView parentView, EditJobsTabPresenter parentPresenter, EmfSession session) {
        this.view = view;
        this.session = session;
        this.parentPresenter = parentPresenter;
        this.parentView = parentView;
    }
    
    public EditCaseJobPresenterImpl(EditCaseJobView view, 
           EditJobsTabPresenter parentPresenter, EmfSession session) {
        this.view = view;
        this.session = session;
        this.parentPresenter = parentPresenter;
    }
    
    
    public void display(CaseJob job) throws EmfException {
        this.job = job;
        view.observe(this);
        view.display(job);
        view.populateFields();
    }
    
    public void doAddJobFields(JComponent container, 
            JobFieldsPanelView inputFields) throws EmfException {
        jobFieldsPresenter = new JobFieldsPanelPresenter(inputFields, session, parentPresenter, parentPresenter.getCaseObj());
        jobFieldsPresenter.display(job, container);
    }
    
    public void saveJob() throws EmfException {
        CaseJob savedJob = jobFieldsPresenter.doSave();
        parentView.setMessage("Saved \"" + savedJob.getName() + "\". Refresh to see the changes in the table.");
        parentPresenter.refreshJobList();
    }
    
    

}
