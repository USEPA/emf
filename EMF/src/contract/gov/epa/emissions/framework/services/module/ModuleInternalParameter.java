package gov.epa.emissions.framework.services.module;

import java.io.Serializable;

public class ModuleInternalParameter implements Serializable {

    private int id;

    private Module compositeModule;

    private String parameterPath; // slash delimited list of submodule ids (at least one) and the parameter name

    private String parameterPathNames; // slash delimited list of submodule names (at least one) and the parameter name

    private ModuleTypeVersionParameter moduleTypeVersionParameter;
    
    private boolean keep;

    public ModuleInternalParameter deepCopy(Module newCompositeModule) {
        ModuleInternalParameter newModuleInternalParameter = new ModuleInternalParameter();
        newModuleInternalParameter.setCompositeModule(newCompositeModule);
        newModuleInternalParameter.setParameterPath(parameterPath);
        newModuleInternalParameter.setParameterPathNames(parameterPathNames);
        newModuleInternalParameter.setModuleTypeVersionParameter(moduleTypeVersionParameter);
        newModuleInternalParameter.setKeep(keep);
        return newModuleInternalParameter;
    }

    public boolean isValid(final StringBuilder error) {
        error.setLength(0);
        return true;
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

    public String getParameterPath() {
        return parameterPath;
    }

    public void setParameterPath(String parameterPath) {
        this.parameterPath = parameterPath;
    }

    public String getParameterPathNames() {
        return parameterPathNames;
    }

    public void setParameterPathNames(String parameterPathNames) {
        this.parameterPathNames = parameterPathNames;
    }

    public ModuleTypeVersionParameter getModuleTypeVersionParameter() {
        return moduleTypeVersionParameter;
    }

    public void setModuleTypeVersionParameter(ModuleTypeVersionParameter moduleTypeVersionParameter) {
        this.moduleTypeVersionParameter = moduleTypeVersionParameter;
    }

    public boolean getKeep() {
        return keep;
    }

    public void setKeep(boolean keep) {
        this.keep = keep;
    }

    public String getQualifiedName() {
        return compositeModule.getName() + "/" + parameterPath;
    }

    // standard methods
    
    public String toString() {
        return getQualifiedName();
    }

    public int hashCode() {
        return getQualifiedName().hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof ModuleInternalParameter && ((ModuleInternalParameter) other).getQualifiedName().equals(getQualifiedName()));
    }

    public int compareTo(ModuleInternalParameter o) {
        return getQualifiedName().compareTo(o.getQualifiedName());
    }
}
