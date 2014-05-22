package gov.epa.emissions.framework.client.tempalloc.editor;

import gov.epa.emissions.framework.services.EmfException;

public class TemporalAllocationOutputTabPresenter implements TemporalAllocationTabPresenter {
    private TemporalAllocationOutputTab view;
    
    public TemporalAllocationOutputTabPresenter(TemporalAllocationTabView view) {
        this.view = (TemporalAllocationOutputTab) view;
    }
    
    public void doDisplay() throws EmfException {
        view.display();
    }

    public void doSave() throws EmfException {
        view.save();
    }
}
