package gov.epa.emissions.framework.services.module;

import java.io.Serializable;

import gov.epa.emissions.commons.data.DatasetType;

public class ModuleTypeVersionDataset implements Serializable {

    public static final String IN    = "IN";
    public static final String INOUT = "INOUT";
    public static final String OUT   = "OUT";
    
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

    public boolean isModeIN() {
        return this.mode.equals(IN);
    }

    public boolean isModeINOUT() {
        return this.mode.equals(INOUT);
    }

    public boolean isModeOUT() {
        return this.mode.equals(OUT);
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
