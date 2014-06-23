package gov.epa.emissions.framework.client.casemanagement.history;

import gov.epa.emissions.framework.client.EmfSession;

public interface ShowHistoryTabView {

    void display(EmfSession session, int caseId, ShowHistoryTabPresenter presenter);

    int numberOfRecord();

    void clearMessage();
    
//    void notifychanges();

}
