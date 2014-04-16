package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DatasetNote;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Note;
import gov.epa.emissions.framework.services.data.NoteType;
import gov.epa.emissions.framework.services.editor.DataEditorService;

import org.jmock.Mock;
import org.jmock.core.Constraint;
import org.jmock.core.constraint.IsInstanceOf;

public class EditNotesTabPresenterTest extends EmfMockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws Exception {
        Mock view = mock(EditNotesTabView.class);

        EmfDataset dataset = new EmfDataset();
        dataset.setId(2);
        Note[] notes = new Note[0];
        Mock dataCommons = mock(DataCommonsService.class);
        dataCommons.stubs().method("getNotes").with(eq(dataset.getId())).will(returnValue(notes));

        EmfSession session = session(null, (DataCommonsService) dataCommons.proxy(), null);
        EditNotesTabPresenter presenter = new EditNotesTabPresenterImpl(dataset, session, (EditNotesTabView) view
                .proxy());
        view.expects(once()).method("display").with(same(notes), same(presenter));

        presenter.display();
    }

    private EmfSession session(User user, DataCommonsService dataCommons, DataEditorService dataEditor) {
        Mock session = mock(EmfSession.class);
        session.stubs().method("dataCommonsService").will(returnValue(dataCommons));
        session.stubs().method("dataEditorService").will(returnValue(dataEditor));
        session.stubs().method("user").will(returnValue(user));

        return (EmfSession) session.proxy();
    }

    public void testShouldAddNoteOnSave() throws Exception {
        Mock service = mock(DataCommonsService.class);
        Note note = new Note();
        service.expects(once()).method("addNote").with(same(note));

        Mock view = mock(EditNotesTabView.class);
        Note[] notes = new Note[] { note };
        view.expects(once()).method("additions").will(returnValue(notes));

        EditNotesTabView viewProxy = (EditNotesTabView) view.proxy();
        EmfDataset dataset = new EmfDataset();
        dataset.setId(2);

        User user = new User();
        EmfSession session = session(user, (DataCommonsService) service.proxy(), null);

        EditNotesTabPresenter presenter = new EditNotesTabPresenterImpl(dataset, session, viewProxy);
        presenter.doSave();
    }

    public void testShouldAddNoteToViewOnAddNote() throws Exception {
        NoteType[] types = new NoteType[0];
        Version[] versions = new Version[0];
        Note note = new Note();
        DatasetNote[] notes = {};

        EmfDataset dataset = new EmfDataset();
        dataset.setId(2);
        User user = new User();
        EmfSession session = session(user, null, null);

        Mock view = mock(EditNotesTabView.class);
        expects(view, 1, "addNote", same(note));
        stub(view, "additions", new Note[0]);

        EditNotesTabPresenterImpl presenter = new EditNotesTabPresenterImpl(dataset, session, (EditNotesTabView) view
                .proxy());

        Mock newNoteView = mock(NewNoteView.class);
        Constraint[] constraints = { same(user), same(dataset), new IsInstanceOf(new Note[0].getClass()), same(types),
                same(versions) };
        newNoteView.stubs().method("display").with(constraints);
        newNoteView.stubs().method("shouldCreate").will(returnValue(Boolean.TRUE));
        newNoteView.stubs().method("note").will(returnValue(note));

        presenter.addDatasetNote((NewNoteView) newNoteView.proxy(), user, dataset, notes, types, versions);
    }
    
    public void testShouldDisplayNoteOnViewNote() throws Exception {
        DatasetNote note = new DatasetNote();
        
        EmfDataset dataset = new EmfDataset();
        dataset.setId(2);
        User user = new User();
        EmfSession session = session(user, null, null);
        
        Mock view = mock(NoteView.class);
        expects(view, 1, "display", same(note));
        
        Mock view2 = mock(EditNotesTabView.class);
        
        EditNotesTabPresenterImpl presenter = new EditNotesTabPresenterImpl(dataset, session, (EditNotesTabView) view2
                .proxy());
        
        presenter.doViewNote(note, (NoteView)view.proxy());
    }
}
