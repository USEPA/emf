package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.ControlStrategyMeasure;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.Serializable;

public class ControlStrategyTargetPollutant implements Serializable {
    
    private int id;

    private Pollutant pollutant;

    private Double maxEmisReduction;

    private Double maxControlEfficiency;

    private Double minCostPerTon;

    private Double minAnnCost;

    private Double replacementControlMinEfficiencyDiff;

    private String invFilter;

    private EmfDataset countyDataset;

    private Integer countyDatasetVersion;

    public ControlStrategyTargetPollutant() {
        //
    }
    
    public ControlStrategyTargetPollutant(Pollutant pollutant) {
        this.pollutant = pollutant;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Pollutant getPollutant() {
        return pollutant;
    }

    public void setPollutant(Pollutant pollutant) {
        this.pollutant = pollutant;
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

    public void setReplacementControlMinEfficiencyDiff(Double replacementControlMinEfficiencyDiff) {
        this.replacementControlMinEfficiencyDiff = replacementControlMinEfficiencyDiff;
    }

    public Double getReplacementControlMinEfficiencyDiff() {
        return replacementControlMinEfficiencyDiff;
    }

    public void setCountyDataset(EmfDataset countyDataset) {
        this.countyDataset = countyDataset;
    }

    public EmfDataset getCountyDataset() {
        return countyDataset;
    }

    public void setCountyDatasetVersion(Integer countyDatasetVersion) {
        this.countyDatasetVersion = countyDatasetVersion;
    }

    public Integer getCountyDatasetVersion() {
        return countyDatasetVersion;
    }

    public void setInvFilter(String invFilter) {
        this.invFilter = invFilter;
    }

    public String getInvFilter() {
        return invFilter;
    }
}
