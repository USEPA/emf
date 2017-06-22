package gov.epa.emissions.framework.client.transport;

import java.util.concurrent.ConcurrentSkipListMap;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.DefaultEmfSession.ObjectCacheType;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.module.LiteModule;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.ModuleService;
import gov.epa.emissions.framework.services.module.ModuleType;
import gov.epa.emissions.framework.services.module.ModuleTypeVersion;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionSubmodule;
import gov.epa.emissions.framework.services.module.ParameterType;
import gov.epa.emissions.framework.services.module.Tag;

public class ModuleServiceTransport implements ModuleService {

    private CallFactory callFactory;

    private DataMappings mappings;
    
    private EmfCall call;

    private EmfSession emfSession;
    
    public ModuleServiceTransport(String endpoint, EmfSession emfSession) {
        callFactory = new CallFactory(endpoint);
        mappings = new DataMappings();
        this.emfSession = emfSession;
    }

    private EmfCall call() throws EmfException {
        if (call == null)
            call = callFactory.createSessionEnabledCall("Module Service");
        
        return call;
    }

    @Override
    public synchronized ParameterType[] getParameterTypes() throws EmfException {
        EmfCall call = call();

        call.setOperation("getParameterTypes");
        call.setReturnType(mappings.parameterTypes());

        return (ParameterType[]) call.requestResponse(new Object[] {});
    }


    @Override
    public synchronized Tag[] getTags() throws EmfException {
        EmfCall call = call();

        call.setOperation("getTags");
        call.setReturnType(mappings.tags());

        return (Tag[]) call.requestResponse(new Object[] {});
    }

    @Override
    public synchronized void addTag(Tag tag) throws EmfException {
        EmfCall call = call();

        call.setOperation("addTag");
        call.addParam("tag", mappings.tag());
        call.setVoidReturnType();

        call.request(new Object[] { tag });
    }

    @Override
    public synchronized ModuleType[] getModuleTypes() throws EmfException {
        EmfCall call = call();

        call.setOperation("getModuleTypes");
        call.setReturnType(mappings.moduleTypes());

        return (ModuleType[]) call.requestResponse(new Object[] {});
    }

    @Override
    public synchronized ModuleType getModuleType(int id) throws EmfException {
        EmfCall call = call();

        call.setOperation("getModuleType");
        call.addIntegerParam("id");
        call.setReturnType(mappings.moduleType());

        return (ModuleType) call.requestResponse(new Object[] { new Integer(id) });
    }

    @Override
    public synchronized ModuleType getModuleType(String name) throws EmfException {
        EmfCall call = call();

        call.setOperation("getModuleType");
        call.addStringParam("name");
        call.setReturnType(mappings.moduleType());

        return (ModuleType) call.requestResponse(new Object[] { name });
    }

    @Override
    public synchronized ModuleType addModuleType(ModuleType type) throws EmfException
    {
        EmfCall call = call();

        call.setOperation("addModuleType");
        call.addParam("type", mappings.moduleType());
        call.setReturnType(mappings.moduleType());

        return (ModuleType) call.requestResponse(new Object[] { type });
    }

    @Override
    public ModuleType updateModuleTypeVersion(ModuleTypeVersion moduleTypeVersion, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateModuleTypeVersion");
        call.addParam("moduleTypeVersion", mappings.moduleTypeVersion());
        call.addParam("user", mappings.user());
        call.setReturnType(mappings.moduleType());

        return (ModuleType) call.requestResponse(new Object[] { moduleTypeVersion, user });
    }

    @Override
    public ModuleType finalizeModuleTypeVersion(int moduleTypeVersionId, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("finalizeModuleTypeVersion");
        call.addIntegerParam("moduleTypeVersionId");
        call.addParam("user", mappings.user());
        call.setReturnType(mappings.moduleType());

        return (ModuleType) call.requestResponse(new Object[] { new Integer(moduleTypeVersionId), user });
    }

    @Override
    public ModuleType removeModuleTypeVersion(int moduleTypeVersionId) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeModuleTypeVersion");
        call.addIntegerParam("moduleTypeVersionId");
        call.setReturnType(mappings.moduleType());

        return (ModuleType) call.requestResponse(new Object[] { new Integer(moduleTypeVersionId) });
    }

//    @Override
//    public synchronized ModuleType updateModuleType(ModuleType moduleType) throws EmfException {
//        EmfCall call = call();
//
//        call.setOperation("updateModuleType");
//        call.addParam("moduleType", mappings.moduleType());
//        call.setReturnType(mappings.moduleType());
//
//        return (ModuleType) call.requestResponse(new Object[] { moduleType });
//    }

    @Override
    public void deleteModuleTypes(User owner, ModuleType[] types) throws EmfException {
        EmfCall call = call();

        call.setOperation("deleteModuleTypes");
        call.addParam("owner", mappings.user());
        call.addParam("types", mappings.moduleTypes());
        call.setVoidReturnType();

        call.request(new Object[]{owner, types}); 
    }

    @Override
    public synchronized ModuleType obtainLockedModuleType(User owner, int moduleTypeId) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainLockedModuleType");
        call.addParam("owner", mappings.user());
        call.addIntegerParam("moduleTypeId");
        call.setReturnType(mappings.moduleType());

        return (ModuleType) call.requestResponse(new Object[] { owner, moduleTypeId });
    }

    @Override
    public synchronized ModuleType releaseLockedModuleType(User owner, int moduleTypeId) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLockedModuleType");
        call.addParam("owner", mappings.user());
        call.addIntegerParam("moduleTypeId");
        call.setReturnType(mappings.moduleType());

        return (ModuleType) call.requestResponse(new Object[] { owner, moduleTypeId });
    }

    @Override
    public LiteModule[] getLiteModules() throws EmfException {
        EmfCall call = call();

        call.setOperation("getLiteModules");
        call.setReturnType(mappings.liteModules());

        return (LiteModule[]) call.requestResponse(new Object[] {});
    }

//    @Override
//    public synchronized Module[] getModules() throws EmfException {
//        EmfCall call = call();
//
//        call.setOperation("getModules");
//        call.setReturnType(mappings.modules());
//
//        return (Module[]) call.requestResponse(new Object[] {});
//    }

    @Override
    public synchronized Module getModule(int moduleId) throws EmfException
    {
        EmfCall call = call();

        call.setOperation("getModule");
        call.addIntegerParam("moduleId");
        call.setReturnType(mappings.module());

        Module module = (Module) call.requestResponse(new Object[] { new Integer(moduleId) });
        return module;
    }

    @Override
    public synchronized Module addModule(Module module) throws EmfException
    {
        EmfCall call = call();

        call.setOperation("addModule");
        call.addParam("module", mappings.module());
        call.setReturnType(mappings.module());

        module = (Module) call.requestResponse(new Object[] { module });
        
        emfSession.getObjectCache().invalidate(ObjectCacheType.LITE_MODULES_LIST);
        
        return module;
    }

    @Override
    public synchronized Module updateModule(Module module) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateModule");
        call.addParam("module", mappings.module());
        call.setReturnType(mappings.module());

        module = (Module) call.requestResponse(new Object[] { module });

        emfSession.getObjectCache().invalidate(ObjectCacheType.LITE_MODULES_LIST);
        
        return module;
    }

    @Override
    public synchronized int[] deleteModules(User owner, int[] moduleIds) throws EmfException {
        EmfCall call = call();

        call.setOperation("deleteModules");
        call.addParam("owner", mappings.user());
        call.addParam("moduleIds", mappings.integers());
        call.setReturnType(mappings.integers());

        int[] deletedModuleIds = (int[]) call.requestResponse(new Object[] {owner, moduleIds} ); 

        // refresh the lite modules cache
        ConcurrentSkipListMap<Integer, LiteModule> liteModules = emfSession.getLiteModules();
        for (int deletedModuleId : deletedModuleIds) {
            liteModules.remove(deletedModuleId);
        }
        
        return deletedModuleIds;
    }

    @Override
    public synchronized Module obtainLockedModule(User owner, int moduleId) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainLockedModule");
        call.addParam("owner", mappings.user());
        call.addIntegerParam("moduleId");
        call.setReturnType(mappings.module());

        Module module = (Module) call.requestResponse(new Object[] { owner, new Integer(moduleId) });

        // refresh the lite modules cache
        ConcurrentSkipListMap<Integer, LiteModule> liteModules = emfSession.getLiteModules();
        LiteModule liteModule = liteModules.get(moduleId);
        liteModule.setLockDate(module.getLockDate());
        liteModule.setLockOwner(module.getLockOwner());
        
        return module;
    }

    @Override
    public synchronized Module releaseLockedModule(User owner, int moduleId) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLockedModule");
        call.addParam("owner", mappings.user());
        call.addIntegerParam("moduleId");
        call.setReturnType(mappings.module());

        Module module = (Module) call.requestResponse(new Object[] { owner, new Integer(moduleId) });

        // refresh the lite modules cache
        ConcurrentSkipListMap<Integer, LiteModule> liteModules = emfSession.getLiteModules();
        LiteModule liteModule = liteModules.get(moduleId);
        liteModule.setLockDate(module.getLockDate());
        liteModule.setLockOwner(module.getLockOwner());
        
        return module;
    }

    @Override
    public synchronized int[] lockModules(User owner, int[] moduleIds) throws EmfException {
        EmfCall call = call();

        call.setOperation("lockModules");
        call.addParam("owner", mappings.user());
        call.addParam("moduleIds", mappings.integers());
        call.setReturnType(mappings.integers());

        int[] lockedModuleIds = (int[]) call.requestResponse(new Object[] { owner, moduleIds });

        emfSession.getObjectCache().invalidate(ObjectCacheType.LITE_MODULES_LIST);
        
        return lockedModuleIds;
    }

    @Override
    public synchronized int[] unlockModules(User owner, int[] moduleIds) throws EmfException {
        EmfCall call = call();

        call.setOperation("unlockModules");
        call.addParam("owner", mappings.user());
        call.addParam("moduleIds", mappings.integers());
        call.setReturnType(mappings.integers());

        int[] unlockedModuleIds = (int[]) call.requestResponse(new Object[] { owner, moduleIds });

        emfSession.getObjectCache().invalidate(ObjectCacheType.LITE_MODULES_LIST);
        
        return unlockedModuleIds;
    }

    @Override
    public synchronized void runModules(int[] moduleIds, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("runModules");
        call.addParam("moduleIds", mappings.integers());
        call.addParam("user", mappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { moduleIds, user });

        // NOTE modules are still running
        
        emfSession.getObjectCache().invalidate(ObjectCacheType.LITE_MODULES_LIST);
    }

    @Override
    public synchronized EmfDataset getEmfDatasetForModuleDataset(int moduleDatasetId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getEmfDatasetForModuleDataset");
        call.addIntegerParam("moduleId");
        call.setReturnType(mappings.dataset());

        return (EmfDataset) call.requestResponse(new Object[] { new Integer(moduleDatasetId) });
    }

    @Override
    public synchronized EmfDataset getEmfDatasetForModuleDataset(int moduleDatasetId, Integer newDatasetId, String newDatasetNamePattern) throws EmfException {
        EmfCall call = call();

        call.setOperation("getEmfDatasetForModuleDataset");
        call.addIntegerParam("moduleId");
        call.addIntegerParam("newDatasetId");
        call.addStringParam("newDatasetNamePattern");
        call.setReturnType(mappings.dataset());

        return (EmfDataset) call.requestResponse(new Object[] { new Integer(moduleDatasetId), newDatasetId, newDatasetNamePattern });
    }

    @Override
    public synchronized LiteModule[] getRelatedLiteModules(int datasetId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getRelatedLiteModules");
        call.addIntegerParam("datasetId");
        call.setReturnType(mappings.liteModules());

        LiteModule[] relatedLiteModules = (LiteModule[]) call.requestResponse(new Object[] { new Integer(datasetId) });
        
        // refresh the lite modules cache
        ConcurrentSkipListMap<Integer, LiteModule> liteModules = emfSession.getLiteModules();
        for (LiteModule liteModule : relatedLiteModules) {
            liteModules.put(liteModule.getId(), liteModule);
        }
        
        return relatedLiteModules;
    }

    @Override
    public ModuleTypeVersionSubmodule[] getSubmodulesUsingModuleTypeVersion(int moduleTypeVersionId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getSubmodulesUsingModuleTypeVersion");
        call.addIntegerParam("moduleTypeVersionId");
        call.setReturnType(mappings.moduleTypeVersionSubmodules());

        return (ModuleTypeVersionSubmodule[]) call.requestResponse(new Object[] { new Integer(moduleTypeVersionId) });
    }

    @Override
    public ModuleTypeVersionSubmodule[] getAllSubmodulesUsingModuleTypeVersion(int moduleTypeVersionId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getAllSubmodulesUsingModuleTypeVersion");
        call.addIntegerParam("moduleTypeVersionId");
        call.setReturnType(mappings.moduleTypeVersionSubmodules());

        return (ModuleTypeVersionSubmodule[]) call.requestResponse(new Object[] { new Integer(moduleTypeVersionId) });
    }
    
    @Override
    public ModuleTypeVersion[] getModuleTypeVersionsUsingModuleTypeVersion(int moduleTypeVersionId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getModuleTypeVersionsUsingModuleTypeVersion");
        call.addIntegerParam("moduleTypeVersionId");
        call.setReturnType(mappings.moduleTypeVersions());

        return (ModuleTypeVersion[]) call.requestResponse(new Object[] { new Integer(moduleTypeVersionId) });
    }
    
    @Override
    public ModuleTypeVersion[] getAllModuleTypeVersionsUsingModuleTypeVersion(int moduleTypeVersionId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getAllModuleTypeVersionsUsingModuleTypeVersion");
        call.addIntegerParam("moduleTypeVersionId");
        call.setReturnType(mappings.moduleTypeVersions());

        return (ModuleTypeVersion[]) call.requestResponse(new Object[] { new Integer(moduleTypeVersionId) });
    }
    
    @Override
    public ModuleType[] getModuleTypesUsingModuleTypeVersion(int moduleTypeVersionId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getModuleTypesUsingModuleTypeVersion");
        call.addIntegerParam("moduleTypeVersionId");
        call.setReturnType(mappings.moduleTypes());

        return (ModuleType[]) call.requestResponse(new Object[] { new Integer(moduleTypeVersionId) });
    }
    
    @Override
    public ModuleType[] getAllModuleTypesUsingModuleTypeVersion(int moduleTypeVersionId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getAllModuleTypesUsingModuleTypeVersion");
        call.addIntegerParam("moduleTypeVersionId");
        call.setReturnType(mappings.moduleTypes());

        return (ModuleType[]) call.requestResponse(new Object[] { new Integer(moduleTypeVersionId) });
    }
    
    @Override
    public Module[] getModulesUsingModuleTypeVersion(int moduleTypeVersionId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getModulesUsingModuleTypeVersion");
        call.addIntegerParam("moduleTypeVersionId");
        call.setReturnType(mappings.modules());

        return (Module[]) call.requestResponse(new Object[] { new Integer(moduleTypeVersionId) });
    }

    
    @Override
    public Module[] getAllModulesUsingModuleTypeVersion(int moduleTypeVersionId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getAllModulesUsingModuleTypeVersion");
        call.addIntegerParam("moduleTypeVersionId");
        call.setReturnType(mappings.modules());

        return (Module[]) call.requestResponse(new Object[] { new Integer(moduleTypeVersionId) });
    }
}
