package gov.epa.emissions.framework.services.tempalloc;

import java.util.Date;
import java.util.List;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EMFService;
import gov.epa.emissions.framework.services.EmfException;

public interface TemporalAllocationService extends EMFService {
    
    TemporalAllocation getById(int id) throws EmfException;

    TemporalAllocation[] getTemporalAllocations() throws EmfException;
    
    TemporalAllocationOutput[] getTemporalAllocationOutputs(TemporalAllocation element) throws EmfException;
    
    int addTemporalAllocation(TemporalAllocation element) throws EmfException;
    
    int copyTemporalAllocation(TemporalAllocation element, User creator) throws EmfException;
    
    void removeTemporalAllocations(int[] ids, User user) throws EmfException;

    TemporalAllocation obtainLocked(User owner, int id) throws EmfException;
    
    void releaseLocked(User user, int id) throws EmfException;
    
    TemporalAllocation updateTemporalAllocationWithLock(TemporalAllocation element) throws EmfException;
    
    TemporalAllocationResolution[] getResolutions() throws EmfException;
    
    void runTemporalAllocation(User user, TemporalAllocation element) throws EmfException;
    
    List<TemporalAllocation> getTemporalAllocationsByRunStatus(String runStatus) throws EmfException;
    
    Long getTemporalAllocationRunningCount() throws EmfException;
    
    void setRunStatusAndCompletionDate(TemporalAllocation element, String runStatus, Date completionDate) throws EmfException;
    
    int isDuplicateName(String name) throws EmfException;
}
