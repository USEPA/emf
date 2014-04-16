package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyService;

import org.jmock.Mock;

public class ControlStrategiesManagerPresenterTest extends EmfMockObjectTestCase {

    public void testShouldDisplayBrowserOnDisplay() throws EmfException {
        Mock browser = mock(ControlStrategyManagerView.class);
        ControlStrategy[] controlStrategies = new ControlStrategy[0];
        expects(browser, 1, "display", eq(controlStrategies));

        Mock service = mock(ControlStrategyService.class);
        stub(service, "getControlStrategies", controlStrategies);

        Mock session = mock(EmfSession.class);
        stub(session, "controlStrategyService", service.proxy());

        ControlStrategiesManagerPresenter presenter = new ControlStrategiesManagerPresenterImpl((EmfSession) session.proxy(),
                (ControlStrategyManagerView) browser.proxy());
        expects(browser, 1, "observe", same(presenter));

        presenter.display();
    }

//    public void testShouldRefreshBrowserOnRefresh() throws EmfException {
//        Mock browser = mock(ControlStrategyManagerView.class);
//        ControlStrategy[] controlStrategies = new ControlStrategy[0];
//        expects(browser, 1, "refresh", eq(controlStrategies));
//
//        Mock service = mock(ControlStrategyService.class);
//        stub(service, "getControlStrategies", controlStrategies);
//
//        Mock session = mock(EmfSession.class);
//        stub(session, "controlStrategyService", service.proxy());
//
//        ControlStrategiesManagerPresenter presenter = new ControlStrategiesManagerPresenterImpl((EmfSession) session.proxy(),
//                (ControlStrategyManagerView) browser.proxy());
//
//        presenter.doRefresh();
//    }

    public void testShouldCloseViewOnClose() {
        Mock browser = mock(ControlStrategyManagerView.class);
        expects(browser, 1, "disposeView");

        ControlStrategiesManagerPresenter presenter = new ControlStrategiesManagerPresenterImpl(null, (ControlStrategyManagerView) browser.proxy());

        presenter.doClose();
    }
}
