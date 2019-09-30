package gov.epa.emissions.framework.services.module;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.DataEditorService;

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
    
    public boolean isOutOfDate(final StringBuilder explanation, DataService dataService, DataEditorService dataEditorService) {
        if (datasetId == null) {
            explanation.append("Internal dataset for placeholder '" + placeholderPathNames + "' is missing.\n");
            return true;
        }
        EmfDataset dataset = null;
        try {
            dataset = dataService.getDataset(datasetId);
        } catch (EmfException e) {
            e.printStackTrace();
            explanation.append("Internal dataset for placeholder '" + placeholderPathNames + "' is missing: " + e.getMessage() + "\n");
            return true;
        }
        Version datasetVersion = null;
        try {
            datasetVersion = dataEditorService.getVersion(datasetId, version);
        } catch (EmfException e) {
            e.printStackTrace();
            explanation.append("Internal dataset \"" + dataset.getName() + "\" version " + version + " for placeholder '" + placeholderPathNames + "' is missing: " + e.getMessage() + "\n");
            return true;
        }
        if (datasetVersion == null) {
            explanation.append("Internal dataset \"" + dataset.getName() + "\" version " + version + " for placeholder '" + placeholderPathNames + "' is missing");
            return true;
        }
        String finalText = datasetVersion.isFinalVersion() ? " final " : " ";
        Date endDate = history.endDate();
        if (datasetVersion.getLastModifiedDate().after(endDate)) {
            explanation.append("Internal dataset \"" + dataset.getName() + "\"" + finalText + "version " + version + " for placeholder '" + placeholderPathNames + "' was modified after the end of the last run.");
            return true;
        }
        return false;
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
