package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.ControlStrategyMeasure;
import gov.epa.emissions.framework.services.cost.LightControlMeasure;

public interface ViewControlStrategyMeasuresTabPresenter extends ViewControlStrategyTabPresenter {

    void doDisplay() throws EmfException;

    ControlMeasureClass[] getAllClasses() throws EmfException;

    ControlMeasureClass[] getControlMeasureClasses();

    ControlStrategyMeasure[] getControlMeasures();

    LightControlMeasure[] getAllControlMeasures();
}