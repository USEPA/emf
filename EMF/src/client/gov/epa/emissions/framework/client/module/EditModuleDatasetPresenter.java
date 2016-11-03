package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.module.ModuleDataset;

public class EditModuleDatasetPresenter {
       
    private EditModuleDatasetView view;
    private ModuleDatasetsObserver datasetsObserver;

    private EmfSession session;

    public EditModuleDatasetPresenter(EmfSession session, EditModuleDatasetView view, ModuleDatasetsObserver datasetsObserver) {
        this.session = session;
        this.view = view;
        this.datasetsObserver = datasetsObserver;
    }

    public DatasetType[] getDatasetTypes() {
        try {
            return service().getDatasetTypes();
        }
        catch(EmfException ex){
            return new DatasetType[]{};
        }
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

    public void doSave(ModuleDataset moduleDataset) throws EmfException {
        datasetsObserver.refreshDatasets();
    }

}
