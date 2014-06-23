package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.services.EmfException;

public interface PropertiesEditorTabPresenter {

    void doSave() throws EmfException;
    
    void checkIfLockedByCurrentUser() throws EmfException;
    
}
