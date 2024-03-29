package gov.epa.emissions.framework.services.module;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModulesExportImport implements Serializable {

    String exportEmfServer;
    String exportEmfVersion;
    String exportFileName;
    String exportUserName;
    Date   exportDate;
    
    Module[] modules;
    Map<Integer, Map<String, String>> moduleDatasetInfo;

    public ModulesExportImport() {
        modules = null;
        moduleDatasetInfo = new HashMap<Integer, Map<String, String>>();
    }
    
    public ModulesExportImport(List<Module> modulesList, Map<Integer, Map<String, String>> moduleDatasetInfo) {
        modules = modulesList.toArray(new Module[0]);
        this.moduleDatasetInfo = moduleDatasetInfo;
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

    public Module[] getModules() {
        return modules;
    }

    public void setModules(Module[] modules) {
        this.modules = modules;
    }

    public Map<Integer, Map<String, String>> getModuleDatasetInfo() {
        return moduleDatasetInfo;
    }

    public void setModuleDatasetInfo(Map<Integer, Map<String, String>> moduleDatasetInfo) {
        this.moduleDatasetInfo = moduleDatasetInfo;
    }
    
    public void prepareForExport() {
        for (Module module : modules) {
            module.prepareForExport();
        }
    }
}
