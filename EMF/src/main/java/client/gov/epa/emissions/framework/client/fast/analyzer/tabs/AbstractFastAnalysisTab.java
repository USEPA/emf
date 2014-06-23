package gov.epa.emissions.framework.client.fast.analyzer.tabs;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfInternalFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.fast.analyzer.FastAnalysisPresenter;
import gov.epa.emissions.framework.client.fast.analyzer.FastAnalysisTabView;
import gov.epa.emissions.framework.services.fast.FastAnalysis;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.Cursor;

import javax.swing.JDesktopPane;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public abstract class AbstractFastAnalysisTab extends JPanel implements FastAnalysisTabView {

    private FastAnalysis analysis;

    private MessagePanel messagePanel;

    private EmfInternalFrame parentInternalFrame;

    private EmfSession session;

    private EmfConsole parentConsole;

    private DesktopManager desktopManager;

    private FastAnalysisPresenter presenter;

    public AbstractFastAnalysisTab(FastAnalysis analysis, EmfSession session, MessagePanel messagePanel,
            EmfInternalFrame parentInternalFrame, DesktopManager desktopManager, EmfConsole parentConsole,
            FastAnalysisPresenter presenter) {

        this.analysis = analysis;
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

    public FastAnalysis getAnalysis() {
        return analysis;
    }

    public void setAnalysis(FastAnalysis analysis) {
        this.analysis = analysis;
    }

    public MessagePanel getMessagePanel() {
        return messagePanel;
    }

    public ManageChangeables getChangeablesList() {
        return parentInternalFrame;
    }

    public JDesktopPane getDesktopPane() {
        return parentInternalFrame.getDesktopPane();
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

    public void refresh(FastAnalysis analysis) {

        this.setAnalysis(analysis);

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

    public void save(FastAnalysis analysis) {
        // NOTE Auto-generated method stub

    }

    public void viewOnly() {
        // NOTE Auto-generated method stub

    }

    public FastAnalysisPresenter getPresenter() {
        return presenter;
    }
}
