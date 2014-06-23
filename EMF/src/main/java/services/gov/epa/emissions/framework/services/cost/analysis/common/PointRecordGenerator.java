package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PointRecordGenerator implements RecordGenerator {
    private ControlStrategyResult strategyResult;
    private String comment = "";
    private double reducedEmission;
    private double invenControlEfficiency;
    private double invenRulePenetration;
    private double invenRuleEffectiveness;
    private Double annualCost;
    private DecimalFormat decFormat;
    private CostEquationFactory costEquationsFactory;
    
    public PointRecordGenerator(DatasetType datasetType, ControlStrategyResult result, DecimalFormat decFormat, CostEquationFactory costEquationsFactory) {
        this.strategyResult = result;
        this.decFormat = decFormat;
        this.costEquationsFactory = costEquationsFactory;
    }

    public Record getRecord(ResultSet resultSet, BestMeasureEffRecord maxCM, double originalEmissions,  boolean displayOriginalEmissions, boolean displayFinalEmissions) throws SQLException, EmfException {
        Record record = new Record();
        record.add(tokens(resultSet, maxCM, originalEmissions, displayOriginalEmissions, displayFinalEmissions, true));

        return record;
    }

    public double reducedEmission() {
        return reducedEmission;
    }

    public List tokens(ResultSet resultSet, BestMeasureEffRecord bestMeasureEffRecord, double originalEmissions, boolean displayOriginalEmissions, boolean displayFinalEmissions,
            boolean hasSICandNAICS) throws SQLException, EmfException {
        List<String> tokens = new ArrayList<String>();
        double effectiveReduction = bestMeasureEffRecord.effectiveReduction();
        Double om;
        Double annulizedCCost;
        Double capitalCost;
       
        calculateEmissionReduction(resultSet, bestMeasureEffRecord);
        reducedEmission = originalEmissions * effectiveReduction;
        Double minStackFlowRate = resultSet.getDouble("stkflow");
        if (resultSet.wasNull()) minStackFlowRate = null;
        Double designCapacity = null;
        String designCapacityUnitNumerator = null;
        String designCapacityUnitDenominator = null;
        //these fields might not be in the table, yet these were added later, so if this is an older table,
        //the table won't have them
        try {
            designCapacity = resultSet.getDouble("design_capacity");
            if (resultSet.wasNull()) designCapacity = null;
            designCapacityUnitNumerator = resultSet.getString("design_capacity_unit_numerator");
            if (resultSet.wasNull()) designCapacityUnitNumerator = null;
            designCapacityUnitDenominator = resultSet.getString("design_capacity_unit_denominator");
            if (resultSet.wasNull()) designCapacityUnitDenominator = null;
        } catch (Exception e) {
            //Do nothing, see remark above...
        }
        CostEquation costEquations = costEquationsFactory.getCostEquation(resultSet.getString("poll"), reducedEmission, 
                bestMeasureEffRecord, minStackFlowRate, 
                designCapacity, designCapacityUnitNumerator,
                designCapacityUnitDenominator);
        
        
        tokens.add(""); // record id
        tokens.add("" + strategyResult.getDetailedResultDataset().getId());  //dataset ID
        tokens.add("" + 0);  // version
        tokens.add("");     //delete_versions

        tokens.add("false");    //disable
        tokens.add(bestMeasureEffRecord.measure().getAbbreviation());  //measure abbreviation
        tokens.add(resultSet.getString("poll"));
        tokens.add(resultSet.getString("scc"));
        String fullFips = resultSet.getString("fips").trim();
        tokens.add(fullFips);  // 5 digit FIPS state+county code

        tokens.add(resultSet.getString("PLANTID")); //plant ID
        tokens.add(resultSet.getString("POINTID")); //point ID
        tokens.add(resultSet.getString("STACKID")); //stack ID
        tokens.add(resultSet.getString("SEGMENT")); //segment
        
       
        om=costEquations.getOperationMaintenanceCost();
        tokens.add("" + (om!= null ? decFormat.format(om) : ""));
        annulizedCCost=costEquations.getAnnualizedCapitalCost();
        tokens.add("" + (annulizedCCost != null ? decFormat.format(annulizedCCost) : ""));//capital cost
        capitalCost=costEquations.getCapitalCost();
        tokens.add("" + (capitalCost != null ? decFormat.format(capitalCost) : ""));//Total capital cost
        annualCost=costEquations.getAnnualCost();
        tokens.add("" +  (annualCost!= null ? decFormat.format(annualCost) : ""));
                //maxCM.adjustedCostPerTon() * reducedEmission));//annual cost for source
        
        tokens.add("" + (costEquations.getComputedCPT() != null ? decFormat.format(costEquations.getComputedCPT()) : "")); //annual cost per ton
        tokens.add("" + decFormat.format(bestMeasureEffRecord.controlEfficiency()));   //control efficiency
        tokens.add("" + 100);
        tokens.add("" + bestMeasureEffRecord.ruleEffectiveness()); //rule effectiveness
        tokens.add("" + decFormat.format(bestMeasureEffRecord.effectiveReduction() * 100));

        tokens.add("" + invenControlEfficiency);    // inventory CE
        tokens.add("" + invenRulePenetration);      // inventory RP   
        tokens.add("" + invenRuleEffectiveness);    // inventory RE
        tokens.add("" + (displayFinalEmissions ? decFormat.format(originalEmissions - reducedEmission) : 0));  //final emissions
        tokens.add("" + decFormat.format(reducedEmission));     // emissions reduction
        tokens.add("" + (displayOriginalEmissions ? originalEmissions : 0));    //inventory emissions
        tokens.add("1"); // inventory emissions 
        tokens.add("" + (displayOriginalEmissions ? decFormat.format(originalEmissions) : 0)); // inventory emissions 
        tokens.add("" + (displayFinalEmissions ? decFormat.format(originalEmissions - reducedEmission) : 0)); // final emissions

        tokens.add("" + fullFips.substring(fullFips.length()-5,2));  // FIPS state
        tokens.add("" + fullFips.substring(fullFips.length()-3));    // FIPS county - accounts for possible country code
        tokens.add("" + resultSet.getString("sic"));  // SIC
        tokens.add("" + resultSet.getString("naics")); // NAICS
        
        tokens.add("" + resultSet.getInt("Record_Id")); // sourceID from inventory
        tokens.add("" + strategyResult.getInputDataset().getId());    //inputDatasetID
        tokens.add("" + strategyResult.getControlStrategyId());
        tokens.add("" + bestMeasureEffRecord.measure().getId());   // control measureID
        tokens.add("" + comment);

        return tokens;
    }
    
    public void calculateEmissionReduction(ResultSet resultSet, BestMeasureEffRecord maxMeasure) throws SQLException {
        invenControlEfficiency = resultSet.getFloat("CEFF");
        invenRulePenetration = 100;
        invenRuleEffectiveness = resultSet.getFloat("CEFF") > 0 && resultSet.getFloat("REFF") == 0 ? 100 : resultSet.getFloat("REFF");
/*
        originalEmissions = resultSet.getFloat("ANN_EMIS");

//        double invenEffectiveReduction = invenControlEfficiency * invenRulePenetration * invenRuleEffectiveness
//                / (100 * 100 * 100);
        double effectiveReduction = maxMeasure.effectiveReduction();

        reducedEmission = 0.0;
        finalEmissions = 0.0;

        //FIXME -- TEMPORARY - Ignore if inv item has an exisiting measure, just replace for now...
        reducedEmission = originalEmissions * effectiveReduction;
        finalEmissions = originalEmissions - reducedEmission;
*/
        if (1 == 2) throw new SQLException("");
        return;

//        if (invenEffectiveReduction == 0.0) {
//            reducedEmission = originalEmissions * effectiveReduction;
//            finalEmissions = originalEmissions - reducedEmission;
//            return;
//        }
//
//        if (invenEffectiveReduction < effectiveReduction) {
//            this.comment += "Existing control measure replaced; ";
//            originalEmissions = originalEmissions / invenEffectiveReduction;
//            reducedEmission = originalEmissions * effectiveReduction;
//            finalEmissions = originalEmissions - reducedEmission;
//            return;
//        }
//
//        this.comment += "Controlled with existing control measure; ";
//        originalEmissions = originalEmissions / invenControlEfficiency;
//        reducedEmission = originalEmissions * invenEffectiveReduction;
//        finalEmissions = originalEmissions - reducedEmission;
    }

    public Double totalCost() {
        return annualCost;
    }

}
