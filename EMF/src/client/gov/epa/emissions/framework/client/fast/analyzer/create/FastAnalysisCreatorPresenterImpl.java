package gov.epa.emissions.framework.client.fast.analyzer.create;

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
import java.util.Date;
import java.util.List;

public class FastAnalysisCreatorPresenterImpl implements FastAnalysisPresenter {

    private EmfSession session;

    private FastAnalysisView view;

    private FastAnalysis analysis;

    private List<FastAnalysisTabPresenter> presenters;

    // private boolean inputsLoaded = false;

    private boolean hasResults = false;

    // private FastManagerPresenter fastManagerPresenter;

    public FastAnalysisCreatorPresenterImpl(EmfSession session, FastAnalysisView view,
            FastAnalysisManagerPresenter fastManagerPresenter) {

        this.session = session;
        this.view = view;
        this.presenters = new ArrayList<FastAnalysisTabPresenter>();
        // this.fastManagerPresenter = fastManagerPresenter;
    }

    public FastAnalysisCreatorPresenterImpl(EmfSession session, FastAnalysisView view) {

        this.session = session;
        this.view = view;
        this.presenters = new ArrayList<FastAnalysisTabPresenter>();
    }

    public void addTab(FastAnalysisTabView view) {

        FastAnalysisTabPresenter tabPresenter = new FastAnalysisTabPresenterImpl(view);
        tabPresenter.doDisplay();
        this.presenters.add(tabPresenter);
    }

    public void doRun() throws EmfException {

        this.doSave();
        this.getService().runFastAnalysis(this.session.user(), this.analysis.getId());
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

    public void doRefresh() {
        this.refreshTabs();
    }

    protected void refreshTabs() {

        for (FastAnalysisTabPresenter presenter : this.presenters) {
            presenter.doRefresh(this.analysis);
        }
    }

    public void doDisplay() {

        this.view.observe(this);

        this.analysis = new FastAnalysis();
        this.analysis.setCreator(this.session.user());
        this.analysis.setLastModifiedDate(new Date());
        this.analysis.setRunStatus("Not started");

        this.view.display(this.analysis);
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
        int id = getService().addFastAnalysis(this.analysis);

        /*
         * now lock this control program, so it can be further edited...
         */
        this.analysis = getService().obtainLockedFastAnalysis(session.user(), id);
    }

    protected void saveTabs() throws EmfException {

        for (FastAnalysisTabPresenter presenter : this.presenters) {
            presenter.doSave(this.analysis);
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
