package gov.epa.emissions.framework.client.meta.info;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.DataTabPresenter;
import gov.epa.emissions.framework.client.meta.DataTabView;
import gov.epa.emissions.framework.client.meta.versions.VersionsView;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.DataViewService;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class DataTabPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayInternalSourcesAndVersionsOnDisplay() throws Exception {
        EmfDataset dataset = new EmfDataset();
        dataset.setId(1);
        dataset.addInternalSource(new InternalSource());

        DatasetType type = new DatasetType();
        dataset.setDatasetType(type);

        Mock view = mock(DataTabView.class);

        Mock editorService = mock(DataViewService.class);
        Version[] versions = new Version[0];
        editorService.stubs().method("getVersions").with(eq(Long.valueOf(1))).will(returnValue(versions));

        DataViewService serviceProxy = (DataViewService) editorService.proxy();
        view.expects(once()).method("display").with(same(dataset));

        DataTabPresenter presenter = new DataTabPresenter((DataTabView) view.proxy(), dataset, session(serviceProxy));
        view.expects(once()).method("observe").with(same(presenter));

        presenter.doDisplay();
    }

    private EmfSession session(DataViewService service) {
        Mock session = mock(EmfSession.class);
        session.stubs().method("dataViewService").will(returnValue(service));

        return (EmfSession) session.proxy();
    }

    public void testShouldDisplayVersionsPanelOnDisplayVersions() throws Exception {
        EmfDataset dataset = new EmfDataset();
        dataset.setId(1);
        dataset.addInternalSource(new InternalSource());

        DatasetType type = new DatasetType();
        dataset.setDatasetType(type);

        Mock view = mock(DataTabView.class);

        Mock editorService = mock(DataViewService.class);
        Version[] versions = new Version[0];
        editorService.stubs().method("getVersions").with(eq(Integer.valueOf(1))).will(returnValue(versions));

        DataViewService serviceProxy = (DataViewService) editorService.proxy();

        DataTabPresenter presenter = new DataTabPresenter((DataTabView) view.proxy(), dataset, session(serviceProxy));

        Mock versionsView = mock(VersionsView.class);
        versionsView.expects(once()).method("observe");
        versionsView.expects(once()).method("display");

        presenter.displayVersions((VersionsView) versionsView.proxy());
    }

    public void testShouldDoNothingOnSave() {
        DataTabPresenter presenter = new DataTabPresenter(null, null, null);
        presenter.doSave();
    }

}
