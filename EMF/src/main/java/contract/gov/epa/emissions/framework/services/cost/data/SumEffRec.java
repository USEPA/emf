package gov.epa.emissions.framework.services.cost.data;

import gov.epa.emissions.commons.data.Pollutant;

import java.io.Serializable;

public class SumEffRec implements Serializable {

    private Pollutant pollutant;
    private Float maxCE;
    private Float minCE;
    private Float avgCE;
    private Float maxCPT;
    private Float minCPT;
    private Float avgCPT;
    private Float avgRE;
    private Float avgRP;

    public SumEffRec() {
        //
    }

    public SumEffRec(Pollutant pollutant, Float maxEfficiency,
            Float minEfficiency, Float avgEfficiency,
            Float maxCostPerTon, Float minCostPerTon,
            Float avgCostPerTon, Float avgRuleEffectiveness,
            Float avgRulePenetration) {
        this.pollutant = pollutant;
        this.maxCE = maxEfficiency;
        this.minCE = minEfficiency;
        this.avgCE = avgEfficiency;
        this.maxCPT = maxCostPerTon;
        this.minCPT = minCostPerTon;
        this.avgCPT = avgCostPerTon;
        this.avgRE = avgRuleEffectiveness;
        this.avgRP = avgRulePenetration;
    }
    
    public Pollutant getPollutant() {
        return pollutant;
    }

    public void setPollutant(Pollutant pollutant) {
        this.pollutant = pollutant;
    }

    public Float getMaxCE() {
        return maxCE;
    }

    public void setMaxCE(Float maxEfficiency) {
        this.maxCE = maxEfficiency;
    }

    public Float getMinCE() {
        return minCE;
    }

    public void setMinCE(Float minEfficiency) {
        this.minCE = minEfficiency;
    }

    public Float getAvgCE() {
        return avgCE;
    }

    public void setAvgCE(Float avgEfficiency) {
        this.avgCE = avgEfficiency;
    }

    public Float getMaxCPT() {
        return maxCPT;
    }

    public void setMaxCPT(Float maxCostPerTon) {
        this.maxCPT = maxCostPerTon;
    }

    public Float getMinCPT() {
        return minCPT;
    }

    public void setMinCPT(Float minCostPerTon) {
        this.minCPT = minCostPerTon;
    }

    public Float getAvgCPT() {
        return avgCPT;
    }

    public void setAvgCPT(Float avgCostPerTon) {
        this.avgCPT = avgCostPerTon;
    }

    public Float getAvgRE() {
        return avgRE;
    }

    public void setAvgRE(Float avgRuleEffectiveness) {
        this.avgRE = avgRuleEffectiveness;
    }

    public Float getAvgRP() {
        return avgRP;
    }

    public void setAvgRP(Float avgRulePenetration) {
        this.avgRP = avgRulePenetration;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof SumEffRec))
            return false;

        return true;
    }

    public int hashCode() {
        return this.hashCode();
    }

}
