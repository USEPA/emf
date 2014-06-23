package gov.epa.emissions.framework.client.cost.controlprogram.editor;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.cost.controlprogram.ControlProgramManagerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlProgramService;
import gov.epa.emissions.framework.services.cost.ControlProgramType;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ControlProgramPresenterImpl implements ControlProgramPresenter {

    private EmfSession session;

    private ControlProgramView view;

    private ControlProgram controlProgram;

    private List<ControlProgramTabPresenter> presenters;

    private ControlProgramMeasuresTabPresenter measuresTabPresenter;

    private ControlProgramTechnologiesTabPresenter technologiesTabPresenter;

    private ControlProgramSummaryTabPresenter summaryTabPresenter;
    
//    private EditControlProgramMeasuresTabPresenter measuresTabPresenter;
//
//    private boolean inputsLoaded = false;
    
    private boolean hasResults = false;
    
    private ControlProgramManagerPresenter controlProgramManagerPresenter;
    
    public ControlProgramPresenterImpl(ControlProgram controlProgram, EmfSession session, 
            ControlProgramView view, ControlProgramManagerPresenter controlProgramManagerPresenter) {
        this.controlProgram = controlProgram;
        this.session = session;
        this.view = view;
        this.presenters = new ArrayList<ControlProgramTabPresenter>();
        this.controlProgramManagerPresenter = controlProgramManagerPresenter;
    }

    public ControlProgramPresenterImpl(ControlProgram controlProgram, EmfSession session, 
            ControlProgramView view) {
        this.controlProgram = controlProgram;
        this.session = session;
        this.view = view;
        this.presenters = new ArrayList<ControlProgramTabPresenter>();
    }
    
    public void doDisplay() throws EmfException {
        view.observe(this);
        
        controlProgram = service().obtainLocked(session.user(), controlProgram.getId());
        
        if (!controlProgram.isLocked(session.user())) {// view mode, locked by another user
            view.notifyLockFailure(controlProgram);
            return;
        }
        view.display(controlProgram);
    }

    public void doDisplayNew()  {
        view.observe(this);
        
        view.display(controlProgram);
    }

    public ControlProgram getControlProgram(int id) throws EmfException {
        return service().getControlProgram(id);
    }

    public void doClose() throws EmfException {
        //only release if its an existing program
        if (controlProgram.getId() != 0)
            service().releaseLocked(session.user(), controlProgram.getId());
        closeView();
    }

    private void closeView() {
        view.disposeView();
    }

    public void doSave() throws EmfException {
        saveTabs();
        controlProgram.setLastModifiedDate(new Date());
        controlProgram = service().updateControlProgramWithLock(controlProgram);
//        managerPresenter.doRefresh();
    }

    public void doAdd() throws EmfException {
        saveTabs();
        controlProgram.setCreator(session.user());
        controlProgram.setLastModifiedDate(new Date());
        if (controlProgram.getId() == 0) {
            int id = service().addControlProgram(controlProgram);
            //now lock this control program, so it can be further edited...
            controlProgram = service().obtainLocked(session.user(), id);
        } else {
            service().updateControlProgramWithLock(controlProgram);
        }
        controlProgramManagerPresenter.doRefresh();
    }

    protected void saveTabs() throws EmfException {
        for (Iterator<ControlProgramTabPresenter> iter = presenters.iterator(); iter.hasNext();) {
            ControlProgramTabPresenter element = iter.next();
            element.doSave(controlProgram);
        }
    }

    private ControlProgramService service() {
        return session.controlProgramService();
    }

    public void set(ControlProgramSummaryTab view) throws EmfException {
        this.summaryTabPresenter = new ControlProgramSummaryTabPresenter(view, controlProgram, 
                session);
        summaryTabPresenter.doDisplay();
        presenters.add(summaryTabPresenter);
    }

    public void set(ControlProgramMeasuresTab view) {
        measuresTabPresenter = new ControlProgramMeasuresTabPresenter(view,
                controlProgram, session);
        measuresTabPresenter.doDisplay();
        presenters.add(measuresTabPresenter);
    }

    public void set(ControlProgramTechnologiesTab view) throws EmfException {
        technologiesTabPresenter = new ControlProgramTechnologiesTabPresenter(view,
                controlProgram, session);
        technologiesTabPresenter.doDisplay();
        presenters.add(technologiesTabPresenter);
    }

//    public void doLoad(String tabTitle) throws EmfException {
//        if (!inputsLoaded ) {
//            measuresTabPresenter.doDisplay();
//            summaryTabPresenter.doDisplay();
//            inputsLoaded = true;
//        }
//    }

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

    public boolean hasResults() {
        return this.hasResults;
    }
    
    public EmfDataset getDataset(int id) throws EmfException {
        return session.dataService().getDataset(id);
    }

    public void doChangeControlProgramType(ControlProgramType controlProgramType) {
//        if (constraintsTabPresenter != null)
//            constraintsTabPresenter.doChangeStrategyType(strategyType);
//        if (inventoryTabPresenter != null)
//            inventoryTabPresenter.doChangeStrategyType(strategyType);
    }
}
