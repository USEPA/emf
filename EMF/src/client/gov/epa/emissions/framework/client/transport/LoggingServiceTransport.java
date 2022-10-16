package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.AccessLog;
import gov.epa.emissions.framework.services.basic.LoggingService;

public class LoggingServiceTransport implements LoggingService {

    private CallFactory callFactory;

    private DataMappings mappings;
    
    private EmfCall call;

    public LoggingServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new DataMappings();
    }

    private EmfCall call() throws EmfException {
        if (call == null)
            call = callFactory.createSessionEnabledCall("User Service");
        
        return call;
    }

    public synchronized AccessLog[] getAccessLogs(int datasetid) throws EmfException {
        EmfCall call = call();

        call.setOperation("getAccessLogs");
        call.addIntegerParam("datasetId");
        call.setReturnType(mappings.logs());

        return (AccessLog[]) call.requestResponse(new Object[] { Integer.valueOf(datasetid) });
    }

    public synchronized String getLastExportedFileName(int datasetId) throws EmfException {
        EmfCall call = call();
       
        call.setOperation("getLastExportedFileName");
        call.addIntegerParam("datasetId");
        call.setReturnType(mappings.string());
        return (String) call.requestResponse(new Object[]{Integer.valueOf(datasetId)});
    }

}
