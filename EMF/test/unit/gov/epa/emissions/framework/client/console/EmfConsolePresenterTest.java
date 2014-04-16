package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.client.console.EmfConsolePresenter;
import gov.epa.emissions.framework.client.console.EmfConsoleView;
import gov.epa.emissions.framework.services.EmfException;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class EmfConsolePresenterTest extends MockObjectTestCase {

    private Mock view;

    private EmfConsolePresenter presenter;

    protected void setUp() {
        view = mock(EmfConsoleView.class);

        presenter = new EmfConsolePresenter();
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").withNoArguments();
    }

    public void testShouldSetAsObserverOnObserve() {
        presenter.display((EmfConsoleView) view.proxy());
    }

    public void testShouldDisplayUserManagerOnNotifyManagerUsers() throws EmfException {
        presenter.display((EmfConsoleView) view.proxy());

        view.expects(once()).method("displayUserManager").withNoArguments();

        presenter.notifyManageUsers();
    }

}
