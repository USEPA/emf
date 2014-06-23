package gov.epa.emissions.framework.client.fast;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastAnalysis;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public abstract class AbstractMPSDTManagerTab extends JPanel implements RefreshObserver {

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private EmfSession session;

    private DesktopManager desktopManager;

    public AbstractMPSDTManagerTab(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager) {

        this.setLayout(new BorderLayout());
        this.setSession(session);
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManager;
    }

    public abstract String getTitle();

    public boolean hasChanges() {
        // NOTE Auto-generated method stub
        return false;
    }

    public boolean isAlive() {
        // NOTE Auto-generated method stub
        return false;
    }

    public void resetChanges() {
        // NOTE Auto-generated method stub

    }

    public boolean shouldDiscardChanges() {
        // NOTE Auto-generated method stub
        return false;
    }

    public void windowClosing() {
        // NOTE Auto-generated method stub

    }

    public void populateFields() {
        //
    }

    public void refreshData() {
        //
    }

    public void showError(String message) {
        this.messagePanel.setError(message);
    }

    public void showMessage(String message) {
        this.messagePanel.setMessage(message);
    }

    public void clearMessage() {
        this.messagePanel.clear();
    }

    public MessagePanel getMessagePanel() {
        return messagePanel;
    }

    protected JPanel createTopPanel() {

        JPanel panel = new JPanel(new BorderLayout());

        this.messagePanel = new SingleLineMessagePanel();
        this.messagePanel.setOpaque(false);
        panel.add(this.messagePanel, BorderLayout.CENTER);

        Button button = new RefreshButton(this, "Refresh Fast Entities", this.messagePanel);
        panel.add(button, BorderLayout.EAST);

        return panel;
    }

    protected JPanel createControlPanel() {

        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.add(createButtonPanel(), BorderLayout.WEST);

        return controlPanel;
    }

    protected abstract JComponent createButtonPanel();

    public EmfConsole getParentConsole() {
        return this.parentConsole;
    }

    public DesktopManager getDesktopManager() {
        return this.desktopManager;
    }

    public void setSession(EmfSession session) {
        this.session = session;
    }

    public EmfSession getSession() {
        return session;
    }

    public void showConstrutionMessage() {

        this.clearMessage();
        this.showMessage("Under construction");
    }

    protected void executeCommand(ExceptionHandlingCommand command) throws EmfException {

        try {

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
                command.execute();
            } catch (EmfException e) {
                command.handleException(e);
            } finally {
                command.postExecute();
            }

        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    protected static int[] getIds(List<FastAnalysis> list) {

        int size = list.size();
        int[] ids = new int[size];
        for (int i = 0; i < size; i++) {
            ids[i] = list.get(i).getId();
        }

        return ids;
    }

}