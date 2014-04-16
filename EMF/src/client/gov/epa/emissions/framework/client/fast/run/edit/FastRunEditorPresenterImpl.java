package gov.epa.emissions.framework.client.fast.run.edit;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.fast.ExportPresenter;
import gov.epa.emissions.framework.client.fast.ExportView;
import gov.epa.emissions.framework.client.fast.MPSDTUtils;
import gov.epa.emissions.framework.client.fast.run.FastRunManagerPresenter;
import gov.epa.emissions.framework.client.fast.run.FastRunPresenter;
import gov.epa.emissions.framework.client.fast.run.FastRunView;
import gov.epa.emissions.framework.client.fast.run.tabs.FastRunTabPresenter;
import gov.epa.emissions.framework.client.fast.run.tabs.FastRunTabPresenterImpl;
import gov.epa.emissions.framework.client.fast.run.tabs.FastRunTabView;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.fast.FastOutputExportWrapper;
import gov.epa.emissions.framework.services.fast.FastRun;
import gov.epa.emissions.framework.services.fast.FastService;

import java.util.ArrayList;
import java.util.List;

public class FastRunEditorPresenterImpl implements FastRunPresenter {

    private EmfSession session;

    private FastRunView view;

    private FastRun run;

    private int id;

    private List<FastRunTabPresenter> presenters;

    // private boolean inputsLoaded = false;

    private boolean hasResults = false;

    // private FastManagerPresenter fastManagerPresenter;

    public FastRunEditorPresenterImpl(int id, EmfSession session, FastRunView view,
            FastRunManagerPresenter fastManagerPresenter) {

        this(id, session, view);
        // this.fastManagerPresenter = fastManagerPresenter;
    }

    public FastRunEditorPresenterImpl(int id, EmfSession session, FastRunView view) {

        this.id = id;
        this.session = session;
        this.view = view;
        this.presenters = new ArrayList<FastRunTabPresenter>();
    }

    public void doViewData(int id) throws EmfException {
        throw new EmfException("View data not implemented.");
    }

    public void doExport(ExportView exportView, ExportPresenter presenter,
            List<FastOutputExportWrapper> outputExportWrappers) {

        if (outputExportWrappers.size() == 0) {
            view.showMessage("To Export outputs, you will need to select at least one output");
        } else {

            view.clearMessage();
            presenter.display(exportView);
        }
    }

    public void doDisplay() throws EmfException {

        this.view.observe(this);

        this.run = this.getService().obtainLockedFastRun(this.session.user(), this.id);

        if (!this.run.isLocked(this.session.user())) {
            this.view.notifyLockFailure(this.run);
        } else {
            this.view.display(this.run);
        }
    }

    public void doCreate() {

        view.observe(this);
        view.display(this.run);
    }

    public void doClose() throws EmfException {

        /*
         * only release if its an existing fast run
         */
        if (this.run.getId() != 0) {
            this.getService().releaseLockedFastRun(this.session.user(), this.run.getId());
        }

        this.closeView();
    }

    public void doRun() throws EmfException {

        this.doSave();
        this.getService().runFastRun(this.session.user(), this.run.getId());
    }

    public void doRefresh() {
        this.refreshTabs();
    }

    protected void refreshTabs() {

        for (FastRunTabPresenter presenter : this.presenters) {
            presenter.doRefresh(this.run);
        }
    }

    private void closeView() {
        this.view.disposeView();
    }

    public void doSave() throws EmfException {

        this.saveTabs();
        this.run = getService().updateFastRunWithLock(this.run);
    }

    protected void saveTabs() throws EmfException {

        for (FastRunTabPresenter presenter : this.presenters) {
            presenter.doSave(this.run);
        }
    }

    private FastService getService() {
        return this.session.fastService();
    }

    public void addTab(FastRunTabView view) {

        FastRunTabPresenter tabPresenter = new FastRunTabPresenterImpl(view);
        tabPresenter.doDisplay();
        this.presenters.add(tabPresenter);
    }

    public void fireTracking() {
        view.signalChanges();
    }

    public boolean hasResults() {
        return this.hasResults;
    }

    public DatasetType getDatasetType(String name) throws EmfException {
        return MPSDTUtils.getDatasetType(this.session, name);
    }

    public Version[] getVersions(EmfDataset dataset) throws EmfException {
        return MPSDTUtils.getVersions(this.session, dataset);
    }

    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException {

        view.clearMessage();
        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }

}
