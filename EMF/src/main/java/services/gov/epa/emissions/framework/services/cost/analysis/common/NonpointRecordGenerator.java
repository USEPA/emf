package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class NonpointRecordGenerator implements RecordGenerator {

    private ControlStrategyResult strategyResult;

    private String comment;

    private double reducedEmission;

    private double invenControlEfficiency;

    private double invenRulePenetration;

    private double invenRuleEffectiveness;

//    private double originalEmissions;

//    private double finalEmissions;

    private DecimalFormat decFormat;

    private Double annualCost;
    
    private CostEquationFactory costEquationsFactory;

    public NonpointRecordGenerator(ControlStrategyResult result, DecimalFormat decFormat, CostEquationFactory costEquationsFactory) {
        this.strategyResult = result;
        this.comment = "";
        this.decFormat = decFormat;
        this.costEquationsFactory = costEquationsFactory;
    }

    public Record getRecord(ResultSet resultSet, BestMeasureEffRecord bestMeasureEffRecord, double originalEmissions, boolean displayOriginalEmissions, boolean displayFinalEmissions) throws SQLException, EmfException {
        Record record = new Record();
        record.add(tokens(resultSet, bestMeasureEffRecord, originalEmissions, displayOriginalEmissions, displayFinalEmissions,true));

        return record;
    }

    public List tokens(ResultSet resultSet, BestMeasureEffRecord bestMeasureEffRecord, double originalEmissions, boolean displayOriginalEmissions, boolean displayFinalEmissions,
            boolean hasSICandNAICS) throws SQLException, EmfException {
        // here the resultSet is probably the result of the query to get all the inventory records
        List<String> tokens = new ArrayList<String>();
        double effectiveReduction = bestMeasureEffRecord.effectiveReduction();
        
        calculateEmissionReduction(resultSet,bestMeasureEffRecord);
        reducedEmission = originalEmissions * effectiveReduction;
        
        
        tokens.add(""); // record id
        tokens.add("" + strategyResult.getDetailedResultDataset().getId());  // dataset ID
        tokens.add("" + 0);  // version
        tokens.add("");  // delete_versions

        tokens.add("false");  // disable
        tokens.add(bestMeasureEffRecord.measure().getAbbreviation());  // measure abbreviation
        tokens.add(resultSet.getString("poll"));
        tokens.add(resultSet.getString("scc"));
        String fullFips = resultSet.getString("fips").trim();
        tokens.add(fullFips);  // 5 digit FIPS state+county code

        // these columns are only relevant to point sources, leave empty for nonpoint
        
        CostEquation costEquations = costEquationsFactory.getCostEquation(resultSet.getString("poll"), reducedEmission, 
                bestMeasureEffRecord, null, 
                null, null, 
                null);
        
        tokens.add(""); // plant Id
        tokens.add(""); // Point ID
        tokens.add(""); // stack ID
        tokens.add(""); // segment

        tokens.add(""); // O&M
        tokens.add(""); // Annualizd Capital
        tokens.add(""); // Total Capital Cost
        annualCost=costEquations.getAnnualCost();
 //       annualCost = maxCM.adjustedCostPerTon() * reducedEmission;
        tokens.add("" + (annualCost != null ? decFormat.format(annualCost) : ""));  // annual cost for source
        tokens.add("" + (costEquations.getComputedCPT() != null ? decFormat.format(costEquations.getComputedCPT()) : ""));  // annual cost per ton
        
        tokens.add("" + decFormat.format(bestMeasureEffRecord.controlEfficiency()));   // control efficiency
        tokens.add("" + bestMeasureEffRecord.rulePenetration());  // rule penetration
        tokens.add("" + bestMeasureEffRecord.ruleEffectiveness());  // rule effectiveness
        tokens.add("" + decFormat.format(effectiveReduction * 100));   // percent reduction

        tokens.add("" + invenControlEfficiency);  // inventory CE
        tokens.add("" + invenRulePenetration);    // inventory RP
        tokens.add("" + invenRuleEffectiveness);  // inventory RE
        tokens.add("" + (displayFinalEmissions ? decFormat.format(originalEmissions - reducedEmission) : 0)); // final emissions
        tokens.add("" + decFormat.format(reducedEmission));  // emissions reduction
        tokens.add("" + (displayOriginalEmissions ? decFormat.format(originalEmissions) : 0)); // inventory emissions 

        tokens.add("1"); // inventory emissions 
        tokens.add("" + (displayOriginalEmissions ? decFormat.format(originalEmissions) : 0)); // inventory emissions 
        tokens.add("" + (displayFinalEmissions ? decFormat.format(originalEmissions - reducedEmission) : 0)); // final emissions
//        Column applyOrder = new Column("Apply_Order", types.intType(), new IntegerFormatter(), "DEFAULT 1");
//        Column inputEmissions = new Column("input_emis", types.realType(), new RealFormatter());
//        Column outputEmissions = new Column("output_emis", types.realType(), new RealFormatter());


        
        tokens.add("" + fullFips.substring(fullFips.length()-5,2));  // FIPS state
        tokens.add("" + fullFips.substring(fullFips.length()-3));    // FIPS county - accounts for possible country code
        
        if (hasSICandNAICS)  // this is needed because nonroad and onroad also execute this code and don't have SIC and NAICS
        {
            tokens.add("" + resultSet.getString("sic"));  // SIC
            tokens.add("" + resultSet.getString("naics")); // NAICS
        }
        else
        {
            tokens.add("");  // SIC
            tokens.add(""); // NAICS          
        }

        tokens.add("" + resultSet.getInt("Record_Id"));  // sourceID from inventory
        tokens.add("" + strategyResult.getInputDataset().getId());  // inputDatasetID
        tokens.add("" + strategyResult.getControlStrategyId());  
        tokens.add("" + bestMeasureEffRecord.measure().getId());  // control measureID
        tokens.add("" + comment);

        return tokens;
    }

    public void calculateEmissionReduction(ResultSet resultSet, BestMeasureEffRecord bestMeasureEffRecord) throws SQLException {
        invenControlEfficiency = resultSet.getDouble("CEFF");
        invenRulePenetration = resultSet.getDouble("RPEN");
        invenRuleEffectiveness = resultSet.getDouble("REFF");
/*
        invenControlEfficiency = resultSet.getDouble("CEFF");
        invenRulePenetration = resultSet.getDouble("RPEN");
        invenRuleEffectiveness = resultSet.getDouble("REFF");
        originalEmissions = resultSet.getDouble("ANN_EMIS");

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

    public double reducedEmission() {
        return reducedEmission;
    }

    public Double totalCost() {
        return annualCost;
    }

}
