package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasureEquation;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;

public class Type1CostEquation implements CostEquation {

    private BestMeasureEffRecord bestMeasureEffRecord;
    private double discountRate;
    private Double boilerCapacity;
    private double reducedEmission;
    private Double capRecFactor;
    private Double capCostMultiplier;
    private Double omCostMultiFixed;
    private Double omCostMultiVariable;
    private Double scalingFactorSize;
    private Double scalingFactorExponent;
    private Double capacityFactor;
//    private Double incCapCostMultiplier;
//    private Double incCapCostExponent;
//    private Double incAnnCostMultiplier;
//    private Double incAnnCostExponent;
    private int costYear;
    private CostYearTable costYearTable;

    public Type1CostEquation(CostYearTable costYearTable, double discountRate) {
        this.costYearTable = costYearTable;
        this.discountRate = discountRate / 100;     
    }

    public void setUp(double emissionReduction, BestMeasureEffRecord bestMeasureEffRecord, Double boilerCapacity) {
        //define required inputs
        this.bestMeasureEffRecord = bestMeasureEffRecord;
        this.boilerCapacity = boilerCapacity;
        this.reducedEmission=emissionReduction;
        this.capRecFactor=getCapRecFactor();
        this.costYear = bestMeasureEffRecord.measure().getCostYear();
        //populate variables for use with the equations
        populateVariables();
    }

    private void populateVariables() {
        ControlMeasureEquation[] equations = bestMeasureEffRecord.measure().getEquations();
        for (int i = 0; i < equations.length; i++) {
            capCostMultiplier = equations[i].getValue1();
            omCostMultiFixed = equations[i].getValue2();
            omCostMultiVariable = equations[i].getValue3();
            scalingFactorSize = equations[i].getValue4();
            scalingFactorExponent = equations[i].getValue5();
            capacityFactor = equations[i].getValue6();
//                    } else if (variableName.equalsIgnoreCase("Incremental Capital Cost Multiplier")) {
//                        incCapCostMultiplier = equations[i].getValue();
//                    } else if (variableName.equalsIgnoreCase("Incremental Capital Cost Exponent")) {
//                        incCapCostExponent = equations[i].getValue();
//                    } else if (variableName.equalsIgnoreCase("Incremental Annual Cost Multiplier")) {
//                        incAnnCostMultiplier = equations[i].getValue();
//                    } else if (variableName.equalsIgnoreCase("Incremental Annual Cost Exponent")) {
//                        incAnnCostExponent = equations[i].getValue();
        }
    }

    public Double getAnnualCost() throws EmfException {
        if (boilerCapacity == null 
                || capCostMultiplier == 0.0 || capCostMultiplier== null
                || omCostMultiFixed == 0.0 || omCostMultiFixed ==null
                || omCostMultiVariable == 0.0 || omCostMultiVariable == null
                || scalingFactorExponent == null) return null;
        return getAnnualizedCapitalCost() + getOperationMaintenanceCost();
    }

    public Double getCapitalCost() throws EmfException {
        Double scallFactor=getScallingFactor();
        if (boilerCapacity == null 
                || capCostMultiplier == 0.0 || capCostMultiplier== null
                || scallFactor==0.0         || scallFactor== null) return null;
        return costYearTable.factor(costYear) * capCostMultiplier * boilerCapacity*scallFactor*1000;
    }

    public Double getScallingFactor() {
        String abbre=bestMeasureEffRecord.measure().getAbbreviation();
        if ((abbre=="NSCR_UBCW" ||abbre =="NSCR_UBCT") && boilerCapacity>=600)  return 1.0; 
        if (boilerCapacity>=500) return 1.0; 
        if (scalingFactorSize==0.0 ||scalingFactorSize==null
                || scalingFactorExponent==0.0 ||scalingFactorExponent==null)
            return null;
        return Math.pow(scalingFactorSize, scalingFactorExponent);
    }

    public Double getOperationMaintenanceCost() throws EmfException {
        Double omCostFixed=getOperationMaintenanceCostFixed();
        Double omCostvariable=getOperationMaintenanceCostVariable();
        if (omCostFixed == null 
                || omCostvariable == null) return null;
        return costYearTable.factor(costYear) * (omCostFixed+omCostvariable);
    }
    
    public Double getOperationMaintenanceCostFixed(){
        if (boilerCapacity == null 
        || omCostMultiFixed == 0.0 || omCostMultiFixed== null) return null;
        return omCostMultiFixed*boilerCapacity*1000;
    }
    
    public Double getOperationMaintenanceCostVariable() throws EmfException {
        if (boilerCapacity == null 
        || capacityFactor == 0.0 || capacityFactor== null
        || omCostMultiVariable == 0.0 || omCostMultiVariable== null) return null;
        return costYearTable.factor(costYear) * omCostMultiVariable*boilerCapacity*capacityFactor*8760 ;
    }
    
    public Double getAnnualizedCapitalCost() throws EmfException { 
        Double capitalCost = getCapitalCost();
        if (capitalCost == null || capRecFactor == null) return null;
        return costYearTable.factor(costYear) * capitalCost * capRecFactor;
    }

    public Double getComputedCPT() throws EmfException {
        Double totalCost=getAnnualCost();
        if (totalCost==null || reducedEmission == 0.0) return null; 
        return totalCost/reducedEmission;
    }
    public Double getCapRecFactor() {
        // Calculate capital recovery factor
        return getCapRecFactor(bestMeasureEffRecord.measure().getEquipmentLife(), 
                bestMeasureEffRecord.efficiencyRecord().getCapRecFactor());
        
    }

    public Double getCapRecFactor(float equipmentLife, Double effRecCapRecFactor) {
        // Calculate capital recovery factor 
        Double capRecFactor = effRecCapRecFactor;
        if (equipmentLife!=0) 
             capRecFactor = DefaultCostEquation.calculateCapRecFactor(discountRate, equipmentLife);
        
        if (capRecFactor != null && capRecFactor != 0) {
            return capRecFactor; 
        }
        return null;
    }
}