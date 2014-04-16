package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class ControlMeasuresManagerPresenterTest extends MockObjectTestCase {

    private Mock view;

    private ControlMeasuresManagerPresenter presenter;

    private Mock controlMeasureService;

    private Mock serviceLocator;

    private Mock session;

    
    protected void setUp() {
        view = mock(ControlMeasuresManagerView.class);

        controlMeasureService = mock(ControlMeasureService.class);
        serviceLocator = mock(ServiceLocator.class);
        serviceLocator.stubs().method("controlMeasureService").withNoArguments().will(returnValue(controlMeasureService.proxy()));

        session = mock(EmfSession.class);
        session.stubs().method("user").withNoArguments().will(returnValue(new User()));

        presenter = new ControlMeasuresManagerPresenter((EmfSession) session.proxy());

        ControlMeasure[] measures = new ControlMeasure[0];
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(eq(measures));

        try {
            presenter.doDisplay((ControlMeasuresManagerView) view.proxy());
        } catch (EmfException e) {
            return;
        }
    }

    public void testShouldCloseViewOnClickOfCloseButton() {
        view.expects(once()).method("disposeView").withNoArguments();

        presenter.doClose();
    }

    public void testShouldRefreshViewOnClickOfRefreshButton() throws EmfException {
        ControlMeasure[] measures = new ControlMeasure[0];
        controlMeasureService.stubs().method("getMeasures").withNoArguments().will(returnValue(measures));

        view.expects(once()).method("refresh").with(eq(measures));

        Mock session = mock(EmfSession.class);
        session.stubs().method("controlMeasureService").will(returnValue(controlMeasureService.proxy()));

        ControlMeasuresManagerPresenter presenter = new ControlMeasuresManagerPresenter((EmfSession) session.proxy());
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(eq(measures));
        view.expects(once()).method("clearMessage").withNoArguments();

        presenter.doDisplay((ControlMeasuresManagerView) view.proxy());

        presenter.doRefresh();
    }

}
