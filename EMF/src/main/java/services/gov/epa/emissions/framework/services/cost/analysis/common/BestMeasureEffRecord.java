package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.analysis.common.EfficiencyRecordUtil;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

// This class represents the best measure and its efficiency record for the source, this will be applied to the source during the strategy processing.
public class BestMeasureEffRecord {

    private ControlMeasure controlMeasure;

    private EfficiencyRecord effRecord;

    private CostYearTable table;

    private EfficiencyRecordUtil efficiencyRecordUtil;

    public BestMeasureEffRecord(ControlMeasure controlMeasure, EfficiencyRecord record, CostYearTable table) {
        this.controlMeasure = controlMeasure;
        this.effRecord = record;
        this.table = table;
        efficiencyRecordUtil = new EfficiencyRecordUtil();
    }

    public ControlMeasure measure() {
        return controlMeasure;
    }

    public EfficiencyRecord efficiencyRecord() {
        return effRecord;
    }

    public Double adjustedCostPerTon() throws EmfException {
        return efficiencyRecordUtil.adjustedCostPerTon(effRecord, table);
    }

    public double effectiveReduction() {
        return efficiencyRecordUtil.effectiveReduction(controlMeasure, effRecord);
    }

    public double costPerTon() {
        return effRecord.getCostPerTon();
    }

    public double controlEfficiency() {
        return effRecord.getEfficiency();
    }

    public double rulePenetration() {
        return controlMeasure.getRulePenetration() != null ? controlMeasure.getRulePenetration() : effRecord.getRulePenetration();
    }

    public double ruleEffectiveness() {
        return controlMeasure.getRuleEffectiveness() != null ? controlMeasure.getRuleEffectiveness() : effRecord.getRuleEffectiveness();
    }
}
