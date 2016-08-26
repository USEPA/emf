package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.module.ModuleType;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.util.ArrayList;
import java.util.List;

public class ModuleTypesManagerPresenter implements RefreshObserver {

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

//    public void doEdit(ModuleType type, EditableModuleTypeView editable, ViewableModuleTypeView viewable)
//            throws EmfException {
//        EditableModuleTypePresenter p = new EditableModuleTypePresenterImpl(session, editable, viewable, type);
//        edit(p);
//    }
//
//    void edit(EditableModuleTypePresenter presenter) throws EmfException {
//        presenter.doDisplay();
//    }
//
//    public void doView(ModuleType type, ViewableModuleTypeView viewable) throws EmfException {
//        ViewableModuleTypePresenter p = new ViewableModuleTypePresenterImpl(viewable, type);
//        view(p);
//    }
//
//    void view(ViewableModuleTypePresenter presenter) throws EmfException {
//        presenter.doDisplay();
//    }

    public void displayNewModuleTypeView(NewModuleTypeView view) {
        NewModuleTypePresenter presenter = new NewModuleTypePresenter(session, view);
        presenter.doDisplay();
    }

    public void doRemove(ModuleType[] types) throws EmfException {

        ModuleType[] lockedTypes = getLockedTypes(types);

        if (lockedTypes == null)
            return;

        try {
            session.moduleService().deleteModuleTypes(session.user(), lockedTypes);
        } catch (EmfException e) {
            throw new EmfException(e.getMessage());
        } finally {
            releaseLocked(lockedTypes);
        }
    }

    private ModuleType[] getLockedTypes(ModuleType[] types) throws EmfException{
        List<ModuleType> lockedList = new ArrayList<ModuleType>();
        for (int i=0; i < types.length; i++){
            ModuleType locked = session.moduleService().obtainLockedModuleType(session.user(), types[i]);
            if (locked == null) {
                releaseLocked(lockedList.toArray(new ModuleType[0]));
                return null;
            }
            lockedList.add(locked);
        }
        return lockedList.toArray(new ModuleType[0]);
    }

    private void releaseLocked(ModuleType[] lockedTypes) {
        if (lockedTypes.length == 0)
            return;

        for(int i = 0; i < lockedTypes.length; i++) {
            try {
                session.moduleService().releaseLockedModuleType(session.user(), lockedTypes[i]);
            } catch (Exception e) { //so that it go release locks continuously
                e.printStackTrace();
            }
        }
    }

    public void doRefresh() throws EmfException {
        view.refresh(session.moduleService().getModuleTypes());
    }

    public ModuleType[] getModuleTypes() throws EmfException {
        return session.moduleService().getModuleTypes();
    }

}
