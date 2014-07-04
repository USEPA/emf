package gov.epa.emissions.framework.client.tempalloc.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocation;

public interface TemporalAllocationTabPresenter {

    public void doDisplay() throws EmfException;
    
    public void doSave() throws EmfException;
    
    public void updateView(TemporalAllocation temporalAllocation);
}
