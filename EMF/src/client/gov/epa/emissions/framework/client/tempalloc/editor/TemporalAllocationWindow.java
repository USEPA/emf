package gov.epa.emissions.framework.client.tempalloc.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.RunButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.commons.gui.buttons.StopButton;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocation;
import gov.epa.emissions.framework.ui.InfoDialog;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

public class TemporalAllocationWindow extends DisposableInteralFrame implements TemporalAllocationView {

    protected TemporalAllocationPresenter presenter;

    protected SingleLineMessagePanel messagePanel;

    protected EmfSession session;

    protected EmfConsole parentConsole;
    
    protected Button runButton, refreshButton, stopButton;
    
    public TemporalAllocationWindow(DesktopManager desktopManager, EmfSession session, EmfConsole parentConsole) {
        super("View Temporal Allocation", new Dimension(760, 580), desktopManager);

        this.session = session;
        this.parentConsole = parentConsole;
    }
    
    public void observe(TemporalAllocationPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(TemporalAllocation temporalAllocation) {
        if (presenter.isEditing()) {
            super.setTitle("Edit Temporal Allocation");
        }
        super.setLabel(super.getTitle() + ": " + temporalAllocation.getName());
        doLayout(temporalAllocation);
        super.display();
    }

    private void doLayout(TemporalAllocation temporalAllocation) {
        Container contentPane = getContentPane();
        contentPane.removeAll();

        messagePanel = new SingleLineMessagePanel();

        JPanel layout = new JPanel();
        layout.setLayout(new BorderLayout());
        layout.add(messagePanel, BorderLayout.PAGE_START);
        layout.add(createTabbedPane(temporalAllocation));
        layout.add(createButtonsPanel(temporalAllocation), BorderLayout.PAGE_END);

        contentPane.add(layout);
    }

    private JTabbedPane createTabbedPane(TemporalAllocation temporalAllocation) {
        final JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.addTab(presenter.SUMMARY_TAB, createTab(presenter.SUMMARY_TAB, temporalAllocation));
        tabbedPane.addTab(presenter.INVENTORIES_TAB, createTab(presenter.INVENTORIES_TAB, temporalAllocation));
        tabbedPane.addTab(presenter.TIMEPERIOD_TAB, createTab(presenter.TIMEPERIOD_TAB, temporalAllocation));
        tabbedPane.addTab(presenter.PROFILES_TAB, createTab(presenter.PROFILES_TAB, temporalAllocation));
        tabbedPane.addTab(presenter.OUTPUT_TAB, createTab(presenter.OUTPUT_TAB, temporalAllocation));
        return tabbedPane;
    }

    private JPanel createTab(String tabName, TemporalAllocation temporalAllocation) {
        TemporalAllocationTabView tabView = null;
        if (presenter.SUMMARY_TAB.equals(tabName)) {
            tabView = new TemporalAllocationSummaryTab(temporalAllocation, session, this, messagePanel, 
                    presenter);
        } else if (presenter.INVENTORIES_TAB.equals(tabName)) {
            tabView = new TemporalAllocationInventoriesTab(temporalAllocation, session, this, messagePanel, 
                    parentConsole, desktopManager, presenter);
        } else if (presenter.TIMEPERIOD_TAB.equals(tabName)) {
            tabView = new TemporalAllocationTimePeriodTab(temporalAllocation, session, this, messagePanel,
                    presenter);
        } else if (presenter.PROFILES_TAB.equals(tabName)) {
            tabView = new TemporalAllocationProfilesTab(temporalAllocation, session, this, messagePanel,
                    parentConsole, desktopManager, presenter);
        } else if (presenter.OUTPUT_TAB.equals(tabName)) {
            tabView = new TemporalAllocationOutputTab(temporalAllocation, session, this, messagePanel,
                    parentConsole, desktopManager, presenter);
        }
        if (tabView != null) {
            try {
                presenter.set(tabName, tabView);
            } catch (EmfException e) {
                showError("Could not load " + tabName + ".");
                if (presenter.SUMMARY_TAB.equals(tabName))
                    return createErrorTab("Could not create " + tabName + "." + e.getMessage());
            }
            return (JPanel) tabView;
        }
        
        return createErrorTab("Unknown tab");
    }
    
    private JPanel createErrorTab(String message) {
        JPanel panel = new JPanel(false);
        JLabel label = new JLabel(message);
        label.setForeground(Color.RED);
        panel.add(label);

        return panel;
    }

    private void showError(String message) {
        messagePanel.setError(message);
    }
    
    private JPanel createButtonsPanel(TemporalAllocation temporalAllocation) {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(15);
        container.setLayout(layout);

        Button saveButton = new SaveButton(saveAction());
        saveButton.setEnabled(presenter.isEditing());
        container.add(saveButton);
        
        runButton = new RunButton(runAction());
        container.add(runButton);

        refreshButton = new Button("Refresh", refreshAction());
        container.add(refreshButton);
        
        stopButton = new StopButton(stopAction());
        container.add(stopButton);
        
        updateRunButtonStatus(temporalAllocation);

        Button closeButton = new CloseButton(closeAction());
        container.add(closeButton);
        getRootPane().setDefaultButton(saveButton);

        container.add(Box.createHorizontalStrut(20));

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }
    
    private void updateRunButtonStatus(TemporalAllocation temporalAllocation) {
        if (!presenter.isEditing()) {
            runButton.setEnabled(false);
            stopButton.setEnabled(false);
            return;
        }
        String status = temporalAllocation.getRunStatus();
        if (status != null && 
            (status.equals("Not started") || 
             status.equals("Finished") || 
             status.equals("Failed") || 
             status.equals("Cancelled"))) {
            runButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
        else {
            runButton.setEnabled(false);
            stopButton.setEnabled(true);
        }
    }

    protected void save() throws EmfException {
        clearMessage();
        presenter.doSave();
        messagePanel.setMessage("Temporal Allocation was saved successfully.");
        resetChanges();
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                clearMessage();
                doClose();
            }
        };

        return action;
    }

    protected void doClose() {
        try {
            if (!presenter.isEditing() || shouldDiscardChanges()) presenter.doClose();
        } catch (EmfException e) {
            messagePanel.setError("Could not close: " + e.getMessage());
        }
    }

    private Action saveAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                // must be editing to save temporal allocation
                if (!presenter.isEditing()) return;
                try {
                    save();
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        };

        return action;
    }
    
    private Action runAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                // must be editing to run temporal allocation
                if (!presenter.isEditing()) return;
                try {
                    save();
                    presenter.doPrepareRun();
                    runButton.setEnabled(false);
                    stopButton.setEnabled(true);
                    presenter.runTemporalAllocation();
                    messagePanel.setMessage("Running temporal allocation. Monitor the status window for progress.");
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        };
        
        return action;
    }

    private Action refreshAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                clearMessage();
                try {
                    presenter.doRefresh();
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        };
    }
    
    private Action stopAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                clearMessage();
                try {
                    String title = "Confirm Cancellation";
                    
                    String[] messages = {"Are you sure you want to stop the temporal allocation run?",
                            "The run will be stopped once the currently running task is completed."};
                    int selection = JOptionPane.showConfirmDialog(parentConsole, messages, title, JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (selection == JOptionPane.YES_OPTION) {
                        presenter.doStop();
                        messagePanel.setMessage("Request to cancel run submitted. Monitor the status window for cancellation.");
                    }
                } catch (EmfException e) {
                    messagePanel.setError("Error stopping running temporal allocation: " + e.getMessage());
                }
            }
        };
    }

    public void notifyLockFailure(TemporalAllocation temporalAllocation) {
        String message = "Cannot edit Temporal Allocation: " + temporalAllocation
                + System.getProperty("line.separator") + " as it was locked by User: " + temporalAllocation.getLockOwner()
                + "(at " + format(temporalAllocation.getLockDate()) + ")";
        InfoDialog dialog = new InfoDialog(this, "Message", message);
        dialog.confirm();
    }

    private String format(Date lockDate) {
        return CustomDateFormat.format_YYYY_MM_DD_HH_MM(lockDate);
    }

    protected void clearMessage() {
        messagePanel.clear();
    }
    
    public void refresh(TemporalAllocation temporalAllocation) {
        updateRunButtonStatus(temporalAllocation);
    }
}