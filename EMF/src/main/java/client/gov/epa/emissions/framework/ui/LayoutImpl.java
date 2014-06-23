package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.client.console.EmfConsoleView;

import java.util.EmptyStackException;
import java.util.Stack;

public class LayoutImpl implements Layout {

    private EmfConsoleView console;

    private Stack managedViews;

    public LayoutImpl(EmfConsoleView console) {
        this.console = console;
        managedViews = new Stack();
    }

    public void add(ManagedView view) {
        Position lastViewPosition = lastViewPosition();
        Position currentViewPosition = cascade(lastViewPosition);
        if (checkForFullWindowDisplayInsideConsole(currentViewPosition, view, console)) {
            view.setPosition(currentViewPosition);
        } else {
            view.setPosition(new Position(0, 0));
        }
        managedViews.push(view);
    }

    public void remove(ManagedView manageView) {
        managedViews.remove(manageView);
    }

    private boolean checkForFullWindowDisplayInsideConsole(Position currentViewPosition, ManagedView view,
            EmfConsoleView console) {
        int consoleWidth = console.width() - 20; //inset
        int consoleHeight = console.height() - 20; //inset
        int width = currentViewPosition.x() + view.width();
        int height = currentViewPosition.y() + view.height();
        return (width < consoleWidth) && (height < consoleHeight);
    }

    private Position lastViewPosition() {
        try {
            ManagedView view = (ManagedView) managedViews.pop();
            return view.getPosition();
        } catch (EmptyStackException e) {
            /* this will get adjusted to 0,0 when 20 is added in x and y */
            return new Position(-20, -20);
        }
    }

    private Position cascade(Position lastViewPosition) {
        return new Position(lastViewPosition.x() + 20, lastViewPosition.y() + 20);
    }

}
