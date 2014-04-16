package gov.epa.emissions.framework.client.cost.controlprogram.viewer;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.cost.controlprogram.ControlProgramManagerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlProgramService;
import gov.epa.emissions.framework.services.cost.ControlProgramType;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class ControlProgramPresenterImpl implements ControlProgramPresenter {

    private EmfSession session;

    private ControlProgramView view;

    private ControlProgram controlProgram;

    private ControlProgramMeasuresTabPresenter measuresTabPresenter;

    private ControlProgramTechnologiesTabPresenter technologiesTabPresenter;

    private ControlProgramSummaryTabPresenter summaryTabPresenter;

    private boolean hasResults = false;

    public ControlProgramPresenterImpl(ControlProgram controlProgram, EmfSession session, ControlProgramView view,
            ControlProgramManagerPresenter controlProgramManagerPresenter) {

        this.controlProgram = controlProgram;
        this.session = session;
        this.view = view;
    }

    public ControlProgramPresenterImpl(ControlProgram controlProgram, EmfSession session, ControlProgramView view) {

        this.controlProgram = controlProgram;
        this.session = session;
        this.view = view;
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

    public void doDisplayNew() {

        view.observe(this);
        view.display(controlProgram);
    }

    public ControlProgram getControlProgram(int id) throws EmfException {
        return service().getControlProgram(id);
    }

    public void doClose() throws EmfException {

        // only release if its an existing program
        if (controlProgram.getId() != 0) {
            service().releaseLocked(session.user(), controlProgram.getId());
        }

        closeView();
    }

    private void closeView() {
        view.disposeView();
    }

    private ControlProgramService service() {
        return session.controlProgramService();
    }

    public void setSummaryTab(ControlProgramSummaryTab view) throws EmfException {

        this.summaryTabPresenter = new ControlProgramSummaryTabPresenter(view, session);
        summaryTabPresenter.doDisplay();
    }

    public void setMeasuresTab(ControlProgramMeasuresTab view) {

        measuresTabPresenter = new ControlProgramMeasuresTabPresenter(view, controlProgram, session);
        measuresTabPresenter.doDisplay();
    }

    public void setTechnologiesTab(ControlProgramTechnologiesTab view) {

        technologiesTabPresenter = new ControlProgramTechnologiesTabPresenter(view, controlProgram, session);
        technologiesTabPresenter.doDisplay();
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

    public boolean hasResults() {
        return this.hasResults;
    }

    public EmfDataset getDataset(int id) throws EmfException {
        return session.dataService().getDataset(id);
    }

    public void doChangeControlProgramType(ControlProgramType controlProgramType) {
        // if (constraintsTabPresenter != null)
        // constraintsTabPresenter.doChangeStrategyType(strategyType);
        // if (inventoryTabPresenter != null)
        // inventoryTabPresenter.doChangeStrategyType(strategyType);
    }
}
