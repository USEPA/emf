package gov.epa.emissions.framework.client.casemanagement.history;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;

public class ViewableHistoryTabPresenterImpl extends ShowHistoryTabPresenterImpl{

    private ViewableHistoryTab view;

    public ViewableHistoryTabPresenterImpl(EmfSession session, ViewableHistoryTab view, Case caseObj) {
        super(session, caseObj);       
        this.view = view;        
    }

    public void display() {
        view.doDisplay(this, caseObj.getId());
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
