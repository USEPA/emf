package gov.epa.emissions.framework.services.module;

import java.io.Serializable;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class ModuleDataset implements Serializable {

    public static final String NEW = "NEW";
    public static final String REPLACE = "REPLACE";
    
    private int id;

    private Module module;

    private String placeholderName;

    private String outputMethod; // 'NEW', 'REPLACE'

    private Integer datasetId;

    private Integer version;

    private String datasetNamePattern;

    private Boolean overwriteExisting;

    public ModuleDataset() {
    }
    
    public ModuleDataset deepCopy(Module newModule) {
        ModuleDataset newModuleDataset = new ModuleDataset();
        newModuleDataset.setModule(newModule);
        newModuleDataset.setPlaceholderName(placeholderName);
        newModuleDataset.setOutputMethod(outputMethod);
        newModuleDataset.setDatasetId(datasetId);
        newModuleDataset.setVersion(version);
        newModuleDataset.setDatasetNamePattern(datasetNamePattern);
        newModuleDataset.setOverwriteExisting(overwriteExisting);
        return newModuleDataset;
    }

    public boolean isValid(final StringBuilder error) {
        error.setLength(0);
        ModuleTypeVersionDataset moduleTypeVersionDataset = getModuleTypeVersionDataset();
        if (!moduleTypeVersionDataset.isValid(error)) return false;
        String mode = moduleTypeVersionDataset.getMode();
        boolean needsDatasetNamePattern = mode.equals(ModuleTypeVersionDataset.OUT) && outputMethod.equals(NEW);
        boolean hasDatasetNamePattern = (datasetNamePattern != null) && (datasetNamePattern.trim().length() > 0);
        if (needsDatasetNamePattern && !hasDatasetNamePattern) {
            error.append(String.format("The dataset name pattern for placeholder '%s' has not been set.", placeholderName));
            return false;
        }
        if (!needsDatasetNamePattern && (datasetId == null)) {
            error.append(String.format("The dataset for placeholder '%s' has not been set.", placeholderName));
            return false;
        }
        return true;
    }

    public EmfDataset getEmfDataset(DataService dataService) {
        try {
            if (datasetId != null) {
                return dataService.getDataset(datasetId);
            } else if (datasetNamePattern != null) {
                return dataService.getDataset(datasetNamePattern);
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

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public String getPlaceholderName() {
        return placeholderName;
    }

    public void setPlaceholderName(String placeholderName) {
        this.placeholderName = placeholderName;
    }

    public ModuleTypeVersionDataset getModuleTypeVersionDataset() {
        return module.getModuleTypeVersion().getModuleTypeVersionDatasets().get(placeholderName);
    }

    public String getOutputMethod() {
        return outputMethod;
    }

    public void setOutputMethod(String outputMethod) {
        this.outputMethod = outputMethod;
    }

    public Integer getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Integer datasetId) {
        this.datasetId = datasetId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getDatasetNamePattern() {
        return datasetNamePattern;
    }

    public void setDatasetNamePattern(String datasetNamePattern) {
        this.datasetNamePattern = datasetNamePattern;
    }

    public Boolean getOverwriteExisting() {
        return overwriteExisting;
    }

    public void setOverwriteExisting(Boolean overwriteExisting) {
        this.overwriteExisting = overwriteExisting;
    }

    public String getQualifiedName() {
        return module.getName() + " . " + placeholderName;
    }

    // standard methods
    
    public String toString() {
        return getQualifiedName();
    }

    public int hashCode() {
        return getQualifiedName().hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof ModuleDataset && ((ModuleDataset) other).getQualifiedName() == getQualifiedName());
    }

    public int compareTo(ModuleDataset o) {
        return getQualifiedName().compareTo(o.getQualifiedName());
    }
}
