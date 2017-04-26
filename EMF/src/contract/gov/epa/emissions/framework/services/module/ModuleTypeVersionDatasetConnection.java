package gov.epa.emissions.framework.services.module;

import java.io.Serializable;
import java.util.Map;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.services.EmfException;

public class ModuleTypeVersionDatasetConnection implements Serializable {

    private int id;

    private ModuleTypeVersion compositeModuleTypeVersion;

    private String connectionName;
    
    // can be null when disconnected or when connected to external sources (composite module IN/INOUT datasets)
    private ModuleTypeVersionSubmodule sourceSubmodule;
    
    // can be null when disconnected
    private String sourcePlaceholderName;
    
    // can be null for external targets (composite module OUT/INOUT datasets)
    private ModuleTypeVersionSubmodule targetSubmodule;

    // cannot be null
    private String targetPlaceholderName;
    
    private String description;

    public ModuleTypeVersionDatasetConnection deepCopy() {
        ModuleTypeVersionDatasetConnection newModuleTypeVersionConnectionDatasets = new ModuleTypeVersionDatasetConnection();
        newModuleTypeVersionConnectionDatasets.setCompositeModuleTypeVersion(compositeModuleTypeVersion);
        newModuleTypeVersionConnectionDatasets.setConnectionName(connectionName);
        newModuleTypeVersionConnectionDatasets.setSourceSubmodule(sourceSubmodule);
        newModuleTypeVersionConnectionDatasets.setSourcePlaceholderName(sourcePlaceholderName);
        newModuleTypeVersionConnectionDatasets.setTargetSubmodule(targetSubmodule);
        newModuleTypeVersionConnectionDatasets.setTargetPlaceholderName(targetPlaceholderName);
        newModuleTypeVersionConnectionDatasets.setDescription(description);
        return newModuleTypeVersionConnectionDatasets;
    }
    
    public boolean isValid(final StringBuilder error) {
        error.setLength(0);
        if (targetPlaceholderName == null) {
            error.append("Target placeholder name is missing.");
            return false;
        }
        if (targetSubmodule != null) {
            // internal target
            if (!targetSubmodule.getModuleTypeVersion().getModuleTypeVersionDatasets().containsKey(targetPlaceholderName)) {
                error.append(String.format("Target placeholder name %s is invalid.", getTargetName()));
                return false;
            }
        } else if (!compositeModuleTypeVersion.getModuleTypeVersionDatasets().containsKey(targetPlaceholderName)) {
            error.append(String.format("Target placeholder name %s is invalid.", targetPlaceholderName));
            return false;
        }
 
        if (sourcePlaceholderName == null) {
            error.append("Source placeholder name is missing.");
            return false;
        }
        if (sourceSubmodule != null) {
            // internal source
            if (!sourceSubmodule.getModuleTypeVersion().getModuleTypeVersionDatasets().containsKey(sourcePlaceholderName)) {
                error.append(String.format("Source placeholder name %s is invalid.", getSourceName()));
                return false;
            }
        } else if (!compositeModuleTypeVersion.getModuleTypeVersionDatasets().containsKey(sourcePlaceholderName)) {
            error.append(String.format("Source placeholder name %s is invalid.", sourcePlaceholderName));
            return false;
        }
 
        if (connectionName == null) {
            error.append("Connection name is missing.");
            return false;
        }
        if (!connectionName.equals(getTargetName())) {
            error.append(String.format("Invalid dataset connection name (%s instead of %s).", connectionName, getTargetName()));
            return false;
        }
        
        return true;
    }
    
    public DatasetType getSourceDatasetType() throws EmfException {
        try {
            if (sourcePlaceholderName == null)
                return null; // no source
            if (sourceSubmodule != null) {
                // internal source
                return sourceSubmodule.getModuleTypeVersion().getModuleTypeVersionDataset(sourcePlaceholderName).getDatasetType();
            }
            // external source
            return compositeModuleTypeVersion.getModuleTypeVersionDataset(sourcePlaceholderName).getDatasetType();
        } catch (Exception e) {
            String errorMessage = String.format("%s connection \"%s\" source: %s", compositeModuleTypeVersion.fullNameSS("\"%s\" version \"%s\""), connectionName, e.getMessage());
            throw new EmfException(errorMessage);
        }
    }
    
    public String getSourceDatasetTypeName() throws EmfException {
        DatasetType datasetType = getSourceDatasetType();
        return (datasetType == null) ? "" : datasetType.getName();
    }
    
    public DatasetType getTargetDatasetType() throws EmfException {
        try {
            if (targetSubmodule != null) {
                // internal target
                return targetSubmodule.getModuleTypeVersion().getModuleTypeVersionDataset(targetPlaceholderName).getDatasetType();
            }
            // external target
            return compositeModuleTypeVersion.getModuleTypeVersionDataset(targetPlaceholderName).getDatasetType();
        } catch (Exception e) {
            String errorMessage = String.format("%s connection \"%s\" target: %s", compositeModuleTypeVersion.fullNameSS("\"%s\" version \"%s\""), connectionName, e.getMessage());
            throw new EmfException(errorMessage);
        }
    }
    
    public String getTargetDatasetTypeName() throws EmfException {
        DatasetType datasetType = getTargetDatasetType();
        return (datasetType == null) ? "" : datasetType.getName();
    }
    
    public String getSourceName() {
        return ((sourceSubmodule == null) ? "" : (sourceSubmodule.getName() + " / ")) + ((sourcePlaceholderName == null) ? "" : sourcePlaceholderName);
    }
    
    public String getTargetName() {
        return ((targetSubmodule == null) ? "" : (targetSubmodule.getName() + " / ")) + ((targetPlaceholderName == null) ? "" : targetPlaceholderName);
    }
    
    public Map<String, ModuleTypeVersionDatasetConnectionEndpoint> getSourceDatasetEndpoints() {
        return compositeModuleTypeVersion.getSourceDatasetEndpoints(this);
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

    public String getSourcePlaceholderName() {
        return sourcePlaceholderName;
    }

    public void setSourcePlaceholderName(String sourcePlaceholderName) {
        this.sourcePlaceholderName = sourcePlaceholderName;
    }

    public ModuleTypeVersionSubmodule getTargetSubmodule() {
        return targetSubmodule;
    }

    public void setTargetSubmodule(ModuleTypeVersionSubmodule targetSubmodule) {
        this.targetSubmodule = targetSubmodule;
        this.connectionName = getTargetName(); 
    }

    public String getTargetPlaceholderName() {
        return targetPlaceholderName;
    }

    public void setTargetPlaceholderName(String targetPlaceholderName) {
        this.targetPlaceholderName = targetPlaceholderName;
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
        if (!(other instanceof ModuleTypeVersionDatasetConnection)) return false;
        ModuleTypeVersionDatasetConnection otherConnection = (ModuleTypeVersionDatasetConnection) other;
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

    public int compareTo(ModuleTypeVersionDatasetConnection otherConnection) {
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
