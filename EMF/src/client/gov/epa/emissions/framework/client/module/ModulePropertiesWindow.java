package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.ViewMode;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.module.History;
import gov.epa.emissions.framework.services.module.HistoryDataset;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.ModuleDataset;
import gov.epa.emissions.framework.services.module.ModuleParameter;
import gov.epa.emissions.framework.services.module.ModuleType;
import gov.epa.emissions.framework.services.module.ModuleTypeVersion;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionDataset;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionParameter;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.moduletype.ModuleTypeVersionPropertiesWindow;
import gov.epa.emissions.framework.client.moduletype.ModuleTypeVersionSelectionDialog;
import gov.epa.emissions.framework.client.moduletype.ModuleTypeVersionSelectionPresenter;
import gov.epa.emissions.framework.client.util.ComponentUtility;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;

public class ModulePropertiesWindow extends DisposableInteralFrame implements ModulePropertiesView, RefreshObserver {
    private ModulePropertiesPresenter presenter;

    private EmfConsole parentConsole;
    private EmfSession session;
    private static int counter = 0;

    ViewMode viewMode;
    
    private Date date;
    private Module module;
    private ModuleType moduleType;
    private ModuleTypeVersion moduleTypeVersion;

    // layout
    private JPanel layout;
    private SingleLineMessagePanel messagePanel;
    private JTabbedPane tabbedPane;
    private JPanel summaryPanel;
    private JPanel datasetsPanel;
    private JPanel parametersPanel;
    private JPanel algorithmPanel;
    private JPanel historyPanel;

    // summary
    private Label     moduleTypeName;
    private Label     moduleTypeVersionNumber;
    private Button    selectModuleTypeVersion;
    
    private TextField moduleName;
    private TextArea  moduleDescription;
    private Label     moduleCreator;
    private Label     moduleCreationDate;
    private Label     moduleLastModifiedDate;
    private Label     moduleLockOwner;
    private Label     moduleLockDate;
    private Label     moduleIsFinal;

    // datasets
    private JPanel datasetsTablePanel;
    private SelectableSortFilterWrapper datasetsTable;
    private ModuleDatasetsTableData datasetsTableData;

    // parameters
    private JPanel parametersTablePanel;
    private SelectableSortFilterWrapper parametersTable;
    private ModuleParametersTableData parametersTableData;

    // algorithm
    private TextArea algorithm;

    // history
    private JPanel historyTablePanel;
    private SelectableSortFilterWrapper historyTable;
    private ModuleHistoryTableData historyTableData;
    private SelectAwareButton viewButton;

    // buttons
    Button validateButton;
    Button saveButton;
    Button runButton;
    Button finalizeButton;
    Button closeButton;
    
    public ModulePropertiesWindow(EmfConsole parentConsole, DesktopManager desktopManager, EmfSession session, ViewMode viewMode, Module module, ModuleTypeVersion moduleTypeVersion) {
        super(getWindowTitle(viewMode, module), new Dimension(800, 600), desktopManager);

        layout = new JPanel();
        layout.setLayout(new BorderLayout());
        this.parentConsole = parentConsole;
        this.session = session;
        super.getContentPane().add(layout);
        
        this.viewMode = viewMode;
        
        date = new Date();
        if (module == null) {
            // assert moduleTypeVersion != null
            this.module = new Module();
            this.module.setName("");
            this.module.setDescription("");
            this.module.setCreationDate(date);
            this.module.setLastModifiedDate(date);
            this.module.setCreator(session.user());
            this.module.setIsFinal(false);
            setModuleTypeVersion(moduleTypeVersion);
        } else {
            this.module = module;
            this.moduleTypeVersion = module.getModuleTypeVersion();
            this.moduleType = this.moduleTypeVersion.getModuleType();
        }
    }

    private static String getWindowTitle(ViewMode viewMode, Module module) {
        if (module == null)
            return "New Module";
        
        String viewModeText = "";
        switch (viewMode)
        {
            case NEW:  viewModeText = "New"; break;
            case EDIT: viewModeText = "Edit"; break;
            case VIEW: viewModeText = "View"; break;
            default: break;
        }
        return viewModeText + " Module - " + module.getName();
    }
    
    public static ModuleTypeVersion selectModuleTypeVersion(EmfConsole parentConsole, EmfSession session, ModuleTypeVersion initialModuleTypeVersion) {
        ModuleTypeVersionSelectionDialog selectionView = new ModuleTypeVersionSelectionDialog(parentConsole, initialModuleTypeVersion);
        ModuleTypeVersionSelectionPresenter selectionPresenter = new ModuleTypeVersionSelectionPresenter(selectionView, session);
        try {
            selectionPresenter.display();
        } catch (Exception e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return selectionView.getSelectedModuleTypeVersion();
    }
    
    public static ModuleTypeVersion selectModuleTypeVersion(EmfConsole parentConsole, EmfSession session) {
        return selectModuleTypeVersion(parentConsole, session, null);
    }
    
    private void setModuleTypeVersion(ModuleTypeVersion moduleTypeVersion) {
        this.moduleTypeVersion = moduleTypeVersion;
        
        moduleType = moduleTypeVersion.getModuleType();
        
        module.setModuleTypeVersion(moduleTypeVersion);

        module.clearModuleDatasets();
        for(ModuleTypeVersionDataset moduleTypeVersionDataset : moduleTypeVersion.getModuleTypeVersionDatasets().values()) {
            ModuleDataset moduleDataset = new ModuleDataset();
            moduleDataset.setModule(module);
            moduleDataset.setPlaceholderName(moduleTypeVersionDataset.getPlaceholderName());
            if (moduleTypeVersionDataset.isModeOUT()) {
                moduleDataset.setOutputMethod(ModuleDataset.NEW);
                moduleDataset.setDatasetNamePattern(null);
                moduleDataset.setOverwriteExisting(null);
            } else {
                moduleDataset.setDatasetId(null);
                moduleDataset.setVersion(null);
            }
            module.addModuleDataset(moduleDataset);
        }
        
        module.clearModuleParameters();
        for(ModuleTypeVersionParameter moduleTypeVersionParameter : moduleTypeVersion.getModuleTypeVersionParameters().values()) {
            ModuleParameter moduleParameter = new ModuleParameter();
            moduleParameter.setModule(module);
            moduleParameter.setParameterName(moduleTypeVersionParameter.getParameterName());
            moduleParameter.setValue("");
            module.addModuleParameter(moduleParameter);
        }
    }

    private void refreshModuleTypeVersion() {
        moduleTypeName.setText(moduleType.getName());
        moduleTypeVersionNumber.setText(String.valueOf(moduleTypeVersion.getVersion()));
        refreshDatasets();
        refreshParameters();
        algorithm.setText(moduleTypeVersion.getAlgorithm());
        refreshHistory();
    }
    
    private Action selectModuleTypeVersionAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    ModuleTypeVersion newModuleTypeVersion = selectModuleTypeVersion(parentConsole, session);
                    if (newModuleTypeVersion != null && !newModuleTypeVersion.equals(moduleTypeVersion)) {
                        setModuleTypeVersion(newModuleTypeVersion);
                        refreshModuleTypeVersion();
                    }
                } catch (Exception ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        };

        return action;
    }

    private void doLayout(JPanel layout) {
        JPanel topPanel = new JPanel(new BorderLayout());
        messagePanel = new SingleLineMessagePanel();
        topPanel.add(messagePanel, BorderLayout.CENTER);
        Button button = new RefreshButton(this, "Refresh Module Properties", messagePanel);
        topPanel.add(button, BorderLayout.EAST);
        
        layout.add(topPanel, BorderLayout.NORTH);
        layout.add(tabbedPane(), BorderLayout.CENTER);
        layout.add(createButtonsPanel(), BorderLayout.SOUTH);
    }


    @Override
    public void doRefresh() throws EmfException {
        refreshDatasets();
        refreshParameters();
        refreshHistory();
    }

    public void observe(ModulePropertiesPresenter presenter) {
        this.presenter = presenter;
    }

    public void display() {
        layout.removeAll();
        doLayout(layout);
        super.display();
    }

    private JTabbedPane tabbedPane() {
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Summary", summaryPanel());
        tabbedPane.addTab("Datasets", datasetsPanel());
        tabbedPane.addTab("Parameters", parametersPanel());
        tabbedPane.addTab("Algorithm", algorithmPanel());
        tabbedPane.addTab("History", historyPanel());
        return tabbedPane;
    }

    private JPanel summaryPanel() {
        summaryPanel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        moduleTypeName = new Label(moduleType.getName());
        layoutGenerator.addLabelWidgetPair("Module Type:", moduleTypeName, formPanel);

        moduleTypeVersionNumber = new Label(String.valueOf(moduleTypeVersion.getVersion()));
        layoutGenerator.addLabelWidgetPair("Version:", moduleTypeVersionNumber, formPanel);

        selectModuleTypeVersion = new Button("Select Module Type Version", selectModuleTypeVersionAction());
        selectModuleTypeVersion.setEnabled(viewMode != ViewMode.VIEW);
        layoutGenerator.addLabelWidgetPair("", selectModuleTypeVersion, formPanel);

        moduleName = new TextField("name", 60);
        moduleName.setText(module.getName());
        moduleName.setMaximumSize(new Dimension(575, 20));
        moduleName.setEnabled(viewMode != ViewMode.VIEW);
        addChangeable(moduleName);
        layoutGenerator.addLabelWidgetPair("Module Name:", moduleName, formPanel);

        moduleDescription = new TextArea("description", module.getDescription(), 60, 8);
        moduleDescription.setEnabled(viewMode != ViewMode.VIEW);
        addChangeable(moduleDescription);
        ScrollableComponent descScrollableTextArea = new ScrollableComponent(moduleDescription);
        descScrollableTextArea.setMaximumSize(new Dimension(575, 200));
        layoutGenerator.addLabelWidgetPair("Description:", descScrollableTextArea, formPanel);

        moduleCreator = new Label(module.getCreator().getName());
        layoutGenerator.addLabelWidgetPair("Creator:", moduleCreator, formPanel);

        moduleCreationDate = new Label(CustomDateFormat.format_MM_DD_YYYY_HH_mm(module.getCreationDate()));
        layoutGenerator.addLabelWidgetPair("Creation Date:", moduleCreationDate, formPanel);

        moduleLastModifiedDate = new Label(CustomDateFormat.format_MM_DD_YYYY_HH_mm(module.getLastModifiedDate()));
        layoutGenerator.addLabelWidgetPair("Last Modified:", moduleLastModifiedDate, formPanel);

        String lockOwner = module.getLockOwner();
        String safeLockOwner = (lockOwner == null) ? "" : lockOwner;
        moduleLockOwner = new Label(safeLockOwner);
        layoutGenerator.addLabelWidgetPair("Lock Owner:", moduleLockOwner, formPanel);

        Date lockDate = module.getLockDate();
        String safeLockDate = (module.getLockDate() == null) ? "" : CustomDateFormat.format_MM_DD_YYYY_HH_mm(lockDate);
        moduleLockDate = new Label(safeLockDate);
        layoutGenerator.addLabelWidgetPair("Lock Date:", moduleLockDate, formPanel);

        moduleIsFinal = new Label(module.getIsFinal() ? "Yes" : "No");
        layoutGenerator.addLabelWidgetPair("Is Final:", moduleIsFinal, formPanel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(formPanel, 11, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        summaryPanel.add(formPanel, BorderLayout.PAGE_START);
        return summaryPanel;
    }

    private JPanel datasetsPanel() {
        datasetsTablePanel = new JPanel(new BorderLayout());
        datasetsTableData = new ModuleDatasetsTableData(module.getModuleDatasets(), session);
        datasetsTable = new SelectableSortFilterWrapper(parentConsole, datasetsTableData, null);
        datasetsTablePanel.add(datasetsTable);

        datasetsPanel = new JPanel(new BorderLayout());
        datasetsPanel.add(datasetsTablePanel, BorderLayout.CENTER);
        datasetsPanel.add(datasetsCrudPanel(), BorderLayout.SOUTH);

        return datasetsPanel;
    }

    private JPanel parametersPanel() {
        parametersTablePanel = new JPanel(new BorderLayout());
        parametersTableData = new ModuleParametersTableData(module.getModuleParameters(), false); // TODO: include OUT parameters when showing the results
        parametersTable = new SelectableSortFilterWrapper(parentConsole, parametersTableData, null);
        parametersTablePanel.add(parametersTable);

        parametersPanel = new JPanel(new BorderLayout());
        parametersPanel.add(parametersTablePanel, BorderLayout.CENTER);
        parametersPanel.add(parametersCrudPanel(), BorderLayout.SOUTH);

        return parametersPanel;
    }

    private JPanel algorithmPanel() {
        algorithmPanel = new JPanel(new BorderLayout());
        algorithm = new TextArea("algorithm", moduleTypeVersion.getAlgorithm(), 60);
        algorithm.setEditable(false);
        algorithm.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        ScrollableComponent scrollableAlgorithm = new ScrollableComponent(algorithm);
        scrollableAlgorithm.setMaximumSize(new Dimension(575, 200));
        algorithmPanel.add(scrollableAlgorithm);

        return algorithmPanel;
    }

    private JPanel historyPanel() {
        historyTablePanel = new JPanel(new BorderLayout());
        historyTableData = new ModuleHistoryTableData(module.getModuleHistory());
        historyTable = new SelectableSortFilterWrapper(parentConsole, historyTableData, null);
        historyTablePanel.add(historyTable);

        historyPanel = new JPanel(new BorderLayout());
        historyPanel.add(historyTablePanel, BorderLayout.CENTER);
        historyPanel.add(historyCrudPanel(), BorderLayout.SOUTH);

        return historyPanel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        validateButton = new Button("Validate", validateAction());
        validateButton.setMnemonic('V');
        
        saveButton = new SaveButton(saveAction());
        saveButton.setEnabled(viewMode != ViewMode.VIEW);
        
        runButton = new Button("Run", runAction());
        runButton.setMnemonic('R');
        runButton.setEnabled((viewMode == ViewMode.EDIT) && session.user().isAdmin());
        
        finalizeButton = new Button("Finalize", finalizeAction());
        finalizeButton.setMnemonic('F');
        finalizeButton.setEnabled((viewMode != ViewMode.VIEW) && !module.getIsFinal() && session.user().isAdmin());
        
        closeButton = new CloseButton("Close", closeAction());
        
        container.add(validateButton);
        container.add(saveButton);
        container.add(runButton);
        container.add(finalizeButton);
        container.add(closeButton);
        getRootPane().setDefaultButton(saveButton);

        panel.add(container, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel datasetsCrudPanel() {
        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editDatasets();
            }
        };
        SelectAwareButton editButton = new SelectAwareButton("Edit", editAction, datasetsTable, confirmDialog);

        Action viewAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewDatasets();
            }
        };
        SelectAwareButton viewDatasetsButton = new SelectAwareButton("View Dataset", viewAction, datasetsTable, confirmDialog);

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        crudPanel.add(editButton);
        crudPanel.add(viewDatasetsButton);
        if (!session.user().isAdmin() || (viewMode == ViewMode.VIEW)) {
            editButton.setEnabled(false);
        }

        return crudPanel;
    }

    private void editDatasets() {
        List selected = selectedDatasets();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more datasets");
            return;
        }   

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ModuleDataset moduleDataset = (ModuleDataset) iter.next();
            try {
                EditModuleDatasetWindow view = new EditModuleDatasetWindow(parentConsole, desktopManager, session, moduleDataset);
                EditModuleDatasetPresenter presenter = new EditModuleDatasetPresenter(session, view, this);
                presenter.doDisplay();
            }
            catch (Exception e) {
                messagePanel.setError("Could not edit: " + moduleDataset.getPlaceholderName() + "." + e.getMessage());
                break;
            }
        }
    }

    private void viewDatasets() {
        clear();
        final List<HistoryDataset> datasets = selectedDatasets();
        if (datasets.isEmpty()) {
            messagePanel.setMessage("Please select one or more Datasets");
            return;
        }
        
        //long running methods.....
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ComponentUtility.enableComponents(this, false);

        //Instances of javax.swing.SwingWorker are not reusable, so
        //we create new instances as needed.
        class ViewDatasetPropertiesTask extends SwingWorker<Void, Void> {
            
            private Container parentContainer;

            public ViewDatasetPropertiesTask(Container parentContainer) {
                this.parentContainer = parentContainer;
            }

            /*
             * Main task. Executed in background thread.
             * don't update gui here
             */
            @Override
            public Void doInBackground() throws EmfException  {
                for (Iterator iter = datasets.iterator(); iter.hasNext();) {
                    ModuleDataset moduleDataset = (ModuleDataset) iter.next();
                    DatasetPropertiesViewer view = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
                    presenter.doDisplayDatasetProperties(view, moduleDataset);
                }
                return null;
            }

            /*
             * Executed in event dispatching thread
             */
            @Override
            public void done() {
                try {
                    //make sure something didn't happen
                    get();
                    
                } catch (InterruptedException e1) {
//                    messagePanel.setError(e1.getMessage());
//                    setErrorMsg(e1.getMessage());
                } catch (ExecutionException e1) {
//                    messagePanel.setError(e1.getCause().getMessage());
//                    setErrorMsg(e1.getCause().getMessage());
                } finally {
//                    this.parentContainer.setCursor(null); //turn off the wait cursor
//                    this.parentContainer.
                    ComponentUtility.enableComponents(parentContainer, true);
                    this.parentContainer.setCursor(null); //turn off the wait cursor
                }
            }
        };
        new ViewDatasetPropertiesTask(this).execute();
    }

    private List selectedDatasets() {
        return datasetsTable.selected();
    }

    public void refreshDatasets() {
        datasetsTableData = new ModuleDatasetsTableData(module.getModuleDatasets(), session);
        datasetsTable.refresh(datasetsTableData);
    }

    private JPanel parametersCrudPanel() {
        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editParameters();
            }
        };
        SelectAwareButton editButton = new SelectAwareButton("Edit", editAction, parametersTable, confirmDialog);

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        crudPanel.add(editButton);
        if (!session.user().isAdmin() || (viewMode == ViewMode.VIEW)) {
            editButton.setEnabled(false);
        }

        return crudPanel;
    }

    private void editParameters() {
        List selected = selectedParameters();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more parameters");
            return;
        }   

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ModuleParameter moduleParameter = (ModuleParameter) iter.next();
            if (moduleParameter.getModuleTypeVersionParameter().isModeOUT())
                continue;
            try {
                EditModuleParameterWindow view = new EditModuleParameterWindow(parentConsole, desktopManager, session, moduleParameter);
                EditModuleParameterPresenter presenter = new EditModuleParameterPresenter(session, view, this);
                presenter.doDisplay();
            }
            catch (Exception e) {
                messagePanel.setError("Could not edit: " + moduleParameter.getParameterName() + "." + e.getMessage());
                break;
            }
        }
    }

    private List selectedParameters() {
        return parametersTable.selected();
    }

    public void refreshParameters() {
        parametersTableData = new ModuleParametersTableData(module.getModuleParameters(), false); // TODO: include OUT parameters when showing the results);
        parametersTable.refresh(parametersTableData);
    }

    private JPanel historyCrudPanel() {
        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        Action viewAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewHistory();
            }
        };
        viewButton = new SelectAwareButton("View", viewAction, historyTable, confirmDialog);
        viewButton.setEnabled(!historyTableData.rows().isEmpty());

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        crudPanel.add(viewButton);
        return crudPanel;
    }

    private void viewHistory() {
        List selected = selectedHistory();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more history records");
            return;
        }   

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            History history = (History) iter.next();
            try {
                HistoryDetailsWindow view = new HistoryDetailsWindow(parentConsole, desktopManager, session, history);
                HistoryDetailsPresenter presenter = new HistoryDetailsPresenter(session, view);
                presenter.doDisplay();
            }
            catch (Exception e) {
                messagePanel.setError("Could not view module history record #" + history.getRunId() + ": " + e.getMessage());
                break;
            }
        }
    }

    private List selectedHistory() {
        return historyTable.selected();
    }

    public void refreshHistory() {
        historyTableData = new ModuleHistoryTableData(module.getModuleHistory());
        historyTable.refresh(historyTableData);
        viewButton.setEnabled(!historyTableData.rows().isEmpty());
    }

    private void clear() {
        messagePanel.clear();
    }

    private boolean checkTextFields() {
        if (moduleName.getText().equals(""))
            messagePanel.setError("Name field should be a non-empty string.");
        else{
            messagePanel.clear();
            return true;
        }

        return false;
    }

    private Action validateAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                StringBuilder error = new StringBuilder();
                if (module.isValid(error)) {
                    messagePanel.setMessage("This module is valid.");
                }
                else {
                    messagePanel.setError(error.toString());
                }
            }
        };

        return action;
    }

    private void doRun() {
        String message = "Are you sure you want to run the '" + module.getName() + "' module?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (selection != JOptionPane.YES_OPTION)
            return;
        
        StringBuilder error = new StringBuilder();
        if (!module.isValid(error)) {
            messagePanel.setError(String.format("Module is invalid: %s", error.toString()));
            return;
        }
        
        try {
            presenter.runModule(module);
            messagePanel.setMessage("The module has been executed.");
        } catch (EmfException e) {
            messagePanel.setError("The module failed to run: " + e.getMessage());
        }
    }
    
    private void doFinalize() {
        StringBuilder error = new StringBuilder();
        if (!module.isValid(error)) {
            messagePanel.setError("Can't finalize. This module is invalid. " + error.toString());
            return;
        }

        if (!moduleTypeVersion.getIsFinal()) {
            messagePanel.setError("Can't finalize. The module type version is not final.");
            return;
        }
        
        // check that all input datasets are final
        for(ModuleDataset moduleDataset : module.getModuleDatasets().values()) {
            ModuleTypeVersionDataset moduleTypeVersionDataset = moduleDataset.getModuleTypeVersionDataset();
            String mode = moduleTypeVersionDataset.getMode();
            if (mode.equals(ModuleTypeVersionDataset.OUT))
                continue;
            if (moduleDataset.getDatasetId() == null || moduleDataset.getVersion() == null) {
                String errorMessage = String.format("Can't finalize. The module dataset for '%s' placeholder was not.", moduleDataset.getPlaceholderName());
                messagePanel.setError(errorMessage);
                return;
            }
            Version version = null;
            try {
                version = session.dataEditorService().getVersion(moduleDataset.getDatasetId(), moduleDataset.getVersion());
                if (version == null) {
                    String errorMessage = String.format("Can't finalize. The module dataset version for '%s' placeholder is invalid.", moduleDataset.getPlaceholderName());
                    messagePanel.setError(errorMessage);
                    return;
                }
            } catch (Exception e) {
                String errorMessage = String.format("Can't finalize. The module dataset version for '%s' placeholder is invalid.", moduleDataset.getPlaceholderName());
                messagePanel.setError(errorMessage);
                return;
            }
            if (!version.isFinalVersion()) {
                String errorMessage = String.format("Can't finalize. The module dataset version for '%s' placeholder is not final.", moduleDataset.getPlaceholderName());
                messagePanel.setError(errorMessage);
                return;
            }
        }
        
        String title = module.getName();
        int selection = JOptionPane.showConfirmDialog(parentConsole, "Are you sure you want to finalize this module?",
                                                      title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            module.setIsFinal(true);
            moduleIsFinal.setText(module.getIsFinal() ? "Yes" : "No");
            if (doSave()) {
                JOptionPane.showConfirmDialog(parentConsole, "This module has been finalized!",
                                              title, JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE);
                doClose();
            }
        }
    }

    private Action runAction() {
        final ModulePropertiesWindow modulePropertiesWindow = this;
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                modulePropertiesWindow.doRun();
            }
        };
        return action;
    }

    private Action finalizeAction() {
        final ModulePropertiesWindow modulePropertiesWindow = this;
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                modulePropertiesWindow.doFinalize();
            }
        };
        return action;
    }

    private boolean doSave() {
        try {
            resetChanges();
            
            Date date = new Date();
            module.setName(moduleName.getText());
            module.setDescription(moduleDescription.getText());
            module.setLastModifiedDate(date);

            if (viewMode == ViewMode.NEW) {
                module.setCreationDate(date);
                module.setCreator(session.user());
                module = presenter.addModule(module);
                viewMode = ViewMode.EDIT;
                runButton.setEnabled((viewMode == ViewMode.EDIT) && session.user().isAdmin());
                Module lockedModule = presenter.obtainLockedModule(module);
                if (lockedModule == null || !lockedModule.isLocked(session.user())) {
                    throw new EmfException("Failed to lock module.");
                }
                module = lockedModule;
            } else {
                presenter.updateModule(module);
            }
            doRefresh();
            messagePanel.setMessage("Saved module.");
            return true;
        }
        catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
        return false;
    }
    
    private Action saveAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if (checkTextFields()) {
                    doSave();
                }
            }
        };

        return action;
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
        if (shouldDiscardChanges())
            if (viewMode == ViewMode.EDIT) {
                try {
                    module = presenter.releaseLockedModule(module);
                } catch (EmfException e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
            }
            presenter.doClose();
    }
}
