package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.services.EmfException;

public interface ObserverPanel {
    void update(int changes);
    
    int getPreviousNumber();
    
    void refresh(String filter, String sortOrder) throws EmfException;
}
