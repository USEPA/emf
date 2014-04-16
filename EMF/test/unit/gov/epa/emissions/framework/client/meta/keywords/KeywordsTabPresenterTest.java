package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.framework.services.data.EmfDataset;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class KeywordsTabPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() {
        KeyVal[] values = new KeyVal[] { new KeyVal(), new KeyVal() };
        Mock view = mock(KeywordsTabView.class);
        view.expects(once()).method("display").with(eq(values));
        
        DatasetType datasetType = new DatasetType();
        Mock dataset = mock(EmfDataset.class);
        dataset.stubs().method("getKeyVals").will(returnValue(values));
        dataset.stubs().method("getDatasetType").will(returnValue(datasetType));

        KeywordsTabPresenter presenter = new KeywordsTabPresenter(((KeywordsTabView) view.proxy()),
                (EmfDataset) dataset.proxy(), null);

        presenter.display();
    }

}
