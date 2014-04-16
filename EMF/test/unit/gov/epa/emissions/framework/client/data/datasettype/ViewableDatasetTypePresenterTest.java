package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.client.data.datasettype.ViewableDatasetTypePresenter;
import gov.epa.emissions.framework.client.data.datasettype.ViewableDatasetTypePresenterImpl;
import gov.epa.emissions.framework.client.data.datasettype.ViewableDatasetTypeView;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class ViewableDatasetTypePresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws Exception {
        DatasetType type = new DatasetType();


        Mock view = mock(ViewableDatasetTypeView.class);
        ViewableDatasetTypePresenter presenter = new ViewableDatasetTypePresenterImpl((ViewableDatasetTypeView) view
                .proxy(), type);
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(same(type));

        presenter.doDisplay();
    }

    public void testShouldCloseViewOnClose() {
        Mock view = mock(ViewableDatasetTypeView.class);
        view.expects(once()).method("disposeView");

        ViewableDatasetTypePresenter presenter = new ViewableDatasetTypePresenterImpl((ViewableDatasetTypeView) view
                .proxy(), null);

        presenter.doClose();
    }

}
