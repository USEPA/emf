package gov.epa.emissions.framework.services.cost.controlStrategy;

import java.io.Serializable;

public class StrategyResultType implements Serializable {
    
    private int id;
    
    private String name;
    
    private Boolean optional;
    
    private String className;

    public static final String strategyMeasureSummary = "Strategy Measure Summary";
    
    public static final String strategyCountySummary = "Strategy County Summary";
    
    public static final String detailedStrategyResult = "Strategy Detailed Result";
    
    public static final String leastCostControlMeasureWorksheet = "Least Cost Control Measure Worksheet";
    
    public static final String leastCostCurveSummary = "Least Cost Curve Summary";
    
    public static final String controlledInventory = "Controlled Inventory";
    
    public static final String annotatedInventory = "Annotated Inventory";
    
    public static final String strategyMessages = "Strategy Messages";
    
    public static final String strategyImpactSummary = "Strategy Impact Summary";
    
    public static final String rsmPercentReduction = "RSM Percent Reduction Summary";
        
    public StrategyResultType(){
        //
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOptional(Boolean optional) {
        this.optional = optional;
    }

    public Boolean getOptional() {
        return optional;
    }

    public String toString() {
        return this.name;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
