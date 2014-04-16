package gov.epa.emissions.framework.client.data.viewer;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.notes.NewNoteView;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Note;
import gov.epa.emissions.framework.services.data.NoteType;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.editor.DataViewService;

import org.jmock.Mock;
import org.jmock.core.Constraint;
import org.jmock.core.constraint.HasPropertyWithValue;
import org.jmock.core.constraint.IsInstanceOf;

public class DataViewPresenterTest extends EmfMockObjectTestCase {

    public void testShouldLoadTablesOfDatasetOnDisplay() throws Exception {
        Version version = new Version();
        String table = "table";
        version.markFinal();

        Mock service = mock(DataViewService.class);
        Constraint constraint = tokenConstraint(version, table);
        service.expects(once()).method("openSession").with(constraint);
        TableMetadata tableMetadata = new TableMetadata();
        stub(service, "getTableMetadata", tableMetadata);

        DataViewService serviceProxy = (DataViewService) service.proxy();

        Mock view = mock(DataView.class);
        view.expects(once()).method("display").with(eq(version), eq(table), same(tableMetadata));

        EmfSession session = session(serviceProxy, null);
        DataViewPresenter p = new DataViewPresenter(null, version, table, (DataView) view.proxy(), session);
        view.expects(once()).method("observe").with(same(p));

        p.display();
    }

    public void testShouldDisplayTablePresenterOnDisplayTable() throws Exception {
        DataViewPresenter p = new DataViewPresenter(null, null, null, null, null);

        Mock tablePresenter = mock(TablePresenter.class);
        expects(tablePresenter, 1, "display");
        
        p.displayTable((TablePresenter) tablePresenter.proxy());
    }

    private EmfSession session(DataViewService viewService, DataCommonsService commons) {
        Mock session = mock(EmfSession.class);
        session.stubs().method("dataViewService").will(returnValue(viewService));
        session.stubs().method("dataCommonsService").will(returnValue(commons));

        return (EmfSession) session.proxy();
    }

    public void testShouldCloseViewAndCloseDataEditSessionOnClose() throws Exception {
        Mock view = mock(DataView.class);
        view.expects(once()).method("disposeView").withNoArguments();

        Version version = new Version();
        String table = "table";

        Mock service = mock(DataViewService.class);
        Constraint constraint = tokenConstraint(version, table);
        service.expects(once()).method("closeSession").with(constraint);

        EmfSession session = session((DataViewService) service.proxy(), null);
        DataViewPresenter p = new DataViewPresenter(null, version, table, (DataView) view.proxy(), session);

        p.doClose();
    }

    private Constraint tokenConstraint(Version version, String table) {
        Constraint propertyConstraint = and(new HasPropertyWithValue("version", same(version)),
                new HasPropertyWithValue("table", eq(table)));
        Constraint constraint = and(new IsInstanceOf(DataAccessToken.class), propertyConstraint);
        return constraint;
    }

    public void testShouldRunNewNoteViewAndAddNoteOnAddNote() throws Exception {
        Note note = new Note();
        NoteType[] types = new NoteType[0];
        Version[] versions = new Version[0];
        User user = new User();
        Note[] notes = {};

        Mock service = mock(DataCommonsService.class);
        service.expects(once()).method("addNote").with(same(note));
        service.stubs().method("getNotes").will(returnValue(notes));

        EmfSession session = session(null, (DataCommonsService) service.proxy());
        Version version = new Version();
        DataViewPresenter presenter = new DataViewPresenter(null, version, null, null, session);

        Mock view = mock(NewNoteView.class);
        EmfDataset dataset = new EmfDataset();
        Constraint[] constraints = { same(user), same(dataset), same(version), same(notes), same(types), same(versions) };
        view.stubs().method("display").with(constraints);
        view.stubs().method("shouldCreate").will(returnValue(Boolean.TRUE));
        view.stubs().method("note").will(returnValue(note));

        presenter.addNote((NewNoteView) view.proxy(), user, dataset, types, versions);
    }

}
