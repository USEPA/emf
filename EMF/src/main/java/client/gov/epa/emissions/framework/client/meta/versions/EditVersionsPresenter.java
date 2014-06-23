package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.data.editor.DataEditorPresenter;
import gov.epa.emissions.framework.client.data.editor.DataEditorPresenterImpl;
import gov.epa.emissions.framework.client.data.editor.DataEditorView;
import gov.epa.emissions.framework.client.data.viewer.DataView;
import gov.epa.emissions.framework.client.data.viewer.DataViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.editor.DataEditorService;

public class EditVersionsPresenter {

    private EditVersionsView view;

    private EmfDataset dataset;

    private EmfSession session;

    public EditVersionsPresenter(EmfDataset dataset, EmfSession session) {
        this.dataset = dataset;
        this.session = session;
    }

    public void display(EditVersionsView view) throws EmfException {
        this.view = view;
        view.observe(this);

        Version[] versions = editorService().getVersions(dataset.getId());
        view.display(versions);
    }

    private DataEditorService editorService() {
        return session.dataEditorService();
    }

    private DataService dataService() {
        return session.dataService();
    }

    public void doNew(Version base, String name) throws EmfException {
        editorService().derive(base, session.user(), name);
        reload();
    }

    public void doView(Version version, String table, DataView view) throws EmfException {
        // if (!version.isFinalVersion())
        // throw new EmfException("Cannot view a Version that is not Final. Please choose edit for Version "+
        // version.getName());

        DataViewPresenter presenter = new DataViewPresenter(dataset, version, table, view, session);
        presenter.display();
    }

    public void doEdit(Version version, String table, DataEditorView view, EditVersionsView parentview) throws EmfException {
        DataEditorPresenter presenter = new DataEditorPresenterImpl(dataset, version, table, parentview, session);
        edit(version, view, presenter);
    }

    void edit(Version version, DataEditorView view, DataEditorPresenter presenter) throws EmfException {
        if (version.isFinalVersion())
            throw new EmfException("Cannot edit a Version that is Final. Please choose View for Version "
                    + version.getName());
        presenter.display(view);
    }

    public void doMarkFinal(Version[] versions) throws EmfException {
        for (int i = 0; i < versions.length; i++) {
            if (versions[i].isFinalVersion())
                throw new EmfException("Version: " + versions[i].getName()
                        + " is already Final. It cannot be marked as final again.");
            editorService().markFinal(token(versions[i]));
        }

        reload();
    }

    private DataAccessToken token(Version version) {
        return new DataAccessToken(version, null);
    }

    public void reload() throws EmfException {
        Version[] updatedVersions = editorService().getVersions(dataset.getId());
        view.reload(updatedVersions);
    }

    public EmfSession getSession() {
        return session;
    }

    public void doChangeVersionName(Version version) throws EmfException {
        dataService().updateVersionNReleaseLock(version);
        reload();
    }

    private User getUser() {
        return session.user();
    }

    public Version getLockedVersion(Version version) throws EmfException {
        Version locked = dataService().obtainedLockOnVersion(getUser(), version.getId());
        if (!locked.isLocked(getUser())) {// view mode, locked by another user
            view.notifyLockFailure(version);
            return null;
        }

        return locked;
    }

    public void markFinalNDefault(Version version) throws Exception {
        editorService().markFinal(token(version));

        EmfDataset locked = dataService().obtainLockedDataset(getUser(), dataset);

        if (locked != null && locked.isLocked(getUser())) {
            locked.setDefaultVersion(version.getVersion());
            dataService().updateDataset(locked);
        }

        reload();

        if (locked == null || !locked.isLocked(getUser()))
            throw new EmfException("Cannot obtain lock on dataset. Set default version failed.");
    }

    public void releaseLock(Version lockedVersion) throws EmfException {
        dataService().updateVersionNReleaseLock(lockedVersion);
    }

    public void copyDataset(Version version) throws EmfException {
        if (!version.isFinalVersion())
            throw new EmfException("Can only copy a version that is Final");

        dataService().copyDataset(dataset.getId(), version, session.user());
    }
    
    public Integer[] getDatasetRecords(int datasetID, Version[] versions, String tableName) throws EmfException{
        return dataService().getNumOfRecords(datasetID, versions, tableName);
    }
}
