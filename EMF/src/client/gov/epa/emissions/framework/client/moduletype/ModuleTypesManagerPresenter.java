package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.ModuleType;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.util.ArrayList;
import java.util.List;

public class ModuleTypesManagerPresenter {

    private ModuleTypesManagerView view;

    private EmfSession session;

    public ModuleTypesManagerPresenter(EmfSession session, ModuleTypesManagerView view) {
        this.session = session;
        this.view = view;
    }

    public void doDisplay() throws EmfException {
        view.observe(this);
        view.display();
        view.populate();
    }

    public void doClose() {
        view.disposeView();
    }

    public void displayModuleTypeVersions(ModuleTypeVersionsManagerView view) throws EmfException {
        ModuleTypeVersionsManagerPresenter presenter = new ModuleTypeVersionsManagerPresenter(session, view);
        presenter.doDisplay();
    }

    public void displayNewModuleTypeView(ModuleTypeVersionPropertiesView view) {
        ModuleTypeVersionPropertiesPresenter presenter = new ModuleTypeVersionPropertiesPresenter(session, view);
        presenter.doDisplay();
    }

    public void doRemove(ModuleType[] types) throws EmfException {

        ModuleType[] lockedTypes = getLockedModuleTypes(types);

        if (lockedTypes == null)
            return;

        try {
            session.moduleService().deleteModuleTypes(session.user(), lockedTypes);
        } catch (EmfException e) {
            throw new EmfException(e.getMessage());
        } finally {
            releaseLockedModuleTypes(lockedTypes);
        }
    }

    private ModuleType[] getLockedModuleTypes(ModuleType[] moduleTypes) throws EmfException{
        List<ModuleType> lockedList = new ArrayList<ModuleType>();
        for (int i=0; i < moduleTypes.length; i++){
            ModuleType locked = session.moduleService().obtainLockedModuleType(session.user(), moduleTypes[i].getId());
            if (locked == null) {
                releaseLockedModuleTypes(lockedList.toArray(new ModuleType[0]));
                return null;
            }
            lockedList.add(locked);
        }
        return lockedList.toArray(new ModuleType[0]);
    }

    private void releaseLockedModuleTypes(ModuleType[] moduleTypes) {
        if (moduleTypes.length == 0)
            return;

        for(int i = 0; i < moduleTypes.length; i++) {
            try {
                session.moduleService().releaseLockedModuleType(session.user(), moduleTypes[i].getId());
            } catch (Exception e) { //so that it go release locks continuously
                e.printStackTrace();
            }
        }
    }

    public ModuleType[] getModuleTypes() throws EmfException {
        return session.moduleService().getModuleTypes();
    }

    public ModuleType addModuleType(ModuleType moduleType) throws EmfException {
        return session.moduleService().addModuleType(moduleType);
    }
}
