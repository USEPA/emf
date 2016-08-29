package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.ModuleService;
import gov.epa.emissions.framework.services.module.ModuleType;

public class ModulePropertiesPresenter {

    private ModulePropertiesView view;

    private EmfSession session;

    public ModulePropertiesPresenter(EmfSession session, ModulePropertiesView view) {
        this.session = session;
        this.view = view;
    }

    public void doDisplay() {
        view.observe(this);
        view.display();
    }

    public void doClose() {
        closeView();
    }

    public ModuleType[] getModuleTypes() {
        try {
            return service().getModuleTypes();
        }
        catch (EmfException ex) {
            return new ModuleType[]{};
        }
    }

    private void closeView() {
        view.disposeView();
    }

    public Module obtainLockedModule(Module module) throws EmfException {
        return service().obtainLockedModule(session.user(), module);
    }
    
    public Module releaseLockedModule(Module module) throws EmfException {
        return service().obtainLockedModule(session.user(), module);
    }
    
    public void addModule(Module module) throws EmfException {
        service().addModule(module);
    }

    public void updateModule(Module module) throws EmfException {
        service().updateModule(module);
    }

    private ModuleService service() {
        return session.moduleService();
    }

}
