package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.module.ModuleTypeVersion;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionSubmodule;

public class ModuleTypeVersionSubmodulePresenter {
       
    private ModuleTypeVersionSubmoduleView view;
    private ModuleTypeSubmodulesObserver submodulesObserver;

    private EmfSession session;

    public ModuleTypeVersionSubmodulePresenter(EmfSession session, ModuleTypeVersionSubmoduleView view, ModuleTypeSubmodulesObserver submodulesObserver) {
        this.session = session;
        this.view = view;
        this.submodulesObserver = submodulesObserver;
    }

    private DataCommonsService service() {
        return session.dataCommonsService();
    }

    public void doDisplay() {
        view.observe(this);
        view.display();
    }

    public void doClose() {
        closeView();
    }

    private void closeView() {
        view.disposeView();
    }

    public void doRemove(ModuleTypeVersion moduleTypeVersion, ModuleTypeVersionSubmodule moduleTypeVersionSubmodule) throws EmfException {
        moduleTypeVersion.removeModuleTypeVersionSubmodule(moduleTypeVersionSubmodule);
        submodulesObserver.refreshSubmodules();
    }

    public void doSave(ModuleTypeVersion moduleTypeVersion, ModuleTypeVersionSubmodule moduleTypeVersionSubmodule) throws EmfException {
        moduleTypeVersion.addModuleTypeVersionSubmodule(moduleTypeVersionSubmodule);
        submodulesObserver.refreshSubmodules();
    }
}
