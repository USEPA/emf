package gov.epa.emissions.framework.services.sms;

import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.Serializable;
import java.util.Date;

public class SectorScenarioOutput implements Serializable {
    
    private int id;
    private int sectorScenarioId;
    private SectorScenarioOutputType type;
    private EmfDataset inventoryDataset;
    private Integer inventoryDatasetVersion;
    private Date completionDate;
    private Date startDate;
    private String runStatus;
    private EmfDataset outputDataset;
   
    public SectorScenarioOutput() {
        //
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSectorScenarioId() {
        return sectorScenarioId;
    }

    public void setSectorScenarioId(int id) {
        this.sectorScenarioId = id;
    }


    public SectorScenarioOutputType getType() {
        return type;
    }

    public void setType(SectorScenarioOutputType type) {
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

    public EmfDataset getInventoryDataset() {
        return inventoryDataset;
    }

    public void setInventoryDataset(EmfDataset inputDataset) {
        this.inventoryDataset = inputDataset;
    }

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

    public void setInventoryDatasetVersion(Integer inputDatasetVersion) {
        this.inventoryDatasetVersion = inputDatasetVersion;
    }

    public Integer getInventoryDatasetVersion() {
        return inventoryDatasetVersion;
    }
}
