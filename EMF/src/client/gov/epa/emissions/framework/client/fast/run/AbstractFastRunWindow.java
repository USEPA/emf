package gov.epa.emissions.framework.client.fast.run;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.RunButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.fast.AbstractFastAction;
import gov.epa.emissions.framework.client.fast.run.tabs.FastRunConfigurationTab;
import gov.epa.emissions.framework.client.fast.run.tabs.FastRunInventoriesTab;
import gov.epa.emissions.framework.client.fast.run.tabs.FastRunOutputsTab;
import gov.epa.emissions.framework.client.fast.run.tabs.FastRunSummaryTab;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastRun;
import gov.epa.emissions.framework.ui.InfoDialog;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public abstract class AbstractFastRunWindow extends DisposableInteralFrame implements FastRunView {

    private FastRunPresenter presenter;

    private SingleLineMessagePanel messagePanel;

    private EmfSession session;

    private EmfConsole parentConsole;

    private Button saveButton;

    private RunButton runButton;

    public AbstractFastRunWindow(String title, DesktopManager desktopManager, EmfSession session,
            EmfConsole parentConsole) {

        super(title, new Dimension(760, 580), desktopManager);

        this.session = session;
        this.parentConsole = parentConsole;
        this.messagePanel = new SingleLineMessagePanel();
    }

    public void observe(FastRunPresenter presenter) {
        this.presenter = presenter;
    }

    protected MessagePanel getMessagePanel() {
        return this.messagePanel;
    }

    public void display(FastRun run) {

        this.setLabel(this.getTitle() + ": " + run.getName());
        doLayout(run);
        super.display();
    }

    abstract protected void doLayout(FastRun run);

    protected JComponent createSummaryTab(FastRun run) {

        try {
            FastRunSummaryTab tab = new FastRunSummaryTab(run, session, messagePanel, this, this.desktopManager,
                    parentConsole, this.presenter);
            this.presenter.addTab(tab);
            return tab;
        } catch (EmfException e) {

            String message = "Could not create Summary Tab." + e.getMessage();
            showError(message);
            return createErrorTab(message);
        }
    }

    protected JComponent createConfigurationTab(FastRun run) {

        try {
            FastRunConfigurationTab tab = new FastRunConfigurationTab(run, session, messagePanel, this,
                    this.desktopManager, parentConsole, this.presenter);
            this.presenter.addTab(tab);
            return tab;
        } catch (EmfException e) {

            String message = "Could not create Configuration Tab." + e.getMessage();
            showError(message);
            return createErrorTab(message);
        }
    }

    protected JComponent createInventoriesTab(FastRun run) {

        try {
            FastRunInventoriesTab tab = new FastRunInventoriesTab(run, session, messagePanel, this,
                    this.desktopManager, parentConsole, this.presenter);
            this.presenter.addTab(tab);
            return tab;
        } catch (EmfException e) {

            String message = "Could not load Inputs Tab." + e.getMessage();
            showError(message);
            return createErrorTab(message);
        }
    }

    protected JComponent createOutputsTab(FastRun run) {

        try {
            FastRunOutputsTab tab = new FastRunOutputsTab(run, session, messagePanel, this, this.desktopManager,
                    parentConsole, this.presenter);
            this.presenter.addTab(tab);
            return tab;
        } catch (EmfException e) {

            String message = "Could not load Outputs Tab." + e.getMessage();
            showError(message);
            return createErrorTab(message);
        }
    }

    protected JComponent createErrorTab(String message) {

        JLabel label = new JLabel(message);
        label.setForeground(Color.RED);

        return label;
    }

    protected JPanel createButtonsPanel() {

        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(15);
        container.setLayout(layout);

        saveButton = new SaveButton(saveAction());
        container.add(saveButton);

        Button closeButton = new CloseButton(closeAction());
        container.add(closeButton);
        getRootPane().setDefaultButton(saveButton);

        runButton = new RunButton("Execute", this.runAction());
        container.add(runButton);

        Button refreshButton = new Button("Refresh", refreshAction());
        container.add(refreshButton);

        container.add(Box.createHorizontalStrut(20));

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    protected Action refreshAction() {
        Action action = new AbstractFastAction(this.getMessagePanel(), "Error refreshing Fast run") {

            @Override
            protected void doActionPerformed(ActionEvent e) throws EmfException {
                doRefreshRun();
            }
        };

        return action;
    }

    private void doRefreshRun() throws EmfException {
        presenter.doRefresh();
    }

    protected Action runAction() {
        Action action = new AbstractFastAction(this.getMessagePanel(), "Error executing Fast run") {

            @Override
            protected void doActionPerformed(ActionEvent e) throws EmfException {
                doExecuteRun();
            }
        };

        return action;
    }

    private void doExecuteRun() throws EmfException {

        this.clearMessage();

        String message = "Are you sure you want to execute the Fast run?";

        int selection = JOptionPane.showConfirmDialog(this, message, "Warning", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {

            presenter.doRun();
            this
                    .showMessage("Executing Fast run. Monitor the status window for progress, and refresh this window after completion to see results");
        }
    }

    protected Action closeAction() {
        Action action = new AbstractFastAction(this.getMessagePanel(), "Error closing window") {

            @Override
            protected void doActionPerformed(ActionEvent e) {
                doClose();
            }
        };

        return action;
    }

    protected void doClose() {

        try {
            if (shouldDiscardChanges()) {
                presenter.doClose();
            }
        } catch (EmfException e) {
            messagePanel.setError("Could not close: " + e.getMessage());
        }
    }

    protected Action saveAction() {
        Action action = new AbstractFastAction(this.getMessagePanel(), "Error saving changes") {

            @Override
            protected void doActionPerformed(ActionEvent e) throws EmfException {

                presenter.doSave();
                showMessage("Run was saved successfully.");
                resetChanges();
            }
        };

        return action;
    }

    public void notifyLockFailure(FastRun run) {

        String message = "Cannot modify Fast Run: " + run + System.getProperty("line.separator")
                + " as it was locked by User: " + run.getLockOwner() + "(at " + this.format(run.getLockDate()) + ")";
        InfoDialog dialog = new InfoDialog(this, "Message", message);
        dialog.confirm();
    }

    protected String format(Date date) {
        return CustomDateFormat.format_YYYY_MM_DD_HH_MM(date);
    }

    public void windowClosing() {
        doClose();
    }

    public void signalChanges() {

        clearMessage();
        super.signalChanges();
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

    public void refresh(FastRun run) {
        /*
         * no-op
         */
    }
}