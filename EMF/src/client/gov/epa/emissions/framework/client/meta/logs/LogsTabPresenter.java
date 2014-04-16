package gov.epa.emissions.framework.client.meta.logs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.AccessLog;
import gov.epa.emissions.framework.services.basic.LoggingService;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class LogsTabPresenter {

    private LogsTabView view;

    //private LoggingService service;

    private EmfDataset dataset;
    
    private EmfSession session; 

    public LogsTabPresenter(LogsTabView view, EmfDataset dataset, EmfSession session) {
        this.view = view;
        this.dataset = dataset;
        this.session = session;
    }

    public void display() throws EmfException {
        view.observe(this);
        view.display(getLogs());
    }
    
    public AccessLog[] getLogs() throws EmfException{
        return service().getAccessLogs(dataset.getId());
    }

    public LoggingService service(){
        return session.loggingService();
    }
    public void doSave() {
        // No Op
    }
    
    public void checkIfLockedByCurrentUser() throws EmfException{
        EmfDataset reloaded = session.dataService().getDataset(dataset.getId());
        if (!reloaded.isLocked())
            throw new EmfException("Lock on current dataset object expired. " );  
        if (!reloaded.isLocked(session.user()))
            throw new EmfException("Lock on current dataset object expired. User " + reloaded.getLockOwner()
                    + " has it now.");    
    }

}
