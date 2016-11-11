package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.ModuleService;

import java.util.ArrayList;
import java.util.List;

public class ModulesManagerPresenter {

    private ModulesManagerView view;

    private EmfSession session;

    public ModulesManagerPresenter(EmfSession session, ModulesManagerView view) {
        this.session = session;
        this.view = view;
    }

    public void doDisplay() {
        view.observe(this);
        view.display();
        view.populate();
    }

    private ModuleService service() {
        return session.moduleService();
    }

    public void doClose() {
        view.disposeView();
    }

    public void displayNewModuleView(ModulePropertiesView view) {
        ModulePropertiesPresenter presenter = new ModulePropertiesPresenter(session, view);
        presenter.doDisplay();
    }

    public void doRemove(Module[] modules) throws EmfException {

        Module[] lockedModules = getLockedModules(modules);

        if (lockedModules == null)
            return;

        try {
            service().deleteModules(session.user(), lockedModules);
        } catch (EmfException e) {
            throw new EmfException(e.getMessage());
        } finally {
            releaseLocked(lockedModules);
        }
    }

    public void runModules(Module[] modules) throws EmfException {

        Module[] lockedModules = getLockedModules(modules);

        if (lockedModules == null)
            return;

        try {
            session.moduleService().runModules(lockedModules, session.user());
        } catch (EmfException e) {
            throw new EmfException(e.getMessage());
        } finally {
            releaseLocked(lockedModules);
        }
    }

    private Module[] getLockedModules(Module[] modules) throws EmfException{
        List<Module> lockedList = new ArrayList<Module>();
        for (int i=0; i < modules.length; i++){
            Module locked = service().obtainLockedModule(session.user(), modules[i]);
            if (locked == null) {
                releaseLocked(lockedList.toArray(new Module[0]));
                return null;
            }
            lockedList.add(locked);
        }
        return lockedList.toArray(new Module[0]);
    }

    private void releaseLocked(Module[] lockedModules) {
        if (lockedModules.length == 0)
            return;

        for(int i = 0; i < lockedModules.length; i++) {
            try {
                service().releaseLockedModule(session.user(), lockedModules[i]);
            } catch (Exception e) { //so that it go release locks continuously
                e.printStackTrace();
            }
        }
    }

    public Module[] getModules() throws EmfException {
        return service().getModules();
    }

}
