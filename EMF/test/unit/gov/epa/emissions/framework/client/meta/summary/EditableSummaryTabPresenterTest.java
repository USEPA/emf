package gov.epa.emissions.framework.client.meta.summary;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class EditableSummaryTabPresenterTest extends MockObjectTestCase {

    public void testUpdateDatasetOnSave() throws EmfException {
        Mock dataset = mock(EmfDataset.class);
        //dataset.expects(once()).method("setModifiedDateTime").with(new IsInstanceOf(Date.class));
        dataset.expects(once()).method("getName").withNoArguments().will(returnValue("test"));
        
        Mock view = mock(EditableSummaryTabView.class);
        Mock session = mock (EmfSession.class);
        Object datasetProxy = dataset.proxy();
        view.expects(once()).method("save").with(eq(datasetProxy));

        EditableSummaryTabPresenter presenter = new EditableSummaryTabPresenterImpl((EmfDataset) datasetProxy, (EditableSummaryTabView) view
                .proxy(), (EmfSession) session.proxy());

        presenter.doSave();
    }
    
    public void testShouldNotAllowEmptyNameOnSave() {
        Mock dataset = mock(EmfDataset.class);
        //dataset.expects(once()).method("setModifiedDateTime").with(new IsInstanceOf(Date.class));
        dataset.expects(once()).method("getName").withNoArguments().will(returnValue(" "));
        
        Mock view = mock(EditableSummaryTabView.class);
        Mock session = mock (EmfSession.class);
        Object datasetProxy = dataset.proxy();
        view.expects(once()).method("save").with(eq(datasetProxy));

        EditableSummaryTabPresenter presenter = new EditableSummaryTabPresenterImpl((EmfDataset) datasetProxy, (EditableSummaryTabView) view
                .proxy(), (EmfSession) session.proxy());

        try {
            presenter.doSave();
        } catch (EmfException e) {
            assertEquals("Name field should be a non-empty string.",e.getMessage());
        }
    }
}
