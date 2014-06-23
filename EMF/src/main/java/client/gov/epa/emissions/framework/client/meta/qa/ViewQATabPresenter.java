package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;

public interface ViewQATabPresenter {

    void display() throws EmfException;

    void doView(QAStep step, QAStepView view) throws EmfException;

}