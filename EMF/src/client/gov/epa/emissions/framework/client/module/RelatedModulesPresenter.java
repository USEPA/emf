package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.module.LiteModule;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.ModuleService;

public class RelatedModulesPresenter {

    private RelatedModulesView view;

    private EmfSession session;

    public RelatedModulesPresenter(EmfSession session, RelatedModulesView view) {
        this.session = session;
        this.view = view;
    }

    public void doDisplay() {
        view.observe(this);
        view.display();
        view.populate();
    }

    private ModuleService service() {
        return session.moduleService();
    }

    public void doClose() {
        view.disposeView();
    }

    public LiteModule[] getRelatedLiteModules(int datasetId) throws EmfException {
        return service().getRelatedLiteModules(datasetId);
    }

    public Module getModule(int id) throws EmfException {
        return service().getModule(id);
    }

    public void displayModuleProperties(ModulePropertiesView view) {
        ModulePropertiesPresenter presenter = new ModulePropertiesPresenter(session, view);
        presenter.doDisplay();
    }
}
