package gov.epa.emissions.framework.services.dao;

import java.util.List;

import org.hibernate.Session;

import gov.epa.emissions.framework.services.basic.AccessLog;

public interface LoggingDao {

    void insertAccessLog(AccessLog accesslog);

    List<AccessLog> getAccessLogs(int datasetid);

    String getLastExportedFileName(int datasetId);

}