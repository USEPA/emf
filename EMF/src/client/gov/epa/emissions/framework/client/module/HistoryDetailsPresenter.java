package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.module.HistoryDataset;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.ModuleService;
import gov.epa.emissions.framework.services.module.ModuleType;

public class HistoryDetailsPresenter {

    private HistoryDetailsView view;

    private EmfSession session;

    public HistoryDetailsPresenter(EmfSession session, HistoryDetailsView view) {
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

    private DataService dataService() {
        return session.dataService();
    }
    
    public EmfDataset getDataset(int datasetId) throws EmfException {
        return dataService().getDataset(datasetId);
    }
    
    public void doDisplayPropertiesView(PropertiesView propertiesView, HistoryDataset historyDataset) throws EmfException {
        if (historyDataset.getDatasetId() == null)
            return;
        EmfDataset dataset = getDataset(historyDataset.getDatasetId());
        if (dataset == null)
            return;
        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }

    private void closeView() {
        view.disposeView();
    }

    public Module obtainLockedModule(Module module) throws EmfException {
        return service().obtainLockedModule(session.user(), module);
    }
    
    public Module releaseLockedModule(Module module) throws EmfException {
        return service().releaseLockedModule(session.user(), module);
    }
    
    public Module addModule(Module module) throws EmfException {
        return service().addModule(module);
    }

    public void updateModule(Module module) throws EmfException {
        service().updateModule(module);
    }

    private ModuleService service() {
        return session.moduleService();
    }

}
