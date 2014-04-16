package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.data.editor.DataEditorPresenter;
import gov.epa.emissions.framework.client.data.viewer.DataView;
import gov.epa.emissions.framework.client.data.viewer.DataViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.editor.DataEditorService;
import gov.epa.emissions.framework.services.editor.DataViewService;

import org.jmock.Mock;
import org.jmock.core.constraint.IsInstanceOf;

public class EditVersionsPresenterTest extends EmfMockObjectTestCase {

    public void testShouldDisplayTableViewOnView() throws Exception {
        Version version = new Version();
        String table = "table";
        version.markFinal();

        Mock service = mock(DataViewService.class);
        service.expects(once()).method("openSession").withAnyArguments();
        TableMetadata tableMetadata = new TableMetadata();
        stub(service, "getTableMetadata", tableMetadata);
        DataViewService serviceProxy = (DataViewService) service.proxy();

        Mock dataView = mock(DataView.class);
        dataView.expects(once()).method("display").with(same(version), eq(table), same(tableMetadata));
        dataView.expects(once()).method("observe").with(new IsInstanceOf(DataViewPresenter.class));

        EmfSession session = session(null, null, serviceProxy);
        EditVersionsPresenter presenter = new EditVersionsPresenter(null, session);
        presenter.doView(version, table, (DataView) dataView.proxy());
    }

    private EmfSession session(User user, DataEditorService editor, DataViewService view) {
        Mock session = mock(EmfSession.class);
        session.stubs().method("dataEditorService").will(returnValue(editor));
        session.stubs().method("dataViewService").will(returnValue(view));
        session.stubs().method("user").will(returnValue(user));

        return (EmfSession) session.proxy();
    }

    public void testShouldRaiseErrorWhenAttemptedToViewNonFinalVersionOnDisplay() throws Exception {
        Version version = new Version();

        EditVersionsPresenter presenter = new EditVersionsPresenter(null, null);

        try {
            presenter.doView(version, null, null);
        } catch (EmfException e) {
            return;
        }

        fail("Should have raised an error if user attempts to view a non-final version");
    }

    public void testShouldDisplayEditableTableViewOnEdit() throws Exception {
        Version version = new Version();

        EditVersionsPresenter presenter = new EditVersionsPresenter(null, null);

        Mock editorPresenter = mock(DataEditorPresenter.class);
        expects(editorPresenter, 1, "display");

        presenter.edit(version, null, (DataEditorPresenter) editorPresenter.proxy());
    }

    public void testShouldRaiseErrorOnEditWhenVersionIsFinal() throws Exception {
        Version version = new Version();
        version.markFinal();

        EditVersionsPresenter presenter = new EditVersionsPresenter(null, null);

        try {
            presenter.edit(version, null, null);
        } catch (EmfException e) {
            assertEquals("Cannot edit a Version that is Final. Please choose View for Version "+
                    version.getName(), e.getMessage());
            return;
        }

        fail("Should have failed to edit a Version that is already Final.");
    }

    public void testShouldDeriveNewVersionOnNew() throws Exception {
        Version version = new Version();
        Version derived = new Version();
        String derivedName = "name";

        Mock service = mock(DataEditorService.class);
        User user = new User();
        service.expects(once()).method("derive").with(same(version),same(user), eq(derivedName)).will(returnValue(derived));

        Mock view = mock(EditVersionsView.class);
        view.expects(once()).method("add").with(same(derived));

        EditVersionsPresenter presenter = displayPresenter(service,user, view);

        presenter.doNew(version, derivedName);
    }

    private EditVersionsPresenter displayPresenter(Mock service, User user,Mock view) throws EmfException {
        EmfDataset dataset = new EmfDataset();
        dataset.setId(1);
        Version[] versions = new Version[0];
        InternalSource[] internalSources = new InternalSource[0];

        service.stubs().method("getVersions").with(eq(new Integer(dataset.getId()))).will(returnValue(versions));
        EmfSession session = session(user, (DataEditorService) service.proxy(), null);
        EditVersionsPresenter presenter = new EditVersionsPresenter(dataset, session);
        view.expects(once()).method("observe").with(same(presenter));
        view.expects(once()).method("display").with(eq(versions), eq(internalSources));

        presenter.display((EditVersionsView) view.proxy());

        return presenter;
    }

    public void testShouldObserveAndDisplayViewOnDisplay() throws Exception {
        Mock service = mock(DataEditorService.class);
        Mock view = mock(EditVersionsView.class);

        displayPresenter(service,null, view);
    }

    public void testShouldMarkVersionAsFinalOnMarkFinal() throws Exception {
        Version version = new Version();
        version.setVersion(8);

        Mock service = mock(DataEditorService.class);
        service.expects(once()).method("markFinal").with(new IsInstanceOf(DataAccessToken.class)).will(
                returnValue(new Version()));

        Version[] versions = {};

        Mock view = mock(EditVersionsView.class);
        view.expects(once()).method("reload").with(eq(versions));

        EditVersionsPresenter p = displayPresenter(service,null, view);

        p.doMarkFinal(new Version[] { version });
    }

    public void testShouldRaiseErrorOnMarkFinalWhenVersionIsAlreadyFinal() throws Exception {
        Version version = new Version();
        version.setVersion(2);
        version.markFinal();

        EditVersionsPresenter p = new EditVersionsPresenter(null, null);

        try {
            p.doMarkFinal(new Version[] { version });
        } catch (EmfException e) {
            assertEquals("Version: " + version.getName()+
                    " is already Final. It cannot be marked as final again.", 
                    e.getMessage());
            return;
        }

        fail("Should have failed to mark Final when Version is already Final");
    }

}
