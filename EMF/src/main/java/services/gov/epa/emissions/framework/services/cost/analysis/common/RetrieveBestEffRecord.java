package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

public class RetrieveBestEffRecord {

    private EfficiencyRecordUtil efficiencyRecordUtil;

    private CostYearTable costYearTable;

    public RetrieveBestEffRecord(CostYearTable costYearTable) {
        this.costYearTable = costYearTable;
        this.efficiencyRecordUtil = new EfficiencyRecordUtil();
    }

    public EfficiencyRecord findBestEfficiencyRecord(ControlMeasure controlMeasure, EfficiencyRecord[] ers) throws EmfException {
        if (ers.length == 0)
            return null;

        if (ers.length == 1)
            return ers[0];

        EfficiencyRecord maxRecord = ers[0];

        for (int i = 1; i < ers.length; i++) {
            maxRecord = findBestEfficiencyRecord(controlMeasure, ers[i], 
                    controlMeasure, maxRecord);
        }

        return maxRecord;

    }

    public EfficiencyRecord findBestEfficiencyRecord(ControlMeasure controlMeasure, EfficiencyRecord record, 
            ControlMeasure bestControlMeasure, EfficiencyRecord bestRecord) throws EmfException {
        if (record == null && bestRecord == null) 
            return null;
        
        if (bestRecord == null) 
            return record;
        
        if (record == null) 
            return bestRecord;
        
        double red1 = efficiencyRecordUtil.effectiveReduction(controlMeasure, record);
        double red2 = efficiencyRecordUtil.effectiveReduction(bestControlMeasure, bestRecord);

        if (red1 > red2) {
            return record;
        }
        if (red1 < red2)
            return bestRecord;

        return compareCost(record, bestRecord);
    }

    private EfficiencyRecord compareCost(EfficiencyRecord record, EfficiencyRecord maxRecord) throws EmfException {
        double cost = efficiencyRecordUtil.adjustedCostPerTon(record, costYearTable);
        double maxCost = efficiencyRecordUtil.adjustedCostPerTon(maxRecord, costYearTable);

        if (cost >= maxCost) {
            return maxRecord;
        }
        return record;
    }
}