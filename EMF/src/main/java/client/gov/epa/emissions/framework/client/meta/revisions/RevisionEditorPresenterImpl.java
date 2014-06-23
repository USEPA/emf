package gov.epa.emissions.framework.client.meta.revisions;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.Revision;

public class RevisionEditorPresenterImpl implements RevisionEditorPresenter {

    private Revision revision;

    private EmfSession session;
    
    private RevisionsTabView parentView;

    public RevisionEditorPresenterImpl(Revision revision, EmfSession session, RevisionsTabView parentView) {

        this.session = session;
        this.revision = revision;
        this.parentView = parentView;
    }

    public void display(RevisionEditorView view, Revision revision, EmfDataset dataset) throws EmfException {

        view.observe(this);

        /*
         * We shouldn't really have to get a lock, because we already have the dataset
         * lock. But the server-side DAO is expecting a lockable.
         */
        this.revision = service().obtainLockedRevision(session.user(), revision);
        if (!this.revision.isLocked(this.session.user())) {
            view.notifyLockFailure(this.revision);
        } else {
            view.display(this.revision, dataset);
        }
    }

    public DataCommonsService service() {
        return this.session.dataCommonsService();
    }

    public void doSave() throws EmfException {
        this.service().updateRevision(this.revision);
    }
    
    public void doRefresh() {
        this.parentView.refreshMSG(); 
        //(this.revision);
    }

}
