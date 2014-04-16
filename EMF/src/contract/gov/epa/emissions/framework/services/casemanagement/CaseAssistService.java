package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.framework.services.EMFService;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;

public interface CaseAssistService extends EMFService {
    // For command line client uses
    void recordJobMessages(JobMessage[] msgs, String[] keys) throws EmfException;
    
    void registerOutputs(CaseOutput[] outputs, String[] jobKeys) throws EmfException;
    
    String printStatusCaseJobTaskManager() throws EmfException;

 }
