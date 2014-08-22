package gov.epa.emissions.framework.client.data.viewer;

import java.awt.Container;
import java.awt.Cursor;
import java.util.Date;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.notes.NewNoteView;
import gov.epa.emissions.framework.client.swingworker.GenericSwingWorker;
import gov.epa.emissions.framework.client.util.ComponentUtility;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DatasetNote;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.NoteType;
import gov.epa.emissions.framework.services.editor.DataAccessService;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.editor.DataEditorService;
import gov.epa.emissions.framework.services.editor.DataViewService;
import gov.epa.emissions.framework.ui.MessagePanel;

public class DataViewPresenter {

    private DataView view;

    private Version version;

    private String table;

    private DataAccessToken token;

    private EmfSession session;

    private EmfDataset dataset;

//    private TableMetadata tableMetadata;
//, TableMetadata tableMetadata
    public DataViewPresenter(EmfDataset dataset, Version version, String table, DataView view, EmfSession session) {
        this.dataset = dataset;
        this.version = version;
        this.table = table;
        this.view = view;
        this.session = session;
//        this.tableMetadata = tableMetadata;

        token = new DataAccessToken(version, table);
    }

    private DataViewService viewService() {
        return session.dataViewService();
    }

    private DataEditorService accessService() {
        return session.dataEditorService();
    }

    private DataCommonsService commonsService() {
        return session.dataCommonsService();
    }

    public void display() throws EmfException {
        view.observe(this);
        view.display(version, table);
    }

    public DataAccessToken openSession() throws EmfException {
        token = viewService().openSession(token);
        return token;
    }
    
    public Page applyConstraints(DataAccessToken token, String rowFilter, String sortOrder) throws EmfException {
        return viewService().applyConstraints(token, rowFilter, sortOrder);
    }
    
    public TableMetadata getTableMetadata(String table) throws EmfException {
        return viewService().getTableMetadata(table);
    }
    
    public void displayTable(ViewerPanel tableView, Container parentContainer, MessagePanel messagePanel, TableMetadata tableMetadata) throws EmfException {
        TablePresenter tablePresenter = new ViewableTablePresenter(dataset.getDatasetType(), token, tableMetadata,
                tableView, accessService(), viewService(), parentContainer, messagePanel);
        displayTable(tablePresenter);
    }

    void displayTable(TablePresenter tablePresenter) throws EmfException {
        tablePresenter.display();
    }

    public void closeSession() throws EmfException {
        viewService().closeSession(token);
    }
    
    public void doClose() {
        view.disposeView();
    }

    public void doAddNote(NewNoteView view) throws EmfException {
        NoteType[] types = commonsService().getNoteTypes();
        Version[] versions = session.dataEditorService().getVersions(dataset.getId());

        addNote(view, session.user(), dataset, types, versions);
    }

    void addNote(NewNoteView view, User user, EmfDataset dataset, NoteType[] types, Version[] versions)
            throws EmfException {
        DatasetNote[] notes = commonsService().getDatasetNotes(dataset.getId());
        view.display(user, dataset, version, notes, types, versions);
        if (view.shouldCreate())
            commonsService().addDatasetNote(view.DSnote());
    }

}
