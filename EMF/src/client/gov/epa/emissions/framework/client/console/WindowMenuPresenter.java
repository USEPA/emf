package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.client.ManagedView;

public class WindowMenuPresenter {

    private WindowMenuView view;

    private DesktopManager desktopManager;

    public WindowMenuPresenter(WindowMenuView view) {
        this.view = view;
    }

    public void setDesktopManager(DesktopManager desktopManager) {
        this.desktopManager = desktopManager;
    }

    public void select(ManagedView managedView) {
        managedView.bringToFront();
    }

    public void closeAll() {
        desktopManager.closeAll();
    }
    
    public void hideAll() {
        desktopManager.hideAll();
    }

    public void addPermanently(ManagedView managedView) {
        view.addPermanently(managedView);
        desktopManager.ensurePresence(managedView);
    }

    public int numberOfOpenWindows() {
        return desktopManager.numberOfOpenWindows();
    }

}
