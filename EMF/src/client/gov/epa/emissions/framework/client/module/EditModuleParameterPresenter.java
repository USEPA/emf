package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.module.ModuleParameter;

public class EditModuleParameterPresenter {
       
    private EditModuleParameterView view;
    private ModuleParametersObserver parametersObserver;

    private EmfSession session;

    public EditModuleParameterPresenter(EmfSession session, EditModuleParameterView view, ModuleParametersObserver parametersObserver) {
        this.session = session;
        this.view = view;
        this.parametersObserver = parametersObserver;
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

    public void doSave(ModuleParameter moduleParameter) throws EmfException {
        parametersObserver.refreshParameters();
        closeView();
    }

}
