package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;

public interface ModuleService {

    // Module Types

    ModuleType[] getModuleTypes() throws EmfException;

    ModuleType getModuleType(int id) throws EmfException;
    
    ParameterType[] getParameterTypes() throws EmfException;

    ModuleType addModuleType(ModuleType moduleType) throws EmfException;

    ModuleType updateModuleType(ModuleType moduleType) throws EmfException;

    void deleteModuleTypes(User owner, ModuleType[] moduleTypes) throws EmfException;

    ModuleType obtainLockedModuleType(User owner, ModuleType moduleType) throws EmfException;

    ModuleType releaseLockedModuleType(User owner, ModuleType moduleType) throws EmfException;

    // Modules

    Module[] getModules() throws EmfException;

    Module getModule(int id) throws EmfException;
    
    Module addModule(Module module) throws EmfException;

    Module updateModule(Module module) throws EmfException;

    void deleteModules(User owner, Module[] modules) throws EmfException;

    Module obtainLockedModule(User owner, Module module) throws EmfException;

    Module releaseLockedModule(User owner, Module module) throws EmfException;

    void runModules(Module[] modules, User user) throws EmfException;
}
