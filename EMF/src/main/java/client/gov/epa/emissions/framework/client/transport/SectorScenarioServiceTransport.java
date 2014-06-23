package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.sms.SectorScenarioService;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.sms.SectorScenario;
import gov.epa.emissions.framework.services.sms.SectorScenarioOutput;

import java.util.Date;
import java.util.List;

public class SectorScenarioServiceTransport implements SectorScenarioService {
    private CallFactory callFactory;

    private DataMappings mappings;
    
    private EmfCall call;

    public SectorScenarioServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new DataMappings();
    }

    private EmfCall call() throws EmfException {
        if (call == null)
            call = callFactory.createSessionEnabledCall("Sector Scenario Service");
        
        return call;
    }

    public synchronized SectorScenario[] getSectorScenarios() throws EmfException {
        EmfCall call = call();

        call.setOperation("getSectorScenarios");
        call.setReturnType(mappings.sectorScenarios());

        return (SectorScenario[]) call.requestResponse(new Object[] {});
    }

    public synchronized int addSectorScenario(SectorScenario element) throws EmfException {
        EmfCall call = call();

        call.setOperation("addSectorScenario");
        call.addParam("element", mappings.sectorScenario());
        call.setIntegerReturnType();

        return (Integer) call.requestResponse(new Object[] { element });
    }

    public synchronized SectorScenario obtainLocked(User owner, int id) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainLocked");
        call.addParam("owner", mappings.user());
        call.addIntegerParam("id");
        call.setReturnType(mappings.sectorScenario());

        return (SectorScenario) call.requestResponse(new Object[] { owner, new Integer(id) });

    }

//    public void releaseLocked(SectorScenario locked) throws EmfException {
//        EmfCall call = call();
//
//        call.setOperation("releaseLocked");
//        call.addParam("element", mappings.sectorScenario());
//        call.setReturnType(mappings.sectorScenario());
//
//        call.request(new Object[] { locked });
//    }

    public synchronized void releaseLocked(User user, int id) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLocked");
        call.addParam("user", mappings.user());
        call.addIntegerParam("id");
        call.setReturnType(mappings.sectorScenario());

        call.request(new Object[] { user, new Integer(id) });
    }

    public synchronized SectorScenario updateSectorScenario(SectorScenario element) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateSectorScenario");
        call.addParam("element", mappings.sectorScenario());
        call.setReturnType(mappings.sectorScenario());

        return (SectorScenario) call.requestResponse(new Object[] { element });
    }

    public synchronized SectorScenario updateSectorScenarioWithLock(SectorScenario element) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateSectorScenarioWithLock");
        call.addParam("element", mappings.sectorScenario());
        call.setReturnType(mappings.sectorScenario());

        return (SectorScenario) call.requestResponse(new Object[] { element });
    }

//    public void removeSectorScenarios(SectorScenario[] elements, User user) throws EmfException {
//        EmfCall call = call();
//
//        call.setOperation("removeSectorScenarios");
//        call.addParam("elements", mappings.controlStrategies());
//        call.addParam("user", mappings.user());
//        call.setVoidReturnType();
//
//        call.request(new Object[] { elements, user });
//    }

    public synchronized void removeSectorScenarios(int[] ids, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeSectorScenarios");
        call.addIntArrayParam();
        call.addParam("user", mappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { ids, user });
    }

    public synchronized void runSectorScenario(User user, int sectorScenarioId) throws EmfException {
        EmfCall call = call();

        call.setOperation("runSectorScenario");
        call.addParam("user", mappings.user());
        call.addIntegerParam("sectorScenarioId");
        call.setVoidReturnType();

        call.request(new Object[] { user, new Integer(sectorScenarioId) });
    }

    public synchronized void summarizeStrategy(User user, int sectorScenarioId, 
            String exportDirectory, StrategyResultType strategyResultType) throws EmfException {
        EmfCall call = call();

        call.setOperation("summarizeStrategy");
        call.addParam("user", mappings.user());
        call.addIntegerParam("sectorScenarioId");
        call.addStringParam("exportDirectory");
        call.addParam("strategyResultType", mappings.strategyResultType());
        call.setVoidReturnType();

        call.request(new Object[] { user, new Integer(sectorScenarioId), exportDirectory, strategyResultType });
    }

    public synchronized StrategyType[] getStrategyTypes() throws EmfException {
        EmfCall call = call();

        call.setOperation("getStrategyTypes");
        call.setReturnType(mappings.strategyTypes());

        return (StrategyType[]) call.requestResponse(new Object[] {});
    }

    public synchronized void stopRunSectorScenario(int sectorScenarioId) throws EmfException {
        EmfCall call = call();

        call.setOperation("stopRunSectorScenario");
        call.addIntegerParam("sectorScenarioId");
        call.setVoidReturnType();

        call.request(new Object[] { new Integer(sectorScenarioId) });
    }

    public synchronized String sectorScenarioRunStatus(int id) throws EmfException {
        EmfCall call = call();

        call.setOperation("sectorScenarioRunStatus");
        call.addIntParam();

        call.setReturnType(mappings.string());

        return (String) call.requestResponse(new Object[] { new Integer(id) });
    }

    public synchronized ControlMeasureClass[] getControlMeasureClasses(int sectorScenarioId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getControlMeasureClasses");
        call.addIntParam();
        call.setReturnType(mappings.controlMeasureClasses());

        return (ControlMeasureClass[]) call.requestResponse(new Object[] { new Integer(sectorScenarioId) });
    }

    public synchronized int isDuplicateName(String name) throws EmfException {
        EmfCall call = call();

        call.setOperation("isDuplicateName");
        call.addStringParam("name");
        call.setIntegerReturnType();
        return (Integer) call.requestResponse(new Object[] { new String(name) });
    }
    
    public synchronized int isDuplicateAbbre(String abbre) throws EmfException {
        EmfCall call = call();

        call.setOperation("isDuplicateAbbre");
        call.addStringParam("abbre");
        call.setIntegerReturnType();
        return (Integer) call.requestResponse(new Object[] { new String(abbre) });
    }

    public synchronized int copySectorScenario(int id, User creator) throws EmfException {
        EmfCall call = call();

        call.setOperation("copySectorScenario");
        call.addIntegerParam("id");
        call.addParam("creator", mappings.user());
        call.setIntegerReturnType();
        return (Integer) call.requestResponse(new Object[] { new Integer(id), creator });
    }

    public synchronized SectorScenario getById(int id) throws EmfException {
        EmfCall call = call();

        call.setOperation("getById");
        call.addIntegerParam("id");
        call.setReturnType(mappings.sectorScenario());
        return (SectorScenario) call.requestResponse(new Object[] { new Integer(id) });
    }

    public synchronized SectorScenarioOutput[] getSectorScenarioOutputs(int sectorScenarioId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getSectorScenarioOutputs");
        call.addIntegerParam("sectorScenarioId");
        call.setReturnType(mappings.sectorScenarioOutputs());

        return (SectorScenarioOutput[]) call.requestResponse(new Object[] { new Integer(sectorScenarioId) });
    }

    public List<SectorScenario> getSectorScenariosByRunStatus(String runStatus) {
        // NOTE Auto-generated method stub
        return null;
    }

    public Long getSectorScenarioRunningCount() {
        // NOTE Auto-generated method stub
        return null;
    }

    public void setSectorScenarioRunStatusAndCompletionDate(int id, String runStatus, Date completionDate)
            {
        // NOTE Auto-generated method stub
        
    }

    public synchronized String getDefaultExportDirectory() throws EmfException {
        EmfCall call = call();

        call.setOperation("getDefaultExportDirectory");
        call.setReturnType(mappings.string());

        return (String) call.requestResponse(new Object[] {  });
    }

    public StrategyResultType[] getOptionalStrategyResultTypes() throws EmfException {
        EmfCall call = call();

        call.setOperation("getOptionalStrategyResultTypes");
        call.setReturnType(mappings.strategyResultTypes());

        return (StrategyResultType[]) call.requestResponse(new Object[] { });
    }

    public synchronized String getStrategyRunStatus(int id) throws EmfException {
        EmfCall call = call();

        call.setOperation("getStrategyRunStatus");
        call.addIntegerParam("id");
        call.setReturnType(mappings.string());

        return (String) call.requestResponse(new Object[] { new Integer(id) });
    }

    public synchronized String[] getDistinctSectorListFromDataset(int datasetId, int versionNumber) throws EmfException {
        EmfCall call = call();

        call.setOperation("getDistinctSectorListFromDataset");
        call.addIntegerParam("datasetId");
        call.addIntegerParam("versionNumber");
        call.setReturnType(mappings.strings());

        return (String[]) call.requestResponse(new Object[] { new Integer(datasetId), new Integer(versionNumber) });
    }

}
