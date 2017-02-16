package gov.epa.emissions.framework.services.module;

import java.io.Serializable;

import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class ModuleInternalDataset implements Serializable {

    private int id;

    private Module compositeModule;

    private String placeholderPath; // slash delimited list of submodule ids (at least one) and the placeholder name

    private String placeholderPathNames; // slash delimited list of submodule names (at least one) and the placeholder name

    private ModuleTypeVersionDataset moduleTypeVersionDataset;
    
    private boolean keep;
    
    private String datasetNamePattern;

    public ModuleInternalDataset deepCopy(Module newCompositeModule) {
        ModuleInternalDataset newModuleInternalDataset = new ModuleInternalDataset();
        newModuleInternalDataset.setCompositeModule(newCompositeModule);
        newModuleInternalDataset.setPlaceholderPath(placeholderPath);
        newModuleInternalDataset.setPlaceholderPathNames(placeholderPathNames);
        newModuleInternalDataset.setModuleTypeVersionDataset(moduleTypeVersionDataset);
        newModuleInternalDataset.setKeep(keep);
        newModuleInternalDataset.setDatasetNamePattern(datasetNamePattern);
        return newModuleInternalDataset;
    }

    public static boolean isValidDatasetNamePattern(String datasetNamePattern, final StringBuilder error) {
        return ModuleDataset.isValidDatasetNamePattern(datasetNamePattern, error);
    }
    
    public boolean isValid(final StringBuilder error) {
        error.setLength(0);
        boolean needsDatasetNamePattern = keep;
        boolean hasDatasetNamePattern = (datasetNamePattern != null) && (datasetNamePattern.trim().length() > 0);
        if (needsDatasetNamePattern) {
            if (!hasDatasetNamePattern) {
                error.append(String.format("The dataset name pattern for internal placeholder '%s' has not been set.", placeholderPathNames));
                return false;
            } else if (!isValidDatasetNamePattern(datasetNamePattern, error)) {
                error.insert(0, String.format("The dataset name pattern for internal placeholder '%s' is invalid: ", placeholderPathNames));
                return false;
            }
        }
        return true;
    }

    
    public boolean isSimpleDatasetName() {
        return ModuleDataset.isSimpleDatasetName(datasetNamePattern);
    }

    public EmfDataset getEmfDataset(DataService dataService) {
//        try {
//            if (datasetId != null) {
//                return dataService.getDataset(datasetId);
//            } else if (isSimpleDatasetName()) {
//                return dataService.getDataset(datasetNamePattern);
//            } else {
//                List<History> history = module.getModuleHistory();
//                HistoryDataset historyDataset = null;
//                if (history.size() > 0) {
//                    History lastHistory = history.get(history.size() - 1);
//                    if (lastHistory.getResult().equals(History.SUCCESS)) {
//                        historyDataset = lastHistory.getHistoryDatasets().get(placeholderPath);
//                    }
//                }
//                if ((historyDataset != null) && (historyDataset.getDatasetId() != null)) {
//                    return dataService.getDataset(historyDataset.getDatasetId());
//                }
//            }
//        } catch (EmfException ex) {
//            // ignore exception
//        }
        
        return null;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Module getCompositeModule() {
        return compositeModule;
    }

    public void setCompositeModule(Module compositeModule) {
        this.compositeModule = compositeModule;
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

    public String getDatasetNamePattern() {
        return datasetNamePattern;
    }

    public ModuleTypeVersionDataset getModuleTypeVersionDataset() {
        return moduleTypeVersionDataset;
    }

    public void setModuleTypeVersionDataset(ModuleTypeVersionDataset moduleTypeVersionDataset) {
        this.moduleTypeVersionDataset = moduleTypeVersionDataset;
    }

    public boolean getKeep() {
        return keep;
    }

    public void setKeep(boolean keep) {
        this.keep = keep;
    }

    public void setDatasetNamePattern(String datasetNamePattern) {
        this.datasetNamePattern = datasetNamePattern;
    }

    public String getQualifiedName() {
        return compositeModule.getName() + "/" + placeholderPath;
    }

    // standard methods
    
    public String toString() {
        return getQualifiedName();
    }

    public int hashCode() {
        return getQualifiedName().hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof ModuleInternalDataset && ((ModuleInternalDataset) other).getQualifiedName().equals(getQualifiedName()));
    }

    public int compareTo(ModuleInternalDataset o) {
        return getQualifiedName().compareTo(o.getQualifiedName());
    }
}
