package gov.epa.emissions.framework.services.tempalloc;

import org.hibernate.Session;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class TemporalAllocationRunTask implements Runnable {

    private TemporalAllocationTask temporalAllocationTask;
    
    private Services services;
    
    private TemporalAllocationService taService;
    
    private HibernateSessionFactory sessionFactory;

    public TemporalAllocationRunTask(TemporalAllocationTask temporalAllocationTask, 
            Services services, 
            TemporalAllocationService service, 
            HibernateSessionFactory sessionFactory) {
        this.services = services;
        this.temporalAllocationTask = temporalAllocationTask;
        this.taService = service;
        this.sessionFactory = sessionFactory;
    }
    
    public void run() {
        Long poolSize = strategyPoolSize();
        Long runningCount = getTemporalAllocationRunningCount();

        if (runningCount < poolSize) {
            try {
                temporalAllocationTask.run();
            } catch (EmfException e) {
                
            }
        }
    }

    private Long strategyPoolSize() {
        Session session = sessionFactory.getSession();
        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty("strategy-pool-size", session);
            return Long.parseLong(property.getValue());
        } finally {
            session.close();
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
}
