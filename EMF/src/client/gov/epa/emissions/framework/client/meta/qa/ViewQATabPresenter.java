package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.client.swingworker.LightSwingWorkerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;

public interface ViewQATabPresenter extends LightSwingWorkerPresenter{

    void display() throws EmfException;

    void doView(QAStep step, QAStepView view) throws EmfException;

}