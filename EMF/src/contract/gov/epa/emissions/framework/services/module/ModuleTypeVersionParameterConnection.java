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

    public boolean matchesImportedParameterConnection(String indent, final StringBuilder differences, ModuleTypeVersionParameterConnection importedParameterConnection) {
        boolean result = true;
        differences.setLength(0);
        
        if (this == importedParameterConnection)
            return result;

        // skipping id;

        // skipping compositeModuleTypeVersion
        
        if (!connectionName.equals(importedParameterConnection.getConnectionName())) { // should never happen
            differences.append(String.format("%sERROR: Local \"%s\" parameter connection name differs from imported \"%s\" parameter connection name.\n",
                                             indent, connectionName, importedParameterConnection.getConnectionName()));
            result = false;
        }

        String localSourceName = getSourceName();
        String importedSourceName = importedParameterConnection.getSourceName();
        
        if (!localSourceName.equals(importedSourceName)) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local \"%s\" parameter connection source name \"%s\" differs from imported parameter connection source name \"%s\".\n",
                                             indent, connectionName, localSourceName, importedSourceName));
            result = false;
        }
        
        String localTargetName = getTargetName();
        String importedTargetName = importedParameterConnection.getTargetName();
        
        if (!localTargetName.equals(importedTargetName)) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local \"%s\" parameter connection target name \"%s\" differs from imported parameter connection target name \"%s\".\n",
                                             indent, connectionName, localTargetName, importedTargetName));
            result = false;
        }
        
        if ((description == null) != (importedParameterConnection.getDescription() == null) ||
           ((description != null) && !description.equals(importedParameterConnection.getDescription()))) { // could happen and it's OK
            differences.append(String.format("%sWARNING: Local \"%s\" parameter connection description differs from imported \"%s\" parameter connection description.\n",
                                             indent, connectionName, importedParameterConnection.getConnectionName()));
            // result = false;
        }
        
        return result;
    }
    
    public void prepareForExport() {
        id = 0;
    }
    
    // could return null (source invalid or not set)
    public ModuleTypeVersionParameter getSourceModuleTypeVersionParameter() {
        ModuleTypeVersionParameter moduleTypeVersionParameter = null;
        try {
            if ((sourceSubmodule != null) && (sourceParameterName != null)) {
                // internal parameter
                moduleTypeVersionParameter = sourceSubmodule.getModuleTypeVersion().getModuleTypeVersionParameter(sourceParameterName);
            } else if (sourceParameterName != null) {
                // external parameter
                moduleTypeVersionParameter = compositeModuleTypeVersion.getModuleTypeVersionParameter(sourceParameterName);
            }
        } catch (EmfException e) {
            e.printStackTrace();
        }
        return moduleTypeVersionParameter;
    }

    // could return null (target invalid)
    public ModuleTypeVersionParameter getTargetModuleTypeVersionParameter() {
        ModuleTypeVersionParameter moduleTypeVersionParameter = null;
        try {
            if ((targetSubmodule != null) && (targetParameterName != null)) {
                // internal parameter
                moduleTypeVersionParameter = targetSubmodule.getModuleTypeVersion().getModuleTypeVersionParameter(targetParameterName);
            } else if (targetParameterName != null) {
                // external parameter
                moduleTypeVersionParameter = compositeModuleTypeVersion.getModuleTypeVersionParameter(targetParameterName);
            }
        } catch (EmfException e) {
            e.printStackTrace();
        }
        return moduleTypeVersionParameter;
    }

    public boolean sourceIsSet() {
        return getSourceModuleTypeVersionParameter() != null; 
    }

    public boolean sourceIsOptional() {
        ModuleTypeVersionParameter sourceModuleTypeVersionParameter = getSourceModuleTypeVersionParameter();
        if (sourceModuleTypeVersionParameter == null)
            return true; // source invalid or not set
        return sourceModuleTypeVersionParameter.getIsOptional();
    }

    public boolean targetIsOptional() {
        ModuleTypeVersionParameter targetModuleTypeVersionParameter = getTargetModuleTypeVersionParameter();
        if (targetModuleTypeVersionParameter == null)
            return true; // target invalid 
        return targetModuleTypeVersionParameter.getIsOptional();
    }

    public boolean isOptional() {
        return targetIsOptional();
    }

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
 
        if (!isOptional() || (sourceParameterName != null) || (sourceSubmodule != null)) {
            if (sourceParameterName == null) {
                error.append("Source parameter name is missing for target " + getTargetName() + ".");
                return false;
            }
            if (sourceSubmodule != null) {
                // internal source
                if (!sourceSubmodule.getModuleTypeVersion().getModuleTypeVersionParameters().containsKey(sourceParameterName)) {
                    error.append(String.format("Source name %s is invalid for target %s.", getSourceName(), getTargetName()));
                    return false;
                }
            } else if (!compositeModuleTypeVersion.getModuleTypeVersionParameters().containsKey(sourceParameterName)) {
                error.append(String.format("Source parameter name %s is invalid for target %s.", sourceParameterName, getTargetName()));
                return false;
            }
        }
        
        if (connectionName == null) {
            error.append("Connection name is missing.");
            return false;
        }
        if (!connectionName.equals(getTargetName())) {
            error.append(String.format("Invalid parameter connection name (%s instead of %s).", connectionName, getTargetName()));
            return false;
        }
 
        if (sourceIsOptional() && !targetIsOptional()) {
            error.append(String.format("Invalid parameter connection from optional %s to required %s.", getSourceName(), getTargetName()));
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
