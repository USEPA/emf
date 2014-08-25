package gov.epa.emissions.framework.client.tempalloc;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocation;

public interface TemporalAllocationManagerView extends ManagedView {

    void display(TemporalAllocation[] temporalAllocations) throws EmfException;

    void observe(TemporalAllocationManagerPresenter presenter);

    void refresh(TemporalAllocation[] temporalAllocations) throws EmfException;
}
