package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.console.DesktopManager;

import java.awt.Dimension;

public abstract class DisposableInteralFrame extends EmfInternalFrame {

    public DisposableInteralFrame(String title, Dimension dimension, DesktopManager desktopManager) {
        super(title, dimension, desktopManager);
    }

    public DisposableInteralFrame(String title, DesktopManager desktopManager) {
        super(title, desktopManager);
    }

    public void windowClosing() {
        disposeView();
    }

    public void disposeView() {
        super.dispose();
        super.disposeView();
    }

    public boolean isAlive() {
        // return !super.isClosed();
        return false;
    }
}
