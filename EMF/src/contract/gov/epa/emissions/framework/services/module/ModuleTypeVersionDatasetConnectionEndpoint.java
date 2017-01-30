package gov.epa.emissions.framework.services.module;

import java.io.Serializable;

import gov.epa.emissions.commons.data.DatasetType;

public class ModuleTypeVersionDatasetConnectionEndpoint implements Serializable {

    private ModuleTypeVersion compositeModuleTypeVersion;

    // can be null when disconnected or when connected to external sources/targets
    private ModuleTypeVersionSubmodule submodule;
    
    // can be null when disconnected
    private String placeholderName;

    public ModuleTypeVersionDatasetConnectionEndpoint() {
    }
    
    public ModuleTypeVersionDatasetConnectionEndpoint(ModuleTypeVersion compositeModuleTypeVersion, ModuleTypeVersionSubmodule submodule, String placeholderName) {
        this();
        this.compositeModuleTypeVersion = compositeModuleTypeVersion;
        this.submodule = submodule;
        this.placeholderName = placeholderName;
    }

    public DatasetType getDatasetType() {
        if (submodule != null) {
            // internal target
            return submodule.getModuleTypeVersion().getModuleTypeVersionDatasets().get(placeholderName).getDatasetType();
        }
        // external target
        return compositeModuleTypeVersion.getModuleTypeVersionDatasets().get(placeholderName).getDatasetType();
    }
    
    public String getDatasetTypeName() {
        DatasetType datasetType = getDatasetType();
        return (datasetType == null) ? "" : datasetType.getName();
    }
    
    public String getEndpointName() {
        return ((submodule == null) ? "" : (submodule.getName() + " / ")) + ((placeholderName == null) ? "" : placeholderName);
    }
    
    public ModuleTypeVersion getCompositeModuleTypeVersion() {
        return compositeModuleTypeVersion;
    }

    public void setCompositeModuleTypeVersion(ModuleTypeVersion compositeModuleTypeVersion) {
        this.compositeModuleTypeVersion = compositeModuleTypeVersion;
    }

    public ModuleTypeVersionSubmodule getSubmodule() {
        return submodule;
    }

    public void setSubmodule(ModuleTypeVersionSubmodule submodule) {
        this.submodule = submodule;
    }

    public String getPlaceholderName() {
        return placeholderName;
    }

    public void setPlaceholderName(String placeholderName) {
        this.placeholderName = placeholderName;
    }
}
