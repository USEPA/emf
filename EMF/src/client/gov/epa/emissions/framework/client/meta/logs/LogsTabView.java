package gov.epa.emissions.framework.client.meta.logs;

import gov.epa.emissions.framework.services.basic.AccessLog;

public interface LogsTabView {

    void display(AccessLog[] accessLogs);
    
    void observe(LogsTabPresenter presenter);
    
    void doRefresh(AccessLog[] accessLogs);
}
