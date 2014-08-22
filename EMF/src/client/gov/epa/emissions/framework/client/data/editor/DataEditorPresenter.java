package gov.epa.emissions.framework.client.data.editor;

import java.awt.Container;

import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.notes.NewNoteView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetNote;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.ui.MessagePanel;

public interface DataEditorPresenter {

    void display(DataEditorView view) throws EmfException;

    void displayTable(EditorPanelView tableView, Container parentContainer, MessagePanel messagePanel, TableMetadata tableMetadata) throws EmfException;

    void doClose() throws EmfException;

    void doDiscard() throws EmfException;

    void discard() throws EmfException;

    void reloadCurrent() throws EmfException;

    void doSave() throws EmfException;

    void doAddNote(NewNoteView view) throws EmfException;
    
    EmfSession getEmfSession();
    
    void setSaveChanged(boolean changeSaved);

    DataAccessToken openSession() throws EmfException;

//    DataAccessToken getToken();

    TableMetadata getTableMetadata() throws EmfException;
    
    DatasetNote[] getDatasetNotes() throws EmfException;
    
    void clearTable();
}