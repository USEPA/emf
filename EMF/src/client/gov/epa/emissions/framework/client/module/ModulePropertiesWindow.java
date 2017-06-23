package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.EditableComboBox;
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
import gov.epa.emissions.framework.client.DefaultEmfSession.ObjectCacheType;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.Projects;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.module.History;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.ModuleDataset;
import gov.epa.emissions.framework.services.module.ModuleInternalDataset;
import gov.epa.emissions.framework.services.module.ModuleInternalParameter;
import gov.epa.emissions.framework.services.module.ModuleParameter;
import gov.epa.emissions.framework.services.module.ModuleType;
import gov.epa.emissions.framework.services.module.ModuleTypeVersion;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionDataset;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionParameter;
import gov.epa.emissions.framework.services.module.Tag;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.moduletype.AddTagsDialog;
import gov.epa.emissions.framework.client.moduletype.ModuleTypeVersionConnectionsTableData;
import gov.epa.emissions.framework.client.moduletype.ModuleTypeVersionPropertiesWindow;
import gov.epa.emissions.framework.client.moduletype.ModuleTypeVersionSelectionDialog;
import gov.epa.emissions.framework.client.moduletype.ModuleTypeVersionSelectionPresenter;
import gov.epa.emissions.framework.client.moduletype.ModuleTypeVersionSubmodulesTableData;
import gov.epa.emissions.framework.client.moduletype.RemoveTagsDialog;
import gov.epa.emissions.framework.client.moduletype.TagsObserver;
import gov.epa.emissions.framework.client.util.ComponentUtility;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

public class ModulePropertiesWindow extends DisposableInteralFrame implements ModulePropertiesView, RefreshObserver, TagsObserver {
    private ModulePropertiesPresenter presenter;

    private EmfConsole parentConsole;
    private EmfSession session;

    private ViewMode viewMode;

    private Project[] allProjects;

    private Date date;
    private Module module;
    private ModuleType moduleType;
    private ModuleTypeVersion moduleTypeVersion;

    private TextField isDirty;
    private String initialStatus;

    // layout
    private JPanel layout;
    private SingleLineMessagePanel messagePanel;
    private JTabbedPane tabbedPane;

    // summary
    private JPanel    summaryPanel;
    private Label     moduleTypeName;
    private Label     moduleTypeVersionNumber;
    private Button    selectModuleTypeVersion;
    
    private TextField moduleName;
    private TextArea  moduleDescription;
    private TextArea  moduleTags;
    private EditableComboBox projectsCB;
    private Label     moduleCreator;
    private Label     moduleCreationDate;
    private Label     moduleLastModifiedDate;
    private Label     moduleLockOwner;
    private Label     moduleLockDate;
    private Label     moduleIsFinal;

    // datasets
    private JPanel datasetsPanel;
    private JPanel datasetsTablePanel;
    private SelectableSortFilterWrapper datasetsTable;
    private ModuleDatasetsTableData datasetsTableData;

    // parameters
    private JPanel parametersPanel;
    private JPanel parametersTablePanel;
    private SelectableSortFilterWrapper parametersTable;
    private ModuleParametersTableData parametersTableData;

    // algorithm
    private JPanel          algorithmPanel;
    private RSyntaxTextArea algorithm;
    private RTextScrollPane algorithmScrollPane;

    // submodules
    private JPanel submodulesPanel;
    private JPanel submodulesTablePanel;
    private SelectableSortFilterWrapper submodulesTable;
    private ModuleTypeVersionSubmodulesTableData submodulesTableData;

    // connections
    private JPanel connectionsPanel;
    private JPanel connectionsTablePanel;
    private SelectableSortFilterWrapper connectionsTable;
    private ModuleTypeVersionConnectionsTableData connectionsTableData;

    // internal datasets
    private JPanel internalDatasetsPanel;
    private JPanel internalDatasetsTablePanel;
    private SelectableSortFilterWrapper internalDatasetsTable;
    private ModuleInternalDatasetsTableData internalDatasetsTableData;

    // internal parameters
    private JPanel internalParametersPanel;
    private JPanel internalParametersTablePanel;
    private SelectableSortFilterWrapper internalParametersTable;
    private ModuleInternalParametersTableData internalParametersTableData;

    // history
    private JPanel historyPanel;
    private JPanel historyTablePanel;
    private SelectableSortFilterWrapper historyTable;
    private ModuleHistoryTableData historyTableData;
    private SelectAwareButton viewButton;

    // buttons
    Button statusButton;
    Label  statusText;
    Button validateButton;
    Button saveButton;
    Button runButton;
    Button finalizeButton;
    Button closeButton;
    
    public ModulePropertiesWindow(EmfConsole parentConsole, DesktopManager desktopManager, EmfSession session, ViewMode viewMode, Module module, ModuleTypeVersion moduleTypeVersion) {
        super(getWindowTitle(viewMode, module), new Dimension(850, 700), desktopManager);

        layout = new JPanel();
        layout.setLayout(new BorderLayout());
        this.parentConsole = parentConsole;
        this.session = session;
        super.getContentPane().add(layout);
        
        this.viewMode = viewMode;
        
        this.isDirty = new TextField("", 5);
        addChangeable(this.isDirty);
        this.initialStatus = new String();
        
        this.date = new Date();
        if (module == null) {
            // assert moduleTypeVersion != null
            this.module = new Module();
            this.module.setName("");
            this.module.setDescription("");
            this.module.setCreationDate(this.date);
            this.module.setLastModifiedDate(this.date);
            this.module.setCreator(session.user());
            this.module.setIsFinal(false);
            setModuleTypeVersion(moduleTypeVersion);
            this.isDirty.setText("true");
            this.initialStatus = "New module.";
        } else {
            this.module = module;
            this.moduleTypeVersion = module.getModuleTypeVersion();
            this.moduleType = this.moduleTypeVersion.getModuleType();
            if (this.module.refresh(this.date)) {
                this.isDirty.setText("true");
                this.initialStatus = "The module has been updated to match the module type version.";
            }
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
            e.printStackTrace();
        }
        return selectionView.getSelectedModuleTypeVersion();
    }
    
    public static ModuleTypeVersion selectModuleTypeVersion(EmfConsole parentConsole, EmfSession session) {
        return selectModuleTypeVersion(parentConsole, session, null);
    }
    
    private void setModuleTypeVersion(ModuleTypeVersion moduleTypeVersion) {
        if (this.moduleTypeVersion != null && this.moduleTypeVersion.getId() == moduleTypeVersion.getId())
            return; // nothing to do
        
        Module oldModuleCopy = module.deepCopy(session.user());
        
        this.moduleTypeVersion = moduleTypeVersion;
        moduleType = moduleTypeVersion.getModuleType();
        module.setModuleTypeVersion(moduleTypeVersion);
        if (module.getTags().isEmpty()) {
            for(Tag tag : module.getModuleTypeVersion().getModuleType().getTags()) {
                module.addTag(tag);
            }
        }

        module.clearModuleDatasets();
        for(ModuleTypeVersionDataset moduleTypeVersionDataset : moduleTypeVersion.getModuleTypeVersionDatasets().values()) {
            ModuleDataset moduleDataset = new ModuleDataset();
            moduleDataset.setModule(module);
            moduleDataset.setPlaceholderName(moduleTypeVersionDataset.getPlaceholderName());
            if (!moduleDataset.transferSettings(oldModuleCopy))
                moduleDataset.initSettings();
            module.addModuleDataset(moduleDataset);
        }
        
        module.clearModuleParameters();
        for(ModuleTypeVersionParameter moduleTypeVersionParameter : moduleTypeVersion.getModuleTypeVersionParameters().values()) {
            ModuleParameter moduleParameter = new ModuleParameter();
            moduleParameter.setModule(module);
            moduleParameter.setParameterName(moduleTypeVersionParameter.getParameterName());
            if (!moduleParameter.transferSettings(oldModuleCopy))
                moduleParameter.initSettings();
            module.addModuleParameter(moduleParameter);
        }
        
        module.setModuleInternalDatasets(module.computeInternalDatasets());
        for(String placeholderPath : module.getModuleInternalDatasets().keySet()) {
            if (!oldModuleCopy.getModuleInternalDatasets().containsKey(placeholderPath))
                continue;
            ModuleInternalDataset newModuleInternalDataset = module.getModuleInternalDatasets().get(placeholderPath);
            ModuleInternalDataset oldModuleInternalDataset = oldModuleCopy.getModuleInternalDatasets().get(placeholderPath);
            newModuleInternalDataset.setKeep(oldModuleInternalDataset.getKeep());
            newModuleInternalDataset.setDatasetNamePattern(oldModuleInternalDataset.getDatasetNamePattern());
        }
        
        module.setModuleInternalParameters(module.computeInternalParameters());
        for(String parameterPath : module.getModuleInternalParameters().keySet()) {
            if (!oldModuleCopy.getModuleInternalParameters().containsKey(parameterPath))
                continue;
            ModuleInternalParameter newModuleInternalParameter = module.getModuleInternalParameters().get(parameterPath);
            ModuleInternalParameter oldModuleInternalParameter = oldModuleCopy.getModuleInternalParameters().get(parameterPath);
            newModuleInternalParameter.setKeep(oldModuleInternalParameter.getKeep());
        }
        
        // TODO should we clear the module history too?
        
        isDirty.setText("true");
    }

    private void refreshModuleTypeVersion() {
        moduleTypeName.setText(moduleType.getName());
        moduleTypeVersionNumber.setText(String.valueOf(moduleTypeVersion.getVersion()));
        refreshTabbedPane();
        refreshDatasets();
        refreshParameters();
        if (moduleTypeVersion.isComposite()) {
            refreshSubmodules();
            refreshConnections();
            refreshInternalDatasets();
            refreshInternalParameters();
        } else {
            algorithm.setText(moduleTypeVersion.getAlgorithm());
        }
        
        refreshHistory();
    }

    private void refreshStatusText() {
        StringBuilder reason = new StringBuilder();
        boolean isOutOfDate = isOutOfDate(reason);
        refreshStatusText(isOutOfDate);
    }
    
    private void refreshStatusText(boolean isOutOfDate) {
        if (isOutOfDate) {
            statusText.setText("Out-Of-Date");
            statusText.setForeground(Color.RED);
        } else {
            statusText.setText("Up-To-Date");
            statusText.setForeground(Color.BLUE);
        }
    }

    @Override
    public void doRefresh() throws EmfException {
        module = presenter.getModule(module.getId());
        moduleTypeVersion = module.getModuleTypeVersion();
        moduleType = moduleTypeVersion.getModuleType();
        moduleName.setText(module.getName());
        moduleDescription.setText(module.getDescription());
        moduleDescription.setText(module.getTagsText());
        moduleCreator.setText(module.getCreator().getName());
        moduleCreationDate.setText(CustomDateFormat.format_MM_DD_YYYY_HH_mm(module.getCreationDate()));
        moduleLastModifiedDate.setText(CustomDateFormat.format_MM_DD_YYYY_HH_mm(module.getLastModifiedDate()));
        String lockOwner = module.getLockOwner();
        String safeLockOwner = (lockOwner == null) ? "" : lockOwner;
        moduleLockOwner.setText(safeLockOwner);
        Date lockDate = module.getLockDate();
        String safeLockDate = (module.getLockDate() == null) ? "" : CustomDateFormat.format_MM_DD_YYYY_HH_mm(lockDate);
        moduleLockDate.setText(safeLockDate);
        moduleIsFinal.setText(module.getIsFinal() ? "Yes" : "No");
        refreshModuleTypeVersion();
        resetChanges();
        refreshStatusText();
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
        if (!initialStatus.isEmpty()) {
            if (viewMode == ViewMode.VIEW)
                messagePanel.setError(initialStatus + " Please edit the module to save the changes.");
            else
                messagePanel.setMessage(initialStatus);
        }
        topPanel.add(messagePanel, BorderLayout.CENTER);
        Button button = new RefreshButton(this, "Refresh Module Properties", messagePanel);
        topPanel.add(button, BorderLayout.EAST);
        
        layout.add(topPanel, BorderLayout.NORTH);
        layout.add(tabbedPane(), BorderLayout.CENTER);
        layout.add(createButtonsPanel(), BorderLayout.SOUTH);
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
        summaryPanel = summaryPanel();
        datasetsPanel = datasetsPanel();
        parametersPanel = parametersPanel();
        submodulesPanel = submodulesPanel();
        connectionsPanel = connectionsPanel();
        // flowchartPanel = flowchartPanel();
        internalDatasetsPanel = internalDatasetsPanel();
        internalParametersPanel = internalParametersPanel();
        algorithmPanel = algorithmPanel();
        historyPanel = historyPanel();
        refreshTabbedPane();
        return tabbedPane;
    }

    private void refreshTabbedPane() {
        tabbedPane.removeAll();
        tabbedPane.addTab("Summary", summaryPanel);
        tabbedPane.addTab("Datasets", datasetsPanel);
        tabbedPane.addTab("Parameters", parametersPanel);
        if (moduleTypeVersion.isComposite()) {
            tabbedPane.addTab("Submodules", submodulesPanel);
            tabbedPane.addTab("Connections", connectionsPanel);
            // tabbedPane.addTab("Flowchart", flowchartPanel);
            tabbedPane.addTab("Internal Datasets", internalDatasetsPanel);
            tabbedPane.addTab("Internal Parameters", internalParametersPanel);
        } else {
            tabbedPane.addTab("Algorithm", algorithmPanel);
        }
        tabbedPane.addTab("History", historyPanel);
    }

    private JPanel summaryPanel() {
        summaryPanel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        moduleTypeName = new Label(moduleType.getName());
        layoutGenerator.addLabelWidgetPair("Module Type:", moduleTypeName, formPanel);

        moduleTypeVersionNumber = new Label(moduleTypeVersion.versionName());
        layoutGenerator.addLabelWidgetPair("Version:", moduleTypeVersionNumber, formPanel);

        selectModuleTypeVersion = new Button("Select Module Type Version", selectModuleTypeVersionAction());
        selectModuleTypeVersion.setEnabled(viewMode != ViewMode.VIEW);
        layoutGenerator.addLabelWidgetPair("", selectModuleTypeVersion, formPanel);

        moduleName = new TextField("name", 60);
        moduleName.setText(module.getName());
        moduleName.setMaximumSize(new Dimension(575, 20));
        moduleName.setEditable(viewMode != ViewMode.VIEW);
        if (viewMode != ViewMode.VIEW) {
            addChangeable(moduleName);
        }
        layoutGenerator.addLabelWidgetPair("Module Name:", moduleName, formPanel);

        moduleDescription = new TextArea("description", module.getDescription(), 60, 5);
        moduleDescription.setLineWrap(true);
        moduleDescription.setEditable(viewMode != ViewMode.VIEW);
        if (viewMode != ViewMode.VIEW) {
            addChangeable(moduleDescription);
        }
        ScrollableComponent descScrollableTextArea = new ScrollableComponent(moduleDescription);
        descScrollableTextArea.setMaximumSize(new Dimension(575, 200));
        layoutGenerator.addLabelWidgetPair("Description:", descScrollableTextArea, formPanel);

        moduleTags = new TextArea("tags", module.getTagsText(), 60, 3);
        moduleTags.setLineWrap(true);
        moduleTags.setEditable(false);
        ScrollableComponent tagsScrollableTextArea = new ScrollableComponent(moduleTags);
        tagsScrollableTextArea.setMaximumSize(new Dimension(575, 200));
        layoutGenerator.addLabelWidgetPair("Tags:", tagsScrollableTextArea, formPanel);

        layoutGenerator.addLabelWidgetPair("", tagsCrudPanel(), formPanel);

        allProjects = session.getProjects();
        projectsCB = new EditableComboBox(allProjects);
        projectsCB.setSelectedItem(module.getProject());
        projectsCB.setPreferredSize(new Dimension(250, 20));
        projectsCB.setEnabled(viewMode != ViewMode.VIEW);
        if (viewMode != ViewMode.VIEW) {
            addChangeable(projectsCB);
            if (!session.user().isAdmin())
                projectsCB.setEditable(false);
        }
        layoutGenerator.addLabelWidgetPair("Project:", projectsCB, formPanel);

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
        String safeLockDate = (lockDate == null) ? "" : CustomDateFormat.format_MM_DD_YYYY_HH_mm(lockDate);
        moduleLockDate = new Label(safeLockDate);
        layoutGenerator.addLabelWidgetPair("Lock Date:", moduleLockDate, formPanel);

        moduleIsFinal = new Label(module.getIsFinal() ? "Yes" : "No");
        layoutGenerator.addLabelWidgetPair("Is Final:", moduleIsFinal, formPanel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(formPanel, 14, 2, // rows, cols
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
        parametersTableData = new ModuleParametersTableData(module.getModuleParameters());
        parametersTable = new SelectableSortFilterWrapper(parentConsole, parametersTableData, null);
        parametersTablePanel.add(parametersTable);

        parametersPanel = new JPanel(new BorderLayout());
        parametersPanel.add(parametersTablePanel, BorderLayout.CENTER);
        parametersPanel.add(parametersCrudPanel(), BorderLayout.SOUTH);

        return parametersPanel;
    }

    private JPanel internalDatasetsPanel() {
        internalDatasetsTablePanel = new JPanel(new BorderLayout());
        internalDatasetsTableData = new ModuleInternalDatasetsTableData(module.getModuleInternalDatasets(), session);
        internalDatasetsTable = new SelectableSortFilterWrapper(parentConsole, internalDatasetsTableData, null);
        internalDatasetsTablePanel.add(internalDatasetsTable);

        internalDatasetsPanel = new JPanel(new BorderLayout());
        internalDatasetsPanel.add(internalDatasetsTablePanel, BorderLayout.CENTER);
        internalDatasetsPanel.add(internalDatasetsCrudPanel(), BorderLayout.SOUTH);

        return internalDatasetsPanel;
    }

    private JPanel internalParametersPanel() {
        internalParametersTablePanel = new JPanel(new BorderLayout());
        internalParametersTableData = new ModuleInternalParametersTableData(module.getModuleInternalParameters());
        internalParametersTable = new SelectableSortFilterWrapper(parentConsole, internalParametersTableData, null);
        internalParametersTablePanel.add(internalParametersTable);

        internalParametersPanel = new JPanel(new BorderLayout());
        internalParametersPanel.add(internalParametersTablePanel, BorderLayout.CENTER);
        internalParametersPanel.add(internalParametersCrudPanel(), BorderLayout.SOUTH);

        return internalParametersPanel;
    }

    private JPanel algorithmPanel() {
        algorithmPanel = new JPanel(new BorderLayout());
        algorithmPanel = new JPanel(new BorderLayout());
        algorithm = new RSyntaxTextArea(((moduleTypeVersion.getAlgorithm() == null) ? "" : moduleTypeVersion.getAlgorithm()));
        algorithm.setEditable(false);
        algorithm.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        algorithm.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        algorithm.setCodeFoldingEnabled(true);
        algorithmScrollPane = new RTextScrollPane(algorithm);
        algorithmPanel.add(algorithmScrollPane);
        return algorithmPanel;
    }

    private JPanel submodulesPanel() {
        submodulesTablePanel = new JPanel(new BorderLayout());
        submodulesTableData = new ModuleTypeVersionSubmodulesTableData(moduleTypeVersion.getModuleTypeVersionSubmodules());
        submodulesTable = new SelectableSortFilterWrapper(parentConsole, submodulesTableData, null);
        submodulesTablePanel.add(submodulesTable);

        submodulesPanel = new JPanel(new BorderLayout());
        submodulesPanel.add(submodulesTablePanel, BorderLayout.CENTER);

        return submodulesPanel;
    }

    private JPanel connectionsPanel() {
        connectionsTablePanel = new JPanel(new BorderLayout());
        connectionsTableData = new ModuleTypeVersionConnectionsTableData(moduleTypeVersion);
        connectionsTable = new SelectableSortFilterWrapper(parentConsole, connectionsTableData, null);
        connectionsTablePanel.add(connectionsTable);

        connectionsPanel = new JPanel(new BorderLayout());
        connectionsPanel.add(connectionsTablePanel, BorderLayout.CENTER);

        return connectionsPanel;
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

        //-----------------------------------------------------
        
        JPanel statusContainer = new JPanel();
        FlowLayout statusLayout = new FlowLayout();
        statusLayout.setHgap(20);
        statusLayout.setVgap(20);
        statusContainer.setLayout(statusLayout);
        
        statusButton = new Button("Status", statusAction());
        statusButton.setMnemonic('S');
        
        statusText = new Label("");
        refreshStatusText();
        
        statusContainer.add(statusButton);
        statusContainer.add(statusText);
        
        //-----------------------------------------------------
        
        JPanel buttonsContainer = new JPanel();
        FlowLayout buttonsLayout = new FlowLayout();
        buttonsLayout.setHgap(20);
        buttonsLayout.setVgap(20);
        buttonsContainer.setLayout(buttonsLayout);

        validateButton = new Button("Validate", validateAction());
        validateButton.setMnemonic('V');
        
        saveButton = new SaveButton(saveAction());
        saveButton.setEnabled(viewMode != ViewMode.VIEW);
        
        runButton = new Button("Run", runAction());
        runButton.setMnemonic('u');
        runButton.setEnabled(viewMode != ViewMode.VIEW);
        
        finalizeButton = new Button("Finalize", finalizeAction());
        finalizeButton.setMnemonic('F');
        finalizeButton.setEnabled(viewMode != ViewMode.VIEW);
        
        closeButton = new CloseButton("Close", closeAction());
        
        buttonsContainer.add(validateButton);
        buttonsContainer.add(saveButton);
        buttonsContainer.add(runButton);
        buttonsContainer.add(finalizeButton);
        buttonsContainer.add(closeButton);

        //-----------------------------------------------------
        
        panel.add(statusContainer, BorderLayout.WEST);
        panel.add(buttonsContainer, BorderLayout.CENTER);

        getRootPane().setDefaultButton(saveButton);
        return panel;
    }

    //-----------------------------------------------------------------
    
    private JPanel tagsCrudPanel() {
        Action addTagsAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                addTags();
            }
        };
        Button addTagsButton = new Button("Add Tags", addTagsAction);
        addTagsButton.setMnemonic('A');

        Action removeTagsAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                removeTags();
            }
        };
        Button removeTagsButton = new Button("Remove Tags", removeTagsAction);
        removeTagsButton.setMnemonic('e');

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        crudPanel.add(addTagsButton);
        crudPanel.add(removeTagsButton);
        if (viewMode == ViewMode.VIEW) {
            addTagsButton.setEnabled(false);
            removeTagsButton.setEnabled(false);
        }

        return crudPanel;
    }

    private void addTags() {
        AddTagsDialog view = new AddTagsDialog(parentConsole, module.getTags(), this);
        try {
            presenter.displayAddTagsView(view);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentConsole, 
                    "Failed to open Add Tags dialog box:\n\n" + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeTags() {
        RemoveTagsDialog view = new RemoveTagsDialog(parentConsole, module.getTags(), this);
        try {
            presenter.displayRemoveTagsView(view);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentConsole, 
                    "Failed to open Remove Tags dialog box:\n\n" + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //-----------------------------------------------------------------
    
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
        SelectAwareButton viewDatasetsButton = new SelectAwareButton("View Dataset Properties", viewAction, datasetsTable, confirmDialog);
        viewDatasetsButton.setMnemonic('D');

        Action viewRelatedModulesAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewRelatedModules();
            }
        };
        Button viewRelatedModulesButton = new Button("View Related Modules", viewRelatedModulesAction);
        viewRelatedModulesButton.setMnemonic('M');

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        crudPanel.add(editButton);
        crudPanel.add(viewDatasetsButton);
        crudPanel.add(viewRelatedModulesButton);
        if (viewMode == ViewMode.VIEW) {
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
                messagePanel.setError("Could not edit: " + moduleDataset.getPlaceholderName() + ". " + e.getMessage());
                break;
            }
        }
    }

    private void viewDatasets() {
        clear();
        @SuppressWarnings("rawtypes")
        final List datasets = selectedDatasets();
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
                    EmfDataset emfDataset = session.moduleService().getEmfDatasetForModuleDataset(moduleDataset.getId(), moduleDataset.getDatasetId(), moduleDataset.getDatasetNamePattern());
                    if (emfDataset != null) {
                        DatasetPropertiesViewer view = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
                        presenter.doDisplayDatasetProperties(view, emfDataset);
                    }
                }
                return null;
            }

            /*
             * Executed in event dispatching thread
             */
            @Override
            public void done() {
                try {
                    get();
                } catch (InterruptedException e1) {
                    // ignore
                } catch (ExecutionException e1) {
                    // ignore
                } finally {
                    ComponentUtility.enableComponents(parentContainer, true);
                    this.parentContainer.setCursor(null); //turn off the wait cursor
                }
            }
        }
        new ViewDatasetPropertiesTask(this).execute();
    }

    private void viewRelatedModules() {
        clear();
        @SuppressWarnings("rawtypes")
        final List datasets = selectedDatasets();
        if (datasets.isEmpty()) {
            messagePanel.setMessage("Please select a dataset");
            return;
        } else if (datasets.size() > 1) {
            messagePanel.setMessage("Please select only one dataset");
            return;
        }
        
        ModuleDataset moduleDataset = (ModuleDataset) datasets.get(0);
        String mode = moduleDataset.getModuleTypeVersionDataset().getMode();
        String outputMethod = moduleDataset.getOutputMethod();
        EmfDataset emfDataset = null;
        try {
            emfDataset = session.moduleService().getEmfDatasetForModuleDataset(moduleDataset.getId(), moduleDataset.getDatasetId(), moduleDataset.getDatasetNamePattern());
        } catch (EmfException e1) {
            // NOTE Auto-generated catch block
            e1.printStackTrace();
        }
        if (emfDataset == null) {
            messagePanel.setMessage("The dataset does not exist");
            return;
        }

        // bring up the window with all related modules
        RelatedModulesWindow view = new RelatedModulesWindow(session, parentConsole, desktopManager, emfDataset);
        presenter.doDisplayRelatedModules(view);
    }

    private List selectedDatasets() {
        return datasetsTable.selected();
    }

    public void refreshDatasets() {
        datasetsTableData = new ModuleDatasetsTableData(module.getModuleDatasets(), session);
        datasetsTable.refresh(datasetsTableData);
        isDirty.setText("true");
    }

    //-----------------------------------------------------------------
    
    private JPanel parametersCrudPanel() {
        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editParameters();
            }
        };
        SelectAwareButton editButton = new SelectAwareButton("Set Input Value", editAction, parametersTable, confirmDialog);

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        crudPanel.add(editButton);
        if (viewMode == ViewMode.VIEW) {
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
            if (moduleParameter.getModuleTypeVersionParameter().isModeOUT()) {
                messagePanel.setMessage("Can't set input values for output parameters.");
                continue;
            }
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
        parametersTableData = new ModuleParametersTableData(module.getModuleParameters());
        parametersTable.refresh(parametersTableData);
        isDirty.setText("true");
    }

    //-----------------------------------------------------------------
    
    private void refreshSubmodules() {
        submodulesTableData = new ModuleTypeVersionSubmodulesTableData(moduleTypeVersion.getModuleTypeVersionSubmodules());
        submodulesTable.refresh(submodulesTableData);
    }

    private void refreshConnections() {
        connectionsTableData = new ModuleTypeVersionConnectionsTableData(moduleTypeVersion);
        connectionsTable.refresh(connectionsTableData);
    }
    
    //-----------------------------------------------------------------
    
    private JPanel internalDatasetsCrudPanel() {

        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        Action keepAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                keepInternalDatasets();
            }
        };
        Button keepButton = new Button("Keep", keepAction);
        keepButton.setMnemonic('K');

        Action dontKeepAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dontKeepInternalDatasets();
            }
        };
        Button dontKeepButton = new Button("Don't Keep", dontKeepAction);
        dontKeepButton.setMnemonic('o');

        Action setInternalDatasetNameAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setInternalDatasetName();
            }
        };
        Button setInternalDatasetNameButton = new Button("Set Dataset Name", setInternalDatasetNameAction);
        setInternalDatasetNameButton.setMnemonic('N');

        Action viewInternalDatasetsAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewInternalDatasets();
            }
        };
        SelectAwareButton viewInternalDatasetsButton = new SelectAwareButton("View Dataset Properties", viewInternalDatasetsAction, internalDatasetsTable, confirmDialog);
        viewInternalDatasetsButton.setMnemonic('D');

        Action viewInternalRelatedModulesAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewInternalRelatedModules();
            }
        };
        Button viewRelatedModulesButton = new Button("View Related Modules", viewInternalRelatedModulesAction);
        viewRelatedModulesButton.setMnemonic('M');

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        crudPanel.add(keepButton);
        crudPanel.add(dontKeepButton);
        crudPanel.add(setInternalDatasetNameButton);
        crudPanel.add(viewInternalDatasetsButton);
        crudPanel.add(viewRelatedModulesButton);
        if (viewMode == ViewMode.VIEW) {
            keepButton.setEnabled(false);
            dontKeepButton.setEnabled(false);
            setInternalDatasetNameButton.setEnabled(false);
        }

        return crudPanel;
    }

    private void viewInternalDatasets() {
        clear();
        @SuppressWarnings("rawtypes")
        final List datasets = selectedInternalDatasets();
        if (datasets.isEmpty()) {
            messagePanel.setMessage("Please select one or more internal datasets");
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
                for (@SuppressWarnings("rawtypes") Iterator iter = datasets.iterator(); iter.hasNext();) {
                    ModuleInternalDataset moduleInternalDataset = (ModuleInternalDataset) iter.next();
                    EmfDataset emfDataset = moduleInternalDataset.getEmfDataset(session.dataService());
                    if (emfDataset != null) {
                        DatasetPropertiesViewer view = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
                        presenter.doDisplayDatasetProperties(view, emfDataset);
                    }
                }
                return null;
            }

            /*
             * Executed in event dispatching thread
             */
            @Override
            public void done() {
                try {
                    get();
                } catch (InterruptedException e1) {
                    // ignore
                } catch (ExecutionException e1) {
                    // ignore
                } finally {
                    ComponentUtility.enableComponents(parentContainer, true);
                    this.parentContainer.setCursor(null); //turn off the wait cursor
                }
            }
        }
        new ViewDatasetPropertiesTask(this).execute();
    }

    private void viewInternalRelatedModules() {
        clear();
        @SuppressWarnings("rawtypes")
        final List datasets = selectedInternalDatasets();
        if (datasets.isEmpty()) {
            messagePanel.setMessage("Please select an internal dataset");
            return;
        } else if (datasets.size() > 1) {
            messagePanel.setMessage("Please select only one internal dataset");
            return;
        }
        
        ModuleInternalDataset moduleInternalDataset = (ModuleInternalDataset) datasets.get(0);
        EmfDataset emfDataset = moduleInternalDataset.getEmfDataset(session.dataService());
        if (emfDataset == null) {
            messagePanel.setMessage("The internal dataset does not exist");
            return;
        }

        // bring up the window with all related modules
        RelatedModulesWindow view = new RelatedModulesWindow(session, parentConsole, desktopManager, emfDataset);
        presenter.doDisplayRelatedModules(view);
    }

    @SuppressWarnings("rawtypes")
    private List selectedInternalDatasets() {
        return internalDatasetsTable.selected();
    }

    public void refreshInternalDatasets() {
        internalDatasetsTableData = new ModuleInternalDatasetsTableData(module.getModuleInternalDatasets(), session);
        internalDatasetsTable.refresh(internalDatasetsTableData);
        isDirty.setText("true");
    }

    private void keepInternalDatasets() {
        @SuppressWarnings("rawtypes")
        List selected = selectedInternalDatasets();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more internal datasets");
            return;
        }   

        boolean mustRefresh = false;
        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ModuleInternalDataset moduleInternalDataset = (ModuleInternalDataset) iter.next();
            if (moduleInternalDataset.getKeep() == false) {
                moduleInternalDataset.setKeep(true);
                mustRefresh = true;
            }
        }
        
        if (mustRefresh) {
            refreshInternalDatasets();
        }
    }

    private void dontKeepInternalDatasets() {
        List selected = selectedInternalDatasets();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more internal datasets");
            return;
        }   

        boolean mustRefresh = false;
        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ModuleInternalDataset moduleInternalDataset = (ModuleInternalDataset) iter.next();
            if (moduleInternalDataset.getKeep() == true) {
                moduleInternalDataset.setKeep(false);
                mustRefresh = true;
            }
        }
        
        if (mustRefresh) {
            refreshInternalDatasets();
        }
    }

    public String getDatasetNamePattern(String initialNamePattern) {
        ModuleDatasetNamePatternDialog selectionView = new ModuleDatasetNamePatternDialog(parentConsole, initialNamePattern);
        ModuleDatasetNamePatternPresenter selectionPresenter = new ModuleDatasetNamePatternPresenter(selectionView, session);
        try {
            selectionPresenter.display();
        } catch (Exception e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        if (selectionView.isOK()) {
            return selectionView.getDatasetNamePattern();
        }
        return null;
    }
    
    private void setInternalDatasetName() {
        List selected = selectedInternalDatasets();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more internal datasets");
            return;
        }   

        boolean mustRefresh = false;
        ModuleInternalDataset moduleInternalDataset = (ModuleInternalDataset)selected.get(0);
        String datasetNamePattern = getDatasetNamePattern(moduleInternalDataset.getDatasetNamePattern());
        if (datasetNamePattern == null) return;
        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            moduleInternalDataset = (ModuleInternalDataset) iter.next();
            if (!moduleInternalDataset.getDatasetNamePattern().equals(datasetNamePattern)) {
                moduleInternalDataset.setDatasetNamePattern(datasetNamePattern);
                mustRefresh = true;
            }
        }
        
        if (mustRefresh) {
            refreshInternalDatasets();
        }
    }

    //-----------------------------------------------------------------
    
    private JPanel internalParametersCrudPanel() {

        Action keepAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                keepInternalParameters();
            }
        };
        Button keepButton = new Button("Keep", keepAction);
        keepButton.setMnemonic('K');

        Action dontKeepAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dontKeepInternalParameters();
            }
        };
        Button dontKeepButton = new Button("Don't Keep", dontKeepAction);
        dontKeepButton.setMnemonic('o');

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        crudPanel.add(keepButton);
        crudPanel.add(dontKeepButton);
        if (viewMode == ViewMode.VIEW) {
            keepButton.setEnabled(false);
            dontKeepButton.setEnabled(false);
        }

        return crudPanel;
    }

    private List selectedInternalParameters() {
        return internalParametersTable.selected();
    }

    public void refreshInternalParameters() {
        internalParametersTableData = new ModuleInternalParametersTableData(module.getModuleInternalParameters());
        internalParametersTable.refresh(internalParametersTableData);
        isDirty.setText("true");
    }

    private void keepInternalParameters() {
        List selected = selectedInternalParameters();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more internal datasets");
            return;
        }   

        boolean mustRefresh = false;
        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ModuleInternalParameter moduleInternalParameter = (ModuleInternalParameter) iter.next();
            if (moduleInternalParameter.getKeep() == false) {
                moduleInternalParameter.setKeep(true);
                mustRefresh = true;
            }
        }
        
        if (mustRefresh) {
            refreshInternalParameters();
        }
    }

    private void dontKeepInternalParameters() {
        List selected = selectedInternalParameters();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more internal datasets");
            return;
        }   

        boolean mustRefresh = false;
        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ModuleInternalParameter moduleInternalParameter = (ModuleInternalParameter) iter.next();
            if (moduleInternalParameter.getKeep() == true) {
                moduleInternalParameter.setKeep(false);
                mustRefresh = true;
            }
        }
        
        if (mustRefresh) {
            refreshInternalParameters();
        }
    }

    //-----------------------------------------------------------------
    
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

    private void showLargeErrorMessage(String title, String error) {
        // reusing implementation from ModuleTypeVersionPropertiesWindow
        ModuleTypeVersionPropertiesWindow.showLargeErrorMessage(messagePanel, title, error);
    }

    private Action statusAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                StringBuilder reason = new StringBuilder(); 
                boolean isOutOfDate = isOutOfDate(reason);
                refreshStatusText(isOutOfDate);
                String title = "This module is " + statusText.getText() + "!";
                if (isOutOfDate) {
                    showLargeErrorMessage(title, reason.toString());
                } else {
                    messagePanel.setMessage(title);
                }
            }
        };

        return action;
    }

    private Action validateAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                StringBuilder error = new StringBuilder();
                if (module.isValid(error)) {
                    messagePanel.setMessage("This module is valid.");
                } else {
                    showLargeErrorMessage("This module is invalid!", error.toString());
                }
            }
        };

        return action;
    }

    private void doRun() {
        if ((viewMode == ViewMode.NEW) || hasChanges()) {
            if (!doSave())
                return;
        }
        String message = "Are you sure you want to run the '" + module.getName() + "' module?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (selection != JOptionPane.YES_OPTION)
            return;
        
        StringBuilder error = new StringBuilder();
        if (!module.isValid(error)) {
            showLargeErrorMessage("Can't run! This module is invalid!", error.toString());
            return;
        }
        
        try {
            presenter.runModule(module);
            messagePanel.setMessage("The module has been executed.");
        } catch (EmfException e) {
            messagePanel.setError("The module failed to run: " + e.getMessage());
        }
    }

    private boolean isOutOfDate(final StringBuilder reason) {
        reason.setLength(0);
        
        StringBuilder error = new StringBuilder();
        if (!module.isValid(error)) {
            reason.append("This module is invalid. " + error.toString() + "\n\n");
        }
        
        if (hasChanges()) {
            reason.append("This module has unsaved changes.\n\n");
        }
        
        History lastHistory = module.lastHistory();
        if (lastHistory == null) {
            reason.append("This module has never been run.\n\n");
            return true;
        }
        
        String result = lastHistory.getResult();
        if (result == null || !result.equals(History.SUCCESS)) {
            reason.append("The last module run was not successful.\n\n");
        }
        
        // verify that the last run's history datasets are identical (id & version) to the current module datasets
        if (!lastHistory.checkModuleDatasets(error)) {
            reason.append("The module datasets have changed since the last sucessfull run:\n" + error.toString() + "\n\n");
        }

        // check module type version last modified date against last run start time
        if (moduleTypeVersion.getLastModifiedDate().after(lastHistory.startDate())) {
            reason.append("The module type version is more recent than the last run.\n\n");
        }
        
        // check module last modified date against last run start time
        if (module.getLastModifiedDate().after(lastHistory.startDate())) {
            reason.append("The module is more recent than the last run.\n\n");
        }

        StringBuilder explanation = new StringBuilder();
        if (lastHistory.isOutOfDate(explanation, session.dataService(), session.dataEditorService())) {
            reason.append("Last run is out of date:\n" + explanation.toString());
        }
        
        return (reason.length() > 0);
    }

    private void doFinalize() {
        StringBuilder error = new StringBuilder();
        if (!module.isValid(error)) {
            messagePanel.setError("Can't finalize. This module is invalid. " + error.toString());
            return;
        }
        if (hasChanges()) {
            messagePanel.setError("Can't finalize. You must save changes first.");
            return;
        }
        History lastHistory = module.lastHistory();
        if (lastHistory == null) {
            messagePanel.setError("Can't finalize. The module must be run at least once.");
            return;
        }
        String result = lastHistory.getResult();
        if (result == null || !result.equals(History.SUCCESS)) {
            messagePanel.setError("Can't finalize. The last module run was not successful.");
            return;
        }
        if (!moduleTypeVersion.getIsFinal()) {
            messagePanel.setError("Can't finalize. The module type " + moduleTypeVersion.fullNameSS("\"%s\" version \"%s\"") + " is not final.");
            return;
        }

        // verify that the last run's history datasets are identical (id & version) to the current module datasets
        if (!lastHistory.checkModuleDatasets(error)) {
            messagePanel.setError("Can't finalize. " + error.toString());
            return;
        }

        // check module type version last modified date against last run start time
        if (moduleTypeVersion.getLastModifiedDate().after(lastHistory.startDate())) {
            messagePanel.setError("Can't finalize. The module type version is more recent than the last run.");
            return;
        }
        
        // check module last modified date against last run start time
        if (module.getLastModifiedDate().after(lastHistory.startDate())) {
            messagePanel.setError("Can't finalize. The module is more recent than the last run.");
            return;
        }

        // check if all input datasets are final
        TreeMap<Integer, Version> nonfinalInputVersions = new TreeMap<Integer, Version>();
        TreeMap<Integer, EmfDataset> nonfinalInputDatasets = new TreeMap<Integer, EmfDataset>(); // index is the Version.id
        StringBuilder nonfinalInputVersionsText = new StringBuilder();
        if (!lastHistory.getNonfinalInputDatasets(error, nonfinalInputVersions, nonfinalInputDatasets, nonfinalInputVersionsText, session.dataService(), session.dataEditorService())) {
            messagePanel.setError("Can't finalize. " + error.toString());
            return;
        }
        
        if (nonfinalInputVersionsText.length() > 0) {
            String title = "Finalizing module " + module.getName(); 
            String message = "Are you sure you want to finalize these input datasets?\n\n" + nonfinalInputVersionsText.toString();
            int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (selection != JOptionPane.YES_OPTION)
                return;
            for (Version version : nonfinalInputVersions.values()) {
                try {
                    session.dataEditorService().markFinal(new DataAccessToken(version, null));
                } catch (EmfException e) {
                    e.printStackTrace();
                    EmfDataset dataset = nonfinalInputDatasets.get(version.getId());
                    String errorMessage = String.format("Failed to finalize dataset \"" + dataset.getName() + "\" version " + version.getId());
                    messagePanel.setError(errorMessage);
                    return;
                }
            }
            messagePanel.setMessage("Finalized the input dataset(s).");
        }
        
        StringBuilder explanation = new StringBuilder();
        if (lastHistory.isOutOfDate(explanation, session.dataService(), session.dataEditorService())) {
            String title = "Failed to finalize module " + module.getName(); 
            showLargeErrorMessage(title, "Module is out of date and must be run again.\n\n" + explanation.toString());
            return;
        }

        // check if all input datasets are final
        TreeMap<Integer, Version> nonfinalOutputVersions = new TreeMap<Integer, Version>();
        TreeMap<Integer, EmfDataset> nonfinalOutputDatasets = new TreeMap<Integer, EmfDataset>(); // index is the Version.id
        StringBuilder nonfinalOutputVersionsText = new StringBuilder();
        if (!lastHistory.getNonfinalOutputDatasets(error, nonfinalOutputVersions, nonfinalOutputDatasets, nonfinalOutputVersionsText, session.dataService(), session.dataEditorService())) {
            messagePanel.setError("Can't finalize. " + error.toString());
            return;
        }
        
        if (nonfinalOutputVersionsText.length() > 0) {
            String message = "Are you sure you want to finalize these output datasets?\n\n" + nonfinalOutputVersionsText.toString();
            int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (selection != JOptionPane.YES_OPTION)
                return;
            for (Version version : nonfinalOutputVersions.values()) {
                try {
                    session.dataEditorService().markFinal(new DataAccessToken(version, null));
                } catch (EmfException e) {
                    e.printStackTrace();
                    EmfDataset dataset = nonfinalOutputDatasets.get(version.getId());
                    String errorMessage = String.format("Failed to finalize dataset \"" + dataset.getName() + "\" version " + version.getId());
                    messagePanel.setError(errorMessage);
                    return;
                }
            }
            messagePanel.setMessage("Finalized the output dataset(s).");
        }
        
        title = module.getName();
        int selection = JOptionPane.showConfirmDialog(parentConsole, "Are you sure you want to finalize this module?",
                                                      title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            module.setIsFinal(true);
            moduleIsFinal.setText(module.getIsFinal() ? "Yes" : "No");
            isDirty.setText("true");
            if (doSave()) {
                JOptionPane.showMessageDialog(parentConsole, "This module has been finalized!", title, JOptionPane.INFORMATION_MESSAGE);
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

    private void updateProject() {
        Object selected = projectsCB.getSelectedItem();
        if (selected instanceof String) {
            String projectName = (String) selected;
            if (projectName.length() > 0) {
                Project project = project(projectName);// checking for duplicates
                module.setProject(project);
                if (project.getId() == 0) {
                    session.getObjectCache().invalidate(ObjectCacheType.PROJECTS_LIST);
                }
            } else {
                module.setProject(null);
            }
        } else if (selected instanceof Project) {
            module.setProject((Project) selected);
        }
    }

    private Project project(String name) {
        return new Projects(allProjects).get(name);
    }

    private boolean doSave() {
        try {
            // TODO save only if it's dirty
            Date date = new Date();
            module.setName(moduleName.getText());
            module.setDescription(moduleDescription.getText());
            updateProject();
            module.setLastModifiedDate(date);

            if (viewMode == ViewMode.NEW) {
                module.setCreationDate(date);
                module.setCreator(session.user());
                module = presenter.addModule(module);
                viewMode = ViewMode.EDIT;
                runButton.setEnabled(viewMode == ViewMode.EDIT);
                Module lockedModule = presenter.obtainLockedModule(module);
                if (lockedModule == null || !lockedModule.isLocked(session.user())) {
                    throw new EmfException("Failed to lock module.");
                }
                module = lockedModule;
            } else {
                module = presenter.updateModule(module);
            }
            doRefresh(); // resets dirty flags also
            messagePanel.setMessage("Saved module.");
        }
        catch (EmfException e) {
            messagePanel.setError(e.getMessage());
            return false;
        }

        return true;
    }
    
    private Action saveAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if (checkTextFields()) {
                    // TODO save only if it's dirty
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
        if (!shouldDiscardChanges()) {
            return;
        }

        StringBuilder error = new StringBuilder();
        if (module.getModuleTypeVersion().isValid(error)) {
            if (!module.isValid(error)) {
                int selection = JOptionPane.showConfirmDialog(parentConsole, error + "\n\nAre you sure you want to close invalid/incomplete module?\n",
                                                              "Module Properties", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (selection == JOptionPane.NO_OPTION)
                    return;
            }
        }

        try {
            if (viewMode == ViewMode.EDIT) {
                module = presenter.releaseLockedModule(module);
            }
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        presenter.doClose();
    }


    @Override
    public void refreshTags() {
        String oldText = moduleTags.getText();
        moduleTags.setText(module.getTagsText());
        String newText = moduleTags.getText();
        if (!newText.equals(oldText)) {
            isDirty.setText("true");
        }
    }
}
