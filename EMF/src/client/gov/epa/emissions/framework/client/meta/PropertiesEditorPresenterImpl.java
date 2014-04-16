package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.exim.ExportPresenter;
import gov.epa.emissions.framework.client.exim.ExportWindow;
import gov.epa.emissions.framework.client.meta.info.InfoTabPresenter;
import gov.epa.emissions.framework.client.meta.info.InfoTabView;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabPresenter;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabPresenterImpl;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabView;
import gov.epa.emissions.framework.client.meta.notes.EditNotesTabPresenterImpl;
import gov.epa.emissions.framework.client.meta.notes.EditNotesTabView;
import gov.epa.emissions.framework.client.meta.qa.EditableQATabPresenter;
import gov.epa.emissions.framework.client.meta.qa.EditableQATabPresenterImpl;
import gov.epa.emissions.framework.client.meta.qa.EditableQATabView;
import gov.epa.emissions.framework.client.meta.revisions.RevisionsTabPresenter;
import gov.epa.emissions.framework.client.meta.revisions.RevisionsTabView;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTabPresenterImpl;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTabView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Keywords;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PropertiesEditorPresenterImpl implements PropertiesEditorPresenter {

    private EmfDataset dataset;

    private DatasetPropertiesEditorView view;

    private EmfSession session;

    private List presenters;
    
    private EditableKeywordsTabPresenter keywordsPresenter;

    public PropertiesEditorPresenterImpl(EmfDataset dataset, DatasetPropertiesEditorView view, EmfSession session) {
        this.dataset = dataset;
        this.session = session;
        this.view = view;
        presenters = new ArrayList();
    }

    public void doDisplay() throws EmfException {
        view.observe(this);

        dataset = dataService().obtainLockedDataset(session.user(), dataset);
        if (!dataset.isLocked(session.user())) {// view mode, locked by another user
            view.notifyLockFailure(dataset);
            return;
        }

        display();
    }

    void display() throws EmfException {
        Version[] versions = session.dataEditorService().getVersions(dataset.getId());
        view.display(dataset, versions);
    }

    public void doClose() throws EmfException {
        dataService().releaseLockedDataset(session.user(), dataset);
        view.disposeView();
    }

    public void doSave() throws EmfException {
        save(dataset, dataService(), presenters, view);
    }

    void save(EmfDataset dataset, DataService service, List presenters, DatasetPropertiesEditorView view)
            throws EmfException {
        for (Iterator iter = presenters.iterator(); iter.hasNext();) {
            PropertiesEditorTabPresenter element = (PropertiesEditorTabPresenter) iter.next();
            element.doSave();
        }
        checkIfLockedByCurrentUser();
        service.updateDataset(dataset);

        view.disposeView();
    }
    
    public void checkIfLockedByCurrentUser() throws EmfException {
        EmfDataset reloaded = dataService().getDataset(dataset.getId());
        
        if (reloaded.isLocked() && !reloaded.isLocked(session.user()))
            throw new EmfException("Lock on current case object expired. User " + reloaded.getLockOwner() + " has it now.");
        if (!reloaded.isLocked())
            reloaded = dataService().obtainLockedDataset(session.user(), reloaded);
        
        if (reloaded == null)
            throw new EmfException("Acquire lock on case failed. Please exit editing the case.");
    
    }

    public void set(EditableSummaryTabView summary) {
        EditableSummaryTabPresenterImpl summaryPresenter = new EditableSummaryTabPresenterImpl(dataset, summary, session);
        presenters.add(summaryPresenter);
    }

    public void set(EditableKeywordsTabView keywordsView) throws EmfException {
        keywordsPresenter = new EditableKeywordsTabPresenterImpl(keywordsView, dataset, session);

        Keywords keywords = new Keywords(session.dataCommonsService().getKeywords());
        keywordsPresenter.display(keywords);

        presenters.add(keywordsPresenter);
    }
    
    public EditableKeywordsTabPresenter getKeywordsPresenter() {
        return keywordsPresenter;
    }

    public void set(EditNotesTabView view) throws EmfException {
        EditNotesTabPresenterImpl notesPresenter = new EditNotesTabPresenterImpl(dataset, session, view);
        notesPresenter.display();

        presenters.add(notesPresenter);
    }

    public void set(EditableQATabView qaTab) throws EmfException {
        EditableQATabPresenterImpl qaPresenter = new EditableQATabPresenterImpl(dataset, session, qaTab);
        set(qaPresenter);

        presenters.add(qaPresenter);
    }

    void set(EditableQATabPresenter presenter) throws EmfException {
        presenter.display();
    }

    private DataService dataService() {
        return session.dataService();
    }

    public void set(DataTabView view) {
        DataTabPresenter presenter = new DataTabPresenter(view, dataset, session);
        presenter.doDisplay();
    }

    public void set(RevisionsTabView view) throws EmfException {
        RevisionsTabPresenter presenter = new RevisionsTabPresenter(dataset, session);
        presenter.display(view);
    }

    public void set(InfoTabView infoView) {
        InfoTabPresenter presenter = new InfoTabPresenter(infoView, dataset, session);
        presenters.add(presenter);
    }
    
    public EmfSession getSession() {
        return this.session;
    }
    
    public void doExport(ExportWindow exportView, ExportPresenter presenter) {
        presenter.display(exportView);
    }
}
