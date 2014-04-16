package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.logs.LogsTabPresenter;
import gov.epa.emissions.framework.client.meta.logs.LogsTabView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.AccessLog;
import gov.epa.emissions.framework.services.basic.LoggingService;
import gov.epa.emissions.framework.services.data.EmfDataset;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class LogsTabPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws EmfException {
        Mock view = mock(LogsTabView.class);
        EmfDataset dataset = new EmfDataset();
        dataset.setId(6);

        Mock loggingServices = mock(LoggingService.class);
        Mock session = mock (EmfSession.class);
        AccessLog[] accessLogs = new AccessLog[0];
        loggingServices.expects(once()).method("getAccessLogs").with(eq(dataset.getId())).will(returnValue(accessLogs));

        LogsTabPresenter presenter = new LogsTabPresenter((LogsTabView) view.proxy(), dataset,
                (EmfSession) session.proxy());

        view.expects(once()).method("display").with(eq(accessLogs));

        presenter.display();
    }

    public void testShouldDoNothingOnSave() {
        LogsTabPresenter presenter = new LogsTabPresenter(null, null, null);

        presenter.doSave();
    }

}
