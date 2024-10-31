package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.analysis.common.BestMeasureEffRecord;
import gov.epa.emissions.framework.services.cost.analysis.common.EfficiencyRecordUtil;
import gov.epa.emissions.framework.services.cost.analysis.common.RegionFilter;
import gov.epa.emissions.framework.services.cost.analysis.common.RetrieveBestEffRecord;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlStrategy.SccControlMeasuresMap;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import javax.persistence.EntityManagerFactory;

public class RetrieveBestMeasureEffRecord {

    private SccControlMeasuresMap map;

    private ControlStrategy controlStrategy;

    private CostYearTable costYearTable;
    
    private BestMeasureEffRecord maxMeasure;
    
    private String fips;
    
    private RetrieveBestEffRecord retrieveBestEffRecord;
    
    private EfficiencyRecordUtil effRecordUtil;
    
    private RegionFilter regionFilter;
    
    public RetrieveBestMeasureEffRecord(SccControlMeasuresMap map, CostYearTable costYearTable,
            ControlStrategy controlStrategy, DbServer dbServer, 
            EntityManagerFactory entityManagerFactory) {
        this.map = map;
        this.costYearTable = costYearTable;
        this.controlStrategy = controlStrategy;
        this.retrieveBestEffRecord = new RetrieveBestEffRecord(costYearTable);
        this.effRecordUtil = new EfficiencyRecordUtil();
        this.regionFilter = new RegionFilter(dbServer, entityManagerFactory);
    }

    //gets the best measure for the target pollutant
    public BestMeasureEffRecord findBestMaxEmsRedMeasureForTargetPollutant(String scc, String fips, 
            double invenControlEfficiency, double invenRulePenetration, 
            double invenRuleEffectiveness, double invenAnnualEmissions) throws EmfException {
        ControlMeasure[] controlMeasures = map.getControlMeasures(scc);
        this.fips = fips;
        MaxEmsRedControlMeasureMap reduction = new MaxEmsRedControlMeasureMap(costYearTable);
        for (int i = 0; i < controlMeasures.length; i++) {
            if (regionFilter.filter(controlMeasures[i], fips)) {
                EfficiencyRecord record = findBestEfficiencyRecordForTargetPollutant(controlMeasures[i], fips, 
                        controlStrategy.getInventoryYear(), invenControlEfficiency, 
                        invenRulePenetration, invenRuleEffectiveness, 
                        invenAnnualEmissions);
                if (record != null) {
                    reduction.add(controlMeasures[i], record);
                }
            }
        }

        return maxMeasure = reduction.findBestMeasure();
    }

    //gets the best measure for the target pollutant
    public BestMeasureEffRecord findBestMaxEmsRedMeasure(String scc, String fips, Pollutant pollutant,
            double invenControlEfficiency, double invenRulePenetration, 
            double invenRuleEffectiveness, double invenAnnualEmissions) throws EmfException {
        ControlMeasure[] controlMeasures = map.getControlMeasures(scc);
        this.fips = fips;
        MaxEmsRedControlMeasureMap reduction = new MaxEmsRedControlMeasureMap(costYearTable);
        for (int i = 0; i < controlMeasures.length; i++) {
            if (regionFilter.filter(controlMeasures[i], fips)) {
                EfficiencyRecord record = findBestEfficiencyRecordForTargetPollutant(controlMeasures[i], fips, 
                        controlStrategy.getInventoryYear(), invenControlEfficiency, 
                        invenRulePenetration, invenRuleEffectiveness, 
                        invenAnnualEmissions);
                if (record != null) {
                    reduction.add(controlMeasures[i], record);
                }
            }
        }
        /*MaxEmsRedControlMeasure */maxMeasure = reduction.findBestMeasure();
        
        //if pollutant is same as target pollutant, don't find best eff record again, since we already 
        //filtered by constraints, the below logic doesn't care about the constraints...
        if (pollutant.equals(controlStrategy.getTargetPollutant())) 
            return maxMeasure;
        
        if (maxMeasure == null) return null;
        ControlMeasure controlMeasure = maxMeasure.measure();

        reduction = new MaxEmsRedControlMeasureMap(costYearTable);
        EfficiencyRecord record = findBestEfficiencyRecord(controlMeasure, fips, pollutant, controlStrategy.getInventoryYear(), invenAnnualEmissions);
        if (record != null) {
            reduction.add(controlMeasure, record);
        }

        return maxMeasure = reduction.findBestMeasure();
    }

    public BestMeasureEffRecord getMaxEmsRedMeasureForCobenefitPollutant(Pollutant pollutant, double invenAnnualEmission) throws EmfException {
        if (maxMeasure == null) return null;
        ControlMeasure controlMeasure = maxMeasure.measure();
        MaxEmsRedControlMeasureMap reduction = new MaxEmsRedControlMeasureMap(costYearTable);
        EfficiencyRecord record = findBestEfficiencyRecord(controlMeasure, fips, pollutant, controlStrategy.getInventoryYear(), invenAnnualEmission);
        if (record != null) {
            reduction.add(controlMeasure, record);
        }

        return reduction.findBestMeasure();
    }

    private EfficiencyRecord findBestEfficiencyRecordForTargetPollutant(ControlMeasure measure, String fips, 
            int inventoryYear, double invenControlEfficiency, 
            double invenRulePenetration, double invenRuleEffectiveness, 
            double invenAnnualEmissions) throws EmfException {
        
        EfficiencyRecord[] efficiencyRecords = effRecordUtil.pollutantFilter(measure.getEfficiencyRecords(), controlStrategy.getTargetPollutant());
        efficiencyRecords = effRecordUtil.minMaxEmisFilter(efficiencyRecords, invenAnnualEmissions);
        efficiencyRecords = effRecordUtil.localeFilter(efficiencyRecords, fips);
        efficiencyRecords = effRecordUtil.effectiveDateFilter(efficiencyRecords, inventoryYear);
        //apply this additional filter ONLY for TARGET POLLUTANTS...
        efficiencyRecords = effRecordUtil.filterByConstraints(measure, controlStrategy.getConstraint(), 
                costYearTable, efficiencyRecords, 
                invenControlEfficiency, invenRulePenetration, 
                invenRuleEffectiveness, invenAnnualEmissions);

        return retrieveBestEffRecord.findBestEfficiencyRecord(measure, efficiencyRecords);
    }

    private EfficiencyRecord findBestEfficiencyRecord(ControlMeasure measure, String fips, 
            Pollutant pollutant, int inventoryYear,
            double invenAnnualEmission) throws EmfException {
        EfficiencyRecord[] efficiencyRecords = effRecordUtil.pollutantFilter(measure.getEfficiencyRecords(), pollutant);
        efficiencyRecords = effRecordUtil.minMaxEmisFilter(efficiencyRecords, invenAnnualEmission);
        efficiencyRecords = effRecordUtil.localeFilter(efficiencyRecords, fips);
        efficiencyRecords = effRecordUtil.effectiveDateFilter(efficiencyRecords, inventoryYear);

        return retrieveBestEffRecord.findBestEfficiencyRecord(measure, efficiencyRecords);
    }
}