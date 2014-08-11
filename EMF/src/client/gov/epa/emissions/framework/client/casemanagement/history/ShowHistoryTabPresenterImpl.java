package gov.epa.emissions.framework.client.casemanagement.history;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;

public class ShowHistoryTabPresenterImpl implements ShowHistoryTabPresenter{

    protected Case caseObj;

    private ShowHistoryTabView view;

    protected EmfSession session;

    public ShowHistoryTabPresenterImpl(EmfSession session, ShowHistoryTabView view, Case caseObj) {
        this(session, caseObj);
        this.view = view;
    }
    
    public ShowHistoryTabPresenterImpl(EmfSession session, Case caseObj) {
        this.caseObj = caseObj;
        this.session = session;
    }

    public void display() {
        view.doDisplay(this, caseObj.getId());
    }

    public void doSave() {
        //doesn't need to save anything
    }

    private CaseService service() {
        return session.caseService();
    }

//    public boolean jobsUsed(CaseJob[] jobs) throws EmfException {
//        if (jobs.length == 0)
//            return false;
//        
//        int caseId = jobs[0].getCaseId();
//        CaseInput[] inputs = service().getCaseInputs(caseId);
//        CaseParameter[] params = service().getCaseParameters(caseId);
//        
//        for (int i = 0; i < jobs.length; i++) {
//            for (int j = 0; j < inputs.length; j++)
//                if (inputs[j].getCaseJobID() == jobs[i].getId())
//                    return true;
//            
//            for (int k = 0; k < params.length; k++)
//                if (params[k].getJobId() == jobs[i].getId())
//                    return true;
//        }
//        
//        return false;
//    }
//    
    public CaseJob[] getCaseJobs() throws EmfException {
        return service().getCaseJobs(caseObj.getId());
    }

    public JobMessage[] getJobMessages(int caseId, int jobId) throws EmfException {
        return service().getJobMessages(caseId, jobId);
    }

    public void doRemove(JobMessage[] msgs) throws EmfException{
        service().removeMessages(session.user(), msgs);
        
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
        return getCaseJobs();
    }

    @Override
    public void swDisplay(Object[] objs) throws EmfException {
        view.display( (CaseJob[]) objs);          
    }

    @Override
    public Object[] refreshProcessData() throws EmfException {
        Integer jobId = view.getSelectedJobId();

        if (jobId != null ){        
            return getJobMessages(caseObj.getId(), jobId);
        }
        return new JobMessage[0];
    }

    @Override
    public void refreshDisplay(Object[] objs) throws EmfException {
        view.refresh((JobMessage[]) objs); 
    }

}
