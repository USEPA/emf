package gov.epa.emissions.framework.client.data.moduletype;

import gov.epa.emissions.commons.data.ModuleType;
import gov.epa.emissions.commons.data.ModuleTypeVersion;
import gov.epa.emissions.commons.data.ModuleTypeVersionParameter;
import gov.epa.emissions.commons.data.ModuleTypeVersionRevision;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;

import java.util.Date;
import java.util.HashMap;

public class NewModuleTypeVersionParameterPresenter {

    private NewModuleTypeVersionParameterView view;
    private NewModuleTypeView parentView;

    private EmfSession session;

    public NewModuleTypeVersionParameterPresenter(EmfSession session, NewModuleTypeVersionParameterView view, NewModuleTypeView parentView) {
        this.session = session;
        this.view = view;
        this.parentView = parentView;
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
        parentView.refreshParameters();
        closeView();
    }

}
