package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.BasicSearchFilter;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlProgramService;
import gov.epa.emissions.framework.services.cost.ControlProgramType;

public class ControlProgramServiceTransport implements ControlProgramService {
    private CallFactory callFactory;

    private DataMappings mappings;
    
    private EmfCall call;

    public ControlProgramServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new DataMappings();
    }

    private EmfCall call() throws EmfException {
        if (call == null)
            call = callFactory.createSessionEnabledCall("ControlProgram Service");
        
        return call;
    }

    public synchronized ControlProgram[] getControlPrograms() throws EmfException {
        EmfCall call = call();

        call.setOperation("getControlPrograms");
        call.setReturnType(mappings.controlPrograms());

        return (ControlProgram[]) call.requestResponse(new Object[] {});
    }

    public synchronized ControlProgram[] getControlPrograms(BasicSearchFilter searchFilter) throws EmfException {
        EmfCall call = call();

        call.setOperation("getControlPrograms");
        call.addParam("searchFilter", mappings.basicSearchFilter());
        call.setReturnType(mappings.controlPrograms());

        return (ControlProgram[]) call.requestResponse(new Object[] { searchFilter });
    }

    public synchronized int addControlProgram(ControlProgram element) throws EmfException {
        EmfCall call = call();

        call.setOperation("addControlProgram");
        call.addParam("element", mappings.controlProgram());
        call.setIntegerReturnType();

        return (Integer) call.requestResponse(new Object[] { element });
    }

    public synchronized ControlProgram obtainLocked(User owner, int id) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainLocked");
        call.addParam("owner", mappings.user());
        call.addIntegerParam("id");
        call.setReturnType(mappings.controlProgram());

        return (ControlProgram) call.requestResponse(new Object[] { owner, Integer.valueOf(id) });

    }

    public synchronized void releaseLocked(User user, int id) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLocked");
        call.addParam("user", mappings.user());
        call.addIntegerParam("id");
        call.setReturnType(mappings.controlProgram());

        call.request(new Object[] { user, Integer.valueOf(id) });
    }

    public synchronized ControlProgram updateControlProgram(ControlProgram element) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateControlProgram");
        call.addParam("element", mappings.controlProgram());
        call.setReturnType(mappings.controlProgram());

        return (ControlProgram) call.requestResponse(new Object[] { element });
    }

    public synchronized ControlProgram updateControlProgramWithLock(ControlProgram element) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateControlProgramWithLock");
        call.addParam("element", mappings.controlProgram());
        call.setReturnType(mappings.controlProgram());

        return (ControlProgram) call.requestResponse(new Object[] { element });
    }

    public synchronized void removeControlPrograms(int[] ids, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeControlPrograms");
        call.addIntArrayParam();
        call.addParam("user", mappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { ids, user });
    }

    public synchronized ControlProgramType[] getControlProgramTypes() throws EmfException {
        EmfCall call = call();

        call.setOperation("getControlProgramTypes");
        call.setReturnType(mappings.controlProgramTypes());

        return (ControlProgramType[]) call.requestResponse(new Object[] {});
    }

    public synchronized int copyControlProgram(int id, User creator) throws EmfException {
        EmfCall call = call();

        call.setOperation("copyControlProgram");
        call.addIntegerParam("id");
        call.addParam("creator", mappings.user());
        call.setIntegerReturnType();
        return (Integer) call.requestResponse(new Object[] { Integer.valueOf(id), creator });
    }

    public ControlProgram getControlProgram(int id) throws EmfException {
        EmfCall call = call();

        call.setOperation("getControlProgram");
        call.addIntegerParam("id");
        call.setReturnType(mappings.controlProgram());

        return (ControlProgram) call.requestResponse(new Object[] { Integer.valueOf(id) });
    }
}
