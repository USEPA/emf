package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.ModuleType;
import gov.epa.emissions.framework.services.module.ModuleTypeVersion;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.util.ArrayList;
import java.util.List;

public class ModuleTypeVersionsManagerPresenter {

    private ModuleTypeVersionsManagerView view;

    private EmfSession session;

    public ModuleTypeVersionsManagerPresenter(EmfSession session, ModuleTypeVersionsManagerView view) {
        this.session = session;
        this.view = view;
    }

    public void doDisplay() {
        view.observe(this);
        view.display();
    }

    public void doClose() {
        view.disposeView();
    }

    public void displayNewModuleTypeView(ModuleTypeVersionPropertiesView view) {
        ModuleTypeVersionPropertiesPresenter presenter = new ModuleTypeVersionPropertiesPresenter(session, view);
        presenter.doDisplay();
    }

    public ModuleType obtainLockedModuleType(int moduleTypeId) throws EmfException {
        return session.moduleService().obtainLockedModuleType(session.user(), moduleTypeId);
    }

    public ModuleType releaseLockedModuleType(int moduleTypeId) throws EmfException {
        return session.moduleService().releaseLockedModuleType(session.user(), moduleTypeId);
    }
    
    public ModuleType getModuleType(int id) throws EmfException {
        return session.moduleService().getModuleType(id);
    }

    public ModuleType[] getModuleTypes() throws EmfException {
        return session.moduleService().getModuleTypes();
    }

    public ModuleType removeModuleTypeVersion(int moduleTypeVersionId) throws EmfException {
        return session.moduleService().removeModuleTypeVersion(moduleTypeVersionId);
    }
        
    // TODO add removeModuleTypeVersion instead
//    public ModuleType updateModuleType(ModuleType moduleType) throws EmfException {
//        return session.moduleService().updateModuleType(moduleType);
//    }
}
