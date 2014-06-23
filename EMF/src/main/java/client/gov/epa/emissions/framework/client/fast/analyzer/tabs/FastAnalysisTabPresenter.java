package gov.epa.emissions.framework.client.fast.analyzer.tabs;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastAnalysis;

public interface FastAnalysisTabPresenter {

    void doSave(FastAnalysis analysis) throws EmfException;

    void doRefresh(FastAnalysis analysis);

    void doDisplay();
}
