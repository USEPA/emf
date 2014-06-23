package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStepResult;

public interface QAStepExportWizardView {
    void display(QAStepResult qaStepResult) throws EmfException;

    void observe(QAStepExportWizardPresenter presenter);

    void finish();

    boolean isCanceled();
}
