package gov.epa.emissions.framework.client.tempalloc.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocation;

public class TemporalAllocationTimePeriodTabPresenter implements TemporalAllocationTabPresenter {
    private TemporalAllocationTimePeriodTab view;
    
    public TemporalAllocationTimePeriodTabPresenter(TemporalAllocationTabView view) {
        this.view = (TemporalAllocationTimePeriodTab) view;
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
    
    public void doRefresh() {
        
    }
    
    public void doPrepareRun() throws EmfException {
        view.prepareRun();
    }
}
