package gov.epa.emissions.framework.services.module;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.io.XFileFormat;
import gov.epa.emissions.commons.security.User;

public class ModuleTypesExportImport implements Serializable{

    DatasetType[] datasetTypes;
     ModuleType[]  moduleTypes; // in order of dependencies
    
    public ModuleTypesExportImport() {
        datasetTypes = null;
         moduleTypes = null;
    }

    public ModuleTypesExportImport(Map<Integer, DatasetType> datasetTypesMap, List<ModuleType> moduleTypesList) {
        datasetTypes = datasetTypesMap.values().toArray(new DatasetType[0]);
         moduleTypes = moduleTypesList.toArray(new ModuleType[0]);
    }

    public DatasetType[] getDatasetTypes() {
        return datasetTypes;
    }

    public void setDatasetTypes(DatasetType[] datasetTypes) {
        this.datasetTypes = datasetTypes;
    }

    public ModuleType[] getModuleTypes() {
        return moduleTypes;
    }

    public void setModuleTypes(ModuleType[] moduleTypes) {
        this.moduleTypes = moduleTypes;
    }

    public static void prepareForExport(XFileFormat fileFormat, User user, String message) {
        if (fileFormat.getId() == 0)
            return;
        fileFormat.setId(0);
        fileFormat.setCreator(null);
        fileFormat.setDescription((fileFormat.getDescription() == null) ? "" : (fileFormat.getDescription() + "\n"));
        fileFormat.setDescription(fileFormat.getDescription() + message);
        // nothing to do for columns
    }
    
    public static void prepareForImport(XFileFormat fileFormat, User user, String message) {
        if (fileFormat.getCreator() == user)
            return;
        fileFormat.setCreator(user);
        fileFormat.setDescription((fileFormat.getDescription() == null) ? "" : (fileFormat.getDescription() + "\n"));
        fileFormat.setDescription(fileFormat.getDescription() + message);
        // nothing to do for columns
    }
    
    public static void prepareForExport(DatasetType datasetType, User user, String message) {
        if (datasetType.getId() == 0)
            return;
        datasetType.setId(0);
        datasetType.setCreator(null);
        datasetType.setDescription((datasetType.getDescription() == null) ? "" : (datasetType.getDescription() + "\n"));
        datasetType.setDescription(datasetType.getDescription() + message);
        datasetType.setKeyVals(null);
        datasetType.setQaStepTemplates(null);
        datasetType.setLockDate(null);
        datasetType.setLockOwner(null);
        XFileFormat fileFormat = datasetType.getFileFormat();
        if (fileFormat != null) {
            prepareForExport(fileFormat, user, message);
        }
    }
    
    public static void prepareForImport(DatasetType datasetType, User user, String message) {
        if (datasetType.getCreator() == user)
            return;
        datasetType.setCreator(user);
        datasetType.setDescription((datasetType.getDescription() == null) ? "" : (datasetType.getDescription() + "\n"));
        datasetType.setDescription(datasetType.getDescription() + message);
        XFileFormat fileFormat = datasetType.getFileFormat();
        if (fileFormat != null) {
            prepareForImport(fileFormat, user, message);
        }
    }
    
    public void prepareForExport(String filename, User user) {
        String message = "Exported to \"" + filename + "\" by " + user.getName() + "."; // TODO add server name or URL
        for (DatasetType datasetType : datasetTypes) {
            prepareForExport(datasetType, user, message);
        }
        for (ModuleType moduleType : moduleTypes) {
            moduleType.prepareForExport(user, message);
        }
    }

    public void prepareForImport(String filename, User user) {
        String message = "Imported from \"" + filename + "\" by " + user.getName() + "."; // TODO add server name or URL
        for (DatasetType datasetType : datasetTypes) {
            prepareForImport(datasetType, user, message);
        }
        for (ModuleType moduleType : moduleTypes) {
            moduleType.prepareForImport(user, message);
        }
    }
    
    public void replaceDatasetType(DatasetType importedDatasetType, DatasetType localDatasetType) {
        for (ModuleType moduleType : moduleTypes) {
            moduleType.replaceDatasetType(importedDatasetType, localDatasetType);
        }
    }
    
    public void replaceModuleType(ModuleType importedModuleType, ModuleType localModuleType) {
        boolean found = false;
        for (ModuleType moduleType : moduleTypes) {
            if (found) {
                for (ModuleTypeVersion importedModuleTypeVersion : importedModuleType.getModuleTypeVersions().values()) {
                    ModuleTypeVersion localModuleTypeVersion = localModuleType.getModuleTypeVersions().get(importedModuleTypeVersion.getVersion());
                    moduleType.replaceModuleTypeVersion(importedModuleTypeVersion, localModuleTypeVersion);
                }
            } else if (moduleType == importedModuleType) {
                found = true;
            }
        }
    }
}
