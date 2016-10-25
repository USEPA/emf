package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.module.HistoryDataset;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.ModuleDataset;
import gov.epa.emissions.framework.services.module.ModuleService;
import gov.epa.emissions.framework.services.module.ModuleType;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionDataset;

public class ModulePropertiesPresenter {

    private ModulePropertiesView view;

    private EmfSession session;

    public ModulePropertiesPresenter(EmfSession session, ModulePropertiesView view) {
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

    private DataService dataService() {
        return session.dataService();
    }
    
    public EmfDataset getDataset(int datasetId) throws EmfException {
        return dataService().getDataset(datasetId);
    }
    
    public EmfDataset getDataset(String name) throws EmfException {
        return dataService().getDataset(name);
    }
    
    public void doDisplayDatasetProperties(PropertiesView propertiesView, ModuleDataset moduleDataset) throws EmfException {
        EmfDataset dataset = null;
        
        ModuleTypeVersionDataset moduleTypeVersionDataset = moduleDataset.getModuleTypeVersionDataset();
        if (moduleTypeVersionDataset.getMode().equals(ModuleTypeVersionDataset.OUT)) {
            if (moduleDataset.getOutputMethod().equals(ModuleDataset.NEW)) {
                String datasetName = moduleDataset.getDatasetNamePattern(); 
                if (datasetName != null)
                    dataset = getDataset(datasetName);
            } else if (moduleDataset.getOutputMethod().equals(ModuleDataset.REPLACE)) {
                Integer datasetId = moduleDataset.getDatasetId();
                if (datasetId != null)
                    dataset = getDataset(datasetId);
            }
        } else { // IN or INOUT
            Integer datasetId = moduleDataset.getDatasetId();
            if (datasetId != null)
                dataset = getDataset(moduleDataset.getDatasetId());
        }
        
        if (dataset != null) {
            PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
            presenter.doDisplay(propertiesView);
        }
    }
    
    public void runModule(Module module) throws EmfException {
        if (module == null)
            return;
        session.moduleService().runModules(new Module[] { module }, session.user());
    }
}
