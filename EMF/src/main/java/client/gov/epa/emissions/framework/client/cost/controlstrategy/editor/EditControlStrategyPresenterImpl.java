package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class EditControlStrategyPresenterImpl implements EditControlStrategyPresenter {

    private EmfSession session;

    private EditControlStrategyView view;

    private ControlStrategiesManagerPresenter managerPresenter;

    private ControlStrategy controlStrategy;

    private List presenters;

    private EditControlStrategySummaryTabView summaryTabView;
    
    private EditControlStrategyTabPresenter inventoryTabPresenter;
    
    private EditControlStrategySummaryTabPresenter summaryTabPresenter;

    private EditControlStrategyMeasuresTabPresenter measuresTabPresenter;

    private ControlStrategyProgramsTabPresenter programsTabPresenter;

    private EditControlStrategyPollutantsTabPresenter pollutantsTabPresenter;

    private EditControlStrategyConstraintsTabPresenter constraintsTabPresenter;
    
    public EditControlStrategyPresenterImpl(ControlStrategy controlStrategy, EmfSession session, 
            EditControlStrategyView view, ControlStrategiesManagerPresenter controlStrategiesManagerPresenter) {
        this.controlStrategy = controlStrategy;
        this.session = session;
        this.view = view;
        this.managerPresenter = controlStrategiesManagerPresenter;
        this.presenters = new ArrayList();
    }

    public void doDisplay() throws EmfException {
        view.observe(this);
        
        //make sure the editor is EITHER the admin or creator of the strategy...
        if (!controlStrategy.getCreator().equals(session.user()) && !session.user().isAdmin()) {
            view.notifyEditFailure(controlStrategy);
            return;
        }
        
        controlStrategy = service().obtainLocked(session.user(), controlStrategy.getId());
        
        if (!controlStrategy.isLocked(session.user())) {// view mode, locked by another user
            view.notifyLockFailure(controlStrategy);
            return;
        }
        if (controlStrategy.getIsFinal() != null && controlStrategy.getIsFinal()) {// view mode only, has been finalized...
            view.notifyFinalFailure(controlStrategy);
            return;
        }
        ControlStrategyResult[] controlStrategyResults = getResult();
//        hasResults = false;
//        if (controlStrategyResults!= null && controlStrategyResults.length > 0) hasResults = true;
        view.display(controlStrategy, controlStrategyResults);
    }

    private ControlStrategyResult[] getResult() throws EmfException {
        return service().getControlStrategyResults(controlStrategy.getId());
    }

    public ControlStrategy getControlStrategy(int id) throws EmfException {
        return service().getById(id);
    }

    public void doClose() throws EmfException {
        service().releaseLocked(session.user(), controlStrategy.getId());
        closeView();
    }

    private void closeView() {
        view.disposeView();
    }

    public void doSave(ControlStrategy controlStrategy) throws EmfException {
        //make sure we don't overwrite the runstatus
        String currentRunStatus = service().getStrategyRunStatus(controlStrategy.getId());
        if (currentRunStatus == null) currentRunStatus = "Not Started";
        controlStrategy.setRunStatus(currentRunStatus);
        
        ControlStrategy loadedStrategy = service().getById(controlStrategy.getId());

        saveTabs(controlStrategy);
        validateName(controlStrategy);
        controlStrategy.setCreator(session.user());
        controlStrategy.setLastModifiedDate(new Date());
        controlStrategy.setTotalCost(loadedStrategy.getTotalCost());
        controlStrategy.setTotalReduction(loadedStrategy.getTotalReduction());
        controlStrategy = service().updateControlStrategyWithLock(controlStrategy);
//        managerPresenter.doRefresh();
    }

    public void doRun(ControlStrategy controlStrategy) throws EmfException {
        saveTabs(controlStrategy);
        runTabs(controlStrategy);
    }

    private void saveTabs(ControlStrategy controlStrategy) throws EmfException {
        for (Iterator iter = presenters.iterator(); iter.hasNext();) {
            EditControlStrategyTabPresenter element = (EditControlStrategyTabPresenter) iter.next();
            element.doSave(controlStrategy);
        }
    }

    private void runTabs(ControlStrategy controlStrategy) throws EmfException {
        for (Iterator iter = presenters.iterator(); iter.hasNext();) {
            EditControlStrategyTabPresenter element = (EditControlStrategyTabPresenter) iter.next();
            element.doRun(controlStrategy);
        }
    }

    private void validateName(ControlStrategy controlStrategy) throws EmfException {
        // emptyName
        String name = controlStrategy.getName();
        if (name.trim().equals(""))
            throw new EmfException("Empty string is not allowed for the name.");

        if (isDuplicate(controlStrategy))
            throw new EmfException("A Control Strategy named '" + name + "' already exists.");
    }

    private boolean isDuplicate(ControlStrategy controlStrategy) throws EmfException {
        int id = service().isDuplicateName(controlStrategy.getName());
        return (id != 0 && controlStrategy.getId() != id);
//        String name = controlStrategy.getName();
//        ControlStrategy[] controlStrategies = service().getControlStrategies();
//
//        for (int i = 0; i < controlStrategies.length; i++) {
//
//            if (controlStrategies[i].getName().equals(name) && controlStrategies[i].getId() != controlStrategy.getId())
//                return true;
//        }
//        return false;
    }

    private ControlStrategyService service() {
        return session.controlStrategyService();
    }

    public void set(EditControlStrategySummaryTabView view) {
        this.summaryTabView = view;
        this.summaryTabPresenter = new EditControlStrategySummaryTabPresenterImpl(this, controlStrategy, view);
        presenters.add(summaryTabPresenter);
    }
    
    public EditControlStrategySummaryTabPresenter getSummaryPresenter() {
        return this.summaryTabPresenter;
    }

    public void set(EditControlStrategyOutputTabView view) throws EmfException {
        EditControlStrategyOutputTabPresenter presenter = new EditControlStrategyOutputTabPresenter(session, view);
        presenter.doDisplay(controlStrategy, getResult());
        presenters.add(presenter);
    }

    public void set(EditControlStrategyTabView view) {
        this.inventoryTabPresenter = new EditControlStrategyTabPresenterImpl(controlStrategy, view);
        presenters.add(this.inventoryTabPresenter);
    }

    public void setResults(ControlStrategy controlStrategy) {
        summaryTabView.setRunMessage(controlStrategy);
    }

    public void stopRun() throws EmfException {
        service().stopRunStrategy(controlStrategy.getId());
        view.stopRun();
    }

    public void runStrategy() throws EmfException {
        
        service().runStrategy(session.user(), controlStrategy.getId());
    }

    public void doRefresh() throws EmfException {
        //ControlStrategyResult result = session.controlStrategyService().controlStrategyResults(controlStrategy);
        ControlStrategyResult[] controlStrategyResults = getResult();
        ControlStrategy strategy = getControlStrategy(controlStrategy.getId());
//        hasResults = false;
//        if (controlStrategyResults!= null && controlStrategyResults.length > 0) hasResults = true;
//        String runStatus = service().controlStrategyRunStatus(controlStrategy.getId());
//        if (runStatus == null || !runStatus.equalsIgnoreCase("Running")) {
            for (Iterator iter = presenters.iterator(); iter.hasNext();) {
                EditControlStrategyTabPresenter element = (EditControlStrategyTabPresenter) iter.next();
                element.doRefresh(strategy, controlStrategyResults);
            }
//        }
    }

    public void set(ControlStrategyMeasuresTabView view) throws EmfException {
        measuresTabPresenter = new EditControlStrategyMeasuresTabPresenter(view,
                controlStrategy, session, 
                managerPresenter);
        measuresTabPresenter.doDisplay();
        presenters.add(measuresTabPresenter);
    }

    public void set(ControlStrategyProgramsTab view) {
        programsTabPresenter = new ControlStrategyProgramsTabPresenter(view,
                controlStrategy, session);
        programsTabPresenter.doDisplay();
        presenters.add(programsTabPresenter);
    }

    public void set(ControlStrategyPollutantsTabView view) {
        pollutantsTabPresenter = new EditControlStrategyPollutantsTabPresenter(view,
                controlStrategy, session);
        pollutantsTabPresenter.doDisplay();
        presenters.add(pollutantsTabPresenter);
    }

    public void set(ControlStrategyConstraintsTabView view) {
        constraintsTabPresenter = new EditControlStrategyConstraintsTabPresenter(view,
                controlStrategy, session);
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
    
    public Version[] getVersions(EmfDataset dataset) throws EmfException 
    {
        if (dataset == null) {
            return new Version[0];
        }
        return session.dataEditorService().getVersions(dataset.getId());
    }

    public EmfDataset[] getDatasets(DatasetType type) throws EmfException
    {
            if (type == null)
                return new EmfDataset[0];

            return session.dataService().getDatasets(type);
    }

    public boolean hasResults() throws EmfException {
        boolean hasStratResults = false;
        ControlStrategyResult[] controlStrategyResults = getResult();
        if (controlStrategyResults != null && controlStrategyResults.length > 0) hasStratResults = true;
        return hasStratResults;
    }
    
    public EmfDataset getDataset(int id) throws EmfException {
        return session.dataService().getDataset(id);
    }

    public void doChangeStrategyType(StrategyType strategyType) {
        if (constraintsTabPresenter != null)
            constraintsTabPresenter.doChangeStrategyType(strategyType);
        if (inventoryTabPresenter != null)
            inventoryTabPresenter.doChangeStrategyType(strategyType);
        view.notifyStrategyTypeChange(strategyType);
    }

    public void resetButtons(boolean enable) {
        view.enableButtons(enable);
        
    }

    public Version getVersion(int datasetId, int version) throws EmfException {
        try {
            Version[] versions = session.dataEditorService().getVersions(datasetId);
            for (Version v : versions) {
                if (v.getVersion() == version)
                    return v;
            }
            return null;
        } finally {
            //
        }
    }

    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException {
        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }
}
