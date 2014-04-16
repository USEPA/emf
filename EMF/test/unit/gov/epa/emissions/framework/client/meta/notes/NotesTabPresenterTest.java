package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DatasetNote;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Note;

import org.jmock.Mock;

public class NotesTabPresenterTest extends EmfMockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws Exception {
        Note[] notes = new Note[0];
        Mock view = mock(NotesTabView.class);

        Mock service = mock(DataCommonsService.class);
        EmfDataset dataset = new EmfDataset();
        dataset.setId(2);
        service.stubs().method("getNotes").with(eq(dataset.getId())).will(returnValue(notes));

        NotesTabPresenter presenter = new NotesTabPresenter(dataset, (DataCommonsService) service.proxy());
        view.expects(once()).method("display").with(eq(notes), same(presenter));

        presenter.display((NotesTabView) view.proxy());
    }

    public void testShouldDisplayNoteViewOnDisplayNote() throws Exception {
        NotesTabPresenter presenter = new NotesTabPresenter(null, null);

        DatasetNote dsNote = new DatasetNote();
        Mock view = mock(NoteView.class);
        view.expects(once()).method("display").with(same(dsNote));
        
        presenter.doViewNote(dsNote, (NoteView)view.proxy());
    }

}
