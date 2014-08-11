package gov.epa.emissions.framework.client.tempalloc.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocation;

public class TemporalAllocationOutputTabPresenter implements TemporalAllocationTabPresenter {
    private TemporalAllocationOutputTab view;
    
    public TemporalAllocationOutputTabPresenter(TemporalAllocationTabView view) {
        this.view = (TemporalAllocationOutputTab) view;
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
        view.refresh();
    }
}
