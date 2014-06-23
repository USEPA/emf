package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.ui.Layout;
import gov.epa.emissions.framework.ui.LayoutImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DesktopManagerImpl implements DesktopManager {

    private WindowMenuView windowMenu;

    private Map windowNames;

    private Layout layout;

    private EmfConsoleView consoleView;

    private EmfDesktop desktop;

    public DesktopManagerImpl(WindowMenuView windowMenu, EmfConsoleView consoleView, EmfDesktop desktop) {
        this.windowMenu = windowMenu;
        this.windowNames = new HashMap();
        this.consoleView = consoleView;
        this.layout = new LayoutImpl(consoleView);
        this.desktop = desktop;
    }

    public void openWindow(ManagedView manageView) {
        String name = manageView.getName();
        if (!windowNames.containsKey(name)) {
            newWindowOpened(manageView, name);
        } else {
            // enforces one window per object TODO: think abt a better way to do this
            manageView = null;
            ManagedView cachedView = (ManagedView) windowNames.get(name);
            cachedView.bringToFront();
        }
    }

    private void newWindowOpened(ManagedView manageView, String name) {
        windowNames.put(name, manageView);
        windowMenu.register(manageView);

        desktop.add(manageView);
        manageView.bringToFront();
        layout.add(manageView);
    }

    public void closeWindow(ManagedView manageView) {
        windowNames.remove(manageView.getName());
        windowMenu.unregister(manageView);
        layout.remove(manageView);
    }
    
    public void hideWindow(ManagedView manageView) {
        manageView.hideMe();
    }

    public ManagedView getWindow(String windowName) {
        return (ManagedView) windowNames.get(windowName);
    }

    public boolean closeAll() {
        List list = new ArrayList(windowNames.keySet());
        for (int i = 0; i < list.size(); i++) {
            Object key = list.get(i);
            ManagedView view = (ManagedView) windowNames.get(key);
            if (!view.hasChanges()) {
                view.windowClosing();// closeWindow is called inside this method
            }
        }
        // check for windows with unsaved changes
        return checkForUnSavedWindows(windowNames);
    }
    
    public boolean hideAll() {
        List list = new ArrayList(windowNames.keySet());
        for (int i = 0; i < list.size(); i++) {
            Object key = list.get(i);
            ManagedView view = (ManagedView) windowNames.get(key);
            view.windowHiding();// hideWindow is called inside this method
        }
        return true;
    }

    private boolean checkForUnSavedWindows(Map windowNames) {
        if (!windowNames.isEmpty()) {
            if (consoleView.confirm()) {
                forceWindowClose(windowNames);
                return true;
            }
            return false;
        }
        return true;
    }

    private void forceWindowClose(Map windowNames) {
        List list = new ArrayList(windowNames.keySet());
        for (int i = 0; i < list.size(); i++) {
            Object key = list.get(i);
            ManagedView view = (ManagedView) windowNames.get(key);
            view.resetChanges();
            view.windowClosing();
        }
    }

    public void ensurePresence(ManagedView view) {
        desktop.ensurePresence(view);
    }

    public int numberOfOpenWindows() {
        return windowNames.size();
    }

}