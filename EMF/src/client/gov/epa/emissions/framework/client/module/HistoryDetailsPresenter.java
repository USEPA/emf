package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.module.HistoryDataset;
import gov.epa.emissions.framework.services.module.Module;

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

    public EmfDataset getDataset(int datasetId) throws EmfException {
        return session.dataService().getDataset(datasetId);
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

    public Module[] getModules() throws EmfException {
        return session.moduleService().getModules();
    }

    public Module getModule(int id) throws EmfException {
        return session.moduleService().getModule(id);
    }
    
    public void doDisplayRelatedModules(RelatedModulesView view) throws EmfException {
        RelatedModulesPresenter presenter = new RelatedModulesPresenter(session, view);
        presenter.doDisplay();
    }
}
