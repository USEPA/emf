package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class VersionedDataPresenter {

    private VersionedDataView view;

    private EmfDataset dataset;

    private EmfSession session;

    public VersionedDataPresenter(EmfDataset dataset, EmfSession session) {
        this.dataset = dataset;
        this.session = session;
    }

    public void display(VersionedDataView view) {
        this.view = view;
        view.observe(this);

        EditVersionsPresenter versionsPresenter = new EditVersionsPresenter(dataset, session);
        view.display(dataset, versionsPresenter);
    }

    public void doClose() {
        view.disposeView();
    }
    
    public String getDatasetNameString() throws EmfException {
        VersionsSet versionsSet = new VersionsSet(session.dataEditorService().getVersions(dataset.getId()));
        return versionsSet.versionString(dataset.getDefaultVersion());
    }
    
    public EmfSession getSession(){
        return session;
    }
}
