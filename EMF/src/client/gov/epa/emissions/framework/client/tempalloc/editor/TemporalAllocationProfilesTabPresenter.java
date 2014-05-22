package gov.epa.emissions.framework.client.tempalloc.editor;

import gov.epa.emissions.framework.services.EmfException;

public class TemporalAllocationProfilesTabPresenter implements TemporalAllocationTabPresenter {
    private TemporalAllocationProfilesTab view;
    
    public TemporalAllocationProfilesTabPresenter(TemporalAllocationTabView view) {
        this.view = (TemporalAllocationProfilesTab) view;
    }
    
    public void doDisplay() throws EmfException {
        view.display();
    }

    public void doSave() throws EmfException {
        view.save();
    }
}
