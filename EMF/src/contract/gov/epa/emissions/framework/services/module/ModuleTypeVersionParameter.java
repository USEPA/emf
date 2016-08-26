package gov.epa.emissions.framework.services.module;

import java.io.Serializable;

public class ModuleTypeVersionParameter implements Serializable {

    public static final String IN    = "IN";
    public static final String INOUT = "INOUT";
    public static final String OUT   = "OUT";
    
    private int id;

    private ModuleTypeVersion moduleTypeVersion;

    private String parameterName;

    private String mode; // 'IN', 'INOUT', 'OUT'

    private String sqlParameterType;

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

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
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

    public String getSqlParameterType() {
        return sqlParameterType;
    }

    public void setSqlParameterType(String sqlParameterType) {
        this.sqlParameterType = sqlParameterType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        return getParameterName();
    }

    public int hashCode() {
        return parameterName.hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof ModuleTypeVersionParameter && ((ModuleTypeVersionParameter) other).getParameterName() == parameterName);
    }

    public int compareTo(ModuleTypeVersionParameter o) {
        return parameterName.compareTo(o.getParameterName());
    }
}
