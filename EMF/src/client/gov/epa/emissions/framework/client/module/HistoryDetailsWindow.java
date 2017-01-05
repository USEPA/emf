package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.module.History;
import gov.epa.emissions.framework.services.module.HistoryDataset;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.ModuleDataset;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.util.ComponentUtility;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;

public class HistoryDetailsWindow extends DisposableInteralFrame implements HistoryDetailsView, RefreshObserver {
    private HistoryDetailsPresenter presenter;

    private EmfConsole parentConsole;
    private EmfSession session;

    private History history;
    
    private Module module;

    // layout
    private JPanel layout;
    private SingleLineMessagePanel messagePanel;
    private JTabbedPane tabbedPane;
    private JPanel summaryPanel;
    private JPanel datasetsPanel;
    private JPanel parametersPanel;
    private JPanel setupScriptPanel;
    private JPanel userScriptPanel;
    private JPanel teardownScriptPanel;
    private JPanel logsPanel;

    // summary
    private Label moduleName;
    private Label runId;
    private Label runDate;
    private Label user;
    private Label status;
    private Label result;
    private Label comment;

    // datasets
    private JPanel datasetsTablePanel;
    private SelectableSortFilterWrapper datasetsTable;
    private HistoryDatasetsTableData datasetsTableData;

    // parameters
    private JPanel parametersTablePanel;
    private SelectableSortFilterWrapper parametersTable;
    private HistoryParametersTableData parametersTableData;

    // scripts
    private TextArea setupScript;
    private TextArea userScript;
    private TextArea teardownScript;

    // logs
    private TextArea logs;

    public HistoryDetailsWindow(EmfConsole parentConsole, DesktopManager desktopManager, EmfSession session, History history) {
        super(getWindowTitle(history), new Dimension(800, 600), desktopManager);

        layout = new JPanel();
        layout.setLayout(new BorderLayout());
        this.parentConsole = parentConsole;
        this.session = session;
        super.getContentPane().add(layout);
        
        this.history = history;
        this.module = history.getModule();
    }

    private static String getWindowTitle(History history) {
        return "Module History Details (ID=" + history.getId() + ")";
    }
    
    public void observe(HistoryDetailsPresenter presenter) {
        this.presenter = presenter;
    }

    public void display() {
        layout.removeAll();
        doLayout(layout);
        super.display();
    }

    private void doLayout(JPanel layout) {
        JPanel topPanel = new JPanel(new BorderLayout());
        messagePanel = new SingleLineMessagePanel();
        topPanel.add(messagePanel, BorderLayout.CENTER);
        Button button = new RefreshButton(this, "Refresh Module History Details", messagePanel);
        topPanel.add(button, BorderLayout.EAST);
        
        layout.add(topPanel, BorderLayout.NORTH);
        layout.add(tabbedPane(), BorderLayout.CENTER);
        layout.add(createButtonsPanel(), BorderLayout.SOUTH);
    }


    private JTabbedPane tabbedPane() {
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Summary", summaryPanel());
        tabbedPane.addTab("Datasets", datasetsPanel());
        tabbedPane.addTab("Parameters", parametersPanel());
        tabbedPane.addTab("Setup Script", setupScriptPanel());
        tabbedPane.addTab("User Script", userScriptPanel());
        tabbedPane.addTab("Teardown Script", teardownScriptPanel());
        tabbedPane.addTab("Logs", logsPanel());
        return tabbedPane;
    }

    private JPanel summaryPanel() {
        summaryPanel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        moduleName = new Label(module.getName());
        layoutGenerator.addLabelWidgetPair("Module Name:", moduleName, formPanel);

        runId = new Label(history.getRunId() + "");
        layoutGenerator.addLabelWidgetPair("Run ID:", runId, formPanel);

        runDate = new Label(CustomDateFormat.format_MM_DD_YYYY_HH_mm_ss(history.getCreationDate()));
        layoutGenerator.addLabelWidgetPair("Run Date:", runDate, formPanel);

        user = new Label(history.getCreator().getName());
        layoutGenerator.addLabelWidgetPair("User:", user, formPanel);

        status = new Label((history.getStatus() == null) ? "" : history.getStatus());
        layoutGenerator.addLabelWidgetPair("Status:", status, formPanel);

        result = new Label((history.getResult() == null) ? "" : history.getResult());
        layoutGenerator.addLabelWidgetPair("Result:", result, formPanel);

        comment = new Label((history.getComment() == null) ? "" : history.getComment());
        layoutGenerator.addLabelWidgetPair("Comment:", comment, formPanel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(formPanel, 7, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        summaryPanel.add(formPanel, BorderLayout.PAGE_START);
        return summaryPanel;
    }

    private JPanel datasetsPanel() {
        datasetsTablePanel = new JPanel(new BorderLayout());
        datasetsTableData = new HistoryDatasetsTableData(history.getHistoryDatasets(), session);
        datasetsTable = new SelectableSortFilterWrapper(parentConsole, datasetsTableData, null);
        datasetsTablePanel.add(datasetsTable);

        datasetsPanel = new JPanel(new BorderLayout());
        datasetsPanel.add(datasetsTablePanel, BorderLayout.CENTER);
        datasetsPanel.add(datasetsCrudPanel(), BorderLayout.SOUTH);

        return datasetsPanel;
    }

    private JPanel parametersPanel() {
        parametersTablePanel = new JPanel(new BorderLayout());
        parametersTableData = new HistoryParametersTableData(history.getHistoryParameters());
        parametersTable = new SelectableSortFilterWrapper(parentConsole, parametersTableData, null);
        parametersTablePanel.add(parametersTable);

        parametersPanel = new JPanel(new BorderLayout());
        parametersPanel.add(parametersTablePanel, BorderLayout.CENTER);

        return parametersPanel;
    }

    private JPanel setupScriptPanel() {
        setupScriptPanel = new JPanel(new BorderLayout());
        setupScript = new TextArea("setup script", history.getSetupScript(), 60);
        setupScript.setEditable(false);
        setupScript.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        ScrollableComponent scrollableScripts = new ScrollableComponent(setupScript);
        scrollableScripts.setMaximumSize(new Dimension(575, 200));
        setupScriptPanel.add(scrollableScripts);

        return setupScriptPanel;
    }

    private JPanel userScriptPanel() {
        userScriptPanel = new JPanel(new BorderLayout());
        userScript = new TextArea("userScript", history.getUserScript(), 60);
        userScript.setEditable(false);
        userScript.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        ScrollableComponent scrollableScripts = new ScrollableComponent(userScript);
        scrollableScripts.setMaximumSize(new Dimension(575, 200));
        userScriptPanel.add(scrollableScripts);

        return userScriptPanel;
    }

    private JPanel teardownScriptPanel() {
        teardownScriptPanel = new JPanel(new BorderLayout());
        teardownScript = new TextArea("teardownScript", history.getTeardownScript(), 60);
        teardownScript.setEditable(false);
        teardownScript.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        ScrollableComponent scrollableScripts = new ScrollableComponent(teardownScript);
        scrollableScripts.setMaximumSize(new Dimension(575, 200));
        teardownScriptPanel.add(scrollableScripts);

        return teardownScriptPanel;
    }

    private JPanel logsPanel() {
        logsPanel = new JPanel(new BorderLayout());
        logs = new TextArea("logs", history.getLogMessages(), 60);
        logs.setEditable(false);
        logs.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        ScrollableComponent scrollableLogs = new ScrollableComponent(logs);
        scrollableLogs.setMaximumSize(new Dimension(575, 200));
        logsPanel.add(scrollableLogs);

        return logsPanel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        container.add(new CloseButton("Close", closeAction()));

        panel.add(container, BorderLayout.SOUTH);

        return panel;
    }

    @Override
    public void doRefresh() throws EmfException {
        module = presenter.getModule(module.getId());
        history = module.getModuleHistory().get(history.getRunId()-1);
        refreshSummary();
        refreshDatasets();
        refreshParameters();
        refreshScripts();
        refreshLogs();
    }

    private void refreshSummary() {
        moduleName.setText(module.getName());
        runId.setText(history.getRunId() + "");
        runDate.setText(CustomDateFormat.format_MM_DD_YYYY_HH_mm_ss(history.getCreationDate()));
        user.setText(history.getCreator().getName());
        status.setText((history.getStatus() == null) ? "" : history.getStatus());
        result.setText((history.getResult() == null) ? "" : history.getResult());
        comment.setText((history.getComment() == null) ? "" : history.getComment());
    }
    
    public void refreshDatasets() {
        datasetsTableData = new HistoryDatasetsTableData(history.getHistoryDatasets(), session);
        datasetsTable.refresh(datasetsTableData);
    }

    public void refreshParameters() {
        parametersTableData = new HistoryParametersTableData(history.getHistoryParameters());
        parametersTable.refresh(parametersTableData);
    }

    public void refreshScripts() {
           setupScript.setText(history.getSetupScript());
            userScript.setText(history.getUserScript());
        teardownScript.setText(history.getTeardownScript());
    }

    public void refreshLogs() {
        logs.setText(history.getLogMessages());
    }

    private JPanel datasetsCrudPanel() {
        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        Action viewAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewDatasets();
            }
        };
        SelectAwareButton viewButton = new SelectAwareButton("View Dataset Properties", viewAction, datasetsTable, confirmDialog);
        viewButton.setMnemonic('D');

        Action viewRelatedModulesAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewRelatedModules();
            }
        };
        Button viewRelatedModulesButton = new Button("View Related Modules", viewRelatedModulesAction);
        viewRelatedModulesButton.setMnemonic('M');

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        crudPanel.add(viewButton);
        crudPanel.add(viewRelatedModulesButton);

        return crudPanel;
    }

    private void viewDatasets() {
        clear();
        final List<?> datasets = selectedDatasets();
        if (datasets.isEmpty()) {
            messagePanel.setMessage("Please select one or more Datasets");
            return;
        }
        
        // Long running methods ...
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ComponentUtility.enableComponents(this, false);

        class ViewDatasetPropertiesTask extends SwingWorker<Void, Void> {
            
            private Container parentContainer;

            public ViewDatasetPropertiesTask(Container parentContainer) {
                this.parentContainer = parentContainer;
            }

            // Main task. Executed in background thread. Don't update GUI here
            @Override
            public Void doInBackground() throws EmfException  {
                for (Iterator iter = datasets.iterator(); iter.hasNext();) {
                    DatasetPropertiesViewer view = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
                    HistoryDataset historyDataset = (HistoryDataset) iter.next();
                    presenter.doDisplayPropertiesView(view, historyDataset);
                }
                return null;
            }

            // Executed in event dispatching thread
            @Override
            public void done() {
                try {
                    get();
                } catch (InterruptedException e1) {
//                    messagePanel.setError(e1.getMessage());
//                    setErrorMsg(e1.getMessage());
                } catch (ExecutionException e1) {
//                    messagePanel.setError(e1.getCause().getMessage());
//                    setErrorMsg(e1.getCause().getMessage());
                } finally {
                    ComponentUtility.enableComponents(parentContainer, true);
                    this.parentContainer.setCursor(null); //turn off the wait cursor
                }
            }
        };
        new ViewDatasetPropertiesTask(this).execute();
    }

    private void viewRelatedModules() {
        clear();
        final List datasets = selectedDatasets();
        if (datasets.isEmpty()) {
            messagePanel.setMessage("Please select a dataset");
            return;
        } else if (datasets.size() > 1) {
            messagePanel.setMessage("Please select only one dataset");
            return;
        }
        
        HistoryDataset historyDataset = (HistoryDataset) datasets.get(0);
        String mode = historyDataset.getModuleTypeVersionDataset().getMode();
        EmfDataset emfDataset = null;
        if (historyDataset.getDatasetId() != null) {
            try {
                emfDataset = session.dataService().getDataset(historyDataset.getDatasetId());
            } catch (EmfException ex) {
                // ignore exception
            }
        }
        if (emfDataset == null) {
            messagePanel.setMessage("The dataset does not exist");
            return;
        }

        Module[] modules = null;
        try {
            modules = presenter.getModules();
        } catch (EmfException e) {
            messagePanel.setError("Failed to get the modules: " + e.getMessage());
        }
        
        // bring up the window with all related modules
        RelatedModulesWindow view = new RelatedModulesWindow(session, parentConsole, desktopManager, emfDataset, modules);
        try {
            presenter.doDisplayRelatedModules(view);
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
    }

    private List<?> selectedDatasets() {
        return datasetsTable.selected();
    }

    private void clear() {
        messagePanel.clear();
    }

    public void windowClosing() {
        doClose();
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doClose();
            }
        };

        return action;
    }

    private void doClose() {
        presenter.doClose();
    }
}
