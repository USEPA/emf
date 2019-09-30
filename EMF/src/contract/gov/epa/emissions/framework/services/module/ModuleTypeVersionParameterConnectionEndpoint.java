package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.data.DatasetType;

import java.io.Serializable;

public class ModuleTypeVersionParameterConnectionEndpoint implements Serializable {

    private ModuleTypeVersion compositeModuleTypeVersion;

    // can be null when disconnected or when connected to external sources/targets
    private ModuleTypeVersionSubmodule submodule;
    
    // can be null when disconnected
    private String parameterName;

    public ModuleTypeVersionParameterConnectionEndpoint() {
    }
    
    public ModuleTypeVersionParameterConnectionEndpoint(ModuleTypeVersion compositeModuleTypeVersion, ModuleTypeVersionSubmodule submodule, String parameterName) {
        this();
        this.compositeModuleTypeVersion = compositeModuleTypeVersion;
        this.submodule = submodule;
        this.parameterName = parameterName;
    }

    public String getSqlType() {
        if (submodule != null) {
            // internal target
            return submodule.getModuleTypeVersion().getModuleTypeVersionParameters().get(parameterName).getSqlParameterType();
        }
        // external target
        return compositeModuleTypeVersion.getModuleTypeVersionParameters().get(parameterName).getSqlParameterType();
    }
    
    public String getEndpointName() {
        return ((submodule == null) ? "" : (submodule.getName() + " / ")) + ((parameterName == null) ? "" : parameterName);
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

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }
}
