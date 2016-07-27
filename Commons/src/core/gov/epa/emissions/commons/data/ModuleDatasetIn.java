package gov.epa.emissions.commons.data;

import java.io.Serializable;

public class ModuleDatasetIn implements Serializable {

    private int id;

    private Module module;

    private String placeholderName;

    private int datasetId;

    private int version;

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

    public int getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(int datasetId) {
        this.datasetId = datasetId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
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
        return (other instanceof ModuleDatasetIn && ((ModuleDatasetIn) other).getQualifiedName() == getQualifiedName());
    }

    public int compareTo(ModuleDatasetIn o) {
        return getQualifiedName().compareTo(o.getQualifiedName());
    }
}
