package gov.epa.emissions.framework.services.tempalloc;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TemporalAllocationRunTask implements Runnable {
    
    private static Log log = LogFactory.getLog(TemporalAllocationRunTask.class);
    
    private User user;

    private TemporalAllocationTask temporalAllocationTask;
    
    private Services services;
    
    private TemporalAllocationService taService;
    
    private EntityManagerFactory entityManagerFactory;

    public TemporalAllocationRunTask(TemporalAllocationTask temporalAllocationTask, User user,
            Services services, 
            TemporalAllocationService service, 
            EntityManagerFactory entityManagerFactory) {
        this.user = user;
        this.services = services;
        this.temporalAllocationTask = temporalAllocationTask;
        this.taService = service;
        this.entityManagerFactory = entityManagerFactory;
    }
    
    public void run() {
        String completeStatus = "";

        Long poolSize = strategyPoolSize();
        Long runningCount = getTemporalAllocationRunningCount();

        if (runningCount < poolSize) {
            try {
                prepare();
                if (temporalAllocationTask.run()) {
                    completeStatus = "Finished";
                    addCompletedStatus();
                } else {
                    completeStatus = "Cancelled";
                    addCancelledStatus();
                }
            } catch (EmfException e) {
                completeStatus = "Failed";
                logError("Failed to run temporal allocation : ", e);
                setStatus("Failed to run temporal allocation: " + "Reason: " + e.getMessage());
            } finally {
                try {
                    setRunStatusAndCompletionDate(completeStatus, new Date());
                    
                    // check for any allocations waiting to run
                    List<TemporalAllocation> waitingItems = taService.getTemporalAllocationsByRunStatus("Waiting");
                    if (waitingItems.size() > 0) {
                        runningCount = getTemporalAllocationRunningCount();
                        if (runningCount < poolSize) {
                            for (TemporalAllocation item : waitingItems.toArray(new TemporalAllocation[0])) {
                                if (runningCount < poolSize) {
                                    taService.runTemporalAllocation(user, item);
                                    runningCount++;
                                }
                            }
                        }
                    }
                } catch (EmfException e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private void prepare() throws EmfException {
        TemporalAllocation temporalAllocation = temporalAllocationTask.getTemporalAllocation();
        temporalAllocation = taService.obtainLocked(temporalAllocation.getCreator(), temporalAllocation.getId());
        temporalAllocation.setStartDate(new Date());
        temporalAllocation.setRunStatus("Running");
        taService.updateTemporalAllocationWithLock(temporalAllocation);
        addStartStatus();
    }

    private void setRunStatusAndCompletionDate(String completeStatus, Date completionDate) throws EmfException {
        TemporalAllocation temporalAllocation = temporalAllocationTask.getTemporalAllocation();
        temporalAllocation.setRunStatus(completeStatus);
        temporalAllocation.setLastModifiedDate(new Date());
        temporalAllocation.setCompletionDate(completionDate);
        taService.setRunStatusAndCompletionDate(temporalAllocation, completeStatus, completionDate);
    }

    private Long strategyPoolSize() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty("strategy-pool-size", entityManager);
            return Long.parseLong(property.getValue());
        } finally {
            entityManager.close();
        }
    }
    
    private Long getTemporalAllocationRunningCount() {
        Long count = 0L;
        try {
            count = taService.getTemporalAllocationRunningCount();
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return count; 
    }
    
    private void addStartStatus() {
        setStatus("Started running temporal allocation: " + temporalAllocationTask.getTemporalAllocation().getName());
    }

    private void addCompletedStatus() {
        setStatus("Completed running temporal allocation: " + temporalAllocationTask.getTemporalAllocation().getName() + ".");
    }

    private void addCancelledStatus() {
        setStatus("Cancelled running temporal allocation: " + temporalAllocationTask.getTemporalAllocation().getName() + ".");
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("TemporalAllocation");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        services.getStatus().add(endStatus);
    }

    private void logError(String messge, Exception e) {
        log.error(messge, e);
    }
}
