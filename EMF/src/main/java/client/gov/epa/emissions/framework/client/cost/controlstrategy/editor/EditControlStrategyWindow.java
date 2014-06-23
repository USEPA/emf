package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.CopyButton;
import gov.epa.emissions.commons.gui.buttons.RunButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.commons.gui.buttons.StopButton;
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
import gov.epa.emissions.framework.ui.RefreshButton;
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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class EditControlStrategyWindow extends DisposableInteralFrame implements EditControlStrategyView {

    private EditControlStrategyPresenter presenter;

    private SingleLineMessagePanel messagePanel;

    private EmfSession session;

    private EmfConsole parentConsole;

    private ControlStrategy controlStrategy;

    private DesktopManager desktopManager;

    private Button saveButton;

    private Button runButton;

    private Button stopButton;
    
    private Button refreshButton;
    
    private EditControlStrategyMeasuresTab measuresTabView;

    private ControlStrategyProgramsTab programsTabView;

    private EditControlStrategyOutputTabView outputTabView;

    private EditControlStrategySummaryTab summaryTabView;
    
    private JPanel programsTab, measuresTab;
    
    final private JTabbedPane tabbedPane = new JTabbedPane();
    
    private StrategyType lastStrategyType;
    
    public EditControlStrategyWindow(DesktopManager desktopManager, EmfSession session, EmfConsole parentConsole) {
        super("Edit Control Strategy", new Dimension(810, 640), desktopManager);
//        this.setMinimumSize(new Dimension(700, 300));
        this.session = session;
        this.desktopManager = desktopManager;
        this.parentConsole = parentConsole;
    }

    public void observe(EditControlStrategyPresenter presenter) {
        this.presenter = presenter;

    }

    public void display(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        super.setLabel(super.getTitle() + ": " + controlStrategy.getName());

        this.controlStrategy = controlStrategy;
        this.lastStrategyType = controlStrategy.getStrategyType();
        doLayout(controlStrategy, controlStrategyResults);
//        pack();
        if ( (this.controlStrategy.getIsFinal() != null ? this.controlStrategy.getIsFinal() : false)){
            this.saveButton.setEnabled(false);
            this.runButton.setEnabled(false);
            this.refreshButton.setEnabled(false);            
        }         
        super.display();
//        super.resetChanges();
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

        if (controlStrategy.getRunStatus().equalsIgnoreCase("Running"))
            enableButtons(false);

        contentPane.add(layout);
    }

    private JTabbedPane createTabbedPane(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
//        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Summary", createSummaryTab(controlStrategy, controlStrategyResults));
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        // These are just added to illustrate what is coming later
        tabbedPane.addTab("Inventories", createInventoryFilterTab(controlStrategy));
//        tabbedPane.addTab("Pollutants", createPollutantsTab(controlStrategy));
        measuresTab = createMeasuresTab(controlStrategy);
        programsTab = createProgramsTab(controlStrategy);
        if (controlStrategy.getStrategyType()!= null && 
           controlStrategy.getStrategyType().getName().equals(StrategyType.projectFutureYearInventory)) 
        {
            tabbedPane.addTab("Programs", programsTab);
        } else {
            tabbedPane.addTab("Measures", measuresTab);
        }
        tabbedPane.addTab("Constraints", createAppliedMeasuresTab(controlStrategy));
        tabbedPane.addTab("Outputs", outputPanel(controlStrategyResults));
//        tabbedPane.removeTabAt(3);
//        tabbedPane.addChangeListener(new ChangeListener(){
//            public void stateChanged(ChangeEvent e) {
//                messagePanel.clear();
//                try {
//                    loadComponents(tabbedPane);
//                } catch (EmfException exc) {
//                    showError("Could not load component: "  + tabbedPane.getSelectedComponent().getName());
//                }
//            }
//        });
        
//DCD 1/26/07 -- see above for new listener code
//        tabbedPane.addChangeListener(new ChangeListener() {
//            public void stateChanged(ChangeEvent e) {
//                messagePanel.clear();
//            }
//        });

        return tabbedPane;
    }

    private JPanel createSummaryTab(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        try {
            summaryTabView = new EditControlStrategySummaryTab(controlStrategy,
                    controlStrategyResults, session, this, messagePanel, parentConsole, this.presenter.getCostYearTable(),
                    presenter);
            this.presenter.set(summaryTabView);
            return summaryTabView;
        } catch (EmfException e) {
            showError("Could not load Summary Tab." + e.getMessage());
            return createErrorTab("Could not load Summary Tab." + e.getMessage());
        }

    }

    private JPanel createInventoryFilterTab(ControlStrategy controlStrategy) {
        try {
            EditControlStrategyTabView view = new EditControlStrategyInventoryFilterTab(controlStrategy, this, 
                    messagePanel, parentConsole, 
                    session, desktopManager,
                    presenter);
            this.presenter.set(view);
            return (JPanel) view;
        } catch (EmfException e) {
            showError("Could not load inventory filter tab." + e.getMessage());
            return createErrorTab("Could not load inventory filter tab." + e.getMessage());
        }
    }

    private JPanel createAppliedMeasuresTab(ControlStrategy controlStrategy) {
        ControlStrategyConstraintsTabView view = null;
        view = new EditControlStrategyConstraintsTab(controlStrategy, this,  messagePanel, parentConsole, session, presenter);
        this.presenter.set(view);
        return (JPanel) view;
    }

    private JPanel createMeasuresTab(ControlStrategy controlStrategy) {
        try {
            measuresTabView = new EditControlStrategyMeasuresTab(controlStrategy, this,  messagePanel, parentConsole, session);
            this.presenter.set(measuresTabView);
        } catch (EmfException e) {
            showError("Could not create Measures tab.");
        }
        
        return measuresTabView;
    }
    
    private JPanel createProgramsTab(ControlStrategy controlStrategy) {
        programsTabView = new ControlStrategyProgramsTab(controlStrategy, this,  messagePanel, parentConsole, desktopManager, session);
        this.presenter.set(programsTabView);
        return programsTabView;
    }
    
//    private JPanel createPollutantsTab(ControlStrategy controlStrategy) {
//        EditControlStrategyPollutantsTab pollutantsTabView = null;
//        try {
//            pollutantsTabView = new EditControlStrategyPollutantsTab(controlStrategy, this,  messagePanel, parentConsole, session);
//            this.presenter.set(pollutantsTabView);
//        } catch (EmfException e) {
//            showError("Could not create Pollutants tab.");
//        }
//        
//        return pollutantsTabView;
//    }
    
    private JPanel outputPanel(ControlStrategyResult[] controlStrategyResults) {
        try {
            outputTabView = new EditControlStrategyOutputTab(controlStrategy,
                    controlStrategyResults, messagePanel, desktopManager, parentConsole, session);
            this.presenter.set(outputTabView);
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

        saveButton = new SaveButton(saveAction());
        container.add(saveButton);

        Button closeButton = new CloseButton(closeAction());
        container.add(closeButton);
        getRootPane().setDefaultButton(saveButton);

        container.add(Box.createHorizontalStrut(20));

        runButton = new RunButton(runAction());
        ControlStrategyResultsSummary summary = new ControlStrategyResultsSummary(controlStrategyResults);
        if (!summary.getRunStatus().equalsIgnoreCase("Running"))
            container.add(runButton);

        refreshButton = new Button("Refresh", refreshAction());
        container.add(refreshButton);

        stopButton = new StopButton(stopAction());
        stopButton.setEnabled(false);
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

    private Action stopAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                clearMessage();
                try {
                    String title = "Warning";
                    
                    String message = "Would you like to stop the strategy run?  This could several minutes to cancel the run.";
                    int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    boolean cancel = false;
                    if (selection == JOptionPane.CANCEL_OPTION) {
                        return;
                    } else if (selection == JOptionPane.YES_OPTION){
                        cancel = true; 
                    } else if (selection == JOptionPane.NO_OPTION){
                        cancel = false; 
                    }
                    if (cancel) {
                        presenter.stopRun();
                    }
                } catch (EmfException e) {
                    messagePanel.setError("Error stopping running strategy: " + e.getMessage());
                }
            }
        };
    }

    private Action runAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    //this step make sure all valid values were specified before running the strategy.
                    presenter.doRun(controlStrategy);
                    
                    //check to see if really want to run strategy, we don't to overwrite if its already been run.
                    boolean deleteResults = false; 
                    if (presenter.hasResults()) {
                        String title = "Warning";
                        
                        String message = "There are results available for this strategy. \n" + " Would you like to delete results and controlled inventories?";
                        int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                        if (selection == JOptionPane.CANCEL_OPTION) {
                            return;
                        }
                        else if (selection == JOptionPane.YES_OPTION){
//                          runStrategyDeleteDataset(getDetailedResults());
                            deleteResults = true; 
                        }
                        else if (selection == JOptionPane.NO_OPTION){
                            deleteResults = false; 
                        }
                    }
                    
//                    validatePath(outputTabView.getExportFolder());
                    controlStrategy.setDeleteResults(deleteResults);
                    controlStrategy.setExportDirectory(outputTabView.getExportFolder());
                    controlStrategy.setRunStatus("Waiting");
                    //get all values from various tabs and persist to strategy object
                    save();
                    controlStrategy.setStartDate(new Date());
                    
                    presenter.setResults(controlStrategy);
                    presenter.runStrategy();
                    outputTabView.notifyStrategyRun(controlStrategy);
                    messagePanel
                            .setMessage("Running strategy. Monitor the status window for progress, and refresh this window after completion to see results");
                    enableButtons(false);
                    //stopButton.setEnabled(true);
                } catch (EmfException e) {
                    enableButtons(true);
                    messagePanel.setError("Error running strategy: " + e.getMessage());
                }
            }
        };
    }
    
//    private void validatePath(String folderPath) throws EmfException {
//        File file = new File(folderPath);
//
//        if (!file.exists() || !file.isDirectory()) {
//            throw new EmfException ("Export folder does not exist: " + folderPath);
//        }
//    }
//
    public void enableButtons(boolean enable) {
        saveButton.setEnabled(enable);
        runButton.setEnabled(enable);
        stopButton.setEnabled(!enable);
    }

    protected void save() throws EmfException { // TODO: disable save, refresh, run button if final
        clearMessage();
        presenter.doSave(controlStrategy);
        if (controlStrategy.getIsFinal())
        {
            this.saveButton.setEnabled(false);
            this.runButton.setEnabled(false);
            this.refreshButton.setEnabled(false);

        }
        messagePanel
            .setMessage("Strategy was saved successfully.");
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
            //first check whether cs is running before checking the discard changes
            if (isRunButtonClicked() || shouldDiscardChanges())
                presenter.doClose();
        } catch (EmfException e) {
            messagePanel.setError("Could not close: " + e.getMessage());
        }
    }

    private boolean isRunButtonClicked() {
        return !runButton.isEnabled();
    }

    private Action saveAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    save();
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        };

        return action;
    }

    public void notifyLockFailure(ControlStrategy controlStrategy) {
        String message = "Cannot edit Control Strategy: " + controlStrategy
                + System.getProperty("line.separator") + " as it was locked by User: " + controlStrategy.getLockOwner()
                + " (at " + format(controlStrategy.getLockDate()) + ")";
        InfoDialog dialog = new InfoDialog(this, "Message", message);
        dialog.confirm();
    }

    public void notifyFinalFailure(ControlStrategy controlStrategy) {
        String message = "Cannot edit Control Strategy: " + controlStrategy
                + System.getProperty("line.separator") + " as it was finalized by User: " + controlStrategy.getCreator()
                + " (at " + format(controlStrategy.getLastModifiedDate()) + ")";
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

    public void startControlMeasuresRefresh() {
        if (measuresTabView != null)
            measuresTabView.startControlMeasuresRefresh();
    }

    public void endControlMeasuresRefresh() {
        if (measuresTabView != null)
            measuresTabView.endControlMeasuresRefresh();
    }

    public void refresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        ControlStrategyResultsSummary summary = new ControlStrategyResultsSummary(controlStrategyResults);
        if (summary.getRunStatus().equalsIgnoreCase("Completed."))
            stopButton.setEnabled(false);
    }

    public void stopRun()  {
        enableButtons(true);
        //stopButton.setEnabled(false);
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