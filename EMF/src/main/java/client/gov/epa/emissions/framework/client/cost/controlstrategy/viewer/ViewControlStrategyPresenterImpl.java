package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.cost.controlstrategy.ControlStrategiesManagerPresenter;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyService;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.util.ArrayList;
import java.util.List;

public class ViewControlStrategyPresenterImpl implements ViewControlStrategyPresenter {

    private EmfSession session;

    private ViewControlStrategyView view;

    private ControlStrategiesManagerPresenter managerPresenter;

    private ControlStrategy controlStrategy;

    private List<ViewControlStrategyTabPresenter> presenters;

    private ViewControlStrategySummaryTabView summaryTabView;

    private ViewControlStrategyTabPresenter inventoryTabPresenter;

    private ViewControlStrategySummaryTabPresenter summaryTabPresenter;

    private ViewControlStrategyMeasuresTabPresenter measuresTabPresenter;

    private ViewControlStrategyProgramsTabPresenter programsTabPresenter;

    private ViewControlStrategyConstraintsTabPresenter constraintsTabPresenter;

    public ViewControlStrategyPresenterImpl(ControlStrategy controlStrategy, EmfSession session,
            ViewControlStrategyView view, ControlStrategiesManagerPresenter controlStrategiesManagerPresenter) {

        this.controlStrategy = controlStrategy;
        this.session = session;
        this.view = view;
        this.managerPresenter = controlStrategiesManagerPresenter;
        this.presenters = new ArrayList<ViewControlStrategyTabPresenter>();
    }

    public void doDisplay() throws EmfException {

        view.observe(this);

        controlStrategy = service().getById(controlStrategy.getId());

        view.display(controlStrategy, getResult());
    }

    private ControlStrategyResult[] getResult() throws EmfException {
        return service().getControlStrategyResults(controlStrategy.getId());
    }

    public ControlStrategy getControlStrategy(int id) throws EmfException {
        return service().getById(id);
    }

    public void doClose() {
       closeView();
    }

    private void closeView() {
        view.disposeView();
    }

    public void doRun(ControlStrategy controlStrategy) throws EmfException {
        runTabs(controlStrategy);
    }

    private void runTabs(ControlStrategy controlStrategy) throws EmfException {

        for (ViewControlStrategyTabPresenter presenter : this.presenters) {
            presenter.doRun(controlStrategy);
        }
    }

    private ControlStrategyService service() {
        return session.controlStrategyService();
    }

    public void setSummaryTab(ViewControlStrategySummaryTabView view) {

        this.summaryTabView = view;
        this.summaryTabPresenter = new ViewControlStrategySummaryTabPresenterImpl(this, controlStrategy, view);
        presenters.add(summaryTabPresenter);
    }

    public void setOutputTab(ViewControlStrategyOutputTabView view) throws EmfException {

        ViewControlStrategyOutputTabPresenter presenter = new ViewControlStrategyOutputTabPresenterImpl(session, view);
        presenter.doDisplay(controlStrategy, getResult());
        presenters.add(presenter);
    }

    public void setInventoryFilterTab(ViewControlStrategyTabView view) {

        this.inventoryTabPresenter = new ViewControlStrategyTabPresenterImpl(controlStrategy, view);
        presenters.add(this.inventoryTabPresenter);
    }

    public void setResults(ControlStrategy controlStrategy) {
        summaryTabView.setRunMessage(controlStrategy);
    }

    public void runStrategy() throws EmfException {

        service().runStrategy(session.user(), controlStrategy.getId());
    }

    public void doRefresh() throws EmfException {

        ControlStrategyResult[] controlStrategyResults = getResult();
        ControlStrategy strategy = getControlStrategy(controlStrategy.getId());
        for (ViewControlStrategyTabPresenter presenter : this.presenters) {
            presenter.doRefresh(strategy, controlStrategyResults);
        }
    }

    public void setMeasuresTab(ViewControlStrategyMeasuresTabView view) throws EmfException {

        measuresTabPresenter = new ViewControlStrategyMeasuresTabPresenterImpl(view, controlStrategy, session,
                managerPresenter);
        measuresTabPresenter.doDisplay();
        presenters.add(measuresTabPresenter);
    }

    public void setProgramsTab(ViewControlStrategyProgramsTab view) {

        programsTabPresenter = new ViewControlStrategyProgramsTabPresenterImpl(view, controlStrategy, session);
        programsTabPresenter.doDisplay();
        presenters.add(programsTabPresenter);
    }

    public void setConstraintsTab(ViewControlStrategyConstraintsTabView view) {

        constraintsTabPresenter = new ViewControlStrategyConstraintsTabPresenterImpl(view, controlStrategy, session);
        constraintsTabPresenter.doDisplay();
        presenters.add(constraintsTabPresenter);
    }

    public CostYearTable getCostYearTable() throws EmfException {
        return session.controlMeasureService().getCostYearTable(CostYearTable.REFERENCE_COST_YEAR);
    }

    public void fireTracking() {
        view.signalChanges();
    }

    public DatasetType getDatasetType(String name) throws EmfException {
        return session.getLightDatasetType(name);
    }

    public Version[] getVersions(EmfDataset dataset) throws EmfException {

        if (dataset == null) {
            return new Version[0];
        }

        return session.dataEditorService().getVersions(dataset.getId());
    }

    public EmfDataset[] getDatasets(DatasetType type) throws EmfException {
        if (type == null)
            return new EmfDataset[0];

        return session.dataService().getDatasets(type);
    }

    public boolean hasResults() throws EmfException {

        boolean hasStratResults = false;
        ControlStrategyResult[] controlStrategyResults = getResult();
        if (controlStrategyResults != null && controlStrategyResults.length > 0) {
            hasStratResults = true;
        }

        return hasStratResults;
    }

    public EmfDataset getDataset(int id) throws EmfException {
        return session.dataService().getDataset(id);
    }

    public Version getVersion(int datasetId, int version) throws EmfException {

        Version[] versions = session.dataEditorService().getVersions(datasetId);
        for (Version v : versions) {
            if (v.getVersion() == version) {
                return v;
            }
        }

        return null;
    }

    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException {
        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }

    public void doChangeStrategyType(StrategyType strategyType) {
        // no-op
    }

    public void stopRun() throws EmfException {
        throw new EmfException();
    }
}
