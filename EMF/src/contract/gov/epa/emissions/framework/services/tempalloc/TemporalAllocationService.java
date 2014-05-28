package gov.epa.emissions.framework.services.tempalloc;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EMFService;
import gov.epa.emissions.framework.services.EmfException;

public interface TemporalAllocationService extends EMFService {

    TemporalAllocation[] getTemporalAllocations() throws EmfException;
    
    int addTemporalAllocation(TemporalAllocation element) throws EmfException;

    TemporalAllocation obtainLocked(User owner, int id) throws EmfException;
    
    void releaseLocked(User user, int id) throws EmfException;
    
    TemporalAllocation updateTemporalAllocationWithLock(TemporalAllocation element) throws EmfException;
    
    TemporalAllocationResolution[] getResolutions() throws EmfException;
}
