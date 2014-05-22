package gov.epa.emissions.framework.client.tempalloc.editor;

import gov.epa.emissions.framework.services.EmfException;

public interface TemporalAllocationTabPresenter {

    public void doDisplay() throws EmfException;
    
    public void doSave() throws EmfException;
}
