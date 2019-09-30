package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.BasicSearchFilter;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface ModuleService {

    // Parameter Types
    
    ParameterType[] getParameterTypes() throws EmfException;

    // Tags
    
    Tag[] getTags() throws EmfException;
    void addTag(Tag tag) throws EmfException;

    // Module Types

    ModuleType[] getModuleTypes() throws EmfException; // TODO add LiteModuleType class (and maybe LiteModuleTypeVersion too) and use the session object cache

    ModuleType getModuleType(int id) throws EmfException;

    ModuleType getModuleType(String name) throws EmfException;
    
    ModuleType addModuleType(ModuleType moduleType) throws EmfException;

    void deleteModuleTypes(User owner, ModuleType[] moduleTypes) throws EmfException;

    ModuleType obtainLockedModuleType(User owner, int moduleTypeId) throws EmfException;

    ModuleType releaseLockedModuleType(User owner, int moduleTypeId) throws EmfException;

    // Module Type Versions
    
    ModuleType updateModuleTypeVersion(ModuleTypeVersion moduleTypeVersion, User user) throws EmfException;

    ModuleType finalizeModuleTypeVersion(int moduleTypeVersionId, User user) throws EmfException;

    ModuleType removeModuleTypeVersion(int moduleTypeVersionId) throws EmfException;

    // Lite Modules

    LiteModule[] getLiteModules() throws EmfException;

    LiteModule[] getLiteModules(BasicSearchFilter searchFilter) throws EmfException;

    LiteModule[] getRelatedLiteModules(int datasetId) throws EmfException;
    
    // Modules

    Module getModule(int moduleId) throws EmfException;

    Module getModule(String name) throws EmfException;
    
    Module addModule(Module module) throws EmfException;

    Module updateModule(Module module) throws EmfException;

    int[] deleteModules(User owner, int[] moduleIds, boolean deleteOutputs) throws EmfException;

    Module obtainLockedModule(User owner, int moduleId) throws EmfException;

    Module releaseLockedModule(User owner, int moduleId) throws EmfException;

    int[] lockModules(User owner, int[] moduleIds) throws EmfException;

    int[] unlockModules(User owner, int[] moduleIds) throws EmfException;
    
    void runModules(int[] moduleIds, User user) throws EmfException;

    EmfDataset getEmfDatasetForModuleDataset(int moduleDatasetId) throws EmfException;
    EmfDataset getEmfDatasetForModuleDataset(int moduleDatasetId, Integer newDatasetId, String newDatasetNamePattern) throws EmfException;
    
    ModuleTypeVersionSubmodule[] getSubmodulesUsingModuleTypeVersion(int moduleTypeVersionId) throws EmfException;    // one level only
    ModuleTypeVersionSubmodule[] getAllSubmodulesUsingModuleTypeVersion(int moduleTypeVersionId) throws EmfException; // all levels (recursive)
    
    ModuleTypeVersion[] getModuleTypeVersionsUsingModuleTypeVersion(int moduleTypeVersionId) throws EmfException;    // one level only
    ModuleTypeVersion[] getAllModuleTypeVersionsUsingModuleTypeVersion(int moduleTypeVersionId) throws EmfException; // all levels (recursive)
    
    ModuleType[] getModuleTypesUsingModuleTypeVersion(int moduleTypeVersionId) throws EmfException;    // one level only
    ModuleType[] getAllModuleTypesUsingModuleTypeVersion(int moduleTypeVersionId) throws EmfException; // all levels (recursive)
    
    Module[] getModulesUsingModuleTypeVersion(int moduleTypeVersionId) throws EmfException;    // one level only
    Module[] getAllModulesUsingModuleTypeVersion(int moduleTypeVersionId) throws EmfException; // all levels (recursive)
    
    // History
    
    History getHistory(int historyId) throws EmfException;
    
    History[] getHistoryForModule(int moduleId) throws EmfException;
    
    void deleteHistory(int historyId) throws EmfException;

    // modules and module types
    
    void releaseOrphanLocks() throws EmfException;
}
