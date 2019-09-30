package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.module.LiteModule;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.ModuleService;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class ModuleComparisonPresenter {

    private ModuleComparisonView view;

    private EmfSession session;

    public ModuleComparisonPresenter(EmfSession session, ModuleComparisonView view) {
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

    public void displayNewModuleView(ModulePropertiesView view) {
        ModulePropertiesPresenter presenter = new ModulePropertiesPresenter(session, view);
        presenter.doDisplay();
    }
}
