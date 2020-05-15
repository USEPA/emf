package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.DisabledButton;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.framework.client.EmfPanel;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlstrategy.AnalysisEngineTableApp;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.ControlStrategyOutputTableData;
import gov.epa.emissions.framework.client.data.viewer.DataViewPresenter;
import gov.epa.emissions.framework.client.data.viewer.DataViewer;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesEditor;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class ViewControlStrategyOutputTab extends EmfPanel implements ViewControlStrategyOutputTabView {

    private TextField exportFolder, exportName;
    
    private JCheckBox download;

    private ViewControlStrategyOutputTabPresenter presenter;

    private ControlStrategy controlStrategy;

    private SelectableSortFilterWrapper table;

    private JPanel tablePanel;

    private Button analysisButton;

    private Button viewDataButton;

    private Button exportButton;

    private Button customizeButton;

    private Button summarizeButton;
    
    private Button browseButton;

    private JRadioButton detailButton;

    private JRadioButton invButton;

    private JRadioButton contInvButton;

    private ButtonGroup buttonGroup;

    private boolean creatingControlledInventories;

    public ViewControlStrategyOutputTab(ControlStrategy controlStrategy, MessagePanel messagePanel,
            EmfConsole parentConsole, DesktopManager desktopManager) {

        super("csOutput", parentConsole, desktopManager, messagePanel);

        this.controlStrategy = controlStrategy;
    }

    public void display(ControlStrategy strategy, ControlStrategyResult[] controlStrategyResults) {
        setLayout(controlStrategyResults);
    }

    private void setLayout(ControlStrategyResult[] controlStrategyResults) {
        // //this is called for both the load window process, so i'll set the boolean flag here...

        setLayout(new BorderLayout());
        removeAll();
        add(outputPanel(controlStrategyResults));
    }

    public void save(ControlStrategy controlStrategy) {
        controlStrategy.setExportDirectory(getExportFolder());
    }

    public void observe(ViewControlStrategyOutputTabPresenter presenter) {
        this.presenter = presenter;
    }

    public void export() {

        try {
            if (!download.isSelected())
                validateFolder(getExportFolder());

            boolean canConcatReports = buttonGroup.getSelection().equals(detailButton.getModel());
            ControlStrategyResult[] controlStrategyResults = getSelectedDatasets();
            List<EmfDataset> datasetList = new ArrayList<EmfDataset>();
            for (int i = 0; i < controlStrategyResults.length; i++) {
                EmfDataset inv = null;
                if (buttonGroup.getSelection().equals(invButton.getModel())) {
                    if (controlStrategyResults[i].getInputDataset() != null) {
                        inv = controlStrategyResults[i].getInputDataset();
                    }
                } else if (buttonGroup.getSelection().equals(detailButton.getModel())) {
                    if (controlStrategyResults[i].getDetailedResultDataset() != null) {
                        datasetList.add((EmfDataset) controlStrategyResults[i].getDetailedResultDataset());
                    }
                    if (!controlStrategyResults[i].getStrategyResultType().getName().equals(StrategyResultType.detailedStrategyResult)) {
                        canConcatReports = false;
                    }
                } else if (buttonGroup.getSelection().equals(contInvButton.getModel())) {
                    if (controlStrategyResults[i].getControlledInventoryDataset() != null) {
                        inv = (EmfDataset) controlStrategyResults[i].getControlledInventoryDataset();
                    } else if (controlStrategyResults[i].getStrategyResultType().getName().equals(
                            StrategyResultType.controlledInventory)
                            && controlStrategyResults[i].getDetailedResultDataset() != null) {
                        inv = (EmfDataset) controlStrategyResults[i].getDetailedResultDataset();
                    } else {
                        showError("Please create controlled inventory first.");
                    }
                }
                if (inv != null) {
                    // exclude comments in header when exporting inventories
                    KeyVal keyword = new KeyVal(new Keyword(Dataset.header_comment_key), "false");
                    inv.addKeyVal(keyword);
                    datasetList.add(inv);
                }
            }
            
            boolean concat = false;
            if (canConcatReports && datasetList.size() > 1) {
                int selection = JOptionPane.showConfirmDialog(getParentConsole(),
                    "Export all selected reports in a single file? The output filename will be based on the first report:\n" +
                    datasetList.get(0).getName(), "Strategy Detailed Result Output", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (selection == JOptionPane.YES_OPTION)
                    concat = true;
            }
            presenter.doExport(datasetList.toArray(new EmfDataset[0]), getExportFolder(), exportName.getText(), download.isSelected(), concat);
            showMessage("Started Export. Please monitor the Status window to track your export request");
        } catch (EmfException e) {
            showMessage(e.getMessage());
        }
    }
    
    public String promptForColumnPrefix() {
        String prefix = JOptionPane.showInputDialog(getParentConsole(), 
                "Enter a prefix to use in the column names (leave blank for no prefix).", 
                "Use column name prefix?",
                JOptionPane.PLAIN_MESSAGE);
        if (prefix == null) prefix = "";
        return prefix;
    }

    public void analyze() {

        clearMessage();
        Thread viewResultsThread = new Thread(new Runnable() {
            public void run() {
                try {
                    ControlStrategyResult[] controlStrategyResults = getSelectedDatasets();
                    if (controlStrategyResults == null || controlStrategyResults.length == 0)
                        throw new EmfException("Please select a result. ");
                    List<EmfDataset> datasetList = new ArrayList<EmfDataset>();
                    for (int i = 0; i < controlStrategyResults.length; i++) {
                        // if
                        // (controlStrategyResults[i].getStrategyResultType().getName().equals(StrategyResultType.detailedStrategyResult))
                        // {
                        if (buttonGroup.getSelection().equals(invButton.getModel())) {
                            // if (controlStrategyResults[i].getInputDataset() != null)
                            // datasetList.add(controlStrategyResults[i].getInputDataset());
                        } else if (buttonGroup.getSelection().equals(detailButton.getModel())) {
                            if (controlStrategyResults[i].getDetailedResultDataset() != null)
                                datasetList.add((EmfDataset) controlStrategyResults[i].getDetailedResultDataset());
                        }
                        // else if (buttonGroup.getSelection().equals(contInvButton.getModel())) {
                        // if (controlStrategyResults[i].getControlledInventoryDataset() != null) {
                        // datasetList.add((EmfDataset)controlStrategyResults[i].getControlledInventoryDataset());
                        // } else if
                        // (controlStrategyResults[i].getStrategyResultType().getName().equals(StrategyResultType.controlledInventory)
                        // && controlStrategyResults[i].getDetailedResultDataset() != null) {
                        // datasetList.add((EmfDataset)controlStrategyResults[i].getDetailedResultDataset());
                        // } else
                        // messagePanel.setError("Please create controlled inventory first.");
                        // }
                        // } else {//if
                        // (controlStrategyResults[i].getStrategyResultType().getName().equals(StrategyResultType.strategySummary))
                        // {
                        // datasetList.add(controlStrategyResults[i].getInputDataset());
                        // }
                    }

                    if (datasetList.size() == 0)
                        throw new EmfException("No dataset is selected. ");
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                    if (presenter.getTableRecordCount(datasetList.get(0)) > 300000) {
                        String title = "Warning";
                        String message = "Are you sure you want to view the result? The table has over 300,000 records. It could take several minutes to load the data.";
                        int selection = JOptionPane.showConfirmDialog(getParentConsole(), message, title,
                                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                        if (selection == JOptionPane.NO_OPTION) {
                            return;
                        }
                    }

                    presenter.doAnalyze(controlStrategy.getName(), datasetList.toArray(new EmfDataset[0]));
                } catch (EmfException e) {
                    showMessage(e.getMessage());
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });
        viewResultsThread.start();
    }

    private Action customizeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doCustomize();
            }

        };
        return action;
    }

    protected void doCustomize() {

        // StrategySummarySelectionView view = new StrategySummarySelectionDialog(parentConsole);
        // StrategySummarySelectionPresenter presenter = new StrategySummarySelectionPresenter(view, session);
        // try {
        // presenter.display();
        // StrategyResultType[] strategyResultTypes = presenter.getStrategyResultTypes();
        // if (strategyResultTypes.length > 0) {
        // for (int i = 0; i < strategyResultTypes.length; i++) {
        // session.controlStrategyService().summarizeStrategy(session.user(), controlStrategy.getId(),
        // "", strategyResultTypes[i]);
        // }
        // messagePanel
        // .setMessage("Running strategy summary. Monitor the status window, and refresh after completion to see results");
        // }
        // } catch (Exception exp) {
        // messagePanel.setError(exp.getMessage());
        // }
    }

    protected void doInventory() {

        try {

            ControlStrategyResult[] controlStrategyResults = getSelectedDatasets();
            if (controlStrategyResults.length == 0) {

                showError("Please select at least one item.");
                return;
            }

            if (controlStrategyResults.length == 1
                    && (!controlStrategyResults[0].getStrategyResultType().getName().equals(
                            StrategyResultType.detailedStrategyResult) && !controlStrategyResults[0]
                            .getStrategyResultType().getName().equals(StrategyResultType.annotatedInventory))) {

                showError("Please select at least one item that has a controlled inventory.");
                return;
            }

            // see if selected items can produce a controlled inventory.
            boolean hasControllableInventory = false;
            for (ControlStrategyResult result : controlStrategyResults) {

                if (result.getStrategyResultType().getName().equals(StrategyResultType.detailedStrategyResult)
                        || result.getStrategyResultType().getName().equals(StrategyResultType.annotatedInventory)) {
                    hasControllableInventory = true;
                }

                if (hasControllableInventory) {
                    break;
                }
            }

            if (!hasControllableInventory) {

                showError("Please select a detailed result in order to create a controlled inventory.");
                return;
            }

            // see if there is already a controlled inventory for this strategy.
            boolean hasControlledInventories = false;
            for (ControlStrategyResult result : controlStrategyResults) {

                if (result.getStrategyResultType().getName().equals(StrategyResultType.controlledInventory)) {
                    hasControlledInventories = true;
                } else if ((result.getStrategyResultType().getName().equals(StrategyResultType.detailedStrategyResult) || result
                        .getStrategyResultType().getName().equals(StrategyResultType.annotatedInventory))
                        && result.getControlledInventoryDataset() != null) {
                    hasControlledInventories = true;
                }

                if (hasControlledInventories) {
                    break;
                }
            }

            // see if cont inv are already being created...
            if (creatingControlledInventories || hasControlledInventories) {

                String title = "Warning";
                String message = "Are you sure you want to create controlled inventories? There are controlled inventories that "
                        + (creatingControlledInventories ? "are already being created" : "have already been created")
                        + ".";
                int selection = JOptionPane.showConfirmDialog(this.getParentConsole(), message, title,
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (selection != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            String namePrefix = null;
            presenter.doInventory(controlStrategy, controlStrategyResults, namePrefix);
            // flag to make sure the user doesn't click the button twice...
            creatingControlledInventories = true;

            showMessage("Creating controlled inventories. Watch the status window for progress and refresh this window after completion.");
        } catch (EmfException e) {
            showError(e.getMessage());
        }
    }

    private JPanel folderPanel() {
        exportFolder = new TextField("folderName", 30);
        String exportDirectory = controlStrategy.getExportDirectory();
        exportDirectory = (exportDirectory != null ? exportDirectory : presenter.folder());
        exportFolder.setText(exportDirectory);

        browseButton = new BrowseButton(browseAction());

        download = new JCheckBox("Download exported file(s) to local machine?");
        download.setName("download");
        download.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {

                Object source = e.getItemSelectable();

                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    exportFolder.setEnabled(true);
                    browseButton.setEnabled(true);
                } else {
                    exportFolder.setEnabled(false);
                    browseButton.setEnabled(false);
                }
            }
        });
        
        exportName = new TextField("fileName", 30);
        exportName.setText("");

        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        layoutGenerator.addLabelWidgetPair("", download, panel);
        JPanel linePanel = new JPanel();
        linePanel.add(exportFolder);
        linePanel.add(browseButton);
        layoutGenerator.addLabelWidgetPair("Server Export Folder:", linePanel, panel);
        layoutGenerator.addLabelWidgetPair("Export Name Prefix:", exportName, panel);
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 5);// xPad, yPad

        return panel;
    }

    private JPanel outputPanel(ControlStrategyResult[] controlStrategyResults) {
        JPanel tablePanel = tablePanel(controlStrategyResults);
        JPanel buttonPanel = buttonPanel();

        JPanel outputPanel = new JPanel(new BorderLayout(5, 10));
        outputPanel.add(tablePanel);
        JPanel panel = new JPanel(new BorderLayout(5, 10));
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(folderPanel(), BorderLayout.SOUTH);
        outputPanel.add(panel, BorderLayout.SOUTH);

        outputPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createTitledBorder("Output Datasets")));

        return outputPanel;
    }

    private JPanel tablePanel(ControlStrategyResult[] controlStrategyResults) {

        ControlStrategyOutputTableData tableData = new ControlStrategyOutputTableData(controlStrategyResults);
        table = new SelectableSortFilterWrapper(this.getParentConsole(), tableData, sortCriteria());
        table.getModel().addTableModelListener(new TableModelListener() {

            public void tableChanged(TableModelEvent e) {
                toggleRadioButtons();
            }
        });

        tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(table);

        return tablePanel;
    }

    private SortCriteria sortCriteria() {

        String[] columnNames = { "Start Time" };
        return new SortCriteria(columnNames, new boolean[] { false }, new boolean[] { false });
    }

    private JPanel buttonPanel() {

        exportButton = new ExportButton(exportAction());
        analysisButton = new Button("Analyze", analysisAction());

        viewDataButton = new Button("View Data", viewDataAction());

        JButton editButton = new DisabledButton("Edit");
      
        summarizeButton = new Button("Summarize", summarizeAction());

        JButton createButton = new DisabledButton("Create");

        customizeButton = new Button("Customize", customizeAction());

        detailButton = new JRadioButton("Result");
        detailButton.addActionListener(radioButtonAction());
        detailButton.setSelected(true);
        invButton = new JRadioButton("Input Inventory");
        invButton.addActionListener(radioButtonAction());
        contInvButton = new JRadioButton("Controlled Inventory");
        contInvButton.addActionListener(radioButtonAction());

        // Create logical relationship between JradioButtons
        buttonGroup = new ButtonGroup();
        buttonGroup.add(invButton);
        buttonGroup.add(detailButton);
        buttonGroup.add(contInvButton);

        JPanel radioPanel = new JPanel();
        radioPanel.add(invButton, radioButtonAction());
        radioPanel.add(detailButton, radioButtonAction());
        radioPanel.add(contInvButton);
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        mainPanel.add(radioPanel, BorderLayout.NORTH);
        buttonPanel.add(viewDataButton);
        buttonPanel.add(editButton);
        buttonPanel.add(summarizeButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(analysisButton);
        buttonPanel.add(createButton);
        buttonPanel.add(customizeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private Action radioButtonAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                toggleRadioButtons();
            }
        };
    }

    private void toggleRadioButtons() {
        if (buttonGroup.getSelection().equals(invButton.getModel())
                || buttonGroup.getSelection().equals(detailButton.getModel())) {

            viewDataButton.setEnabled(true);
            summarizeButton.setEnabled(true);
            analysisButton.setEnabled(false);
            exportButton.setEnabled(true);
        }
        if (buttonGroup.getSelection().equals(detailButton.getModel()))
            analysisButton.setEnabled(true);

        else if (buttonGroup.getSelection().equals(contInvButton.getModel())) {
            ControlStrategyResult[] controlStrategyResults = getSelectedDatasets();

            // see if there is already a controlled inventory for this strategy.
            boolean hasControlledInventories = false;
            boolean hasControllableInventory = false;
            for (ControlStrategyResult result : controlStrategyResults) {
                if (result.getStrategyResultType().getName().equals(StrategyResultType.controlledInventory)) {
                    hasControlledInventories = true;
                } else if ((result.getStrategyResultType().getName().equals(StrategyResultType.detailedStrategyResult) || result
                        .getStrategyResultType().getName().equals(StrategyResultType.annotatedInventory))
                        && result.getControlledInventoryDataset() != null) {
                    hasControlledInventories = true;
                }
                if (result.getStrategyResultType().getName().equals(StrategyResultType.detailedStrategyResult)
                        || result.getStrategyResultType().getName().equals(StrategyResultType.annotatedInventory)) {
                    hasControllableInventory = true;
                }
                if (hasControllableInventory && hasControlledInventories)
                    break;
            }

            if (hasControlledInventories) {

                viewDataButton.setEnabled(true);
                summarizeButton.setEnabled(true);
                analysisButton.setEnabled(true);
                exportButton.setEnabled(true);
            } else {

                viewDataButton.setEnabled(false);
                summarizeButton.setEnabled(false);
                analysisButton.setEnabled(false);
                exportButton.setEnabled(false);
            }
        }
    }

    private Action exportAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                export();
            }
        };
    }

    private Action analysisAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                analyze();
            }
        };
    }

    private Action viewDataAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                viewDataSetsData();
            }
        };
    }

    private Action summarizeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                summarize();
            }
        };
    }

    private Action browseAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                selectFolder();
            }
        };
    }

    private void selectFolder() {

        EmfFileInfo initDir = new EmfFileInfo(getExportFolder(), true, true);

        EmfFileChooser chooser = new EmfFileChooser(getParentConsole().getSession(), initDir, new EmfFileSystemView(this.getSession()
                .dataCommonsService()));
        chooser.setTitle("Select a folder to contain the exported strategy results");
        int option = chooser.showDialog(this.getParentConsole(), "Select a folder");

        EmfFileInfo file = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedDir() : null;
        if (file == null)
            return;

        if (file.isDirectory()) {
            exportFolder.setText(file.getAbsolutePath());
            presenter.setLastFolder(file.getAbsolutePath());
        }
    }

    public String getExportFolder() {
        return exportFolder.getText();
    }

    public void displayAnalyzeTable(String controlStrategyName, String[] fileNames) {
        AnalysisEngineTableApp app = new AnalysisEngineTableApp("Analyze Control Strategy: " + controlStrategyName,
                new Dimension(500, 500), this.getDesktopManager(), this.getParentConsole());
        app.display(fileNames);
    }

    public void refresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        // setLayout(controlStrategy, controlStrategyResults);
        ControlStrategyOutputTableData tableData = new ControlStrategyOutputTableData(controlStrategyResults);
        table.refresh(tableData);
        tablePanel.removeAll();
        tablePanel.add(table);
        detailButton.setSelected(true);
        toggleRadioButtons();
        super.validate();
    }

    // private void viewDataSets() {
    // ControlStrategyResult[] controlStrategyResults = getSelectedDatasets();
    // if (controlStrategyResults.length == 0) {
    // messagePanel.setError("Please select at least one item.");
    // return;
    // }
    //         
    // try{
    // for (int i = 0; i < controlStrategyResults.length; i++) {
    // DatasetPropertiesViewer view = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
    //     
    // // if
    // (controlStrategyResults[i].getStrategyResultType().getName().equals(StrategyResultType.detailedStrategyResult)) {
    // if (buttonGroup.getSelection().equals(invButton.getModel())) {
    // if (controlStrategyResults[i].getInputDataset() != null)
    // presenter.doDisplayPropertiesView(view, controlStrategyResults[i].getInputDataset());
    // } else if (buttonGroup.getSelection().equals(detailButton.getModel())) {
    // if (controlStrategyResults[i].getDetailedResultDataset() != null)
    // presenter.doDisplayPropertiesView(view, (EmfDataset)controlStrategyResults[i].getDetailedResultDataset());
    //    
    // } else if (buttonGroup.getSelection().equals(contInvButton.getModel())) {
    // if (controlStrategyResults[i].getControlledInventoryDataset() != null) {
    // presenter.doDisplayPropertiesView(view, (EmfDataset)controlStrategyResults[i].getControlledInventoryDataset());
    // } else if
    // (controlStrategyResults[i].getStrategyResultType().getName().equals(StrategyResultType.controlledInventory) &&
    // controlStrategyResults[i].getDetailedResultDataset() != null) {
    // presenter.doDisplayPropertiesView(view, (EmfDataset)controlStrategyResults[i].getDetailedResultDataset());
    // }
    // }
    // // } else {//if
    // (controlStrategyResults[i].getStrategyResultType().getName().equals(StrategyResultType.strategySummary)) {
    // // presenter.doDisplayPropertiesView(view, controlStrategyResults[i].getInputDataset());
    // // }
    // }
    // } catch (EmfException e) {
    // messagePanel.setError(e.getMessage());
    // }
    // }

    private void viewDataSetsData() {

        ControlStrategyResult[] controlStrategyResults = getSelectedDatasets();
        if (controlStrategyResults.length == 0) {
            showError("Please select at least one item.");
            return;
        }

        for (int i = 0; i < controlStrategyResults.length; i++) {

            EmfDataset dataset = null;
            if (buttonGroup.getSelection().equals(invButton.getModel())) {
                if (controlStrategyResults[i].getInputDataset() != null) {
                    dataset = controlStrategyResults[i].getInputDataset();
                }
            } else if (buttonGroup.getSelection().equals(detailButton.getModel())) {
                if (controlStrategyResults[i].getDetailedResultDataset() != null) {
                    dataset = (EmfDataset) controlStrategyResults[i].getDetailedResultDataset();
                }
            } else if (buttonGroup.getSelection().equals(contInvButton.getModel())) {
                if (controlStrategyResults[i].getControlledInventoryDataset() != null) {
                    dataset = (EmfDataset) controlStrategyResults[i].getControlledInventoryDataset();
                } else if (controlStrategyResults[i].getStrategyResultType().getName().equals(
                        StrategyResultType.controlledInventory)
                        && controlStrategyResults[i].getDetailedResultDataset() != null) {
                    dataset = (EmfDataset) controlStrategyResults[i].getDetailedResultDataset();
                }
            }

            if (dataset != null) {
                showDatasetDataViewer(dataset);
            }
        }
    }

    private void summarize() {

        ControlStrategyResult[] controlStrategyResults = getSelectedDatasets();
        if (controlStrategyResults.length == 0) {
            showError("Please select at least one item.");
            return;
        }

        try {
            for (int i = 0; i < controlStrategyResults.length; i++) {
                EmfDataset dataset = null;
                DatasetPropertiesEditor view = new DatasetPropertiesEditor(this.getSession(), this.getParentConsole(),
                        this.getDesktopManager());

                if (buttonGroup.getSelection().equals(invButton.getModel())) {
                    if (controlStrategyResults[i].getInputDataset() != null) {
                        dataset = controlStrategyResults[i].getInputDataset();
                    }
                } else if (buttonGroup.getSelection().equals(detailButton.getModel())) {
                    if (controlStrategyResults[i].getDetailedResultDataset() != null) {
                        dataset = (EmfDataset) controlStrategyResults[i].getDetailedResultDataset();
                    }
                } else if (buttonGroup.getSelection().equals(contInvButton.getModel())) {
                    if (controlStrategyResults[i].getControlledInventoryDataset() != null) {
                        dataset = (EmfDataset) controlStrategyResults[i].getControlledInventoryDataset();
                    } else if (controlStrategyResults[i].getStrategyResultType().getName().equals(
                            StrategyResultType.controlledInventory)
                            && controlStrategyResults[i].getDetailedResultDataset() != null) {
                        dataset = (EmfDataset) controlStrategyResults[i].getDetailedResultDataset();
                    }
                }
                if (dataset != null) {
                    presenter.doDisplayPropertiesEditor(view, dataset);
                    view.setDefaultTab(7);
                }
            }
        } catch (EmfException e) {
            showError(e.getMessage());
        }
    }

    private void showDatasetDataViewer(EmfDataset dataset) {
        try {

            DesktopManager desktopManager = this.getDesktopManager();
            EmfConsole parentConsole = this.getParentConsole();
            EmfSession session = this.getSession();

            Version[] versions = presenter.getVersions(dataset.getId());
            // if just one version, then go directly to the dataviewer
            if (versions.length == 1) {

                DataViewer dataViewerView = new DataViewer(dataset, parentConsole, desktopManager, true);
                DataViewPresenter dataViewPresenter = new DataViewPresenter(dataset, versions[0],
                        getTableName(dataset), dataViewerView, session);
                dataViewPresenter.display();
                // else goto to dataset editior and display different version to display
            } else {

                DatasetPropertiesViewer datasetPropertiesViewerView = new DatasetPropertiesViewer(session,
                        parentConsole, desktopManager, true);
                presenter.doDisplayPropertiesView(datasetPropertiesViewerView, dataset);
                datasetPropertiesViewerView.setDefaultTab(1);
            }
            // presenter.doView(version, table, view);
        } catch (EmfException e) {
            // displayError(e.getMessage());
        }
    }

    protected String getTableName(Dataset dataset) {
        InternalSource[] internalSources = dataset.getInternalSources();
        String tableName = "";
        if (internalSources.length > 0)
            tableName = internalSources[0].getTable();
        return tableName;
    }

    private ControlStrategyResult[] getSelectedDatasets() {
        return table.selected().toArray(new ControlStrategyResult[0]);
    }

    public void notifyStrategyTypeChange(StrategyType strategyType) {
        // NOTE Auto-generated method stub

    }

    private void validateFolder(String folder) throws EmfException {
        if (folder == null || folder.trim().isEmpty())
            throw new EmfException("Please select a valid folder to export.");

        if (folder.contains("/home/") || folder.endsWith("/home"))
            throw new EmfException("Export data into user's home directory is not allowed.");
    }

    public void notifyStrategyRun(ControlStrategy controlStrategy) {
        if (controlStrategy.getDeleteResults()) {
            refresh(controlStrategy, new ControlStrategyResult[] {});
        }
    }

    public void run(ControlStrategy controlStrategy) {
        // NOTE Auto-generated method stub

    }
}
