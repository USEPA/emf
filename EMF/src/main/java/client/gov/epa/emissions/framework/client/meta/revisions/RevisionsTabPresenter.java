package gov.epa.emissions.framework.client.meta.revisions;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.Revision;

public class RevisionsTabPresenter {

    private EmfDataset dataset;

    private EmfSession session;

    public RevisionsTabPresenter(EmfDataset dataset, EmfSession session) {
        
        this.dataset = dataset;
        this.session = session;
    }

    public void display(RevisionsTabView view) throws EmfException {
        view.display(getRevisions(), this);
    }
    
    public DataCommonsService service(){
        return session.dataCommonsService();
    }
    
    public Revision[] getRevisions() throws EmfException{
        return service().getRevisions(dataset.getId());
    }

    public void doViewRevision(Revision revision, RevisionView view) {
        view.display(revision, dataset);
    }

    public void doEditRevision(Revision revision, RevisionEditorView view, RevisionsTabView parentView) throws EmfException {

        RevisionEditorPresenter  presenter = new RevisionEditorPresenterImpl(revision, session, parentView);
        view.observe(presenter);
        presenter.display(view, revision, dataset);
    }

    public void checkIfLockedByCurrentUser() throws EmfException{
        EmfDataset reloaded = session.dataService().getDataset(dataset.getId());
        if (!reloaded.isLocked())
            throw new EmfException("Lock on current dataset object expired. " );  
        if (!reloaded.isLocked(session.user()))
            throw new EmfException("Lock on current dataset object expired. User " + reloaded.getLockOwner()
                    + " has it now.");    
    }
}
