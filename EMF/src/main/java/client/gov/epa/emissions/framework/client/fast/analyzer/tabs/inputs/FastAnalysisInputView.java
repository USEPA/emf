package gov.epa.emissions.framework.client.fast.analyzer.tabs.inputs;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.fast.FastAnalysisInput;

public interface FastAnalysisInputView extends ManagedView {

    void observe(FastAnalysisInputPresenter presenter);

    void display(FastAnalysisInput input);

    void refresh(FastAnalysisInput input);

    void notifyLockFailure(FastAnalysisInput input);

    void signalChanges();

    void showError(String message);

    void showMessage(String message);

    void clearMessage();
}
