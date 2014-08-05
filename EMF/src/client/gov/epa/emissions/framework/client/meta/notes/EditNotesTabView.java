package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.framework.services.data.DatasetNote;

public interface EditNotesTabView {

    void display(DatasetNote[] notes);
    
    void doRefresh(DatasetNote[] notes);

    DatasetNote[] additions();

    void addNote(DatasetNote note);
    
    void observe(EditNotesTabPresenter presenter);

}
