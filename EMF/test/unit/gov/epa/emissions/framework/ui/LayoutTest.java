package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.client.console.EmfConsoleView;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class LayoutTest extends MockObjectTestCase {

    public void testShouldAddOneViewAndLayoutTheView() {
        Position position = new Position(0, 0);
        ManagedView manageView = (ManagedView) manageView(position, 200, 200).proxy();
        EmfConsoleView emfConsoleView = emfConsoleProxy();
        Layout layout = new LayoutImpl(emfConsoleView);
        layout.add(manageView);
    }

    public void testShouldRemoveViewFromStackWhenRemoveIsCalled() {
        ManagedView manageView = (ManagedView) manageView(new Position(0, 0), 200, 200).proxy();
        EmfConsoleView emfConsoleView = emfConsoleProxy();

        Layout layout = new LayoutImpl(emfConsoleView);
        layout.add(manageView);
        layout.remove(manageView);
    }

    public void testShouldPositionWindowRelativeToTheLastWindowAdded() {
        ManagedView managedView1 = (ManagedView) manageViewWithGetPosition(new Position(0, 0), 200, 200).proxy();
        ManagedView managedView2 = (ManagedView) manageView(new Position(20, 20), 200, 200).proxy();

        EmfConsoleView emfConsoleView = emfConsoleProxy();

        Layout layout = new LayoutImpl(emfConsoleView);
        layout.add(managedView1);
        layout.add(managedView2);
    }

    public void testShouldPositionWindowToTheUpperLeftHandCornerIfWindowWidthDoesNotDisplayInsideConsoleView() {
        ManagedView managedView1 = (ManagedView) manageViewWithGetPosition(new Position(0, 0), 200, 200).proxy();
        ManagedView managedView2 = (ManagedView) manageView(new Position(0, 0), 700, 200).proxy();

        EmfConsoleView emfConsoleView = emfConsoleProxy();

        Layout layout = new LayoutImpl(emfConsoleView);
        layout.add(managedView1);
        layout.add(managedView2);
    }

    public void testShouldPositionWindowToTheUpperLeftHandCornerIfWindowHeightDoesNotDisplayInsideConsoleView() {
        ManagedView managedView1 = (ManagedView) manageViewWithGetPosition(new Position(0, 0), 200, 200).proxy();
        ManagedView managedView2 = (ManagedView) manageView(new Position(0, 0), 200, 500).proxy();

        EmfConsoleView emfConsoleView = emfConsoleProxy();

        Layout layout = new LayoutImpl(emfConsoleView);
        layout.add(managedView1);
        layout.add(managedView2);
    }

    private Mock manageView(Position position, int width, int height) {
        Mock managedView = mock(ManagedView.class);
        managedView.expects(atLeastOnce()).method("width").withNoArguments().will(returnValue(width));
        managedView.expects(atLeastOnce()).method("height").withNoArguments().will(returnValue(height));
        managedView.expects(atLeastOnce()).method("setPosition").with(eq(position));
        return managedView;
    }

    private Mock manageViewWithGetPosition(Position position, int width, int height) {
        Mock managedView = manageView(position, width, height);
        managedView.expects(once()).method("getPosition").will(returnValue(position));
        return managedView;
    }

    private EmfConsoleView emfConsoleProxy() {
        Mock console = mock(EmfConsoleView.class);
        console.expects(atLeastOnce()).method("width").withNoArguments().will(returnValue(700));
        console.expects(atLeastOnce()).method("height").withNoArguments().will(returnValue(500));
        return (EmfConsoleView) console.proxy();
    }

}
