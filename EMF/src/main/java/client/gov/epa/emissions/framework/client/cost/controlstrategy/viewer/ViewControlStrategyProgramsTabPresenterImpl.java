package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.cost.controlprogram.viewer.ControlProgramPresenter;
import gov.epa.emissions.framework.client.cost.controlprogram.viewer.ControlProgramPresenterImpl;
import gov.epa.emissions.framework.client.cost.controlprogram.viewer.ControlProgramView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

public class ViewControlStrategyProgramsTabPresenterImpl implements ViewControlStrategyProgramsTabPresenter {

    ViewControlStrategyProgramsTab view;

    private EmfSession session;

    ControlStrategy controlStrategy;

    public ViewControlStrategyProgramsTabPresenterImpl(ViewControlStrategyProgramsTab view,
            ControlStrategy controlStrategy, EmfSession session) {
        this.controlStrategy = controlStrategy;
        this.session = session;
        this.view = view;
    }

    public void doDisplay() {
        view.observe(this);
        view.display(this.controlStrategy);
    }

    public ControlProgram[] getAllControlPrograms() throws EmfException {
        return session.controlProgramService().getControlPrograms();
    }

    public void doChangeStrategyType(StrategyType strategyType) {
        // NOTE Auto-generated method stub

    }

    public void doRefresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        this.controlStrategy = controlStrategy;
        view.refresh(controlStrategy);
    }

    public void doRun(ControlStrategy controlStrategy) throws EmfException {
        this.controlStrategy = controlStrategy;
        view.run(controlStrategy);
    }

    public void doView(ControlProgramView view, ControlProgram controlProgram) throws EmfException {

        ControlProgramPresenter presenter = new ControlProgramPresenterImpl(controlProgram, session, view);
        presenter.doDisplay();
    }

}
