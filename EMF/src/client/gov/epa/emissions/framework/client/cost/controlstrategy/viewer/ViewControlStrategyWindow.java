package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.DisabledButton;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.data.ControlStrategyResultsSummary;
import gov.epa.emissions.framework.ui.InfoDialog;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class ViewControlStrategyWindow extends DisposableInteralFrame implements ViewControlStrategyView {

    private ViewControlStrategyPresenter presenter;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private ControlStrategy controlStrategy;

    private DesktopManager desktopManager;

    private ViewControlStrategyMeasuresTab measuresTabView;

    private ViewControlStrategyProgramsTab programsTabView;

    private ViewControlStrategyOutputTabView outputTabView;

    private ViewControlStrategySummaryTab summaryTabView;

    private JPanel programsTab, measuresTab;

    final private JTabbedPane tabbedPane = new JTabbedPane();

    private StrategyType lastStrategyType;

    public ViewControlStrategyWindow(DesktopManager desktopManager, EmfSession session, EmfConsole parentConsole) {
        
        super("View Control Strategy", new Dimension(810, 640), desktopManager);

        this.desktopManager = desktopManager;
        this.parentConsole = parentConsole;
    }

    public void observe(ViewControlStrategyPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {

        this.setLabel(this.getTitle() + ": " + controlStrategy.getName());

        this.controlStrategy = controlStrategy;
        this.lastStrategyType = controlStrategy.getStrategyType();
        this.doLayout(controlStrategy, controlStrategyResults);

        super.display();
    }

    private void doLayout(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {

        Container contentPane = getContentPane();
        contentPane.removeAll();

        messagePanel = new SingleLineMessagePanel();

        JPanel layout = new JPanel();
        layout.setLayout(new BorderLayout());
        layout.add(messagePanel, BorderLayout.PAGE_START);
        layout.add(createTabbedPane(controlStrategy, controlStrategyResults));
        layout.add(createButtonsPanel(controlStrategyResults), BorderLayout.PAGE_END);

        contentPane.add(layout);
    }

    private JTabbedPane createTabbedPane(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        // tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Summary", createSummaryTab(controlStrategy, controlStrategyResults));
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        tabbedPane.addTab("Inventories", createInventoryFilterTab(controlStrategy));

        measuresTab = createMeasuresTab();
        programsTab = createProgramsTab();
        if (controlStrategy.getStrategyType() != null
                && controlStrategy.getStrategyType().getName().equals(StrategyType.projectFutureYearInventory)) {
            tabbedPane.addTab("Programs", programsTab);
        } else {
            tabbedPane.addTab("Measures", measuresTab);
        }

        tabbedPane.addTab("Constraints", createAppliedMeasuresTab(controlStrategy));
        tabbedPane.addTab("Outputs", outputPanel());

        return tabbedPane;
    }

    private JPanel createSummaryTab(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {

        try {
            summaryTabView = new ViewControlStrategySummaryTab(controlStrategy, controlStrategyResults, this.presenter
                    .getCostYearTable(), messagePanel, parentConsole, desktopManager, presenter);
            this.presenter.setSummaryTab(summaryTabView);
            return summaryTabView;
        } catch (EmfException e) {
            showError("Could not load Summary Tab." + e.getMessage());
            return createErrorTab("Could not load Summary Tab." + e.getMessage());
        }

    }

    private JPanel createInventoryFilterTab(ControlStrategy controlStrategy) {

        try {
            ViewControlStrategyTabView view = new ViewControlStrategyInventoryFilterTab(controlStrategy, messagePanel,
                    parentConsole, desktopManager, presenter);
            this.presenter.setInventoryFilterTab(view);
            return (JPanel) view;
        } catch (EmfException e) {
            showError("Could not load inventory filter tab." + e.getMessage());
            return createErrorTab("Could not load inventory filter tab." + e.getMessage());
        }
    }

    private JPanel createAppliedMeasuresTab(ControlStrategy controlStrategy) {

        ViewControlStrategyConstraintsTab view = new ViewControlStrategyConstraintsTab(controlStrategy, messagePanel,
                parentConsole, desktopManager);
        this.presenter.setConstraintsTab(view);

        return view;
    }

    private JPanel createMeasuresTab() {

        try {
            measuresTabView = new ViewControlStrategyMeasuresTab(messagePanel, parentConsole, desktopManager);
            this.presenter.setMeasuresTab(measuresTabView);
        } catch (EmfException e) {
            showError("Could not create Measures tab.");
        }

        return measuresTabView;
    }

    private JPanel createProgramsTab() {

        programsTabView = new ViewControlStrategyProgramsTab(messagePanel, parentConsole, desktopManager);
        this.presenter.setProgramsTab(programsTabView);
        return programsTabView;
    }

    // private JPanel createPollutantsTab(ControlStrategy controlStrategy) {
    // EditControlStrategyPollutantsTab pollutantsTabView = null;
    // try {
    // pollutantsTabView = new EditControlStrategyPollutantsTab(controlStrategy, this, messagePanel, parentConsole,
    // session);
    // this.presenter.set(pollutantsTabView);
    // } catch (EmfException e) {
    // showError("Could not create Pollutants tab.");
    // }
    //        
    // return pollutantsTabView;
    // }

    private JPanel outputPanel() {
        try {
            outputTabView = new ViewControlStrategyOutputTab(controlStrategy, messagePanel, parentConsole,
                    desktopManager);
            this.presenter.setOutputTab(outputTabView);
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return (JPanel) outputTabView;
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

    private JPanel createButtonsPanel(ControlStrategyResult[] controlStrategyResults) {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(15);
        container.setLayout(layout);

        JButton saveButton = new DisabledButton("Save");
        container.add(saveButton);

        Button closeButton = new CloseButton(closeAction());
        container.add(closeButton);
        getRootPane().setDefaultButton(closeButton);

        container.add(Box.createHorizontalStrut(20));

        JButton runButton = new DisabledButton("Run");
        ControlStrategyResultsSummary summary = new ControlStrategyResultsSummary(controlStrategyResults);
        if (!summary.getRunStatus().equalsIgnoreCase("Running")) {
            container.add(runButton);
        }

        Button refreshButton = new Button("Refresh", refreshAction());
        container.add(refreshButton);

        JButton stopButton = new DisabledButton("Stop");
        container.add(stopButton);

        panel.add(container, BorderLayout.CENTER);

        return panel;
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
            presenter.doClose();
        } catch (EmfException e) {
            messagePanel.setError("Could not close: " + e.getMessage());
        }
    }

    public void notifyLockFailure(ControlStrategy controlStrategy) {
        String message = "Cannot edit Control Strategy: " + controlStrategy + System.getProperty("line.separator")
                + " as it was locked by User: " + controlStrategy.getLockOwner() + "(at "
                + format(controlStrategy.getLockDate()) + ")";
        InfoDialog dialog = new InfoDialog(this, "Message", message);
        dialog.confirm();
    }

    public void notifyEditFailure(ControlStrategy controlStrategy) {
        String message = "Cannot edit Control Strategy: " + controlStrategy.getName()
                + " because you must be the creator of the strategy or an Administrator";
        InfoDialog dialog = new InfoDialog(this, "Message", message);
        dialog.confirm();
    }

    private String format(Date lockDate) {
        return CustomDateFormat.format_YYYY_MM_DD_HH_MM(lockDate);
    }

    public void windowClosing() {
        doClose();
    }

    private void clearMessage() {
        messagePanel.clear();
    }

    public void signalChanges() {
        clearMessage();
        super.signalChanges();
    }

    public void refresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        //no-op
    }

    public void notifyStrategyTypeChange(StrategyType strategyType) {
        if (strategyType != null) {
            if (strategyType.getName().equals(StrategyType.projectFutureYearInventory)) {
                if (tabbedPane.getTabCount() == 5) {
                    tabbedPane.removeTabAt(2);
                    tabbedPane.insertTab("Programs", null, programsTab, null, 2);
                }
            }
            if (lastStrategyType != null && lastStrategyType.getName().equals(StrategyType.projectFutureYearInventory)) {
                if (tabbedPane.getTabCount() == 5) {
                    tabbedPane.removeTabAt(2);
                    tabbedPane.insertTab("Measures", null, measuresTab, null, 2);
                }
            }
        }
        lastStrategyType = strategyType;
    }
}