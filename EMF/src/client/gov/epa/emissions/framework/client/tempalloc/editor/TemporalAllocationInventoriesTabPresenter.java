package gov.epa.emissions.framework.client.tempalloc.editor;

import gov.epa.emissions.framework.services.EmfException;

public class TemporalAllocationInventoriesTabPresenter implements TemporalAllocationTabPresenter {
    private TemporalAllocationInventoriesTab view;
    
    public TemporalAllocationInventoriesTabPresenter(TemporalAllocationTabView view) {
        this.view = (TemporalAllocationInventoriesTab) view;
    }
    
    public void doDisplay() throws EmfException {
        view.display();
    }

    public void doSave() throws EmfException {
        view.save();
    }
}
