package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Reference;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EMFService;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

public interface ControlMeasureService extends EMFService {

    ControlMeasure[] getMeasures() throws EmfException;

    int addMeasure(ControlMeasure measure, Scc[] sccs) throws EmfException;

    ControlMeasure updateMeasure(ControlMeasure measure, Scc[] sccs) throws EmfException;

    ControlMeasure updateMeasureAndHoldLock(ControlMeasure measure, Scc[] sccs) throws EmfException;

//    ControlMeasure releaseLockedControlMeasure(ControlMeasure locked) throws EmfException;

    void releaseLockedControlMeasure(User user, int controlMeasureId) throws EmfException;

//    void removeMeasure(ControlMeasure measure) throws EmfException;

    void removeMeasure(int controlMeasureId) throws EmfException;

    public int copyMeasure(int controlMeasureId, User creator) throws EmfException;

//    ControlMeasure obtainLockedMeasure(User user, ControlMeasure measure) throws EmfException;

    ControlMeasure obtainLockedMeasure(User user, int controlMeasureId) throws EmfException;

    ControlMeasure getMeasure(int controlMeasureId) throws EmfException;

//    Scc[] getSccsWithDescriptions(ControlMeasure measure) throws EmfException;
    
    Scc[] getSccsWithDescriptions(int controlMeasureId) throws EmfException;
    
//    Scc[] getSccs(ControlMeasure measure) throws EmfException;

    Scc[] getSccs(int controlMeasureId) throws EmfException;

    ControlTechnology[] getControlTechnologies() throws EmfException;

    Reference[] getReferences(String textContains) throws EmfException;

    int getReferenceCount(String textContains) throws EmfException;

    CostYearTable getCostYearTable(int targetYear) throws EmfException;

    ControlMeasure[] getMeasures(Pollutant pollutant) throws EmfException;

    ControlMeasureClass[] getMeasureClasses() throws EmfException;

    ControlMeasureClass getMeasureClass(String name) throws EmfException;

    LightControlMeasure[] getLightControlMeasures() throws EmfException;

    EfficiencyRecord[] getEfficiencyRecords(int controlMeasureId) throws EmfException;

    int getEfficiencyRecordCount(int controlMeasureId) throws EmfException;

    EfficiencyRecord[] getEfficiencyRecords(int controlMeasureId, int recordLimit, String filter) throws EmfException;

    int addEfficiencyRecord(EfficiencyRecord efficiencyRecord) throws EmfException;

    void removeEfficiencyRecord(int efficiencyRecordId) throws EmfException;

    void updateEfficiencyRecord(EfficiencyRecord efficiencyRecord) throws EmfException;

    ControlMeasure[] getSummaryControlMeasures(int majorPollutantId, String whereFilter) throws EmfException;
    
    ControlMeasure[] getControlMeasures(int majorPollutantId, String whereFilter) throws EmfException;
    
    ControlMeasure[] getSummaryControlMeasures(String whereFilter) throws EmfException;
    
    ControlMeasure[] getControlMeasures(String whereFilter) throws EmfException;
    
    EquationType[] getEquationTypes() throws EmfException;
    
    ControlMeasurePropertyCategory[] getPropertyCategories() throws EmfException;

    ControlMeasurePropertyCategory getPropertyCategory(String categoryName) throws EmfException;
    
    Sector[] getDistinctControlMeasureSectors() throws EmfException;
    
    ControlMeasure[] getControlMeasureBySector(int[] sectorIds) throws EmfException;

    void generateControlMeasurePDFReport(User user, int[] controlMeasureIds) throws EmfException;
}