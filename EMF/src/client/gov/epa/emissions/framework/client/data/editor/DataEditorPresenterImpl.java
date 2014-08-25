package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.notes.NewNoteView;
import gov.epa.emissions.framework.client.meta.versions.EditVersionsView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DatasetNote;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.NoteType;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.editor.DataEditorService;
import gov.epa.emissions.framework.services.editor.DataViewService;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import javax.swing.SwingUtilities;

public class DataEditorPresenterImpl implements DataEditorPresenter {

    DataEditorView view;

    private Version version;

    private String table;

    private DataAccessToken token;

    private EditableTablePresenter tablePresenter;

    private EmfSession session;

    private EmfDataset dataset;

    private boolean changesSaved;
    private EditVersionsView parentview;
    public DataEditorPresenterImpl(EmfDataset dataset, Version version, String table, EmfSession session) {
        this.dataset = dataset;
        this.version = version;
        this.table = table;
        this.session = session;
    }
    
    public void display(DataEditorView view) throws EmfException {
    //        token = new DataAccessToken(version, table);
    //        token = dataEditorService().openSession(session.user(), token);
    
    //        if (!token.isLocked(session.user())) {// abort
    //            view.notifyLockFailure(token);
    //            return;
    //        }
    //        display(token, view);
            this.view = view;
            view.observe(this);
            view.display(version, table, session.user());
    }

    DataEditorService dataEditorService() {
        return session.dataEditorService();
    }

    DataViewService dataViewService() {
            return session.dataViewService();
    }

//    private void display(DataAccessToken token, DataEditorView view) throws EmfException {
//        this.view = view;
//        view.observe(this);
//        displayView(view);
//
//        view.updateLockPeriod(token.lockStart(), token.lockEnd());
//    }

    public TableMetadata getTableMetadata() throws EmfException {
            return dataEditorService().getTableMetadata(table);
    }

    
    public DatasetNote[] getDatasetNotes() throws EmfException {
        return commonsService().getDatasetNotes(dataset.getId());
    }
    
//    private void displayView(DataEditorView view) throws EmfException {
//        TableMetadata tableMetadata = dataEditorService().getTableMetadata(table);
//        DatasetNote[] notes = commonsService().getDatasetNotes(dataset.getId());
//
//        view.display(version, table, session.user(), tableMetadata, notes);
//    }

    public void displayTable(EditorPanelView tableView, Container parentContainer, MessagePanel messagePanel, TableMetadata tableMetadata) throws EmfException {
        tablePresenter = new EditableTablePresenterImpl(dataset.getDatasetType(), token, tableMetadata,
                tableView, dataEditorService(), this, parentContainer, messagePanel);
        displayTable(tablePresenter);
    }

    void displayTable(EditableTablePresenter tablePresenter) throws EmfException {
        tablePresenter.display();
    }

    public void doClose() throws EmfException {
        close(closingRule(), areChangesSaved());
    }

    boolean areChangesSaved() {
        return changesSaved;
    }

    void close(ClosingRule closingRule, boolean changesSaved) throws EmfException {
        closingRule.close(session.user(), changesSaved);
    }

    private ClosingRule closingRule() {
        return new ClosingRule(view, tablePresenter, session, token);
    }

    public void doDiscard() throws EmfException {
        discard(dataEditorService(), token, tablePresenter);
        reset(view);
    }

    public void discard() throws EmfException {
            discard(dataEditorService(), token, tablePresenter);
        }
    
        
    private void reset(DataEditorView view) {
        view.resetChanges();
        view.disableSaveDiscard();
    }

    void discard(DataEditorService service, DataAccessToken token, EditableTablePresenter tablePresenter)
            throws EmfException {
        service.discard(token);
        }
        
            public void reloadCurrent() throws EmfException {
        tablePresenter.reloadCurrent();
    }

    public void doSave() throws EmfException {
            save(tablePresenter, closingRule());
    //        parentview.refresh();
    }

    void save(EditableTablePresenter tablePresenter, ClosingRule closingRule) throws EmfException {
            DataEditorService service = dataEditorService();
        int numOfRecords = service.getTotalRecords(token);
        boolean submitChanges = tablePresenter.submitChanges();
        changesSaved = changesSaved || submitChanges;
        Date currentDate = new Date();
        dataset.setModifiedDateTime(currentDate);
        token.getVersion().setLastModifiedDate(currentDate);
        token.getVersion().setNumberRecords(tablePresenter.getTotalRecords());
        try {
            try {
                token = service.save(token, dataset, version);
            } catch (Exception e) {
                numOfRecords = session.dataService().getNumOfRecords("emissions." + token.getTable(), token.getVersion(), null);
                token.getVersion().setNumberRecords( numOfRecords);
                session.dataService().updateVersion(token.getVersion());
                e.printStackTrace();
//                try {
//                    tablePresenter.reloadCurrent();
//                    view.updateLockPeriod(token.lockStart(), token.lockEnd());
//                    reset(view); 
//                }catch ( Exception e1) {
//                    
//                }
                throw new EmfException(e.getMessage());
            }
                    SwingUtilities.invokeAndWait(new Runnable() {
                            @Override
                            public void run() {
                                view.updateLockPeriod(token.lockStart(), token.lockEnd());
                                reset(view);
                            }
                        });
                    } catch (EmfException | InvocationTargetException | InterruptedException e) {
                        try {
                            SwingUtilities.invokeAndWait(new Runnable() {
                                @Override
                                public void run() {
                                    view.notifySaveFailure(e.getMessage());
                                }
                            });
                        } catch (InvocationTargetException | InterruptedException e1) {
                            // NOTE Auto-generated catch block
                            e1.printStackTrace();
                        }
            discard(service, token, tablePresenter);
            closingRule.proceedWithClose(session.user(), areChangesSaved());
        }
    }

    public void doAddNote(NewNoteView view) throws EmfException {
        NoteType[] types = commonsService().getNoteTypes();
        Version[] versions = dataEditorService().getVersions(dataset.getId());
        DatasetNote[] notes = commonsService().getDatasetNotes(dataset.getId());

        addNote(view, session.user(), dataset, notes, types, versions);
    }

    private DataCommonsService commonsService() {
        return session.dataCommonsService();
    }

    void addNote(NewNoteView view, User user, EmfDataset dataset, DatasetNote[] notes, NoteType[] types, Version[] versions)
            throws EmfException {
        view.display(user, dataset, version, notes, types, versions);
        if (view.shouldCreate())
            commonsService().addDatasetNote(view.DSnote());
    }

    public EmfSession getEmfSession() {
        return session;
    }
    
    public void setSaveChanged(boolean changeSaved) {
        this.changesSaved = changeSaved;
    }

    @Override
    public DataAccessToken openSession() throws EmfException {
        token = new DataAccessToken(version, table);
        token = dataEditorService().openSession(session.user(), token);
        return token;
    }

    @Override
    public void clearTable() {
        tablePresenter.clear();
    }

}
