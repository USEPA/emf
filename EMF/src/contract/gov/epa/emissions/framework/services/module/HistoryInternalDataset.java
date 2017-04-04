package gov.epa.emissions.framework.services.module;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class HistoryInternalDataset implements Serializable {

    private int id;

    private History history;

    private String placeholderPath; // slash delimited list of submodule ids (at least one) and the placeholder name

    private String placeholderPathNames; // slash delimited list of submodule names (at least one) and the placeholder name

    private Integer datasetId;

    private int version;

    public EmfDataset getEmfDataset(DataService dataService) {
        try {
            if (datasetId != null) {
                return dataService.getDataset(datasetId);
            }
        } catch (EmfException ex) {
            // ignore exception
        }
        
        return null;
    }
    
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

    public String getPlaceholderPath() {
        return placeholderPath;
    }

    public void setPlaceholderPath(String placeholderPath) {
        this.placeholderPath = placeholderPath;
    }

    public String getPlaceholderPathNames() {
        return placeholderPathNames;
    }

    public void setPlaceholderPathNames(String placeholderPathNames) {
        this.placeholderPathNames = placeholderPathNames;
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
}
