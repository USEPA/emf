package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.data.datasettype.DatasetTypesManagerPresenter;
import gov.epa.emissions.framework.client.data.datasettype.DatasetTypesManagerView;
import gov.epa.emissions.framework.client.data.datasettype.EditableDatasetTypePresenter;
import gov.epa.emissions.framework.client.data.datasettype.ViewableDatasetTypePresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;

import org.jmock.Mock;

public class DatasetTypesManagerPresenterTest extends EmfMockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws Exception {
        Mock view = mock(DatasetTypesManagerView.class);
        DatasetType[] types = {};
        view.expects(once()).method("display").with(same(types));

        Mock service = mock(DataCommonsService.class);
        stub(service, "getDatasetTypes", types);
        DataCommonsService serviceProxy = (DataCommonsService) service.proxy();

        Mock session = mock(EmfSession.class);
        session.stubs().method("dataCommonsService").withNoArguments().will(returnValue(serviceProxy));

        DatasetTypesManagerPresenter p = new DatasetTypesManagerPresenter((EmfSession) session.proxy(),
                (DatasetTypesManagerView) view.proxy());
        view.expects(once()).method("observe").with(eq(p));

        p.doDisplay();
    }

    public void testShouldCloseViewOnClose() throws Exception {
        Mock view = mock(DatasetTypesManagerView.class);
        view.expects(once()).method("disposeView").withNoArguments();

        DatasetTypesManagerPresenter p = new DatasetTypesManagerPresenter(null, (DatasetTypesManagerView) view.proxy());

        p.doClose();
    }

    public void testShouldDisplayEditableOnEdit() throws Exception {
        DatasetTypesManagerPresenter p = new DatasetTypesManagerPresenter(null, null);

        Mock presenter = mock(EditableDatasetTypePresenter.class);
        presenter.expects(once()).method("doDisplay").withNoArguments();

        p.edit((EditableDatasetTypePresenter) presenter.proxy());
    }

    public void testShouldShowViewableOnView() throws Exception {
        DatasetType type = new DatasetType();
        type.setName("name");

        DatasetTypesManagerPresenter p = new DatasetTypesManagerPresenter(null, null);

        Mock presenter = mock(ViewableDatasetTypePresenter.class);
        presenter.expects(once()).method("doDisplay").withNoArguments();

        p.view((ViewableDatasetTypePresenter) presenter.proxy());
    }

    public void testShouldRefreshViewOnClickOfRefreshButton() throws EmfException {
        Mock view = mock(DatasetTypesManagerView.class);
        DatasetType[] types = {};
        view.expects(once()).method("display").with(same(types));

        Mock service = mock(DataCommonsService.class);
        stub(service, "getDatasetTypes", types);
        DataCommonsService serviceProxy = (DataCommonsService) service.proxy();

        Mock session = mock(EmfSession.class);
        session.stubs().method("dataCommonsService").withNoArguments().will(returnValue(serviceProxy));

        DatasetTypesManagerPresenter p = new DatasetTypesManagerPresenter((EmfSession) session.proxy(),
                (DatasetTypesManagerView) view.proxy());
        view.expects(once()).method("observe").with(eq(p));

        view.expects(once()).method("refresh").with(eq(types));

        p.doDisplay();
        p.doRefresh();
    }
}
