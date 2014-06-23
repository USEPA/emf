package gov.epa.emissions.framework.client.fast.datasets;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.fast.MPSDTManagerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.fast.FastDatasetWrapper;
import gov.epa.emissions.framework.services.fast.FastService;

public abstract class AbstractFastNonPointDatasetPresenterImpl implements FastDatasetPresenter {

    private EmfSession session;

    private FastDatasetView view;

    private FastDatasetWrapper wrapper;

    private boolean hasResults = false;

    // private FastManagerPresenter fastManagerPresenter;

    public AbstractFastNonPointDatasetPresenterImpl(EmfSession session, FastDatasetView view,
            MPSDTManagerPresenter fastManagerPresenter) {

        this.session = session;
        this.view = view;
        // this.fastManagerPresenter = fastManagerPresenter;
    }

    public AbstractFastNonPointDatasetPresenterImpl(EmfSession session, FastDatasetView view) {

        this.session = session;
        this.view = view;
    }

    public void doClose() {
        this.closeView();
    }

    protected void closeView() {
        this.view.disposeView();
    }

    protected FastService getService() {
        return this.session.fastService();
    }

    public void fireTracking() {
        view.signalChanges();
    }

    public boolean hasResults() {
        return this.hasResults;
    }

    public DatasetType getDatasetType(String name) throws EmfException {
        return session.getLightDatasetType(name);
    }

    public Version[] getVersions(EmfDataset dataset) throws EmfException {

        Version[] versions = new Version[0];

        if (dataset != null) {
            versions = this.session.dataEditorService().getVersions(dataset.getId());
        }

        return versions;
    }

    public FastDatasetWrapper getWrapper() {
        return wrapper;
    }

    public void setWrapper(FastDatasetWrapper wrapper) {
        this.wrapper = wrapper;
    }

    public FastDatasetView getView() {
        return view;
    }

    public EmfSession getSession() {
        return session;
    }
}
