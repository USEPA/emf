package gov.epa.emissions.framework.client.moduletype;

import java.util.ArrayList;
import java.util.List;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.ModuleService;
import gov.epa.emissions.framework.services.module.ModuleType;

public class ModuleTypeVersionPropertiesPresenter {

    private ModuleTypeVersionPropertiesView view;

    private EmfSession session;

    public ModuleTypeVersionPropertiesPresenter(EmfSession session, ModuleTypeVersionPropertiesView view) {
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

    public DatasetType[] getDatasetTypes() {
        try {
            return session.dataCommonsService().getDatasetTypes();
        }
        catch (EmfException ex) {
            return new DatasetType[]{};
        }
    }

    private void closeView() {
        view.disposeView();
    }

    public ModuleType obtainLockedModuleType(ModuleType moduleType) throws EmfException{
        return session.moduleService().obtainLockedModuleType(session.user(), moduleType);
    }

    public ModuleType releaseLockedModuleType(ModuleType moduleType) {
        try {
            return session.moduleService().releaseLockedModuleType(session.user(), moduleType);
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    
    public void addModule(ModuleType moduleType) throws EmfException {
        session.moduleService().addModuleType(moduleType);
    }

    public ModuleType updateModuleType(ModuleType moduleType) throws EmfException {
        return session.moduleService().updateModuleType(moduleType);
    }

    public void displayNewModuleTypeVersionDatasetView(ModuleTypeVersionDatasetView view) {
        ModuleTypeVersionDatasetPresenter presenter = new ModuleTypeVersionDatasetPresenter(session, view, this.view);
        presenter.doDisplay();
    }

    public void displayNewModuleTypeVersionParameterView(ModuleTypeVersionParameterView view) {
        ModuleTypeVersionParameterPresenter presenter = new ModuleTypeVersionParameterPresenter(session, view, this.view);
        presenter.doDisplay();
    }
}
