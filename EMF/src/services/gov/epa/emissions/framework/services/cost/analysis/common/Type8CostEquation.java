package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasureEquation;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;

public class Type8CostEquation implements CostEquation {

    private BestMeasureEffRecord bestMeasureEffRecord;
    private double discountRate;
    private Double stackFlowRate;
    private double reducedEmission;
    private Double capRecFactor;
    private Double capCostFactor;
    private Double operMaintCostFactor;
    private Double defaultCapitalCPTFactor;
    private Double defaultOperMaintCPTFactor;
    private Double defaultAnnualizedCPTFactor;
    private boolean hasAllVariables = true;
    private CostYearTable costYearTable;
    private int costYear;

    public Type8CostEquation(CostYearTable costYearTable, double discountRate) {
        this.costYearTable = costYearTable;
        this.discountRate = discountRate / 100;     
    }

    public void setUp(double emissionReduction, BestMeasureEffRecord bestMeasureEffRecord, 
            Double stackFlowRate) {
        //define required inputs
        this.bestMeasureEffRecord = bestMeasureEffRecord;
        this.stackFlowRate = stackFlowRate;
        this.reducedEmission=emissionReduction;
        this.capRecFactor=getCapRecFactor();
        this.costYear = bestMeasureEffRecord.measure().getCostYear();
        //populate variables for use with the equations
        populateVariables();
    }

    private void populateVariables() {
        ControlMeasureEquation[] equations = bestMeasureEffRecord.measure().getEquations();
        for (int i = 0; i < equations.length; i++) {
            capCostFactor = equations[i].getValue1();
            operMaintCostFactor = equations[i].getValue2();
            defaultCapitalCPTFactor = equations[i].getValue3();
            defaultOperMaintCPTFactor = equations[i].getValue4();
            defaultAnnualizedCPTFactor = equations[i].getValue5();
        }
        if (capCostFactor == null
                || operMaintCostFactor == null
                || defaultCapitalCPTFactor == null
                || defaultOperMaintCPTFactor == null
                || defaultAnnualizedCPTFactor == null) 
            hasAllVariables = false;
    }

    public Double getAnnualCost() throws EmfException {
        Double capitalCost = getCapitalCost();
        if (!hasAllVariables
                || capitalCost == null 
                || getOperationMaintenanceCost() == null
                || capRecFactor == null) return null;
        //stack flow rate is greater than or equal 5 cfm
        if (stackFlowRate >= 5)
            return costYearTable.factor(costYear) * (capitalCost * capRecFactor + 0.04 * capitalCost)
                + getOperationMaintenanceCost();
        //stack flow rate is less than 5 cfm
        return costYearTable.factor(costYear) * defaultAnnualizedCPTFactor * reducedEmission;
    }

    public Double getCapitalCost() throws EmfException {
        if (!hasAllVariables
                || stackFlowRate == null 
                || stackFlowRate == 0.0) return null;
        //stack flow rate is greater than or equal 5 cfm
        if (stackFlowRate >= 5)
            return costYearTable.factor(costYear) * capCostFactor * stackFlowRate;
        //stack flow rate is less than 5 cfm
        return costYearTable.factor(costYear) * defaultCapitalCPTFactor * reducedEmission;
    }

    public Double getOperationMaintenanceCost() throws EmfException {
        if (!hasAllVariables
                || stackFlowRate == null 
                || stackFlowRate == 0.0) return null;
        //stack flow rate is greater than or equal 5 cfm
        if (stackFlowRate >= 5)
            return costYearTable.factor(costYear) * operMaintCostFactor * stackFlowRate;
        //stack flow rate is less than 5 cfm
        return costYearTable.factor(costYear) * defaultOperMaintCPTFactor * reducedEmission;
    }
    
    public Double getAnnualizedCapitalCost() throws EmfException { 
        Double capitalCost = getCapitalCost();
        if (capitalCost == null || capRecFactor == null) return null;
        return capitalCost * capRecFactor;
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
    
//    public String toString(){
//        return "Typical Capital Control Cost Factor: " + capCostFactor +
//        " Annual cost: " + getAnnualCost() + "  "
//        + defaultCapitalCPTFactor +" "+defaultOperMaintCPTFactor+ " "
//        + defaultAnnualizedCPTFactor+" "+ hasAllVariables;
//    }
}