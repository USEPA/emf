package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.cost.controlmeasure.ControlMeasureExportService;

public class ControlMeasureExportServiceTransport implements ControlMeasureExportService {

    private CallFactory callFactory;

    private DataMappings mappings;
    
    private EmfCall call;

    public ControlMeasureExportServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new DataMappings();
    }

    private EmfCall call() throws EmfException {
        if (call == null)
            call = callFactory.createSessionEnabledCall("ControlMeasureExportService");
        
        return call;
    }

    public synchronized void exportControlMeasures(String folderPath, String prefix, int[] controlMeasureIds, User user)
            throws EmfException {
        doExport("exportControlMeasures", folderPath, prefix, controlMeasureIds, user);
    }

    public synchronized void exportControlMeasuresWithOverwrite(String folderPath, String prefix, int[] controlMeasureIds,
            User user) throws EmfException {
        doExport("exportControlMeasuresWithOverwrite", folderPath, prefix, controlMeasureIds, user);
    }

    private synchronized void doExport(String operation, String folderPath, String prefix, int[] controlMeasureIds,
            User user) throws EmfException {
        EmfCall call = call();

        call.setOperation(operation);
        call.addParam("folderPath", mappings.string());
        call.addParam("prefix", mappings.string());
        call.addIntArrayParam();
        call.addParam("user", mappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { folderPath, prefix, controlMeasureIds, user });
    }

    public synchronized Status[] getExportStatus(User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("getExportStatus");
        call.addParam("user", mappings.user());

        call.setReturnType(mappings.statuses());

        return (Status[]) call.requestResponse(new Object[] { user });
    }
}
