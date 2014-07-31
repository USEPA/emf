package gov.epa.emissions.framework.client.data.editor;

import java.awt.Container;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.notes.NewNoteView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.MessagePanel;

public interface DataEditorPresenter {

    void display(DataEditorView view) throws EmfException;

    void displayTable(EditorPanelView tableView, Container parentContainer, MessagePanel messagePanel) throws EmfException;

    void doClose() throws EmfException;

    void doDiscard() throws EmfException;

    void doSave() throws EmfException;

    void doAddNote(NewNoteView view) throws EmfException;
    
    EmfSession getEmfSession();
    
    void setSaveChanged(boolean changeSaved);

}