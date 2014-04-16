package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.ui.Position;

import org.jmock.Mock;

public class DesktopManagerTest extends EmfMockObjectTestCase {

    public void testShouldRegisterOpenWindowWithWindowMenu() {
        Mock windowsMenu = mock(WindowMenuView.class);
        Mock managedView = manageView("view");
        Mock emfConsole = emfConsole();

        windowsMenu.expects(once()).method("register").with(same(managedView.proxy()));

        Mock desktop = mock(EmfDesktop.class);
        expects(desktop, 1, "add");

        DesktopManager desktopManager = new DesktopManagerImpl(((WindowMenuView) windowsMenu.proxy()),
                (EmfConsoleView) emfConsole.proxy(), (EmfDesktop) desktop.proxy());
        desktopManager.openWindow((ManagedView) (managedView.proxy()));
    }

    public void testShouldUnRegisterCloseWindowWithWindowMenu() {
        Mock windowsMenu = mock(WindowMenuView.class);
        Mock managedView = mock(ManagedView.class);

        ManagedView managedViewProxy = (ManagedView) managedView.proxy();
        managedView.expects(once()).method("getName").withNoArguments().will(returnValue("view"));
        windowsMenu.expects(once()).method("unregister").with(same(managedViewProxy));

        DesktopManager desktopManager = new DesktopManagerImpl(((WindowMenuView) windowsMenu.proxy()), null, null);
        desktopManager.closeWindow(managedViewProxy);
    }

    public void testShouldCloseAllWindowsAndUnRegisterFromWindowMenu() {
        Mock managedView1 = manageView("view1");
        Mock managedView2 = manageView("view2");
        managedView1.expects(once()).method("getPosition").withNoArguments().will(returnValue(new Position(0, 0)));
        setCloseRealatedExpections(managedView1);
        setCloseRealatedExpections(managedView2);

        Mock windowsMenu = windowsMenu();
        Mock emfConsole = emfConsole();
        emfConsole.expects(atLeastOnce()).method("confirm").withNoArguments().will(returnValue(true));

        Mock desktop = mock(EmfDesktop.class);
        expects(desktop, 2, "add");

        DesktopManager desktopManager = new DesktopManagerImpl(((WindowMenuView) windowsMenu.proxy()),
                (EmfConsoleView) emfConsole.proxy(), (EmfDesktop) desktop.proxy());
        desktopManager.openWindow((ManagedView) managedView1.proxy());
        desktopManager.openWindow((ManagedView) managedView2.proxy());

        desktopManager.closeAll();
    }

    public void testShouldAddViewToDesktopOnOpenWindow() {
        Mock managedView1 = manageView("view");
        ManagedView viewProxy = (ManagedView) managedView1.proxy();

        Mock windowsMenu = windowsMenu();
        Mock emfConsole = emfConsole();

        Mock desktop = mock(EmfDesktop.class);
        desktop.expects(once()).method("add").with(same(viewProxy));

        DesktopManager desktopManager = new DesktopManagerImpl(((WindowMenuView) windowsMenu.proxy()),
                (EmfConsoleView) emfConsole.proxy(), (EmfDesktop) desktop.proxy());
        desktopManager.openWindow(viewProxy);
    }

    public void testShouldDelegateEnsuringPresenceToDesktopOnEnsurePresence() {
        Mock managedView = mock(ManagedView.class);
        ManagedView viewProxy = (ManagedView) managedView.proxy();

        Mock desktop = mock(EmfDesktop.class);
        desktop.expects(once()).method("ensurePresence").with(same(viewProxy));

        DesktopManager desktopManager = new DesktopManagerImpl(null, null, (EmfDesktop) desktop.proxy());

        desktopManager.ensurePresence(viewProxy);
    }

    private void setCloseRealatedExpections(Mock managedView) {
        managedView.expects(atLeastOnce()).method("windowClosing").withNoArguments();
        managedView.expects(atLeastOnce()).method("hasChanges").withNoArguments().will(returnValue(false));
        managedView.expects(atLeastOnce()).method("resetChanges").withNoArguments();
    }

    private Mock emfConsole() {
        Mock console = mock(EmfConsoleView.class);
        console.expects(atLeastOnce()).method("width").withNoArguments().will(returnValue(0));
        console.expects(atLeastOnce()).method("height").withNoArguments().will(returnValue(0));
        return console;
    }

    private Mock windowsMenu() {
        Mock windowsMenu = mock(WindowMenuView.class);
        windowsMenu.expects(atLeastOnce()).method("register").with(isA(ManagedView.class));
        return windowsMenu;
    }

    private Mock manageView(String name) {
        Mock managedView = mock(ManagedView.class);
        managedView.expects(atLeastOnce()).method("getName").withNoArguments().will(returnValue(name));
        managedView.expects(atLeastOnce()).method("bringToFront").withNoArguments();
        managedView.expects(atLeastOnce()).method("width").withNoArguments().will(returnValue(0));
        managedView.expects(atLeastOnce()).method("height").withNoArguments().will(returnValue(0));
        managedView.expects(atLeastOnce()).method("setPosition").withAnyArguments();
        return managedView;
    }

}