package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyConstraint;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import java.util.ArrayList;
import java.util.List;

public class ConstraintFilter {

    private Double maxControlEfficiency;

    private Double minCostPerTon;

    private Double maxEmisReduction;

    private Double minAnnCost;
    
    private CostYearTable costYearTable;
    
    private EfficiencyRecordUtil efficiencyRecordUtil;
    
    public ConstraintFilter(ControlStrategyConstraint constraint, CostYearTable costYearTable) {
        if (constraint != null) {
            this.maxControlEfficiency = constraint.getMaxControlEfficiency();
            this.minCostPerTon = constraint.getMinCostPerTon();
            this.maxEmisReduction = constraint.getMaxEmisReduction();
            this.minAnnCost = constraint.getMinAnnCost();
        }
        this.costYearTable = costYearTable;
        this.efficiencyRecordUtil = new EfficiencyRecordUtil();
    }

    public EfficiencyRecord[] filter(ControlMeasure controlMeasure, EfficiencyRecord[] efficiencyRecords, 
            double invenControlEfficiency, double invenRulePenetration, 
            double invenRuleEffectiveness, double invenAnnualEmissions) throws EmfException {
        return filterByMaxControlEfficiency(
                filterByMinCostPerTon(
                        filterByMaxEmisReduction(
                                controlMeasure, filterByMinAnnCost(controlMeasure, efficiencyRecords, 
                                        invenControlEfficiency, invenRulePenetration, 
                                        invenRuleEffectiveness, invenAnnualEmissions), 
                                invenControlEfficiency, invenRulePenetration, 
                                invenRuleEffectiveness, invenAnnualEmissions)));
    }

    public EfficiencyRecord[] filterByMaxControlEfficiency(EfficiencyRecord[] efficiencyRecords) {
        //return all eff records, if there is no constraint to filter on...
        if (maxControlEfficiency == null) return efficiencyRecords;
        
        List records = new ArrayList();
        for (int i = 0; i < efficiencyRecords.length; i++) {
            if (efficiencyRecords[i].getEfficiency() > maxControlEfficiency)
                records.add(efficiencyRecords[i]);
        }
        return (EfficiencyRecord[]) records.toArray(new EfficiencyRecord[0]);
    }

    public EfficiencyRecord[] filterByMinCostPerTon(EfficiencyRecord[] efficiencyRecords) throws EmfException {
        //return all eff records, if there is no constraint to filter on...
        if (minCostPerTon == null) return efficiencyRecords;
        
        List records = new ArrayList();
        for (int i = 0; i < efficiencyRecords.length; i++) {
            //convert cost to year system wide reference cost year (i.e.,2006)
            if (efficiencyRecords[i].getCostPerTon() == null 
                    || efficiencyRecords[i].getCostPerTon() * costYearTable.factor(CostYearTable.REFERENCE_COST_YEAR, efficiencyRecords[i].getCostYear()) < minCostPerTon)
                records.add(efficiencyRecords[i]);
        }
        return (EfficiencyRecord[]) records.toArray(new EfficiencyRecord[0]);
    }

    public EfficiencyRecord[] filterByMaxEmisReduction(ControlMeasure controlMeasure, EfficiencyRecord[] efficiencyRecords, 
            double invenControlEfficiency, double invenRulePenetration, 
            double invenRuleEffectiveness, double invenAnnualEmissions) {
        //return all eff records, if there is no constraint to filter on...
        if (maxEmisReduction == null) return efficiencyRecords;
        
        List records = new ArrayList();
        for (int i = 0; i < efficiencyRecords.length; i++) {
            if (efficiencyRecordUtil.calculateEmissionReduction(controlMeasure, efficiencyRecords[i], 
                    invenControlEfficiency, invenRulePenetration, 
                    invenRuleEffectiveness, invenAnnualEmissions) > maxEmisReduction)
                records.add(efficiencyRecords[i]);
        }
        return (EfficiencyRecord[]) records.toArray(new EfficiencyRecord[0]);
    }

    public EfficiencyRecord[] filterByMinAnnCost(ControlMeasure controlMeasure, EfficiencyRecord[] efficiencyRecords, 
            double invenControlEfficiency, double invenRulePenetration, 
            double invenRuleEffectiveness, double invenAnnualEmissions) throws EmfException {
        //return all eff records, if there is no constraint to filter on...
        if (minAnnCost == null) return efficiencyRecords;
        
        List records = new ArrayList();
        for (int i = 0; i < efficiencyRecords.length; i++) {
            if (efficiencyRecords[i].getCostPerTon() == null 
                    || efficiencyRecordUtil.calculateEmissionReduction(controlMeasure, efficiencyRecords[i], 
                    invenControlEfficiency, invenRulePenetration, 
                    invenRuleEffectiveness, invenAnnualEmissions) *
                    efficiencyRecords[i].getCostPerTon() * 
                    costYearTable.factor(CostYearTable.REFERENCE_COST_YEAR, efficiencyRecords[i].getCostYear()) < minAnnCost)
                records.add(efficiencyRecords[i]);
        }
        return (EfficiencyRecord[]) records.toArray(new EfficiencyRecord[0]);
    }
}