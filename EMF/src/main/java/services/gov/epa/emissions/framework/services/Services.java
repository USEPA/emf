package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.services.basic.LoggingServiceImpl;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DataServiceImpl;

public class Services {
    private StatusDAO status;

    private DataServiceImpl data;

    private LoggingServiceImpl logging;

    public DataServiceImpl getData() {
        return data;
    }

    public void setDataService(DataServiceImpl data) {
        this.data = data;
    }

    public LoggingServiceImpl getLoggingService() {
        return logging;
    }

    public void setLoggingService(LoggingServiceImpl logging) {
        this.logging = logging;
    }

    public StatusDAO getStatus() {
        return status;
    }

    public void setStatusService(StatusDAO status) {
        this.status = status;
    }

}
