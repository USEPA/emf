package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.DefaultEmfSession.ObjectCacheType;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.ModuleService;
import gov.epa.emissions.framework.services.module.ModuleType;

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

    public synchronized ModuleType[] getModuleTypes() throws EmfException {
        EmfCall call = call();

        call.setOperation("getModuleTypes");
        call.setReturnType(mappings.moduleTypes());

        return (ModuleType[]) call.requestResponse(new Object[] {});
    }

    public synchronized ModuleType addModuleType(ModuleType type) throws EmfException
    {
        EmfCall call = call();

        call.setOperation("addModuleType");
        call.addParam("type", mappings.moduleType());
        call.setReturnType(mappings.moduleType());

        return (ModuleType) call.requestResponse(new Object[] { type });
    }

    public synchronized ModuleType updateModuleType(ModuleType moduleType) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateModuleType");
        call.addParam("moduleType", mappings.moduleType());
        call.setReturnType(mappings.moduleType());

        return (ModuleType) call.requestResponse(new Object[] { moduleType });
    }

    public void deleteModuleTypes(User owner, ModuleType[] types) throws EmfException {
        EmfCall call = call();

        call.setOperation("deleteModuleTypes");
        call.addParam("owner", mappings.user());
        call.addParam("types", mappings.moduleTypes());
        call.setVoidReturnType();

        call.request(new Object[]{owner, types}); 
    }

    public synchronized ModuleType obtainLockedModuleType(User owner, ModuleType type) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainLockedModuleType");
        call.addParam("owner", mappings.user());
        call.addParam("type", mappings.moduleType());
        call.setReturnType(mappings.moduleType());

        return (ModuleType) call.requestResponse(new Object[] { owner, type });
    }

    public synchronized ModuleType releaseLockedModuleType(User owner, ModuleType type) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLockedModuleType");
        call.addParam("owner", mappings.user());
        call.addParam("type", mappings.moduleType());
        call.setReturnType(mappings.moduleType());

        return (ModuleType) call.requestResponse(new Object[] { owner, type });
    }

    public synchronized Module[] getModules() throws EmfException {
        EmfCall call = call();

        call.setOperation("getModules");
        call.setReturnType(mappings.modules());

        return (Module[]) call.requestResponse(new Object[] {});
    }

    public synchronized Module addModule(Module module) throws EmfException
    {
        EmfCall call = call();

        call.setOperation("addModule");
        call.addParam("module", mappings.module());
        call.setReturnType(mappings.module());

        return (Module) call.requestResponse(new Object[] { module });
    }

    public synchronized Module updateModule(Module module) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateModule");
        call.addParam("module", mappings.module());
        call.setReturnType(mappings.module());

        return (Module) call.requestResponse(new Object[] { module });
    }

    public void deleteModules(User owner, Module[] modules) throws EmfException {
        EmfCall call = call();

        call.setOperation("deleteModules");
        call.addParam("owner", mappings.user());
        call.addParam("modules", mappings.modules());
        call.setVoidReturnType();

        call.request(new Object[]{owner, modules}); 

        //make sure we refresh the client-side cache
        this.emfSession.getObjectCache().invalidate(ObjectCacheType.LIGHT_DATASET_TYPES_LIST);
    }

    public synchronized Module obtainLockedModule(User owner, Module module) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainLockedModule");
        call.addParam("owner", mappings.user());
        call.addParam("module", mappings.module());
        call.setReturnType(mappings.module());

        return (Module) call.requestResponse(new Object[] { owner, module });
    }

    public synchronized Module releaseLockedModule(User owner, Module module) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLockedModule");
        call.addParam("owner", mappings.user());
        call.addParam("module", mappings.module());
        call.setReturnType(mappings.module());

        return (Module) call.requestResponse(new Object[] { owner, module });
    }

    public synchronized void runModules(Module[] modules, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("runModules");
        call.addParam("modules", mappings.modules());
        call.addParam("user", mappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { modules, user });
    }
}
