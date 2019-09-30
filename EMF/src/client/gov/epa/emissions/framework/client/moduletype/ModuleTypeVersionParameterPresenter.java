package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.module.ModuleTypeVersion;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionParameter;

public class ModuleTypeVersionParameterPresenter {

    private ModuleTypeVersionParameterView view;
    private ModuleTypeParametersObserver parametersObserver;

    private EmfSession session;

    public ModuleTypeVersionParameterPresenter(EmfSession session, ModuleTypeVersionParameterView view, ModuleTypeParametersObserver parametersObserver) {
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

    public void doRemove(ModuleTypeVersion moduleTypeVersion, ModuleTypeVersionParameter moduleTypeVersionParameter) throws EmfException {
        moduleTypeVersion.removeModuleTypeVersionParameter(moduleTypeVersionParameter);
        parametersObserver.refreshParameters();
    }
    
    public void doSave(ModuleTypeVersion moduleTypeVersion, ModuleTypeVersionParameter moduleTypeVersionParameter) throws EmfException {
        moduleTypeVersion.addModuleTypeVersionParameter(moduleTypeVersionParameter);
        parametersObserver.refreshParameters();
    }
}
