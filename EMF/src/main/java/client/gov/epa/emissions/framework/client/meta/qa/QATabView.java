package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;

public interface QATabView {
    void display(QAStep[] steps, QAStepResult[] results, EmfSession session);

    void observe(ViewQATabPresenter presenter);
}
