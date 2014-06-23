package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.framework.services.EmfException;

public interface LoggingService {

    AccessLog[] getAccessLogs(int datasetid) throws EmfException;
    
    String getLastExportedFileName(int datasetId) throws EmfException;

}
