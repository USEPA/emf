package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.AccessLog;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class LoggingDAO {

    private static final String GET_ACCESS_LOG_QUERY = "from AccessLog as alog where alog.datasetId=:datasetid";

    public void insertAccessLog(AccessLog accesslog, Session session) { // BUG3589 root
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(accesslog);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }

    }

    public List getAccessLogs(long datasetid, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            List allLogs = session.createQuery(GET_ACCESS_LOG_QUERY).setLong("datasetid", datasetid).list();
            tx.commit();

            return allLogs;

        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }

    }

    public String getLastExportedFileName(int datasetId, Session session) throws EmfException {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            String query = "from AccessLog as alog where alog.datasetId=" + datasetId
                    + " order by alog.timestamp desc ";
            List allLogs = session.createQuery(query).list();
            tx.commit();
            if (allLogs.isEmpty()) {
                throw new EmfException("Please export the dataset first");
            }
            return ((AccessLog) allLogs.get(0)).getFolderPath();

        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }

    }
}
