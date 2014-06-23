package gov.epa.emissions.framework.services.cost.controlStrategy;

import java.io.Serializable;

public class ControlStrategyConstraint implements Serializable {
    
    private int id;

    private int controlStrategyId;

    private Double maxEmisReduction;

    private Double maxControlEfficiency;
    
    private Double minCostPerTon;

    private Double minAnnCost;
    
    private Double domainWideEmisReduction;

    private Double domainWidePctReduction;
    
    private Double domainWidePctReductionIncrement;
    
    private Double domainWidePctReductionStart;
    
    private Double domainWidePctReductionEnd;
    
    private Double replacementControlMinEfficiencyDiff;
    
    private Double controlProgramMeasureMinPctRedDiff;
    
    public ControlStrategyConstraint() {
        //
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getControlStrategyId() {
        return controlStrategyId;
    }

    public void setControlStrategyId(int id) {
        this.controlStrategyId = id;
    }

    public void setMaxEmisReduction(Double maxEmisReduction) {
        this.maxEmisReduction = maxEmisReduction;
    }

    public Double getMaxEmisReduction() {
        return maxEmisReduction;
    }

    public void setMaxControlEfficiency(Double maxControlEfficiency) {
        this.maxControlEfficiency = maxControlEfficiency;
    }

    public Double getMaxControlEfficiency() {
        return maxControlEfficiency;
    }

    public void setMinCostPerTon(Double minCostPerTon) {
        this.minCostPerTon = minCostPerTon;
    }

    public Double getMinCostPerTon() {
        return minCostPerTon;
    }

    public void setMinAnnCost(Double minAnnCost) {
        this.minAnnCost = minAnnCost;
    }

    public Double getMinAnnCost() {
        return minAnnCost;
    }
    
    public boolean hasConstraints() {
        return maxEmisReduction != null 
            || maxControlEfficiency != null 
            || minCostPerTon != null 
            || minAnnCost != null;
    }

    public void setDomainWideEmisReduction(Double domainWideEmisReduction) {
        this.domainWideEmisReduction = domainWideEmisReduction;
    }

    public Double getDomainWideEmisReduction() {
        return domainWideEmisReduction;
    }

    public void setDomainWidePctReduction(Double domainWidePctReduction) {
        this.domainWidePctReduction = domainWidePctReduction;
    }

    public Double getDomainWidePctReduction() {
        return domainWidePctReduction;
    }

    public void setDomainWidePctReductionIncrement(Double domainWidePctReductionIncrement) {
        this.domainWidePctReductionIncrement = domainWidePctReductionIncrement;
    }

    public Double getDomainWidePctReductionIncrement() {
        return domainWidePctReductionIncrement;
    }

    public void setDomainWidePctReductionStart(Double domainWidePctReductionStart) {
        this.domainWidePctReductionStart = domainWidePctReductionStart;
    }

    public Double getDomainWidePctReductionStart() {
        return domainWidePctReductionStart;
    }

    public void setDomainWidePctReductionEnd(Double domainWidePctReductionEnd) {
        this.domainWidePctReductionEnd = domainWidePctReductionEnd;
    }

    public Double getDomainWidePctReductionEnd() {
        return domainWidePctReductionEnd;
    }

    public void setReplacementControlMinEfficiencyDiff(Double replacementControlMinEfficiencyDiff) {
        this.replacementControlMinEfficiencyDiff = replacementControlMinEfficiencyDiff;
    }

    public Double getReplacementControlMinEfficiencyDiff() {
        return replacementControlMinEfficiencyDiff;
    }

    public void setControlProgramMeasureMinPctRedDiff(Double controlProgramMeasureMinPctRedDiff) {
        this.controlProgramMeasureMinPctRedDiff = controlProgramMeasureMinPctRedDiff;
    }

    public Double getControlProgramMeasureMinPctRedDiff() {
        return controlProgramMeasureMinPctRedDiff;
    }
}