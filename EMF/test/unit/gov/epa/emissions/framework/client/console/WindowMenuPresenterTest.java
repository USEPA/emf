package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.client.ManagedView;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class WindowMenuPresenterTest extends MockObjectTestCase {

    private WindowMenuPresenter presenter;

    private ManagedView managedViewProxy;

    private Mock view;

    private Mock managedView;

    protected void setUp() {
        view = mock(WindowMenuView.class);
        presenter = new WindowMenuPresenter((WindowMenuView) view.proxy());
        managedView = mock(ManagedView.class);
        managedViewProxy = (ManagedView) managedView.proxy();
    }

    public void testShouldBringManagedViewToFrontOnBeingSelectedFromMenu() {
        managedView.expects(once()).method("bringToFront").withNoArguments();
        presenter.select(managedViewProxy);
    }

    public void testShouldCloseAllOnBeingSelectedFromMenu() {
        Mock mock = mock(DesktopManager.class);
        presenter.setDesktopManager((DesktopManager) mock.proxy());
        mock.expects(once()).method("closeAll").withNoArguments().will(returnValue(true));
        presenter.closeAll();
    }
}
