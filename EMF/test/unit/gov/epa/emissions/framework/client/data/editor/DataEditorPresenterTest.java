package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.notes.NewNoteView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DatasetNote;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Note;
import gov.epa.emissions.framework.services.data.NoteType;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.editor.DataEditorService;

import java.util.Date;

import org.jmock.Mock;
import org.jmock.core.Constraint;
import org.jmock.core.constraint.HasPropertyWithValue;
import org.jmock.core.constraint.IsInstanceOf;
import org.jmock.core.matcher.InvokeCountMatcher;

public class DataEditorPresenterTest extends EmfMockObjectTestCase {

    public void testShouldLoadTablesOfDatasetOnDisplay() throws Exception {
        Version version = new Version();
        String table = "table";

        Mock service = mock(DataEditorService.class);
        Constraint constraint = tokenConstraint(version, table);
        DataAccessToken token = successToken();
        User user = new User();
        service.expects(once()).method("openSession").with(same(user), constraint).will(returnValue(token));
        TableMetadata tableMetaData = new TableMetadata();
        service.stubs().method("getTableMetadata").will(returnValue(tableMetaData));

        DataEditorService serviceProxy = (DataEditorService) service.proxy();

        Mock commonsService = mock(DataCommonsService.class);
        Note[] notes = new Note[0];
        stub(commonsService, "getNotes", notes);

        Mock view = mock(DataEditorView.class);
        Constraint[] constraints = { eq(version), eq(table), same(user), same(tableMetaData), same(notes) };
        view.expects(once()).method("display").with(constraints);
        view.expects(once()).method("updateLockPeriod")
                .with(new IsInstanceOf(Date.class), new IsInstanceOf(Date.class));

        EmfSession session = session(user, serviceProxy, (DataCommonsService) commonsService.proxy());
        DataEditorPresenter p = new DataEditorPresenterImpl(new EmfDataset(), version, table, session);
        view.expects(once()).method("observe").with(same(p));

        p.display((DataEditorView) view.proxy());
    }

    private EmfSession session(User user, DataEditorService editor, DataCommonsService commons) {
        Mock session = mock(EmfSession.class);
        session.stubs().method("dataEditorService").will(returnValue(editor));
        session.stubs().method("dataCommonsService").will(returnValue(commons));
        session.stubs().method("user").will(returnValue(user));

        return (EmfSession) session.proxy();
    }

    public void testShouldAbortWithNotificationIfUnableToObtainLockOnDisplay() throws Exception {
        Version version = new Version();
        String table = "table";

        Mock service = mock(DataEditorService.class);
        DataAccessToken failureToken = failureToken();
        User user = new User();
        service.expects(once()).method("openSession").with(same(user), new IsInstanceOf(DataAccessToken.class)).will(
                returnValue(failureToken));

        DataEditorService serviceProxy = (DataEditorService) service.proxy();

        Mock view = mock(DataEditorView.class);
        view.expects(once()).method("notifyLockFailure");

        EmfSession session = session(user, serviceProxy, null);
        DataEditorPresenter p = new DataEditorPresenterImpl(null, version, table, session);

        p.display((DataEditorView) view.proxy());
    }

    private DataAccessToken successToken() {
        Mock mock = mock(DataAccessToken.class);
        mock.stubs().method("isLocked").will(returnValue(Boolean.TRUE));
        mock.stubs().method("lockStart").will(returnValue(new Date()));
        mock.stubs().method("lockEnd").will(returnValue(new Date()));

        return (DataAccessToken) mock.proxy();
    }

    private DataAccessToken failureToken() {
        Mock mock = mock(DataAccessToken.class);
        mock.stubs().method("isLocked").will(returnValue(Boolean.FALSE));

        return (DataAccessToken) mock.proxy();
    }

    public void testShouldDiscardChangesOnDiscard() throws Exception {
        Mock service = mock(DataEditorService.class);
        DataAccessToken token = new DataAccessToken();
        service.expects(once()).method("discard").with(same(token));

        DataEditorPresenterImpl p = new DataEditorPresenterImpl(null, null, null, null);
        Mock tablePresenter = mock(EditableTablePresenter.class);
        tablePresenter.expects(once()).method("reloadCurrent");

        p.discard((DataEditorService) service.proxy(), token, (EditableTablePresenter) tablePresenter.proxy());
    }

    public void testShouldDisplayTableViewOnDisplayTableView() throws Exception {
        DataEditorPresenterImpl p = new DataEditorPresenterImpl(null, null, null, null);
        Mock tablePresenter = mock(EditableTablePresenter.class);
        tablePresenter.expects(once()).method("display");

        p.displayTable((EditableTablePresenter) tablePresenter.proxy());
    }

    public void testShouldSubmitAnyChangesAndSaveChangesOnSave() throws Exception {
        Mock view = mock(DataEditorView.class);
        expects(view,1,"updateLockPeriod");
        expects(view,1,"resetChanges");
        expects(view,1,"disableSaveDiscard");
        
        EmfDataset dataset = new EmfDataset();

        Mock service = mock(DataEditorService.class);
        DataAccessToken token = new DataAccessToken();
        Version version = new Version();
        token.setVersion(version);
        service.expects(once()).method("save").with(same(token), same(dataset),same(version)).will(returnValue(token));
        DataEditorService serviceProxy = (DataEditorService) service.proxy();

        DataEditorView viewProxy = (DataEditorView) view.proxy();
        Mock tablePresenter = mock(EditableTablePresenter.class);
        expects(tablePresenter, "reloadCurrent");
        tablePresenter.expects(new InvokeCountMatcher(1)).method("submitChanges").will(returnValue(true));

        EditableTablePresenter tablePresenterProxy = (EditableTablePresenter) tablePresenter.proxy();

        DataEditorPresenterImpl p = new DataEditorPresenterImpl(dataset, version, null, null);
        p.save(tablePresenterProxy, null);

        assertTrue("Changes should be saved on save", p.areChangesSaved());
    }

    //not sure discard should be call when save failed
    public void FIXME_testOnSaveShouldDiscardChangesCloseSessionAndNotifyUserOfFailureIfSaveFails() throws Exception {
        Mock view = mock(DataEditorView.class);
        view.expects(once()).method("notifySaveFailure").with(eq("Failure"));
        
        EmfDataset dataset = new EmfDataset();

        Mock service = mock(DataEditorService.class);
        Version version = new Version();
        service.expects(once()).method("save").with(new IsInstanceOf(DataAccessToken.class), same(dataset),same(version)).will(
                throwException(new EmfException("Failure")));

        DataAccessToken token = new DataAccessToken(version,"table");
        service.expects(once()).method("discard").with(same(token));

        DataEditorView viewProxy = (DataEditorView) view.proxy();
        Mock tablePresenter = mock(EditableTablePresenter.class);
        tablePresenter.expects(once()).method("reloadCurrent");
        tablePresenter.expects(new InvokeCountMatcher(1)).method("submitChanges").will(returnValue(true));
        EditableTablePresenter tablePresenterProxy = (EditableTablePresenter) tablePresenter.proxy();

        DataEditorService serviceProxy = (DataEditorService) service.proxy();

        Mock closingRule = mock(ClosingRule.class);
        closingRule.expects(once()).method("proceedWithClose");
        ClosingRule closingRuleProxy = (ClosingRule) closingRule.proxy();

        DataEditorPresenterImpl p = new DataEditorPresenterImpl(dataset, version, null, null);
        p.save(tablePresenterProxy, closingRuleProxy);

        assertFalse("Changes should not be saved on discard", p.areChangesSaved());
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
        DatasetNote[] notes = {};

        Mock service = mock(DataCommonsService.class);
        service.expects(once()).method("addNote").with(same(note));

        EmfSession session = session(user, null, (DataCommonsService) service.proxy());
        Version version = new Version();
        DataEditorPresenterImpl presenter = new DataEditorPresenterImpl(null, version, null, session);

        Mock view = mock(NewNoteView.class);
        EmfDataset dataset = new EmfDataset();
        Constraint[] constraints = { same(user), same(dataset), same(version), same(notes), same(types), same(versions) };
        view.stubs().method("display").with(constraints);
        view.stubs().method("shouldCreate").will(returnValue(Boolean.TRUE));
        view.stubs().method("note").will(returnValue(note));

        presenter.addNote((NewNoteView) view.proxy(), user, dataset, notes, types, versions);
    }

    public void testShouldCloseWithChangedSavedOnClose() throws Exception {
        Mock service = mock(DataCommonsService.class);

        DataCommonsService serviceProxy = (DataCommonsService) service.proxy();
        EmfSession session = session(null, null, serviceProxy);
        DataEditorPresenterImpl presenter = new DataEditorPresenterImpl(null, null, null, session);

        Mock closingRule = mock(ClosingRule.class);
        closingRule.expects(once()).method("close").with(eq(Boolean.TRUE));

        presenter.close((ClosingRule) closingRule.proxy(), true);
    }

}
