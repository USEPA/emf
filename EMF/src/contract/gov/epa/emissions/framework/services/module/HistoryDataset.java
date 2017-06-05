package gov.epa.emissions.framework.services.module;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.DataEditorService;

public class HistoryDataset implements Serializable {

    private int id;

    private History history;

    private String placeholderName;

    private Integer datasetId;

    private int version;

    public boolean isOutOfDate(final StringBuilder explanation, DataService dataService, DataEditorService dataEditorService) {
        ModuleTypeVersionDataset moduleTypeVersionDataset = getModuleTypeVersionDataset();
        if (moduleTypeVersionDataset == null) {
            explanation.append("Module type version dataset for placeholder '" + placeholderName + "' is missing.\n");
            return true;
        }
        if (datasetId == null) {
            if (moduleTypeVersionDataset.getIsOptional())
                return false;
            explanation.append("Dataset for placeholder '" + placeholderName + "' is missing.\n");
            return true;
        }
        EmfDataset dataset = null;
        try {
            dataset = dataService.getDataset(datasetId);
        } catch (EmfException e) {
            e.printStackTrace();
            explanation.append("Dataset for placeholder '" + placeholderName + "' is missing: " + e.getMessage() + "\n");
            return true;
        }
        Version datasetVersion = null;
        try {
            datasetVersion = dataEditorService.getVersion(datasetId, version);
        } catch (EmfException e) {
            e.printStackTrace();
            explanation.append("Dataset \"" + dataset.getName() + "\" version " + version + " for placeholder '" + placeholderName + "' is missing: " + e.getMessage() + "\n");
            return true;
        }
        if (datasetVersion == null) {
            explanation.append("Dataset \"" + dataset.getName() + "\" version " + version + " for placeholder '" + placeholderName + "' is missing");
            return true;
        }
        String finalText = datasetVersion.isFinalVersion() ? " final " : " ";
        if (moduleTypeVersionDataset.isModeOUT()) {
            Date endDate = history.endDate();
            if (datasetVersion.getLastModifiedDate().after(endDate)) {
                explanation.append("Dataset \"" + dataset.getName() + "\"" + finalText + "version " + version + " for output placeholder '" + placeholderName + "' was modified after the end of the last run.");
                return true;
            }
        } else {
            Date startDate = history.startDate();
            if (datasetVersion.getLastModifiedDate().after(startDate)) {
                explanation.append("Dataset \"" + dataset.getName() + "\"" + finalText + "version " + version + " for input placeholder '" + placeholderName + "' was modified after the start of the last run.");
                return true;
            }
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
    public ModuleDataset getModuleDataset() {
        Map<String, ModuleDataset> moduleDatasets = history.getModule().getModuleDatasets();
        if (moduleDatasets.containsKey(placeholderName))
            return moduleDatasets.get(placeholderName);
        return null;
    }

    // could return null if the module type for this module was changed after this history record was created
    public ModuleTypeVersionDataset getModuleTypeVersionDataset() {
        Map<String, ModuleTypeVersionDataset> moduleTypeVersionDatasets = history.getModule().getModuleTypeVersion().getModuleTypeVersionDatasets();
        if (moduleTypeVersionDatasets.containsKey(placeholderName))
            return moduleTypeVersionDatasets.get(placeholderName);
        return null;
    }
}
