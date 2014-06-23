package gov.epa.emissions.framework.client.fast.run.create;

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
import java.util.Date;
import java.util.List;

public class FastRunCreatorPresenterImpl implements FastRunPresenter {

    private EmfSession session;

    private FastRunView view;

    private FastRun run;

    private List<FastRunTabPresenter> presenters;

    // private boolean inputsLoaded = false;

    private boolean hasResults = false;

    // private FastManagerPresenter fastManagerPresenter;

    public FastRunCreatorPresenterImpl(EmfSession session, FastRunView view,
            FastRunManagerPresenter fastManagerPresenter) {

        this.session = session;
        this.view = view;
        this.presenters = new ArrayList<FastRunTabPresenter>();
        // this.fastManagerPresenter = fastManagerPresenter;
    }

    public FastRunCreatorPresenterImpl(EmfSession session, FastRunView view) {

        this.session = session;
        this.view = view;
        this.presenters = new ArrayList<FastRunTabPresenter>();
    }

    public void addTab(FastRunTabView view) {

        FastRunTabPresenter tabPresenter = new FastRunTabPresenterImpl(view);
        tabPresenter.doDisplay();
        this.presenters.add(tabPresenter);
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

    public void doDisplay() {

        this.view.observe(this);

        this.run = new FastRun();
        this.run.setLastModifiedDate(new Date());
        this.run.setRunStatus("Not started");
        this.run.setCreator(this.session.user());
        this.view.display(this.run);
    }

    public void doClose() throws EmfException {

        /*
         * only release if its an existing program
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

        int id = this.run.getId();

        /*
         * update if it's an existing fast run
         */
        if (id != 0) {
            this.getService().updateFastRunWithLock(this.run);
        } else {

            /*
             * add if it's not an existing fast run
             */
            id = getService().addFastRun(this.run);

            /*
             * now lock this fast run, so it can be further edited...
             */
            this.run = getService().obtainLockedFastRun(session.user(), id);
        }
    }

    protected void saveTabs() throws EmfException {

        for (FastRunTabPresenter presenter : this.presenters) {
            presenter.doSave(this.run);
        }
    }

    private FastService getService() {
        return this.session.fastService();
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
