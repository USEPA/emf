package gov.epa.emissions.commons.data;

import java.io.Serializable;

public class ModuleTypeVersionDataset implements Serializable {

    private int id;

    private ModuleTypeVersion moduleTypeVersion;

    private String placeholderName;

    private String mode; // 'IN', 'INOUT', 'OUT'

    private DatasetType datasetType;

    private String description;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ModuleTypeVersion getModuleTypeVersion() {
        return moduleTypeVersion;
    }

    public void setModuleTypeVersion(ModuleTypeVersion moduleTypeVersion) {
        this.moduleTypeVersion = moduleTypeVersion;
    }

    public String getPlaceholderName() {
        return placeholderName;
    }

    public void setPlaceholderName(String placeholderName) {
        this.placeholderName = placeholderName;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public DatasetType getDatasetType() {
        return datasetType;
    }

    public void setDatasetType(DatasetType datasetType) {
        this.datasetType = datasetType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        return getPlaceholderName();
    }

    public int hashCode() {
        return placeholderName.hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof ModuleTypeVersionDataset && ((ModuleTypeVersionDataset) other).getPlaceholderName() == placeholderName);
    }

    public int compareTo(ModuleTypeVersionDataset o) {
        return placeholderName.compareTo(o.getPlaceholderName());
    }
}
