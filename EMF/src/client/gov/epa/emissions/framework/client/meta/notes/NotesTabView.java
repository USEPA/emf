package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.framework.services.data.DatasetNote;

public interface NotesTabView {

    void display(DatasetNote[] notes, NotesTabPresenter presenter);
    void doRefresh(DatasetNote[] notes);

}
