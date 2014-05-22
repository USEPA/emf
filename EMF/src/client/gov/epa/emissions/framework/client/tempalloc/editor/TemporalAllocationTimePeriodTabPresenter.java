package gov.epa.emissions.framework.client.tempalloc.editor;

import gov.epa.emissions.framework.services.EmfException;

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
}
