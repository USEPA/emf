package gov.epa.emissions.framework.client.tempalloc.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocation;

public class TemporalAllocationProfilesTabPresenter implements TemporalAllocationTabPresenter {
    private TemporalAllocationProfilesTab view;
    
    public TemporalAllocationProfilesTabPresenter(TemporalAllocationTabView view) {
        this.view = (TemporalAllocationProfilesTab) view;
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
}
