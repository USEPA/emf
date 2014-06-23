package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.framework.services.cost.ControlMeasureEquation;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;

public class CostEquationFactory {

    private boolean useCostEquations;
    private DefaultCostEquation defaultCostEquations;
    private Type8CostEquation type8CostEquation;
    private Type6CostEquation type6CostEquation;
    private Type5CostEquation type5CostEquation;
    private Type4CostEquation type4CostEquation;
    private Type3CostEquation type3CostEquation;
    private Type2CostEquation type2CostEquation;
    private Type1CostEquation type1CostEquation;
    
    public CostEquationFactory(CostYearTable costYearTable,
            boolean useCostEquations, double discountRate) {
        this.useCostEquations = useCostEquations;
        this.defaultCostEquations = new DefaultCostEquation(discountRate);
        this.type8CostEquation = new Type8CostEquation(costYearTable, discountRate);
        this.type6CostEquation = new Type6CostEquation(costYearTable, discountRate);
        this.type5CostEquation = new Type5CostEquation(costYearTable, discountRate);
        this.type4CostEquation = new Type4CostEquation(costYearTable, discountRate);
        this.type3CostEquation = new Type3CostEquation(costYearTable, discountRate);
        this.type2CostEquation = new Type2CostEquation(costYearTable, discountRate);
        this.type1CostEquation = new Type1CostEquation(costYearTable, discountRate);
    }

    public CostEquation getCostEquation(String pollutantName, double reducedEmission, 
            BestMeasureEffRecord bestMeasureEffRecord, Double minStackFlowRate, 
            Double designCapacity, String designCapacityUnitNumerator,
            String designCapacityUnitDenominator) {
        //always setup the default cost equation, the other equation types will default to using this approach when the other equation don't work
        //for example, maybe some of the inputs are missing, or maybe some constraint is not met...
        defaultCostEquations.setUp(reducedEmission, bestMeasureEffRecord);

        //use CoST equations only for the measures major pollutant, else use the default approach
        if (useCostEquations && pollutantName.equalsIgnoreCase(bestMeasureEffRecord.measure().getMajorPollutant().getName())) {
            //see which type of equation to use...
            ControlMeasureEquation[] equations = bestMeasureEffRecord.measure().getEquations();
            
            //NOTE, we are currently only supporting one equation type, in the future we might need to support multiple 
            //equations (i.e., if Type 6 is the primary but we are missing some inputs we might want to try Type 10, and if
            //we don't have all the inputs for this Type, then we could use default equation approach) 
            if (equations.length > 0) {
                
                //use type 8 equation...
                if (equations[0].getEquationType().getName().equals("Type 8")) {
                    //evaluate inputs, if they missing, use the default
                    if (minStackFlowRate != null && minStackFlowRate != 0.0) {
                        type8CostEquation.setUp(reducedEmission, bestMeasureEffRecord, 
                                minStackFlowRate);
                        return type8CostEquation;
                    }
                }
                
                //use type 6 equation...
                if (equations[0].getEquationType().getName().equals("Type 6")) {
                    //evaluate inputs, if they missing, use the default
                    if (minStackFlowRate != null && minStackFlowRate != 0.0) {
                        type6CostEquation.setUp(reducedEmission, bestMeasureEffRecord, 
                                minStackFlowRate);
                        return type6CostEquation;
                    }
                }
                
                if (equations[0].getEquationType().getName().equals("Type 5")) {
                    //evaluate inputs, if they missing, use the default
                    if (minStackFlowRate != null && minStackFlowRate != 0.0) {
                        type5CostEquation.setUp(reducedEmission, bestMeasureEffRecord, 
                                minStackFlowRate);
                        return type5CostEquation;
                    }
                }
                
                if (equations[0].getEquationType().getName().equals("Type 4")) {
                    //evaluate inputs, if they missing, use the default
                    if (minStackFlowRate != null && minStackFlowRate != 0.0) {
                        type4CostEquation.setUp(reducedEmission, bestMeasureEffRecord, 
                                minStackFlowRate);
                        return type4CostEquation;
                    }
                }
                
                if (equations[0].getEquationType().getName().equals("Type 3")) {
                    //evaluate inputs, if they missing, use the default
                    if (minStackFlowRate != null && minStackFlowRate != 0.0) {
                        type3CostEquation.setUp(reducedEmission, bestMeasureEffRecord, 
                                minStackFlowRate);
                        return type3CostEquation;
                    }
                }
                
                if (equations[0].getEquationType().getName().equals("Type 2")) {
                    //evaluate inputs, if they missing, use the default
                    //design capacity must be less than or equal to 2000 MMBTU/hr (or 586.1665 MW))
                    if (designCapacity != null && designCapacity != 0.0 
                            && type2CostEquation.convertDesignCapacity(designCapacity, designCapacityUnitNumerator, 
                                    designCapacityUnitDenominator) != null
                            && type2CostEquation.convertDesignCapacity(designCapacity, designCapacityUnitNumerator, 
                                    designCapacityUnitDenominator) <= 586.1665) {
                        type2CostEquation.setUp(reducedEmission, bestMeasureEffRecord, 
                                designCapacity, designCapacityUnitNumerator,
                                designCapacityUnitDenominator);
                        return type2CostEquation;
                    }
                }
                
                if (equations[0].getEquationType().getName().equals("Type 1")) {
                    //evaluate inputs, if they missing, use the default
                    if (designCapacity != null && designCapacity != 0.0) {
                        type1CostEquation.setUp(reducedEmission, bestMeasureEffRecord, 
                                designCapacity);
                        return type1CostEquation;
                    }
                }
                
                //future equations go here...
            }
      }

      return defaultCostEquations;
    }



}
