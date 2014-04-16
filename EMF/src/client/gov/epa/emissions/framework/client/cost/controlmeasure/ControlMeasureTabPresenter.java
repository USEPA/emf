package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;

public interface ControlMeasureTabPresenter {

    void doSave(ControlMeasure measure) throws EmfException;

    void doRefresh(ControlMeasure measure);
}
