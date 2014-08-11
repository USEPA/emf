package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.framework.client.meta.PropertiesEditorTabPresenter;
import gov.epa.emissions.framework.client.swingworker.LightSwingWorkerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetNote;
import gov.epa.emissions.framework.services.data.Note;

public interface EditNotesTabPresenter extends PropertiesEditorTabPresenter, LightSwingWorkerPresenter {

    void display() throws EmfException;

    void doAddNote(NewNoteView view) throws EmfException;

    void doViewNote(DatasetNote note, NoteView window);

    void addExistingNotes(AddExistingNotesDialog view)throws EmfException;
    
    DatasetNote[] getSelectedNotes(AddExistingNotesDialog dialog) throws EmfException;
    
    Note[] getNotes(String nameContains) throws EmfException;
    
    DatasetNote[] getDatasetNotes() throws EmfException;
    
}