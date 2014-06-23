package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;


public class Type3CostEquation implements CostEquation {

    private BestMeasureEffRecord bestMeasureEffRecord;
    
    private double emissionReduction; 
    private double discountRate;
    private Double minStackFlowRate;
    private CostYearTable costYearTable;
    
    private static final double capitalCostFactor=192;
    private static final double gasFlowRateFactor=.486;
    private static final double retrofitFactor=1.1;
    private int costYear;
   
    public Type3CostEquation(CostYearTable costYearTable, double discountRate) {
        this.costYearTable = costYearTable;
        this.discountRate = discountRate / 100;
    }

    public void setUp(double emissionReduction, BestMeasureEffRecord bestMeasureEffRecord, Double minStackFlowRate) {
        //define required inputs
        this.bestMeasureEffRecord = bestMeasureEffRecord;
        this.minStackFlowRate = minStackFlowRate;
        this.emissionReduction=emissionReduction;     
        this.costYear = bestMeasureEffRecord.measure().getCostYear();
    }
    

    public Double getAnnualCost() throws EmfException {
        Double capitalCost = getCapitalCost();
        Double capRecFactor=getCapRecFactor();
        Double operationMaintenanceCost = getOperationMaintenanceCost();
        if (capRecFactor == null || capitalCost == null || operationMaintenanceCost == null) return null;
        return capitalCost * capRecFactor + operationMaintenanceCost;
    }

    public Double getCapitalCost() throws EmfException {
        if (minStackFlowRate == null || minStackFlowRate == 0.0) return null;
        if (minStackFlowRate <1028000 )
            return costYearTable.factor(costYear) * Math.pow(1028000/minStackFlowRate, 0.6)*capitalCostFactor*gasFlowRateFactor*retrofitFactor*minStackFlowRate;
        return costYearTable.factor(costYear) * capitalCostFactor*gasFlowRateFactor*retrofitFactor*minStackFlowRate;
    }  
    
    public Double getOperationMaintenanceCost() throws EmfException {
        if (minStackFlowRate == null || minStackFlowRate == 0.0) return null;
        return costYearTable.factor(costYear) * (3.35+(0.00729*8736))* minStackFlowRate*0.9383;
    }
    
    public Double getAnnualizedCapitalCost() throws EmfException { 
        Double capitalCost = getCapitalCost();
        Double capRecFactor=getCapRecFactor();
        if (capitalCost == null || capRecFactor == null) return null;
        return capitalCost * capRecFactor;
    }

    public Double getComputedCPT() throws EmfException {
        Double totalCost=getAnnualCost();
        if (totalCost==null || emissionReduction == 0.0) return null; 
        return totalCost/emissionReduction;
    }
    
    public Double getCapRecFactor(){
        // Calculate capital recovery factor
        return getCapRecFactor(bestMeasureEffRecord.measure().getEquipmentLife(), 
                bestMeasureEffRecord.efficiencyRecord().getCapRecFactor());
    }

    public Double getCapRecFactor(float equipmentLife, Double effRecCapRecFactor){
        // Calculate capital recovery factor 
        Double capRecFactor = effRecCapRecFactor;
//        if (effRecCapRecFactor != null && effRecCapRecFactor!=0) 
//            return capRecFactor;
        
        if (equipmentLife!=0) 
             capRecFactor = DefaultCostEquation.calculateCapRecFactor(discountRate, equipmentLife);
        
        if (capRecFactor != null && capRecFactor != 0) {
            return capRecFactor; 
        }
        return null;
    }
    

}