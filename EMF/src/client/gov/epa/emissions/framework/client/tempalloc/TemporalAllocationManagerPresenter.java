package gov.epa.emissions.framework.client.tempalloc;

import java.util.Date;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.tempalloc.editor.TemporalAllocationPresenter;
import gov.epa.emissions.framework.client.tempalloc.editor.TemporalAllocationView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocation;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocationService;
import gov.epa.emissions.framework.ui.RefreshObserver;

public class TemporalAllocationManagerPresenter implements RefreshObserver {

    private TemporalAllocationManagerView view;

    private EmfSession session;
    
    public TemporalAllocationManagerPresenter(EmfSession session, TemporalAllocationManagerView view) {
        this.session = session;
        this.view = view;
    }
    
    public void display() throws EmfException {
        view.display(service().getTemporalAllocations());
        view.observe(this);
    }
    
    private TemporalAllocationService service() {
        return session.temporalAllocationService();
    }
    
    public void doRefresh() throws EmfException {
        view.refresh(service().getTemporalAllocations());
    }

    public void doClose() {
        view.disposeView();
    }
    
    public void doNew(TemporalAllocationView view) {
        TemporalAllocation temporalAllocation = new TemporalAllocation();
        temporalAllocation.setName("");
        temporalAllocation.setLastModifiedDate(new Date());
        temporalAllocation.setCreator(session.user());
        temporalAllocation.setRunStatus("Not started");
        TemporalAllocationPresenter presenter = new TemporalAllocationPresenter(temporalAllocation, session, view);
        presenter.doDisplayNew();
    }
    
    public void doEdit(TemporalAllocationView view, TemporalAllocation temporalAllocation) throws EmfException {
        TemporalAllocationPresenter presenter = new TemporalAllocationPresenter(temporalAllocation, session, view);
        presenter.doDisplay();
    }
}
