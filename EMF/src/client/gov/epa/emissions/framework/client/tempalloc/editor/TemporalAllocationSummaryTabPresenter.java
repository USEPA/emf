package gov.epa.emissions.framework.client.tempalloc.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocation;

public class TemporalAllocationSummaryTabPresenter implements TemporalAllocationTabPresenter {
    private TemporalAllocationSummaryTab view;
    
    public TemporalAllocationSummaryTabPresenter(TemporalAllocationTabView view) {
        this.view = (TemporalAllocationSummaryTab) view;
    }
    
    public void doDisplay() throws EmfException {
        view.display();
    }

    public void doSave() throws EmfException {
        view.save();
    }
    
    public void updateView(TemporalAllocation temporalAllocation) {
        view.setTemporalAllocation(temporalAllocation);
    }
}
