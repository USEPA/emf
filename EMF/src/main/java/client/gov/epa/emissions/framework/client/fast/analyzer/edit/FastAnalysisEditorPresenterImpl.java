package gov.epa.emissions.framework.client.fast.analyzer.edit;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.fast.ExportPresenter;
import gov.epa.emissions.framework.client.fast.ExportView;
import gov.epa.emissions.framework.client.fast.MPSDTUtils;
import gov.epa.emissions.framework.client.fast.analyzer.FastAnalysisManagerPresenter;
import gov.epa.emissions.framework.client.fast.analyzer.FastAnalysisPresenter;
import gov.epa.emissions.framework.client.fast.analyzer.FastAnalysisTabView;
import gov.epa.emissions.framework.client.fast.analyzer.FastAnalysisView;
import gov.epa.emissions.framework.client.fast.analyzer.tabs.FastAnalysisTabPresenter;
import gov.epa.emissions.framework.client.fast.analyzer.tabs.FastAnalysisTabPresenterImpl;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.fast.FastAnalysis;
import gov.epa.emissions.framework.services.fast.FastOutputExportWrapper;
import gov.epa.emissions.framework.services.fast.FastService;

import java.util.ArrayList;
import java.util.List;

public class FastAnalysisEditorPresenterImpl implements FastAnalysisPresenter {

    private EmfSession session;

    private FastAnalysisView view;

    private FastAnalysis analysis;

    private int id;

    private List<FastAnalysisTabPresenter> presenters;

    private boolean hasResults = false;

    public FastAnalysisEditorPresenterImpl(int id, EmfSession session, FastAnalysisView view,
            FastAnalysisManagerPresenter fastManagerPresenter) {

        this.id = id;
        this.session = session;
        this.view = view;
        this.presenters = new ArrayList<FastAnalysisTabPresenter>();
    }

    public void doRun() throws EmfException {

        this.doSave();
        this.getService().runFastAnalysis(this.session.user(), this.analysis.getId());
    }

    public void doRefresh() {
        this.refreshTabs();
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

    protected void refreshTabs() {

        for (FastAnalysisTabPresenter presenter : this.presenters) {
            presenter.doRefresh(this.analysis);
        }
    }

    public void doDisplay() throws EmfException {

        this.view.observe(this);

        this.analysis = this.getService().obtainLockedFastAnalysis(this.session.user(), this.id);

        if (!this.analysis.isLocked(this.session.user())) {
            this.view.notifyLockFailure(this.analysis);
        } else {
            this.view.display(this.analysis);
        }
    }

    public void doCreate() {

        view.observe(this);
        view.display(this.analysis);
    }

    public void doClose() throws EmfException {

        /*
         * only release if its an existing program
         */
        if (this.analysis.getId() != 0) {
            this.getService().releaseLockedFastAnalysis(this.session.user(), this.analysis.getId());
        }

        this.closeView();
    }

    private void closeView() {
        this.view.disposeView();
    }

    public void doSave() throws EmfException {

        this.saveTabs();
        this.analysis = getService().updateFastAnalysisWithLock(this.analysis);
    }

    protected void saveTabs() throws EmfException {

        for (FastAnalysisTabPresenter presenter : this.presenters) {
            presenter.doSave(this.analysis);
        }
    }

    private FastService getService() {
        return this.session.fastService();
    }

    public void addTab(FastAnalysisTabView tabView) {

        FastAnalysisTabPresenter tabPresenter = new FastAnalysisTabPresenterImpl(tabView);
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
