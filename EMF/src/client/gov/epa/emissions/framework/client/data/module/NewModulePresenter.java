package gov.epa.emissions.framework.client.data.module;

import gov.epa.emissions.commons.data.ModuleType;
import gov.epa.emissions.commons.data.Module;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;

public class NewModulePresenter {

    private NewModuleView view;

    private EmfSession session;

    public NewModulePresenter(EmfSession session, NewModuleView view) {
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

    public void doSave(Module module) throws EmfException {
        service().addModule(module);
        closeView();
    }

//    public void displayNewModuleTypeVersionDatasetView(NewModuleTypeVersionDatasetView view) {
//        NewModuleTypeVersionDatasetPresenter presenter = new NewModuleTypeVersionDatasetPresenter(session, view, this.view);
//        presenter.doDisplay();
//    }
//
//    public void displayNewModuleTypeVersionParameterView(NewModuleTypeVersionParameterView view) {
//        NewModuleTypeVersionParameterPresenter presenter = new NewModuleTypeVersionParameterPresenter(session, view, this.view);
//        presenter.doDisplay();
//    }

    private DataCommonsService service() {
        return session.dataCommonsService();
    }

}
