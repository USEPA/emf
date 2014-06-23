package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DatasetNote;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class NotesTabPresenter {

    private EmfDataset dataset;

    private DataCommonsService service;

    public NotesTabPresenter(EmfDataset dataset, DataCommonsService service) {
        this.dataset = dataset;
        this.service = service;
    }

    public void display(NotesTabView view) throws EmfException {
        //DatasetNote[] notes = service.getDatasetNotes(dataset.getId());
        view.display(getDatasetNotes(), this);
    }

    public void doViewNote(DatasetNote note, NoteView view) {
        view.display(note);
    }
    
    public DatasetNote[] getDatasetNotes() throws EmfException{
        return service.getDatasetNotes(dataset.getId());
    }
}
