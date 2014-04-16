package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.Serializable;
import java.util.Date;

public class FastRunOutput implements Serializable {
    
    private int id;
    private int fastRunId;
    private FastRunOutputType type;
//    private EmfDataset inventoryDataset;
//    private Integer inventoryDatasetVersion;
    private Date completionDate;
    private Date startDate;
    private String runStatus;
    private EmfDataset outputDataset;
    private Grid grid;

    public FastRunOutput() {
        //
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFastRunId() {
        return fastRunId;
    }

    public void setFastRunId(int fastRunId) {
        this.fastRunId = fastRunId;
    }


    public FastRunOutputType getType() {
        return type;
    }

    public void setType(FastRunOutputType type) {
        this.type = type;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public EmfDataset getOutputDataset() {
        return outputDataset;
    }

    public void setOutputDataset(EmfDataset outputDataset) {
        this.outputDataset = outputDataset;
    }

//    public EmfDataset getInventoryDataset() {
//        return inventoryDataset;
//    }
//
//    public void setInventoryDataset(EmfDataset inputDataset) {
//        this.inventoryDataset = inputDataset;
//    }

    public String getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    public Grid getGrid() {
        return grid;
    }

//    public void setInventoryDatasetVersion(Integer inputDatasetVersion) {
//        this.inventoryDatasetVersion = inputDatasetVersion;
//    }
//
//    public Integer getInventoryDatasetVersion() {
//        return inventoryDatasetVersion;
//    }
}
