package gov.epa.emissions.framework.services.module;

import java.io.Serializable;
import java.util.Map;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.services.EmfException;

public class ModuleTypeVersionParameterConnection implements Serializable {

    private int id;

    private ModuleTypeVersion compositeModuleTypeVersion;

    // cannot be null
    private String connectionName = "";
    
    // can be null when disconnected or when connected to external sources (composite module IN/INOUT parameters)
    private ModuleTypeVersionSubmodule sourceSubmodule;
    
    // can be null when disconnected
    private String sourceParameterName;
    
    // can be null for external targets (composite module OUT/INOUT parameters)
    private ModuleTypeVersionSubmodule targetSubmodule;

    // cannot be null
    private String targetParameterName;
    
    private String description;

    public ModuleTypeVersionParameterConnection deepCopy() {
        ModuleTypeVersionParameterConnection newModuleTypeVersionParameterConnection = new ModuleTypeVersionParameterConnection();
        newModuleTypeVersionParameterConnection.setCompositeModuleTypeVersion(compositeModuleTypeVersion);
        newModuleTypeVersionParameterConnection.setConnectionName(connectionName);
        newModuleTypeVersionParameterConnection.setSourceSubmodule(sourceSubmodule);
        newModuleTypeVersionParameterConnection.setSourceParameterName(sourceParameterName);
        newModuleTypeVersionParameterConnection.setTargetSubmodule(targetSubmodule);
        newModuleTypeVersionParameterConnection.setTargetParameterName(targetParameterName);
        newModuleTypeVersionParameterConnection.setDescription(description);
        return newModuleTypeVersionParameterConnection;
    }
    
    public boolean isValid(final StringBuilder error) {
        error.setLength(0);
        if (targetParameterName == null) {
            error.append("Target parameter name is missing.");
            return false;
        }
        if (targetSubmodule != null) {
            // internal target
            if (!targetSubmodule.getModuleTypeVersion().getModuleTypeVersionParameters().containsKey(targetParameterName)) {
                error.append(String.format("Target parameter name %s is invalid.", getTargetName()));
                return false;
            }
        } else if (!compositeModuleTypeVersion.getModuleTypeVersionParameters().containsKey(targetParameterName)) {
            error.append(String.format("Target parameter name %s is invalid.", targetParameterName));
            return false;
        }
 
        if (sourceParameterName == null) {
            error.append("Source parameter name is missing.");
            return false;
        }
        if (sourceSubmodule != null) {
            // internal source
            if (!sourceSubmodule.getModuleTypeVersion().getModuleTypeVersionParameters().containsKey(sourceParameterName)) {
                error.append(String.format("Source parameter name %s is invalid.", getSourceName()));
                return false;
            }
        } else if (!compositeModuleTypeVersion.getModuleTypeVersionParameters().containsKey(sourceParameterName)) {
            error.append(String.format("Source parameter name %s is invalid.", sourceParameterName));
            return false;
        }
        
        if (connectionName == null) {
            error.append("Connection name is missing.");
            return false;
        }
        if (!connectionName.equals(getTargetName())) {
            error.append(String.format("Invalid parameter connection name (%s instead of %s).", connectionName, getTargetName()));
            return false;
        }
 
        return true;
    }
    
    public String getSourceSqlType() throws EmfException {
        try {
            if (sourceParameterName == null)
                return ""; // no source
            if (sourceSubmodule != null) {
                // internal target
                return sourceSubmodule.getModuleTypeVersion().getModuleTypeVersionParameter(sourceParameterName).getSqlParameterType();
            }
            // external target
            return compositeModuleTypeVersion.getModuleTypeVersionParameter(sourceParameterName).getSqlParameterType();
        } catch (Exception e) {
            String errorMessage = String.format("%s parameter connection \"%s\" source: %s", compositeModuleTypeVersion.fullNameSS("\"%s\" version \"%s\""), connectionName, e.getMessage());
            throw new EmfException(errorMessage);
        }
    }
    
    public String getTargetSqlType() throws EmfException {
        try {
            if (targetSubmodule != null) {
                // internal target
                return targetSubmodule.getModuleTypeVersion().getModuleTypeVersionParameter(targetParameterName).getSqlParameterType();
            }
            // external target
            return compositeModuleTypeVersion.getModuleTypeVersionParameter(targetParameterName).getSqlParameterType();
        } catch (Exception e) {
            String errorMessage = String.format("%s parameter connection \"%s\" target: %s", compositeModuleTypeVersion.fullNameSS("\"%s\" version \"%s\""), connectionName, e.getMessage());
            throw new EmfException(errorMessage);
        }
    }
    
    public String getSourceName() {
        return ((sourceSubmodule == null) ? "" : (sourceSubmodule.getName() + " / ")) + ((sourceParameterName == null) ? "" : sourceParameterName);
    }
    
    public String getTargetName() {
        return ((targetSubmodule == null) ? "" : (targetSubmodule.getName() + " / ")) + ((targetParameterName == null) ? "" : targetParameterName);
    }
    
    public Map<String, ModuleTypeVersionParameterConnectionEndpoint> getSourceParameterEndpoints() {
        return compositeModuleTypeVersion.getSourceParameterEndpoints(this);
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ModuleTypeVersion getCompositeModuleTypeVersion() {
        return compositeModuleTypeVersion;
    }

    public void setCompositeModuleTypeVersion(ModuleTypeVersion compositeModuleTypeVersion) {
        this.compositeModuleTypeVersion = compositeModuleTypeVersion;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public ModuleTypeVersionSubmodule getSourceSubmodule() {
        return sourceSubmodule;
    }

    public void setSourceSubmodule(ModuleTypeVersionSubmodule sourceSubmodule) {
        this.sourceSubmodule = sourceSubmodule;
    }

    public String getSourceParameterName() {
        return sourceParameterName;
    }

    public void setSourceParameterName(String sourceParameterName) {
        this.sourceParameterName = sourceParameterName;
    }

    public ModuleTypeVersionSubmodule getTargetSubmodule() {
        return targetSubmodule;
    }

    public void setTargetSubmodule(ModuleTypeVersionSubmodule targetSubmodule) {
        this.targetSubmodule = targetSubmodule;
        this.connectionName = getTargetName(); 
    }

    public String getTargetParameterName() {
        return targetParameterName;
    }

    public void setTargetParameterName(String targetParameterName) {
        this.targetParameterName = targetParameterName;
        this.connectionName = getTargetName(); 
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ModuleTypeVersionParameterConnection)) return false;
        ModuleTypeVersionParameterConnection otherConnection = (ModuleTypeVersionParameterConnection) other;
        return compositeModuleTypeVersion.equals(otherConnection.getCompositeModuleTypeVersion()) &&
               ((connectionName == null) == (otherConnection.getConnectionName() == null)) &&
               ((connectionName == null) || connectionName.equals(otherConnection.getConnectionName()));
    }

     public int hashCode() {
         int result = 23; // seed
         result = 37 * result + ((compositeModuleTypeVersion == null) ? 0 : compositeModuleTypeVersion.hashCode());
         result = 37 * result + ((connectionName == null) ? 0 : connectionName.hashCode());
         return result;
     }

    public int compareTo(ModuleTypeVersionParameterConnection otherConnection) {
        if (this == otherConnection) return 0;
        int comp = compositeModuleTypeVersion.compareTo(otherConnection.getCompositeModuleTypeVersion());
        if (comp != 0) return comp;
        comp = new Boolean(connectionName == null).compareTo(otherConnection.getConnectionName() == null);
        if (comp != 0) return comp;
        if (connectionName != null) {
            comp = connectionName.compareTo(otherConnection.getConnectionName());
            if (comp != 0) return comp;
        }
        return 0;
    }
}
