package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.ui.MessagePanel;

import javax.swing.JPanel;

public class EmfPanel extends JPanel {

    private EmfConsole parentConsole;

    private DesktopManager desktopManager;

    protected MessagePanel messagePanel;

    public EmfPanel(String name, EmfConsole parentConsole, DesktopManager desktopManager, MessagePanel messagePanel) {

        this.setName(name);
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManager;
        this.messagePanel = messagePanel;
    }

    public EmfConsole getParentConsole() {
        return parentConsole;
    }

    public DesktopManager getDesktopManager() {
        return desktopManager;
    }

    public EmfSession getSession() {
        return this.parentConsole.getSession();
    }

    public void showMessage(String message) {
        this.messagePanel.setMessage(message);
    }

    public void showError(String message) {
        this.messagePanel.setError(message);
    }

    public void clearMessage() {
        this.messagePanel.clear();
    }
}
