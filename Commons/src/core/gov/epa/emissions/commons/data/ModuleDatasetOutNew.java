package gov.epa.emissions.commons.data;

import java.io.Serializable;

public class ModuleDatasetOutNew implements Serializable {

    private int id;

    private Module module;

    private String placeholderName;

    private String datasetNamePattern;

    private boolean overwriteExisting;

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

    public String getDatasetNamePattern() {
        return datasetNamePattern;
    }

    public void setDatasetNamePattern(String datasetNamePattern) {
        this.datasetNamePattern = datasetNamePattern;
    }

    public boolean getOverwriteExisting() {
        return overwriteExisting;
    }

    public void setOverwriteExisting(boolean overwriteExisting) {
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
        return (other instanceof ModuleDatasetOutNew && ((ModuleDatasetOutNew) other).getQualifiedName() == getQualifiedName());
    }

    public int compareTo(ModuleDatasetOutNew o) {
        return getQualifiedName().compareTo(o.getQualifiedName());
    }
}
