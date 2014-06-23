package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.data.viewer.DataView;
import gov.epa.emissions.framework.client.data.viewer.DataViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.DataViewService;

public class VersionsViewPresenter {

    private EmfDataset dataset;

    private EmfSession session;

    public VersionsViewPresenter(EmfDataset dataset, EmfSession session) {
        this.dataset = dataset;
        this.session = session;
    }

    public void display(VersionsView view) throws EmfException {
        view.observe(this);

        DataViewService service = session.dataViewService();
        Version[] versions = service.getVersions(dataset.getId());
        view.display(versions, dataset.getInternalSources());
    }

    public void doView(Version version, String table, DataView view) throws EmfException {
        // if (!version.isFinalVersion())
        // throw new EmfException("Can only view a version that is Final");

        DataViewPresenter presenter = new DataViewPresenter(dataset, version, table, view, session);
        presenter.display();
    }

    public void copyDataset(Version version) throws EmfException {
        if (!version.isFinalVersion())
            throw new EmfException("Can only copy a version that is Final");

        session.dataService().copyDataset(dataset.getId(), version, session.user());
    }

    public Integer[] getDatasetRecords(int datasetID, Version[] versions, String tableName) throws EmfException{
        return session.dataService().getNumOfRecords(datasetID, versions, tableName);
    }
}
