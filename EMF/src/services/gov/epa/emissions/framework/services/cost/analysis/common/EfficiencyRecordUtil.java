package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyConstraint;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlStrategy.LocaleFilter;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.util.ArrayList;
import java.util.List;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

public class EfficiencyRecordUtil {

    private LocaleFilter localeFilter;
//    private static Log LOG = LogFactory.getLog(EfficiencyRecordUtil.class);

    public EfficiencyRecordUtil() {
        this.localeFilter = new LocaleFilter();
    }

    public double effectiveReduction(ControlMeasure controlMeasure, EfficiencyRecord record) {
        //use the measure override values, if supplied...
        return (
                    record.getEfficiency() 
                    * (controlMeasure.getRuleEffectiveness() == null ? record.getRuleEffectiveness() : controlMeasure.getRuleEffectiveness()) 
                    * (controlMeasure.getRulePenetration() == null ? record.getRulePenetration() : controlMeasure.getRulePenetration())
                )
                / (100 * 100 * 100);
    }

    public Double adjustedCostPerTon(EfficiencyRecord record, CostYearTable costYearTable) throws EmfException {
        if (record.getCostYear() == null) return 0.0;
        int costYear = record.getCostYear();
        double factor = costYearTable.factor(costYear);
        return record.getCostPerTon() != null ? factor * record.getCostPerTon() : 0;
    }

    public double calculateEmissionReduction(ControlMeasure controlMeasure, EfficiencyRecord record, 
            double invenControlEfficiency, double invenRulePenetration, 
            double invenRuleEffectiveness, double invenAnnualEmissions) {
//        double invenEffectiveReduction = invenControlEfficiency * invenRulePenetration * invenRuleEffectiveness
//                / (100 * 100 * 100);
        double effectiveReduction = effectiveReduction(controlMeasure, record);

        //FIXME -- TEMPORARY - Ignore if inv item has an exisiting measure, just replace for now...
        return invenAnnualEmissions * effectiveReduction;
        
//        if (invenEffectiveReduction == 0.0) {
//            return invenAnnualEmissions * effectiveReduction;
//        }
//
//        if (invenEffectiveReduction < effectiveReduction) {
//            return  invenAnnualEmissions / invenEffectiveReduction * effectiveReduction;
//        }
//
//        return invenAnnualEmissions / invenControlEfficiency * invenEffectiveReduction;
    }

    public EfficiencyRecord[] filter(EfficiencyRecord[] efficiencyRecords, Pollutant pollutant, 
            String fips, int inventoryYear, 
            double invenAnnualEmission) {
        List records = new ArrayList();
        Double minEmis;
        Double maxEmis;
        for (int i = 0; i < efficiencyRecords.length; i++) {

            //check the pollutant
            if (!efficiencyRecords[i].getPollutant().equals(pollutant)) 
                break;
            
            //check the locale
            if (!localeFilter.acceptLocale(efficiencyRecords[i].getLocale(), fips))
                break;

            //check the min max emission constraint
            minEmis = efficiencyRecords[i].getMinEmis();
            maxEmis = efficiencyRecords[i].getMaxEmis();
            //if no min or max is specified, then include it in the arraylist...
            if (minEmis == null && maxEmis != null)  {
                if (!(invenAnnualEmission >= 0 && invenAnnualEmission <= maxEmis))
                    break;
            } else if (minEmis != null && maxEmis != null)  {
                if (!(invenAnnualEmission >= minEmis && invenAnnualEmission <= maxEmis))
                    break;
            //assume maxEmis is infinitely big in this case...
            } else if (minEmis != null && maxEmis == null)  {
                if (!(invenAnnualEmission >= minEmis))
                    break;
            }
            
            records.add(efficiencyRecords[i]);
        }
        
        return effectiveDateFilter(localeFilter.closestRecords(records), inventoryYear);
    }

    public EfficiencyRecord[] effectiveDateFilter(EfficiencyRecord[] efficiencyRecords, int inventoryYear) {
        return new EffectiveDateFilter(efficiencyRecords, inventoryYear).filter();
    }

    public EfficiencyRecord[] pollutantFilter(EfficiencyRecord[] efficiencyRecords, Pollutant pollutant) {
        List records = new ArrayList();
        for (int i = 0; i < efficiencyRecords.length; i++) {
            if (efficiencyRecords[i].getPollutant().equals(pollutant)) {
                records.add(efficiencyRecords[i]);
            }
        }
        return (EfficiencyRecord[]) records.toArray(new EfficiencyRecord[0]);
    }

    public EfficiencyRecord[] minMaxEmisFilter(EfficiencyRecord[] efficiencyRecords, double invenAnnualEmission) {
        return new MinMaxEmissionFilter(efficiencyRecords, invenAnnualEmission).filter();
    }

    public EfficiencyRecord[] localeFilter(EfficiencyRecord[] efficiencyRecords, String fips) {
        List records = new ArrayList();
        for (int i = 0; i < efficiencyRecords.length; i++) {
            String locale = efficiencyRecords[i].getLocale();
            if (localeFilter.acceptLocale(locale, fips))
                records.add(efficiencyRecords[i]);
        }
        return localeFilter.closestRecords(records);
    }

    public EfficiencyRecord[] filterByConstraints(ControlMeasure controlMeasure, ControlStrategyConstraint constraint, CostYearTable costYearTable, EfficiencyRecord[] efficiencyRecords, 
            double invenControlEfficiency, double invenRulePenetration, double invenRuleEffectiveness, 
            double invenAnnualEmissions) throws EmfException {
        return new ConstraintFilter(constraint, costYearTable).filter(controlMeasure, efficiencyRecords, invenControlEfficiency, 
                invenRulePenetration, invenRuleEffectiveness, 
                invenAnnualEmissions);
    }
}
