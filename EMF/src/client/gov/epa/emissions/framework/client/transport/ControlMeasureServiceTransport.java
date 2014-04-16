package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Reference;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.ControlMeasurePropertyCategory;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.services.cost.EquationType;
import gov.epa.emissions.framework.services.cost.LightControlMeasure;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

public class ControlMeasureServiceTransport implements ControlMeasureService {

    private CallFactory callFactory;

    private DataMappings mappings;

    private EmfCall call;

    public ControlMeasureServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new DataMappings();
    }

    private EmfCall call() throws EmfException {
        if (call == null)
            call = callFactory.createSessionEnabledCall("ControlMeasureService");

        return call;
    }

    public synchronized ControlMeasure[] getMeasures() throws EmfException {
        EmfCall call = call();

        call.setOperation("getMeasures");
        call.setReturnType(mappings.controlMeasures());

        return (ControlMeasure[]) call.requestResponse(new Object[] {});
    }

    public synchronized ControlMeasure[] getMeasures(Pollutant pollutant) throws EmfException {
        EmfCall call = call();

        call.setOperation("getMeasures");
        call.addParam("pollutant", mappings.pollutant());
        call.setReturnType(mappings.controlMeasures());

        return (ControlMeasure[]) call.requestResponse(new Object[] { pollutant });
    }

    public synchronized int addMeasure(ControlMeasure measure, Scc[] sccs) throws EmfException {
        EmfCall call = call();

        call.setOperation("addMeasure");
        call.addParam("measure", mappings.controlMeasure());
        call.addParam("sccs", mappings.sccs());
        call.setIntegerReturnType();

        return (Integer) call.requestResponse(new Object[] { measure, sccs });
    }

    public synchronized void removeMeasure(int controlMeasureId) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeMeasure");
        call.addIntegerParam("controlMeasureId");
        call.setVoidReturnType();

        call.request(new Object[] { new Integer(controlMeasureId) });
    }

    public synchronized int copyMeasure(int controlMeasureId, User creator) throws EmfException {
        EmfCall call = call();

        call.setOperation("copyMeasure");
        call.addIntegerParam("controlMeasureId");
        call.addParam("creator", mappings.user());
        call.setIntegerReturnType();

        return (Integer) call.requestResponse(new Object[] { new Integer(controlMeasureId), creator });
    }

    public synchronized ControlMeasure obtainLockedMeasure(User owner, int controlMeasureId) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainLockedMeasure");
        call.addParam("owner", mappings.user());
        call.addIntegerParam("controlMeasureId");
        call.setReturnType(mappings.controlMeasure());

        return (ControlMeasure) call.requestResponse(new Object[] { owner, new Integer(controlMeasureId) });
    }

    // public ControlMeasure releaseLockedControlMeasure(ControlMeasure locked) throws EmfException {
    // EmfCall call = call();
    //
    // call.setOperation("releaseLockedControlMeasure");
    // call.addParam("locked", mappings.controlMeasure());
    // call.setReturnType(mappings.controlMeasure());
    //
    // return (ControlMeasure) call.requestResponse(new Object[] { locked });
    // }

    public synchronized void releaseLockedControlMeasure(User user, int controlMeasureId) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLockedControlMeasure");
        call.addParam("user", mappings.user());
        call.addIntegerParam("controlMeasureId");
        call.setVoidReturnType();

        call.requestResponse(new Object[] { user, new Integer(controlMeasureId) });
    }

    public synchronized ControlMeasure updateMeasure(ControlMeasure measure, Scc[] sccs) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateMeasure");
        call.addParam("measure", mappings.controlMeasure());
        call.addParam("sccs", mappings.sccs());
        call.setReturnType(mappings.controlMeasure());

        return (ControlMeasure) call.requestResponse(new Object[] { measure, sccs });
    }

    public synchronized ControlMeasure updateMeasureAndHoldLock(ControlMeasure measure, Scc[] sccs) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateMeasureAndHoldLock");
        call.addParam("measure", mappings.controlMeasure());
        call.addParam("sccs", mappings.sccs());
        call.setReturnType(mappings.controlMeasure());

        return (ControlMeasure) call.requestResponse(new Object[] { measure, sccs });
    }

    public synchronized Scc[] getSccsWithDescriptions(int controlMeasureId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getSccsWithDescriptions");
        call.addIntegerParam("controlMeasureId");
        call.setReturnType(mappings.sccs());

        Scc[] sccs = (Scc[]) call.requestResponse(new Object[] { new Integer(controlMeasureId) });

        return sccs;
    }

    public synchronized Scc[] getSccs(int controlMeasureId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getSccs");
        call.addIntegerParam("controlMeasureId");
        call.setReturnType(mappings.sccs());

        Scc[] sccs = (Scc[]) call.requestResponse(new Object[] { new Integer(controlMeasureId) });

        return sccs;
    }

    public synchronized ControlTechnology[] getControlTechnologies() throws EmfException {
        EmfCall call = call();

        call.setOperation("getControlTechnologies");
        call.setReturnType(mappings.controlTechnologies());

        ControlTechnology[] technologies = (ControlTechnology[]) call.requestResponse(new Object[] {});

        return technologies;
    }

    public synchronized Reference[] getReferences(String textContains) throws EmfException {

        EmfCall call = call();

        call.setOperation("getReferences");
        call.addStringParam("textContains");
        call.setReturnType(mappings.references());

        Reference[] references = (Reference[]) call.requestResponse(new Object[] { textContains });

        return references;
    }

    public int getReferenceCount(String textContains) throws EmfException {

        EmfCall call = call();

        call.setOperation("getReferenceCount");
        call.addStringParam("textContains");
        call.setIntegerReturnType();

        return (Integer) call.requestResponse(new Object[] { textContains });
    }

    public synchronized CostYearTable getCostYearTable(int targetYear) throws EmfException {
        EmfCall call = call();

        call.setOperation("getCostYearTable");
        call.addIntegerParam("targetYear");

        call.setReturnType(mappings.costYearTable());

        return (CostYearTable) call.requestResponse(new Object[] { new Integer(targetYear) });
    }

    public synchronized ControlMeasureClass[] getMeasureClasses() throws EmfException {
        EmfCall call = call();

        call.setOperation("getMeasureClasses");
        call.setReturnType(mappings.controlMeasureClasses());

        return (ControlMeasureClass[]) call.requestResponse(new Object[] {});
    }

    public synchronized ControlMeasureClass getMeasureClass(String name) throws EmfException {
        EmfCall call = call();

        call.setOperation("getMeasureClass");
        call.addIntegerParam("name");
        call.setReturnType(mappings.controlMeasureClass());

        return (ControlMeasureClass) call.requestResponse(new Object[] { name });
    }

    public synchronized LightControlMeasure[] getLightControlMeasures() throws EmfException {
        EmfCall call = call();

        call.setOperation("getLightControlMeasures");
        call.setReturnType(mappings.lightControlMeasures());

        return (LightControlMeasure[]) call.requestResponse(new Object[] {});
    }

    public synchronized EfficiencyRecord[] getEfficiencyRecords(int controlMeasureId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getEfficiencyRecords");
        call.addIntegerParam("controlMeasureId");
        call.setReturnType(mappings.efficiencyRecords());

        return (EfficiencyRecord[]) call.requestResponse(new Object[] { new Integer(controlMeasureId) });
    }

    public synchronized int getEfficiencyRecordCount(int controlMeasureId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getEfficiencyRecordCount");
        call.addIntegerParam("controlMeasureId");
        call.setIntegerReturnType();

        return (Integer) call.requestResponse(new Object[] { new Integer(controlMeasureId) });
    }

    public synchronized EfficiencyRecord[] getEfficiencyRecords(int controlMeasureId, int recordLimit, String filter)
            throws EmfException {
        EmfCall call = call();

        call.setOperation("getEfficiencyRecords");
        call.addIntegerParam("controlMeasureId");
        call.addIntegerParam("recordLimit");
        call.addStringParam("filter");
        call.setReturnType(mappings.efficiencyRecords());

        return (EfficiencyRecord[]) call.requestResponse(new Object[] { new Integer(controlMeasureId),
                new Integer(recordLimit), filter });
    }

    public synchronized int addEfficiencyRecord(EfficiencyRecord efficiencyRecord) throws EmfException {
        EmfCall call = call();

        call.setOperation("addEfficiencyRecord");
        call.addParam("efficiencyRecord", mappings.efficiencyRecord());
        call.setIntegerReturnType();

        return (Integer) call.requestResponse(new Object[] { efficiencyRecord });
    }

    public synchronized void removeEfficiencyRecord(int efficiencyRecordId) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeEfficiencyRecord");
        call.addIntegerParam("efficiencyRecordId");
        call.setVoidReturnType();

        call.requestResponse(new Object[] { new Integer(efficiencyRecordId) });
    }

    public synchronized void updateEfficiencyRecord(EfficiencyRecord efficiencyRecord) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateEfficiencyRecord");
        call.addParam("efficiencyRecord", mappings.efficiencyRecord());
        call.setVoidReturnType();

        call.requestResponse(new Object[] { efficiencyRecord });
    }

    public synchronized ControlMeasure[] getSummaryControlMeasures(String whereFilter) throws EmfException {
        EmfCall call = call();

        call.setOperation("getSummaryControlMeasures");
        call.addStringParam("whereFilter");
        call.setReturnType(mappings.controlMeasures());
        return (ControlMeasure[]) call.requestResponse(new Object[] { whereFilter });
    }

    public synchronized ControlMeasure[] getControlMeasures(String whereFilter) throws EmfException {
        EmfCall call = call();

        call.setOperation("getControlMeasures");
        call.addStringParam("whereFilter");
        call.setReturnType(mappings.controlMeasures());
        return (ControlMeasure[]) call.requestResponse(new Object[] { whereFilter });
    }

    public synchronized ControlMeasure[] getSummaryControlMeasures(int majorPollutantId, String whereFilter)
            throws EmfException {
        EmfCall call = call();

        call.setOperation("getSummaryControlMeasures");
        call.addIntegerParam("majorPollutantId");
        call.addStringParam("whereFilter");
        call.setReturnType(mappings.controlMeasures());
        return (ControlMeasure[]) call.requestResponse(new Object[] { new Integer(majorPollutantId), whereFilter });
    }

    public synchronized ControlMeasure[] getControlMeasures(int majorPollutantId, String whereFilter)
            throws EmfException {
        EmfCall call = call();

        call.setOperation("getControlMeasures");
        call.addIntegerParam("majorPollutantId");
        call.addStringParam("whereFilter");
        call.setReturnType(mappings.controlMeasures());
        return (ControlMeasure[]) call.requestResponse(new Object[] { new Integer(majorPollutantId), whereFilter });
    }

    public synchronized ControlMeasure getMeasure(int controlMeasureId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getMeasure");
        call.addIntegerParam("controlMeasureId");
        call.setReturnType(mappings.controlMeasure());

        return (ControlMeasure) call.requestResponse(new Object[] { new Integer(controlMeasureId) });
    }

    public synchronized EquationType[] getEquationTypes() throws EmfException {
        EmfCall call = call();

        call.setOperation("getEquationTypes");
        call.setReturnType(mappings.equationTypes());
        return (EquationType[]) call.requestResponse(new Object[] {});
    }

    public synchronized ControlMeasurePropertyCategory[] getPropertyCategories() throws EmfException {
        EmfCall call = call();

        call.setOperation("getPropertyCategories");
        call.setReturnType(mappings.controlMeasurePropertyCategories());
        return (ControlMeasurePropertyCategory[]) call.requestResponse(new Object[] {});
    }

    public ControlMeasurePropertyCategory getPropertyCategory(String categoryName) throws EmfException {
        EmfCall call = call();

        call.setOperation("getPropertyCategory");
        call.addStringParam("categoryName");
        call.setReturnType(mappings.controlMeasurePropertyCategory());
        return (ControlMeasurePropertyCategory) call.requestResponse(new Object[] { categoryName });
    }

    public Sector[] getDistinctControlMeasureSectors() throws EmfException {
        EmfCall call = call();

        call.setOperation("getDistinctControlMeasureSectors");
        call.setReturnType(mappings.sectors());
        return (Sector[]) call.requestResponse(new Object[] {});
    }

    public ControlMeasure[] getControlMeasureBySector(int[] sectorIds) throws EmfException {
        EmfCall call = call();

        call.setOperation("getControlMeasureBySector");
        call.addParam("sectorIds", mappings.integers());
        call.setReturnType(mappings.controlMeasures());
        return (ControlMeasure[]) call.requestResponse(new Object[] { sectorIds });
    }

    public synchronized void generateControlMeasurePDFReport(User user, int[] controlMeasureIds) throws EmfException {
        EmfCall call = call();

        call.setOperation("generateControlMeasurePDFReport");
        call.addParam("user", mappings.user());
        call.addParam("controlMeasureIds", mappings.integers());
        call.setVoidReturnType();

        call.requestResponse(new Object[] { user, controlMeasureIds });
    }
}
