package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class LoggingServiceImpl implements LoggingService {
    private static Log LOG = LogFactory.getLog(LoggingServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private LoggingDAO dao;

    public LoggingServiceImpl() {
        this(HibernateSessionFactory.get());
    }

    public LoggingServiceImpl(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        dao = new LoggingDAO();
    }

    public synchronized void setAccessLog(AccessLog accesslog) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.insertAccessLog(accesslog,session); // BUG3589
        } catch (RuntimeException e) {
            LOG.error("Could not insert access log - " + accesslog, e);
            throw new EmfException("Could not insert access log - " + accesslog);
        } finally {
            session.close();
        }
    }

    public synchronized AccessLog[] getAccessLogs(int datasetid) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List allLogs = dao.getAccessLogs(datasetid, session);

            return (AccessLog[]) allLogs.toArray(new AccessLog[allLogs.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all access logs", e);
            throw new EmfException("Could not get all access logs");
        }
        finally {
            session.close();          
        }

    }

    public synchronized String getLastExportedFileName(int datasetId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            String fileName = dao.getLastExportedFileName(datasetId, session);
 
            return fileName;
            
        } catch(EmfException e){
            throw e;
        }catch (RuntimeException e) {
            LOG.error("Could not get Last Exported File", e);
            throw new EmfException("Could not get all access logs");
        }
        finally {
            session.close();          
        }
    }

}
