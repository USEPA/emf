package gov.epa.emissions.framework.services.module;

import java.io.Serializable;

public class HistoryDataset implements Serializable {

    private int id;

    private History history;

    private String placeholderName;

    private Integer datasetId;

    private int version;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public History getHistory() {
        return history;
    }

    public void setHistory(History history) {
        this.history = history;
    }

    public String getPlaceholderName() {
        return placeholderName;
    }

    public void setPlaceholderName(String placeholderName) {
        this.placeholderName = placeholderName;
    }

    public Integer getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Integer datasetId) {
        this.datasetId = datasetId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    // could return null if the module type for this module was changed after this history record was created
    public ModuleTypeVersionDataset getModuleTypeVersionDataset() {
        return history.getModule().getModuleTypeVersion().getModuleTypeVersionDatasets().get(placeholderName);
    }
}
