package gov.epa.emissions.framework.client.fast.analyzer.tabs;

import gov.epa.emissions.framework.client.fast.analyzer.FastAnalysisTabView;
import gov.epa.emissions.framework.services.fast.FastAnalysis;

public class FastAnalysisTabPresenterImpl implements FastAnalysisTabPresenter {

    private FastAnalysisTabView view;

    public FastAnalysisTabPresenterImpl(FastAnalysisTabView view) {
        this.view = view;
    }

    public void doSave(FastAnalysis analysis) {
        view.save(analysis);
    }

    public void doRefresh(FastAnalysis analysis) {
        view.refresh(analysis);
    }

    
    public void doDisplay() {
        this.view.display();
    }
}
