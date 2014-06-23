package gov.epa.emissions.framework.client.data.dataset;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetVersion;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class SetDatasetVersionPresenter {

    private SetDatasetVersionView view;
    private AddRemoveDatasetVersionWidget addRemoveDatasetVersionWidget;
    private EmfSession session;
    
    public SetDatasetVersionPresenter(AddRemoveDatasetVersionWidget addRemoveDatasetVersionWidget, SetDatasetVersionView view, 
            EmfSession session) {
        this.addRemoveDatasetVersionWidget = addRemoveDatasetVersionWidget;
        this.view = view;
        this.session = session;
    }

    public void display() {
        view.observe(this);
        view.display();

    }

    public void doSetVersion(DatasetVersion datasetVersion) {
        addRemoveDatasetVersionWidget.setDatasetVersions(new DatasetVersion[] { datasetVersion });
    }

    public Version[] getVersions(EmfDataset dataset) throws EmfException {
        if (dataset == null) {
            return new Version[0];
        }
        return session.dataEditorService().getVersions(dataset.getId());
    }
}
