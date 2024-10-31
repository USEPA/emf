package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;

import java.util.List;

import javax.persistence.EntityManager;

public class LoggingDAO {

    private static final String GET_ACCESS_LOG_QUERY = "from AccessLog as alog where alog.datasetId=:datasetid";

    public void insertAccessLog(AccessLog accesslog, EntityManager entityManager) { // BUG3589 root
        new HibernateFacade().executeInsideTransaction(em -> {
            entityManager.merge(accesslog);
        }, entityManager);
    }

    public List getAccessLogs(int datasetid, EntityManager entityManager) {
        return entityManager.createQuery(GET_ACCESS_LOG_QUERY).setParameter("datasetid", datasetid).getResultList();
    }

    public String getLastExportedFileName(int datasetId, EntityManager entityManager) throws EmfException {
        String query = "from AccessLog as alog where alog.datasetId=" + datasetId
                + " order by alog.timestamp desc ";
        List<AccessLog> allLogs = entityManager.createQuery(query, AccessLog.class).getResultList();
        if (allLogs.isEmpty()) {
            throw new EmfException("Please export the dataset first");
        }
        return allLogs.get(0).getFolderPath();
    }
}
