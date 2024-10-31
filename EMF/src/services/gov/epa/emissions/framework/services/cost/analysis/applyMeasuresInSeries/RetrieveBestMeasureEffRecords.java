package gov.epa.emissions.framework.services.cost.analysis.applyMeasuresInSeries;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.analysis.common.BestMeasureEffRecord;
import gov.epa.emissions.framework.services.cost.analysis.common.EfficiencyRecordUtil;
import gov.epa.emissions.framework.services.cost.analysis.common.RegionFilter;
import gov.epa.emissions.framework.services.cost.analysis.common.RetrieveBestEffRecord;
import gov.epa.emissions.framework.services.cost.analysis.common.SortBestMeasureEffRecordByApplyOrderAndLeastCost;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManagerFactory;

public class RetrieveBestMeasureEffRecords {

    private ControlStrategy controlStrategy;

    private CostYearTable costYearTable;
    
    private RetrieveBestEffRecord retrieveBestEffRecord;
    
    private EfficiencyRecordUtil effRecordUtil;
    
    private RegionFilter regionFilter;
    
    public RetrieveBestMeasureEffRecords(ControlStrategy controlStrategy, CostYearTable costYearTable,
            DbServer dbServer, EntityManagerFactory entityManagerFactory) {
        this.controlStrategy = controlStrategy;
        this.costYearTable = costYearTable;
        this.retrieveBestEffRecord = new RetrieveBestEffRecord(costYearTable);
        this.effRecordUtil = new EfficiencyRecordUtil();
        this.regionFilter = new RegionFilter(dbServer, entityManagerFactory);
   }

    //get the best measures map for TARGET POLLUTANT
    //Also, sort the returned list in order for processing the inventory
    //  Sort by ApplyOrder and then Least Cost
    public List<BestMeasureEffRecord> findTargetPollutantBestMeasureEffRecords(ControlMeasure[] controlMeasures, String fips, 
            double invenControlEfficiency, double invenRulePenetration, 
            double invenRuleEffectiveness, double invenAnnualEmissions) throws EmfException {
//        BestMeasureEffRecordMap measureEffRecordMap = new BestMeasureEffRecordMap(costYearTable);
        List<BestMeasureEffRecord> measureEffRecordList = new ArrayList<BestMeasureEffRecord>();
        for (int i = 0; i < controlMeasures.length; i++) {
            if (regionFilter.filter(controlMeasures[i], fips)) {
                EfficiencyRecord record = findTargetPollutantBestEffRecord(controlMeasures[i], fips, 
                        controlStrategy.getInventoryYear(), invenControlEfficiency, 
                        invenRulePenetration, invenRuleEffectiveness, 
                        invenAnnualEmissions);
                if (record != null) {
    //                measureEffRecordList.add(controlMeasures[i], record);
                    measureEffRecordList.add(new BestMeasureEffRecord(controlMeasures[i], record, costYearTable));
                }
            }
        }

        //Sort the list correctly, by apply order, then by least cost (cheapest to most expensive)...
        Collections.sort(measureEffRecordList, new SortBestMeasureEffRecordByApplyOrderAndLeastCost());
//        Collections.sort(measureEffRecordList, new SortBestMeasureEffRecordByLeastCost());
//        sortMeasureEffRecord(measureEffRecordList);

        return measureEffRecordList;
    }

    //get the best measures map for COBENEFIT POLLUTANTs
    //also, sort the returned list in order for processing the inventory
    //  Sort by ApplyOrder and then Least Cost
    public List<BestMeasureEffRecord> findCobenefitPollutantBestMeasureEffRecords(ControlMeasure[] controlMeasures, String fips, Pollutant pollutant, 
            double invenAnnualEmissions) throws EmfException {
//        BestMeasureEffRecordMap measureEffRecordMap = new BestMeasureEffRecordMap(costYearTable);
        List<BestMeasureEffRecord> measureEffRecordList = new ArrayList<BestMeasureEffRecord>();
        for (int i = 0; i < controlMeasures.length; i++) {
            if (regionFilter.filter(controlMeasures[i], fips)) {
                EfficiencyRecord record = findCobenefitPollutantBestEffRecord(controlMeasures[i], fips, pollutant,
                        controlStrategy.getInventoryYear(), invenAnnualEmissions);
                if (record != null) {
    //                measureEffRecordList.add(controlMeasures[i], record);
                    measureEffRecordList.add(new BestMeasureEffRecord(controlMeasures[i], record, costYearTable));
                }
            }
        }

        //BUG, target pollutant dictates the order....
        //Sort the list correctly, by apply order, then by least cost (cheapest to most expensive)...
        Collections.sort(measureEffRecordList, new SortBestMeasureEffRecordByApplyOrderAndLeastCost());
//        Collections.sort(measureEffRecordList, new SortBestMeasureEffRecordByLeastCost());

        return measureEffRecordList;
    }

    private EfficiencyRecord findTargetPollutantBestEffRecord(ControlMeasure measure, String fips, 
            int inventoryYear, double invenControlEfficiency, 
            double invenRulePenetration, double invenRuleEffectiveness, 
            double invenAnnualEmissions) throws EmfException {
//        EfficiencyRecord[] efficiencyRecords = effRecordUtil.filter(measure.getEfficiencyRecords(), controlStrategy.getTargetPollutant(),
//                fips, inventoryYear,
//                invenAnnualEmissions);
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

    private EfficiencyRecord findCobenefitPollutantBestEffRecord(ControlMeasure measure, String fips, Pollutant pollutant,
            int inventoryYear, double invenAnnualEmissions) throws EmfException {
//        EfficiencyRecord[] efficiencyRecords = effRecordUtil.filter(measure.getEfficiencyRecords(), controlStrategy.getTargetPollutant(),
//                fips, inventoryYear,
//                invenAnnualEmissions);
        EfficiencyRecord[] efficiencyRecords = effRecordUtil.pollutantFilter(measure.getEfficiencyRecords(), pollutant);
        efficiencyRecords = effRecordUtil.minMaxEmisFilter(efficiencyRecords, invenAnnualEmissions);
        efficiencyRecords = effRecordUtil.localeFilter(efficiencyRecords, fips);
        efficiencyRecords = effRecordUtil.effectiveDateFilter(efficiencyRecords, inventoryYear);

        return retrieveBestEffRecord.findBestEfficiencyRecord(measure, efficiencyRecords);
    }
    
//    private void sortMeasureEffRecord(List<BestMeasureEffRecord> measureEffRecordList) {
//        //Sort the list correctly, by cheapest to most expensive...
//        Collections.sort(measureEffRecordList, new Comparator<BestMeasureEffRecord>() {
//            public int compare(BestMeasureEffRecord o1, BestMeasureEffRecord o2) {
//                try {
//                    return (
//                            signum((o1.measure().getApplyOrder() != null ? o1.measure().getApplyOrder() : 0)
//                                    - (o2.measure().getApplyOrder() != null ? o2.measure().getApplyOrder() : 0))
//                            +
//                            signum((o1.adjustedCostPerTon() != null ? o1.adjustedCostPerTon() : 0) 
//                            - (o2.adjustedCostPerTon() != null ? o2.adjustedCostPerTon() : 0))
//                            );
//                } catch (EmfException e) {
//                    return 0;
//                }
//            }
//            /**
//             * Collapse number down to +1 0 or -1 depending on sign.
//             * Typically used in compare routines to collapse a difference
//             * of two longs to an int.
//             *
//             * @param diff usually represents the difference of two long.
//             *
//             * @return signum of diff, +1, 0 or -1.
//             */
//             public int signum(double diff) {
//                if ( diff > 0 ) return 1;
//                if ( diff < 0 ) return -1;
//                return 0;
//             }
//        });
//    }
}