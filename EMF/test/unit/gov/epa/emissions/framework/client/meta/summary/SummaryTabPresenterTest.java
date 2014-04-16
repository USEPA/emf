package gov.epa.emissions.framework.client.meta.summary;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.data.EmfDataset;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class SummaryTabPresenterTest extends MockObjectTestCase {

    public void testShouldNoOpOnDisplay() {
        Mock dataset = mock(EmfDataset.class);
        //dataset.expects(once()).method("setModifiedDateTime").with(new IsInstanceOf(Date.class));
        //dataset.expects(once()).method("getName").withNoArguments().will(returnValue(" "));
        
        Mock view = mock(SummaryTabView.class);
        Mock session = mock (EmfSession.class);
        SummaryTabPresenter presenter = new SummaryTabPresenter((SummaryTabView) view
                .proxy(), (EmfDataset) dataset.proxy(), (EmfSession) session.proxy() );
        presenter.display();
    }
}
