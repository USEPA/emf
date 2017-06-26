package gov.epa.emissions.framework.services.module;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.io.XFileFormat;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;

public class ModuleTypesExportImport implements Serializable {

    String exportEmfServer;
    String exportEmfVersion;
    String exportFileName;
    String exportUserName;
    Date   exportDate;
    
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

    public String getExportEmfServer() {
        return exportEmfServer;
    }

    public void setExportEmfServer(String exportEmfServer) {
        this.exportEmfServer = exportEmfServer;
    }

    public String getExportEmfVersion() {
        return exportEmfVersion;
    }

    public void setExportEmfVersion(String exportEmfVersion) {
        this.exportEmfVersion = exportEmfVersion;
    }

    public String getExportFileName() {
        return exportFileName;
    }

    public void setExportFileName(String exportFileName) {
        this.exportFileName = exportFileName;
    }

    public String getExportUserName() {
        return exportUserName;
    }

    public void setExportUserName(String exportUserName) {
        this.exportUserName = exportUserName;
    }

    public Date getExportDate() {
        return exportDate;
    }

    public void setExportDate(Date exportDate) {
        this.exportDate = exportDate;
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

    public static void prepareForExport(XFileFormat fileFormat) {
        if (fileFormat.getId() == 0)
            return;
        fileFormat.setId(0);
        fileFormat.setCreator(null);
        // nothing to do for columns
    }
    
    public static void prepareForImport(XFileFormat fileFormat, String exportImportMessage, User importUser, Date importDate) {
        if (fileFormat.getCreator() == importUser)
            return;
        fileFormat.setCreator(importUser);
        fileFormat.setLastModifiedDate(importDate);
        fileFormat.setDescription((fileFormat.getDescription() == null) ? "" : (fileFormat.getDescription() + "\n"));
        fileFormat.setDescription(fileFormat.getDescription() + exportImportMessage);
        // nothing to do for columns
    }
    
    public static void prepareForExport(DatasetType datasetType) {
        if (datasetType.getId() == 0)
            return;
        datasetType.setId(0);
        datasetType.setCreator(null);
        datasetType.setKeyVals(null);
        datasetType.setQaStepTemplates(null);
        datasetType.setLockDate(null);
        datasetType.setLockOwner(null);
        XFileFormat fileFormat = datasetType.getFileFormat();
        if (fileFormat != null) {
            prepareForExport(fileFormat);
        }
    }
    
    public static void prepareForImport(DatasetType datasetType, String exportImportMessage, User importUser, Date importDate) {
        if (datasetType.getCreator() == importUser)
            return;
        datasetType.setCreator(importUser);
        datasetType.setLastModifiedDate(importDate);
        datasetType.setDescription((datasetType.getDescription() == null) ? "" : (datasetType.getDescription() + "\n"));
        datasetType.setDescription(datasetType.getDescription() + exportImportMessage);
        XFileFormat fileFormat = datasetType.getFileFormat();
        if (fileFormat != null) {
            prepareForImport(fileFormat, exportImportMessage, importUser, importDate);
        }
    }
    
    public void prepareForExport() {
        for (DatasetType datasetType : datasetTypes) {
            prepareForExport(datasetType);
        }
        for (ModuleType moduleType : moduleTypes) {
            moduleType.prepareForExport();
        }
    }

//    public void prepareForImport(String importEmfServer, String importEmfVersion, String importFileName, User importUser, Date importDate) {
//        String exportMessage = String.format("Exported from server \"%s\" version \"%s\" to \"%s\" by %s on %s.",
//                                             exportEmfServer, exportEmfVersion, exportFileName, exportUserName,
//                                             CustomDateFormat.format_MM_DD_YYYY_HH_mm(exportDate));
//        String importMessage = String.format("Imported on server \"%s\" version \"%s\" from file \"%s\" by %s on %s.",
//                                             importEmfServer, importEmfVersion, importFileName, importUser.getUsername(),
//                                             CustomDateFormat.format_MM_DD_YYYY_HH_mm(importDate));
//        for (DatasetType datasetType : datasetTypes) {
//            prepareForImport(datasetType, exportMessage, importMessage, importUser, importDate);
//        }
//        for (ModuleType moduleType : moduleTypes) {
//            moduleType.prepareForImport(exportMessage, importMessage, importUser, importDate);
//        }
//    }
    
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
