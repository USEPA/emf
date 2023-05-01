package gov.epa.emissions.framework.services.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.epa.emissions.framework.services.basic.AccessLog;
import gov.epa.emissions.framework.services.dao.LoggingDao;
import gov.epa.emissions.framework.services.persistence.AbstractJpaDao;

@Repository(value = "loggingDao")
public class LoggingDaoImpl extends AbstractJpaDao<AccessLog> implements LoggingDao {

    private static final String GET_ACCESS_LOG_QUERY = "from AccessLog as alog where alog.datasetId=:datasetid";

    public LoggingDaoImpl() {
        super();
        setClazz(AccessLog.class);
    }

    @Transactional("transactionManager")
    @Override
    public void insertAccessLog(AccessLog accesslog) {
        try {
            create(accesslog);
        } catch (RuntimeException e) {
            throw e;
        }

    }

    @Override
    public List<AccessLog> getAccessLogs(int datasetid) {
        try {
            return entityManager
                    .createQuery(GET_ACCESS_LOG_QUERY, AccessLog.class)
                    .setParameter("datasetid", datasetid)
                    .getResultList();
        } catch (RuntimeException e) {
            throw e;
        }

    }

    @Override
    public String getLastExportedFileName(int datasetId) {
        try {
            String query = "from AccessLog as alog where alog.datasetId=" + datasetId
                    + " order by alog.timestamp desc ";
            List<AccessLog> allLogs = entityManager
                    .createQuery(GET_ACCESS_LOG_QUERY, AccessLog.class)
                    .getResultList();
            if (allLogs.isEmpty()) {
                throw new RuntimeException("Please export the dataset first");
            }
            return ((AccessLog) allLogs.get(0)).getFolderPath();

        } catch (RuntimeException e) {
            throw e;
        }

    }
}
