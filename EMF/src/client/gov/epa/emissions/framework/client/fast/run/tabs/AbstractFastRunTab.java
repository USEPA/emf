package gov.epa.emissions.framework.client.fast.run.tabs;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfInternalFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.fast.run.FastRunPresenter;
import gov.epa.emissions.framework.services.fast.FastRun;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.Cursor;

import javax.swing.JDesktopPane;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public abstract class AbstractFastRunTab extends JPanel implements FastRunTabView {

    private FastRun run;

    private MessagePanel messagePanel;

    private EmfInternalFrame parentInternalFrame;

    private EmfSession session;

    private EmfConsole parentConsole;

    private FastRunPresenter presenter;

    private DesktopManager desktopManager;

    public AbstractFastRunTab(FastRun run, EmfSession session, MessagePanel messagePanel,
            EmfInternalFrame parentInternalFrame, DesktopManager desktopManager, EmfConsole parentConsole,
            FastRunPresenter presenter) {

        this.run = run;
        this.session = session;
        this.messagePanel = messagePanel;
        this.parentInternalFrame = parentInternalFrame;
        this.parentConsole = parentConsole;
        this.presenter = presenter;
        this.desktopManager = desktopManager;
    }

    public DesktopManager getDesktopManager() {
        return desktopManager;
    }

    public FastRunPresenter getPresenter() {
        return presenter;
    }

    public FastRun getRun() {
        return run;
    }

    public void setRun(FastRun run) {
        this.run = run;
    }

    public MessagePanel getMessagePanel() {
        return messagePanel;
    }

    public ManageChangeables getChangeablesList() {
        return this.parentInternalFrame;
    }

    public JDesktopPane getDesktopPane() {
        return parentInternalFrame.getDesktopPane();
    }

    public EmfInternalFrame getParenetInternalFrame() {
        return this.parentInternalFrame;
    }

    public EmfSession getSession() {
        return session;
    }

    public EmfConsole getParentConsole() {
        return parentConsole;
    }

    public void showError(String message) {

        this.messagePanel.setError(message);
        this.refreshLayout();
    }

    public void showMessage(String message) {

        this.messagePanel.setMessage(message);
        this.refreshLayout();
    }

    public void clearMessage() {

        this.messagePanel.clear();
        this.refreshLayout();
    }

    public void refresh(FastRun run) {

        this.setRun(run);

        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            this.refreshData();
            this.refreshLayout();
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    abstract void refreshData();

    void refreshLayout() {
        this.validate();
    }

    public void display() {

        this.populateFields();
        this.addChangables();
    }

    abstract void addChangables();

    abstract void populateFields();

    public void modify() {
        // NOTE Auto-generated method stub

    }

    public void save(FastRun run) {
        // NOTE Auto-generated method stub

    }

    public void viewOnly() {
        // NOTE Auto-generated method stub

    }

}
