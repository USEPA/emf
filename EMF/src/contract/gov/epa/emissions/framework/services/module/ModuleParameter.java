package gov.epa.emissions.framework.services.module;

import java.io.Serializable;

public class ModuleParameter implements Serializable {

    private int id;

    private Module module;

    private String parameterName;

    private String value;
    
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

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public ModuleTypeVersionParameter getModuleTypeVersionParameter() {
        return module.getModuleTypeVersion().getModuleTypeVersionParameters().get(parameterName);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getQualifiedName() {
        return module.getName() + " . " + parameterName;
    }

    // standard methods
    
    public String toString() {
        return getQualifiedName();
    }

    public int hashCode() {
        return getQualifiedName().hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof ModuleParameter && ((ModuleParameter) other).getQualifiedName() == getQualifiedName());
    }

    public int compareTo(ModuleParameter o) {
        return getQualifiedName().compareTo(o.getQualifiedName());
    }
}
