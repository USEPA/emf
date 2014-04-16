package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.Serializable;
import java.util.Date;

public class ControlStrategyResult implements Serializable {
    
    private int id;

    private int controlStrategyId;

    private Double totalCost;

    private Double totalReduction;

    private String runStatus;

    private Date completionTime;

    private Date startTime;

    private EmfDataset inputDataset;

    private Integer inputDatasetVersion;

    //for clarity should be resultDataset don't change will cause issues hibernate 
    private Dataset detailedResultDataset;
    
    private Dataset controlledInventoryDataset;
    
    private StrategyResultType strategyResultType;

    private Integer recordCount = 0;

    public ControlStrategyResult() {
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


    public StrategyResultType getStrategyResultType() {
        return strategyResultType;
    }

    public void setStrategyResultType(StrategyResultType resultType) {
        this.strategyResultType = resultType;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    public Double getTotalReduction() {
        return totalReduction;
    }

    public void setTotalReduction(Double totalReduction) {
        this.totalReduction = totalReduction;
    }

    public Date getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(Date completionTime) {
        this.completionTime = completionTime;
    }

    public Dataset getDetailedResultDataset() {
        return detailedResultDataset;
    }

    public void setDetailedResultDataset(Dataset detailedResultDataset) {
        this.detailedResultDataset = detailedResultDataset;
    }

    public EmfDataset getInputDataset() {
        return inputDataset;
    }

    public void setInputDataset(EmfDataset inputDataset) {
        this.inputDataset = inputDataset;
    }

    public String getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Dataset getControlledInventoryDataset() {
        return controlledInventoryDataset;
    }

    public void setControlledInventoryDataset(Dataset controlledInventoryDataset) {
        this.controlledInventoryDataset = controlledInventoryDataset;
    }

    public Integer getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Integer recordCount) {
        this.recordCount = recordCount;
    }

    public void setInputDatasetVersion(Integer inputDatasetVersion) {
        this.inputDatasetVersion = inputDatasetVersion;
    }

    public Integer getInputDatasetVersion() {
        return inputDatasetVersion;
    }
}
