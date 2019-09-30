package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.ModuleType;
import gov.epa.emissions.framework.services.module.ModuleTypeVersion;

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

    public ModuleType obtainLockedModuleType(int moduleTypeId) throws EmfException{
        return session.moduleService().obtainLockedModuleType(session.user(), moduleTypeId);
    }

    public ModuleType getModuleType(int id) throws EmfException{
        return session.moduleService().getModuleType(id);
    }

    public ModuleType releaseLockedModuleType(int moduleTypeId) {
        try {
            return session.moduleService().releaseLockedModuleType(session.user(), moduleTypeId);
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    
    public ModuleType addModuleType(ModuleType moduleType) throws EmfException {
        return session.moduleService().addModuleType(moduleType);
    }

    public ModuleType updateModuleTypeVersion(ModuleTypeVersion moduleTypeVersion, User user) throws EmfException {
        return session.moduleService().updateModuleTypeVersion(moduleTypeVersion, user);
    }

    public ModuleType finalizeModuleTypeVersion(int moduleTypeVersionId, User user) throws EmfException {
        return session.moduleService().finalizeModuleTypeVersion(moduleTypeVersionId, user);
    }

    public void displayModuleTypeVersionDatasetView(ModuleTypeVersionDatasetView view) {
        ModuleTypeVersionDatasetPresenter presenter = new ModuleTypeVersionDatasetPresenter(session, view, this.view);
        presenter.doDisplay();
    }

    public void displayModuleTypeVersionParameterView(ModuleTypeVersionParameterView view) {
        ModuleTypeVersionParameterPresenter presenter = new ModuleTypeVersionParameterPresenter(session, view, this.view);
        presenter.doDisplay();
    }
    
    public void displayModuleTypeVersionSubmoduleView(ModuleTypeVersionSubmoduleView view) {
        ModuleTypeVersionSubmodulePresenter presenter = new ModuleTypeVersionSubmodulePresenter(session, view, this.view);
        presenter.doDisplay();
    }
    
    public void displayModuleTypeVersionDatasetConnectionView(ModuleTypeVersionDatasetConnectionView view) {
        ModuleTypeVersionDatasetConnectionPresenter presenter = new ModuleTypeVersionDatasetConnectionPresenter(session, view, this.view);
        presenter.doDisplay();
    }
    
    public void displayModuleTypeVersionParameterConnectionView(ModuleTypeVersionParameterConnectionView view) {
        ModuleTypeVersionParameterConnectionPresenter presenter = new ModuleTypeVersionParameterConnectionPresenter(session, view, this.view);
        presenter.doDisplay();
    }

    public void displayAddTagsView(AddTagsView view) throws Exception {
        AddTagsPresenter presenter = new AddTagsPresenter(view, session);
        presenter.display();
    }

    public void displayRemoveTagsView(RemoveTagsView view) throws Exception {
        RemoveTagsPresenter presenter = new RemoveTagsPresenter(view, session);
        presenter.display();
    }
    
    public ModuleTypeVersion[] getModuleTypeVersionsUsingModuleTypeVersion(int moduleTypeVersionId) throws EmfException {
        return session.moduleService().getModuleTypeVersionsUsingModuleTypeVersion(moduleTypeVersionId);
    }
}
