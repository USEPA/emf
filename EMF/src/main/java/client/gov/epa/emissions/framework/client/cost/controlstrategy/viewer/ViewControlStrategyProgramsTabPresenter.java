package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.framework.client.cost.controlprogram.viewer.ControlProgramView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlProgram;

public interface ViewControlStrategyProgramsTabPresenter extends ViewControlStrategyTabPresenter {

    void doDisplay();

    void doView(ControlProgramView view, ControlProgram controlProgram) throws EmfException;
}