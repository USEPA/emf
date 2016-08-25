package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.data.Module;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.module.ModuleService;

public class ModuleServiceTransport implements ModuleService {

    private CallFactory callFactory;

    private DataMappings mappings;
    
    private EmfCall call;

    public ModuleServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new DataMappings();
    }

    private EmfCall call() throws EmfException {
        if (call == null)
            call = callFactory.createSessionEnabledCall("Module Service");
        
        return call;
    }

    public synchronized void runModules(Module[] modules, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("runModules");
        call.addParam("modules", mappings.modules());
        call.addParam("user", mappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { modules, user });
    }
}
