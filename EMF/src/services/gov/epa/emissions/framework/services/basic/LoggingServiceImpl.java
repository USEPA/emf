package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.JpaEntityManagerFactory;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoggingServiceImpl implements LoggingService {
    private static Log LOG = LogFactory.getLog(LoggingServiceImpl.class);

    private EntityManagerFactory entityManagerFactory;

    private LoggingDAO dao;

    public LoggingServiceImpl() {
        this(JpaEntityManagerFactory.get());
    }

    public LoggingServiceImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        dao = new LoggingDAO();
    }

    public synchronized void setAccessLog(AccessLog accesslog) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            dao.insertAccessLog(accesslog,entityManager); // BUG3589
        } catch (RuntimeException e) {
            LOG.error("Could not insert access log - " + accesslog, e);
            throw new EmfException("Could not insert access log - " + accesslog);
        } finally {
            entityManager.close();
        }
    }

    public synchronized AccessLog[] getAccessLogs(int datasetid) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List allLogs = dao.getAccessLogs(datasetid, entityManager);

            return (AccessLog[]) allLogs.toArray(new AccessLog[allLogs.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all access logs", e);
            throw new EmfException("Could not get all access logs");
        }
        finally {
            entityManager.close();          
        }

    }

    public synchronized String getLastExportedFileName(int datasetId) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            String fileName = dao.getLastExportedFileName(datasetId, entityManager);
 
            return fileName;
            
        } catch(EmfException e){
            throw e;
        }catch (RuntimeException e) {
            LOG.error("Could not get Last Exported File", e);
            throw new EmfException("Could not get all access logs");
        }
        finally {
            entityManager.close();          
        }
    }

}
