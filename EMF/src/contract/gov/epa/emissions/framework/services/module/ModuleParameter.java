package gov.epa.emissions.framework.services.module;

import java.io.Serializable;
import java.util.Map;

public class ModuleParameter implements Serializable {

    private int id;

    private Module module;

    private String parameterName;

    private String value;

    public ModuleParameter deepCopy(Module newModule) {
        ModuleParameter newModuleParameter = new ModuleParameter();
        newModuleParameter.setModule(newModule);
        newModuleParameter.setParameterName(parameterName);
        newModuleParameter.setValue(value);
        return newModuleParameter;
    }

    public boolean isValid(final StringBuilder error) {
        error.setLength(0);
        ModuleTypeVersionParameter moduleTypeVersionParameter = getModuleTypeVersionParameter();
        if (!getModuleTypeVersionParameter().isValid(error)) return false;
        String mode = moduleTypeVersionParameter.getMode();
        boolean needsValue = !mode.equals(ModuleTypeVersionParameter.OUT);
        boolean hasValue = (value != null) && (value.trim().length() > 0); 
        if (needsValue && !hasValue) {
            error.append(String.format("Module parameter %s has no value", parameterName));
            return false;
        }
        return true;
    }
    
    // compares the settings against the moduleTypeVersionParameter
    // if the settings don't match, initialize this object
    // returns true if this object was modified in any way
    public boolean updateSettings() {
        // TODO add mode and sqlDataType to ModuleParameter class and modules.modules_parameters table also
        //      in order to detect moduleTypeVersion changes more reliably
        return false;
    }

    public void initSettings() {
        setValue("");
    }

    public boolean transferSettings(Module otherModule) {
        Map<String, ModuleParameter> otherModuleParameters = otherModule.getModuleParameters();
        if (otherModuleParameters.containsKey(parameterName)) {
            ModuleParameter otherModuleParameter = otherModuleParameters.get(parameterName);
            ModuleTypeVersionParameter otherModuleTypeVersionParameter = otherModuleParameter.getModuleTypeVersionParameter();
            if (otherModuleTypeVersionParameter.getMode().equals(getModuleTypeVersionParameter().getMode())) {
                setValue(otherModuleParameter.getValue());
                return true;
            }
        }
        return false;
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
        return (other instanceof ModuleParameter && ((ModuleParameter) other).getQualifiedName().equals(getQualifiedName()));
    }

    public int compareTo(ModuleParameter o) {
        return getQualifiedName().compareTo(o.getQualifiedName());
    }
}
