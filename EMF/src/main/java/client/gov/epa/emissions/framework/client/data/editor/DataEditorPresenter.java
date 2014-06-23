package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.notes.NewNoteView;
import gov.epa.emissions.framework.services.EmfException;

public interface DataEditorPresenter {

    void display(DataEditorView view) throws EmfException;

    void displayTable(EditorPanelView tableView) throws EmfException;

    void doClose() throws EmfException;

    void doDiscard() throws EmfException;

    void doSave() throws EmfException;

    void doAddNote(NewNoteView view) throws EmfException;
    
    EmfSession getEmfSession();
    
    void setSaveChanged(boolean changeSaved);

}