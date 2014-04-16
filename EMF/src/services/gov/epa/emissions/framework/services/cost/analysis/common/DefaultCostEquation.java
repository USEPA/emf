package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.framework.services.EmfException;

public class DefaultCostEquation implements CostEquation {
    
    private BestMeasureEffRecord bestMeasureEffRecord;
    private double emissionReduction;
    private double discountRate;
    private Double capitalCost;
    
    private Double annulizedCCost;
    private Double annualCost;

    public DefaultCostEquation(double discountRate) {
        this.discountRate = discountRate / 100;
    }

    public void setUp(double emissionReduction, BestMeasureEffRecord bestMeasureEffRecord) {
        this.bestMeasureEffRecord = bestMeasureEffRecord;
        this.emissionReduction = emissionReduction;
        
        //
    }

    public Double getAnnualCost() throws EmfException {
        double tAnnualCost = emissionReduction * bestMeasureEffRecord.adjustedCostPerTon();
        if (tAnnualCost == 0) return null;         
        return tAnnualCost;
    }

    public Double getCapitalCost() throws EmfException {
        annualCost=getAnnualCost();
        Double capAnnRatio = bestMeasureEffRecord.efficiencyRecord().getCapitalAnnualizedRatio();
        
        if (capAnnRatio == null || annualCost==null){
            return null;
        }     
        return capAnnRatio * annualCost;
    }  
    
    public Double getOperationMaintenanceCost() throws EmfException {
        annulizedCCost = getAnnualizedCapitalCost();
        annualCost=getAnnualCost();
        if (annulizedCCost == null) return annualCost;
        double omCost = annualCost - annulizedCCost;
        if (omCost==0.0) return null; 
        return omCost;
    }
    
    public Double getAnnualizedCapitalCost() throws EmfException { 
        Double capRecFactor=getCapRecFactor();
        capitalCost = getCapitalCost();
        if (capitalCost == null || capRecFactor == null) return null;
        return capitalCost * capRecFactor;
    }

   
    public Double getCapRecFactor(){
        // Calculate capital recovery factor
        return getCapRecFactor(bestMeasureEffRecord.measure().getEquipmentLife(), 
                bestMeasureEffRecord.efficiencyRecord().getCapRecFactor());
    }
    
    public Double getCapRecFactor(float equipmentLife, Double effRecCapRecFactor){
        // Calculate capital recovery factor 
        Double capRecFactor = effRecCapRecFactor;
        if (equipmentLife!=0) 
             capRecFactor = DefaultCostEquation.calculateCapRecFactor(discountRate, equipmentLife);
        
        if (capRecFactor != null && capRecFactor != 0) {
            return capRecFactor; 
        }
        return null;
    }
    
    public static Double calculateCapRecFactor(double discountRate, double equipmentLife) {
        if(discountRate==0 || equipmentLife==0) return null;
        return (discountRate * Math.pow((1 + discountRate), equipmentLife)) / (Math.pow((discountRate + 1), equipmentLife) - 1);
    }

    public Double getComputedCPT() {
        try { 
            annualCost=getAnnualCost();
        } catch (EmfException e){ 
           e.printStackTrace(); 
        }
        if (annualCost==null ||annualCost==0.0 || emissionReduction==0.0 ) return null;
        return annualCost/emissionReduction;
    }
    
}

