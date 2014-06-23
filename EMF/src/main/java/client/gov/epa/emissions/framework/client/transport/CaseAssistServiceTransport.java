package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseAssistService;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;

public class CaseAssistServiceTransport implements CaseAssistService {

    private CallFactory callFactory;

    private CaseMappings caseMappings;

    public CaseAssistServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        caseMappings = new CaseMappings();
    }

    private EmfCall call() throws EmfException {
        return callFactory.createCall("Case Assistance Service");
    }

    public void recordJobMessages(JobMessage[] msgs, String[] keys) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("recordJobMessages");
        call.addParam("msgs", caseMappings.jobMessages());
        call.addParam("keys", caseMappings.strings());
        call.setVoidReturnType();
        call.setTimeOut(40000); //set time out in milliseconds to terminate if service doesn't response
        call.request(new Object[]{ msgs, keys });
    }

    public String printStatusCaseJobTaskManager() throws EmfException {
        EmfCall call = call();
        call.setOperation("printStatusCaseJobTaskManager");
        call.setStringReturnType();
        return (String) call.requestResponse(new Object[] { });
    }

    public void registerOutputs(CaseOutput[] outputs, String[] jobKeys) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("registerOutputs");
        call.addParam("outputs", caseMappings.caseOutputs());
        call.addParam("jobKeys", caseMappings.strings());
        call.setIntegerReturnType();
        call.setTimeOut(40000); //set time out in milliseconds to terminate if service doesn't response
        call.request(new Object[]{ outputs, jobKeys });
    }

}
