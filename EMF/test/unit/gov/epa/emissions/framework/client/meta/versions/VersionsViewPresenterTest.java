package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.data.viewer.DataView;
import gov.epa.emissions.framework.client.data.viewer.DataViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.DataViewService;

import org.jmock.Mock;
import org.jmock.core.constraint.IsInstanceOf;

public class VersionsViewPresenterTest extends EmfMockObjectTestCase {

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

        VersionsViewPresenter presenter = new VersionsViewPresenter(null, session(serviceProxy));
        presenter.doView(version, table, (DataView) dataView.proxy());
    }

    private EmfSession session(DataViewService service) {
        Mock session = mock(EmfSession.class);
        session.stubs().method("dataViewService").will(returnValue(service));

        return (EmfSession) session.proxy();
    }

    public void testShouldRaiseErrorWhenAttemptedToViewNonFinalVersionOnDisplay() throws Exception {
        Version version = new Version();
        VersionsViewPresenter presenter = new VersionsViewPresenter(null, null);

        try {
            presenter.doView(version, null, null);
        } catch (EmfException e) {
            return;
        }

        fail("Should have raised an error if user attempts to view a non-final version");
    }

    private VersionsViewPresenter displayPresenter(Mock service, Mock view) throws EmfException {
        EmfDataset dataset = new EmfDataset();
        dataset.setId(1);
        Version[] versions = new Version[0];
        InternalSource[] internalSources = new InternalSource[0];

        service.stubs().method("getVersions").with(eq(new Integer(dataset.getId()))).will(returnValue(versions));

        VersionsViewPresenter presenter = new VersionsViewPresenter(dataset, session((DataViewService) service.proxy()));
        view.expects(once()).method("observe").with(same(presenter));
        view.expects(once()).method("display").with(eq(versions), eq(internalSources));

        presenter.display((VersionsView) view.proxy());

        return presenter;
    }

    public void testShouldObserveAndDisplayViewOnDisplay() throws Exception {
        Mock service = mock(DataViewService.class);
        Mock view = mock(VersionsView.class);

        displayPresenter(service, view);
    }

}
