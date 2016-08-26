package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.module.ModuleType;
import gov.epa.emissions.framework.services.module.ModuleTypeVersion;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionParameter;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionRevision;

import java.util.Date;
import java.util.HashMap;

public class NewModuleTypeVersionParameterPresenter {

    private NewModuleTypeVersionParameterView view;
    private ModuleTypeParametersObserver parametersObserver;

    private EmfSession session;

    public NewModuleTypeVersionParameterPresenter(EmfSession session, NewModuleTypeVersionParameterView view, ModuleTypeParametersObserver parametersObserver) {
        this.session = session;
        this.view = view;
        this.parametersObserver = parametersObserver;
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

    public void doSave(ModuleTypeVersion moduleTypeVersion, ModuleTypeVersionParameter moduleTypeVersionParameter) throws EmfException {
        moduleTypeVersion.addModuleTypeVersionParameter(moduleTypeVersionParameter);
        parametersObserver.refreshParameters();
        closeView();
    }

}
