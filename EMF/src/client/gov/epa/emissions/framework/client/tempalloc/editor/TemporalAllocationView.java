package gov.epa.emissions.framework.client.tempalloc.editor;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocation;

public interface TemporalAllocationView extends ManagedView {

    void observe(TemporalAllocationPresenter presenter);
    
    void display(TemporalAllocation temporalAllocation);
    
    void notifyLockFailure(TemporalAllocation temporalAllocation);
}
