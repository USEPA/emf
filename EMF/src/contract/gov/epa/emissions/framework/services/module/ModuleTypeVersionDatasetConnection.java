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

    public boolean matchesImportedDatasetConnection(String indent, final StringBuilder differences, ModuleTypeVersionDatasetConnection importedDatasetConnection) {
        boolean result = true;
        differences.setLength(0);
        
        if (this == importedDatasetConnection)
            return result;

        // skipping id;

        // skipping compositeModuleTypeVersion
        
        if (!connectionName.equals(importedDatasetConnection.getConnectionName())) { // should never happen
            differences.append(String.format("%sERROR: Local \"%s\" dataset connection name differs from imported \"%s\" dataset connection name.\n",
                                             indent, connectionName, importedDatasetConnection.getConnectionName()));
            result = false;
        }

        String localSourceName = getSourceName();
        String importedSourceName = importedDatasetConnection.getSourceName();
        
        if (!localSourceName.equals(importedSourceName)) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local \"%s\" dataset connection source name \"%s\" differs from imported dataset connection source name \"%s\".\n",
                                             indent, connectionName, localSourceName, importedSourceName));
            result = false;
        }
        
        String localTargetName = getTargetName();
        String importedTargetName = importedDatasetConnection.getTargetName();
        
        if (!localTargetName.equals(importedTargetName)) { // could happen and it's not OK
            differences.append(String.format("%sERROR: Local \"%s\" dataset connection target name \"%s\" differs from imported dataset connection target name \"%s\".\n",
                                             indent, connectionName, localTargetName, importedTargetName));
            result = false;
        }
        
        if ((description == null) != (importedDatasetConnection.getDescription() != null) ||
           ((description != null) && !description.equals(importedDatasetConnection.getDescription()))) { // could happen and it's OK
            differences.append(String.format("%sWARNING: Local \"%s\" dataset connection description differs from imported \"%s\" dataset connection description.\n",
                                             indent, connectionName, importedDatasetConnection.getConnectionName()));
            // result = false;
        }
        
        return result;
    }
    
    public void prepareForExport() {
        id = 0;
    }
    
    // could return null (source invalid or not set)
    public ModuleTypeVersionDataset getSourceModuleTypeVersionDataset() {
        ModuleTypeVersionDataset moduleTypeVersionDataset = null;
        try {
            if ((sourceSubmodule != null) && (sourcePlaceholderName != null)) {
                // internal dataset
                moduleTypeVersionDataset = sourceSubmodule.getModuleTypeVersion().getModuleTypeVersionDataset(sourcePlaceholderName);
            } else if (sourcePlaceholderName != null) {
                // external dataset
                moduleTypeVersionDataset = compositeModuleTypeVersion.getModuleTypeVersionDataset(sourcePlaceholderName);
            }
        } catch (EmfException e) {
            e.printStackTrace();
        }
        return moduleTypeVersionDataset;
    }

    // could return null (target invalid)
    public ModuleTypeVersionDataset getTargetModuleTypeVersionDataset() {
        ModuleTypeVersionDataset moduleTypeVersionDataset = null;
        try {
            if ((targetSubmodule != null) && (targetPlaceholderName != null)) {
                // internal dataset
                moduleTypeVersionDataset = targetSubmodule.getModuleTypeVersion().getModuleTypeVersionDataset(targetPlaceholderName);
            } else if (targetPlaceholderName != null) {
                // external dataset
                moduleTypeVersionDataset = compositeModuleTypeVersion.getModuleTypeVersionDataset(targetPlaceholderName);
            }
        } catch (EmfException e) {
            e.printStackTrace();
        }
        return moduleTypeVersionDataset;
    }

    public boolean sourceIsSet() {
        return getSourceModuleTypeVersionDataset() != null; 
    }

    public boolean sourceIsOptional() {
        ModuleTypeVersionDataset sourceModuleTypeVersionDataset = getSourceModuleTypeVersionDataset();
        if (sourceModuleTypeVersionDataset == null)
            return true; // source invalid or not set
        return sourceModuleTypeVersionDataset.getIsOptional();
    }

    public boolean targetIsOptional() {
        ModuleTypeVersionDataset targetModuleTypeVersionDataset = getTargetModuleTypeVersionDataset();
        if (targetModuleTypeVersionDataset == null)
            return true; // target invalid 
        return targetModuleTypeVersionDataset.getIsOptional();
    }

    public boolean isOptional() {
        return targetIsOptional();
    }

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
 
        if (!isOptional() || (sourcePlaceholderName != null) || (sourceSubmodule != null)) {
            if (sourcePlaceholderName == null) {
                error.append("Source placeholder name is missing for target " + getTargetName() + ".");
                return false;
            }
            if (sourceSubmodule != null) {
                // internal source
                if (!sourceSubmodule.getModuleTypeVersion().getModuleTypeVersionDatasets().containsKey(sourcePlaceholderName)) {
                    error.append(String.format("Source name %s is invalid for target %s.", getSourceName(), getTargetName()));
                    return false;
                }
            } else if (!compositeModuleTypeVersion.getModuleTypeVersionDatasets().containsKey(sourcePlaceholderName)) {
                error.append(String.format("Source placeholder name %s is invalid for target %s.", sourcePlaceholderName, getTargetName()));
                return false;
            }
        }
        
        if (connectionName == null) {
            error.append("Connection name is missing.");
            return false;
        }
        if (!connectionName.equals(getTargetName())) {
            error.append(String.format("Invalid dataset connection name (%s instead of %s).", connectionName, getTargetName()));
            return false;
        }
        
        if (sourceIsOptional() && !targetIsOptional()) {
            error.append(String.format("Invalid dataset connection from optional %s to required %s.", getSourceName(), getTargetName()));
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
