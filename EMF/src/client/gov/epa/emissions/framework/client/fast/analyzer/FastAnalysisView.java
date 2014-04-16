package gov.epa.emissions.framework.client.fast.analyzer;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.fast.FastAnalysis;

public interface FastAnalysisView extends ManagedView {

    void observe(FastAnalysisPresenter presenter);

    void display(FastAnalysis analysis);

    void refresh(FastAnalysis analysis);

    void notifyLockFailure(FastAnalysis analysis);

    void signalChanges();

    void showError(String message);

    void showMessage(String message);

    void clearMessage();
}
