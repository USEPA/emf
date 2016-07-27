package gov.epa.emissions.framework.client.data.module;

import gov.epa.emissions.commons.data.Module;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.util.ArrayList;
import java.util.List;

public class ModulesManagerPresenter implements RefreshObserver {

    private ModulesManagerView view;

    private EmfSession session;

    public ModulesManagerPresenter(EmfSession session, ModulesManagerView view) {
        this.session = session;
        this.view = view;
    }

    public void doDisplay() throws EmfException {
        view.observe(this);
        view.display();
        view.populate();
    }

    private DataCommonsService service() {
        return session.dataCommonsService();
    }

    public void doClose() {
        view.disposeView();
    }

//    public void doEdit(Module module, EditableModuleView editable, ViewableModuleView viewable)
//            throws EmfException {
//        EditableModulePresenter p = new EditableModulePresenterImpl(session, editable, viewable, module);
//        edit(p);
//    }
//
//    void edit(EditableModulePresenter presenter) throws EmfException {
//        presenter.doDisplay();
//    }
//
//    public void doView(Module module, ViewableModuleView viewable) throws EmfException {
//        ViewableModulePresenter p = new ViewableModulePresenterImpl(viewable, module);
//        view(p);
//    }
//
//    void view(ViewableModulePresenter presenter) throws EmfException {
//        presenter.doDisplay();
//    }

//    public void displayNewModuleView(NewModuleView view) {
//        NewModulePresenter presenter = new NewModulePresenter(session, view);
//        presenter.doDisplay();
//    }

    public void doRemove(Module[] modules) throws EmfException {

        Module[] lockedTypes = getLockedTypes(modules);

        if (lockedTypes == null)
            return;

        try {
            service().deleteModules(session.user(), lockedTypes);
        } catch (EmfException e) {
            throw new EmfException(e.getMessage());
        } finally {
            releaseLocked(lockedTypes);
        }
    }

    private Module[] getLockedTypes(Module[] modules) throws EmfException{
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

    private void releaseLocked(Module[] lockedTypes) {
        if (lockedTypes.length == 0)
            return;

        for(int i = 0; i < lockedTypes.length; i++) {
            try {
                service().releaseLockedModule(session.user(), lockedTypes[i]);
            } catch (Exception e) { //so that it go release locks continuously
                e.printStackTrace();
            }
        }
    }

    public void doRefresh() throws EmfException {
        view.refresh(service().getModules());
    }

    public Module[] getModules() throws EmfException {
        return service().getModules();
    }

}
