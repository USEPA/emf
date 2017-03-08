package gov.epa.emissions.framework.client.sms.sectorscenario.editor;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlstrategy.AnalysisEngineTableApp;
import gov.epa.emissions.framework.client.data.viewer.DataViewPresenter;
import gov.epa.emissions.framework.client.data.viewer.DataViewer;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesEditor;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.sms.SectorScenario;
import gov.epa.emissions.framework.services.sms.SectorScenarioOutput;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class EditSectorScenarioOutputsTab extends JPanel implements EditSectorScenarioOutputsTabView {

    private TextField folder;

    private EditSectorScenarioOutputsTabPresenter presenter;

    private SectorScenario sectorScenario;

    private MessagePanel messagePanel;

    private DesktopManager desktopManager;

    private EmfConsole parentConsole;

    private SelectableSortFilterWrapper table;

    private JPanel tablePanel; 

    private Button analysisButton, /*view, */viewDataButton, exportButton, editButton, summarizeButton;
    
    private EmfSession session;

//    private JRadioButton detailButton;
//
//    private JRadioButton invButton;
//
//    private JRadioButton contInvButton;
//
//    private ButtonGroup buttonGroup;

    //private boolean creatingControlledInventories;

    public EditSectorScenarioOutputsTab(SectorScenario sectorScenario, 
            MessagePanel messagePanel, EmfConsole parentConsole, 
            EmfSession session, DesktopManager desktopManager) {
        super.setName("outputs");
        this.session = session;
        this.sectorScenario = sectorScenario;
        this.messagePanel = messagePanel;
        this.desktopManager = desktopManager;
        this.parentConsole = parentConsole;
//        setLayout(controlStrategy, controlStrategyResults);
    }

    public void display(SectorScenario sectorScenario, SectorScenarioOutput[] sectorScenarioOutputs) {
      setLayout(sectorScenarioOutputs);
    }

    private void setLayout(SectorScenarioOutput[] sectorScenarioOutputs) {
//        //this is called for both the load window process, so i'll set the boolean flag here...
        
        setLayout(new BorderLayout());
        removeAll();
        add(outputPanel(sectorScenarioOutputs ));
    }

    public void save(SectorScenario sectorScenario) {
        this.sectorScenario = sectorScenario;
        sectorScenario.setExportDirectory(folder.getText());
    }

    public void observe(EditSectorScenarioOutputsTabPresenter presenter) {
        this.presenter = presenter;
    }

    public void export() {
        try {
            validateFolder(folder.getText());
            
            SectorScenarioOutput[] sectorScenarioOutputs = getSelectedDatasets();
            List<EmfDataset> datasetList = new ArrayList<EmfDataset>();
            for (int i = 0; i < sectorScenarioOutputs.length; i++) {
                datasetList.add(sectorScenarioOutputs[i].getOutputDataset());
            }
            presenter.doExport(datasetList.toArray(new EmfDataset[0]), folder.getText());
            messagePanel.setMessage("Started Export. Please monitor the Status window to track your export request");
        } catch (EmfException e) {
            messagePanel.setMessage(e.getMessage());
        }
    }

    public void analyze() {
        clearMsgPanel();
        Thread viewResultsThread = new Thread(new Runnable() {
            public void run() {
                try {
                    SectorScenarioOutput[] sectorScenarioOutputs = getSelectedDatasets();
                    if ( sectorScenarioOutputs==null || sectorScenarioOutputs.length ==0)
                        throw new EmfException("Please select a result. ");
                    List<EmfDataset> datasetList = new ArrayList<EmfDataset>();
                    for (int i = 0; i < sectorScenarioOutputs.length; i++) {

                        datasetList.add(sectorScenarioOutputs[i].getOutputDataset());

                    }

                    if (datasetList.size() == 0)
                        throw new EmfException("No dataset is selected. ");
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    
                    if (presenter.getTableRecordCount(datasetList.get(0)) > 300000) {
                        String title = "Warning";
                        String message = "Are you sure you want to view the result? The table has over 300,000 records. It could take several minutes to load the data.";
                        int selection = JOptionPane.showConfirmDialog(parentConsole, message, title,
                                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                        if (selection == JOptionPane.NO_OPTION) {
                            return;
                        }
                    }
                    
                    presenter.doAnalyze(sectorScenario.getName(), datasetList.toArray(new EmfDataset[0]));
                } catch (EmfException e) {
                    messagePanel.setMessage(e.getMessage());
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });
        viewResultsThread.start();
    }



    private JPanel folderPanel() {
        JLabel folderLabel = new JLabel("Export Folder: ");
        folder = new TextField("folderName", 30);
        String exportDirectory = sectorScenario.getExportDirectory();
        exportDirectory = (exportDirectory != null ? exportDirectory : presenter.folder());
        folder.setText(exportDirectory);
        
        Button browseButton = new BrowseButton(browseAction());

        JPanel panel = new JPanel();
        panel.add(folderLabel);
        panel.add(folder);
        panel.add(browseButton);

        return panel;
    }

    private JPanel outputPanel(SectorScenarioOutput[] sectorScenarioOutputs) {
        JPanel tablePanel = tablePanel(sectorScenarioOutputs);
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

    private JPanel tablePanel(SectorScenarioOutput[] sectorScenarioOutputs) {
        SectorScenarioOutputTableData tableData = new SectorScenarioOutputTableData(sectorScenarioOutputs);
        table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());
//        table.getModel().addTableModelListener(new TableModelListener() {
//
//            public void tableChanged(TableModelEvent e) {
//                toggleRadioButtons();
//            }
//        }
//        );
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
//        analysisButton.setVisible(false);
//        view = new ViewButton("View", viewAction());
        viewDataButton = new Button("View Data", viewDataAction());
        editButton = new Button("Edit", editAction());
        summarizeButton = new Button("Summarize", summarizeAction());
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        
        buttonPanel.add(viewDataButton);
        buttonPanel.add(editButton);
        buttonPanel.add(summarizeButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(analysisButton);
//        buttonPanel.add(createButton);
//        buttonPanel.add(customizeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    private Action editAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    edit();
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }       
        };
    }
    
    private void edit() throws EmfException {
        SectorScenarioOutput[] sectorScenarioResults = getSelectedDatasets();
        if (sectorScenarioResults.length == 0) {
            messagePanel.setError("Please select at least one item.");
            return;
        }

        int counter = 0;
        for (int i = 0; i < sectorScenarioResults.length; i++) {
            DatasetPropertiesEditor view = new DatasetPropertiesEditor(session, parentConsole, desktopManager);

            presenter.doDisplayPropertiesEditor(view, sectorScenarioResults[i].getOutputDataset());
            counter++;
        }
    }
    
    private Action exportAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                export();
            }
        };
    }

    private Action analysisAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                analyze();
            }
        };
    }


    private Action viewDataAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                viewDataSetsData();
            }
        };
    }

    private Action summarizeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
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
        EmfFileInfo initDir = new EmfFileInfo(folder.getText(), true, true);
        
        EmfFileChooser chooser = new EmfFileChooser(parentConsole.getSession(), initDir, new EmfFileSystemView(session.dataCommonsService()));
        chooser.setTitle("Select a folder to contain the exported strategy results");
        int option = chooser.showDialog(parentConsole, "Select a folder");

        EmfFileInfo file = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedDir() : null;
        if (file == null)
            return;

        if (file.isDirectory()) {
            folder.setText(file.getAbsolutePath());
            presenter.setLastFolder(file.getAbsolutePath());
        }
    }

//    public void recentExportFolder(String recentfolder) {
//        if (recentfolder != null)
//            folder.setText(recentfolder);
//    }

    public String getExportFolder() {
        return folder.getText();
    }
    
    public void displayAnalyzeTable(String controlStrategyName, String[] fileNames) {
        AnalysisEngineTableApp app = new AnalysisEngineTableApp("Analyze Control Strategy: " + controlStrategyName, new Dimension(500, 500), desktopManager, parentConsole);
        app.display(fileNames);
    }

    public void refresh(SectorScenario sectorScenario, SectorScenarioOutput[] sectorScenarioOutputs) {
        setLayout(sectorScenarioOutputs);
        SectorScenarioOutputTableData tableData = new SectorScenarioOutputTableData(sectorScenarioOutputs);
        table.refresh(tableData);
        tablePanel.removeAll();
        tablePanel.add(table);
        super.validate();
    }
    
    private void viewDataSetsData() {
        SectorScenarioOutput[] sectorScenarioResults = getSelectedDatasets();
        if (sectorScenarioResults.length == 0) {
            messagePanel.setError("Please select at least one item.");
            return;
        }
         
        for (int i = 0; i < sectorScenarioResults.length; i++) {
            EmfDataset dataset = sectorScenarioResults[i].getOutputDataset();
            if (dataset != null)
                showDatasetDataViewer(dataset);
        }
    }
    
    private void summarize() {
        SectorScenarioOutput[] sectorScenarioResults = getSelectedDatasets();
        if (sectorScenarioResults.length == 0) {
            messagePanel.setError("Please select at least one item.");
            return;
        }

        try{ 
            for (int i = 0; i < sectorScenarioResults.length; i++) {
                EmfDataset dataset = null;
                DatasetPropertiesEditor view = new DatasetPropertiesEditor(session, parentConsole, desktopManager);
                dataset = sectorScenarioResults[i].getOutputDataset();
                if (dataset != null) {
                    presenter.doDisplayPropertiesEditor(view, dataset);
                    view.setDefaultTab(7);
                }
            }
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }
    
    private void showDatasetDataViewer(EmfDataset dataset) {
        try {
            Version[] versions = presenter.getVersions(dataset.getId());
            //if just one version, then go directly to the dataviewer
            if (versions.length == 1) {
                DataViewer dataViewerView = new DataViewer(dataset, parentConsole, desktopManager);
                DataViewPresenter dataViewPresenter = new DataViewPresenter(dataset, versions[0], getTableName(dataset), dataViewerView, session);
                dataViewPresenter.display();
            //else goto to dataset editior and display different version to display
            } else {
                DatasetPropertiesViewer datasetPropertiesViewerView = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
                presenter.doDisplayPropertiesView(datasetPropertiesViewerView, dataset);
                datasetPropertiesViewerView.setDefaultTab(1);
            }
//            presenter.doView(version, table, view);
        } catch (EmfException e) {
//            displayError(e.getMessage());
        }
    }
    
    protected String getTableName(Dataset dataset) {
        InternalSource[] internalSources = dataset.getInternalSources();
        String tableName = "";
        if (internalSources.length > 0)
            tableName = internalSources[0].getTable();
        return tableName;
    }

    private SectorScenarioOutput[] getSelectedDatasets() {
        return table.selected().toArray(new SectorScenarioOutput[0]);
    }

    public void clearMsgPanel() {
        this.messagePanel.clear();
    }

    private void validateFolder(String folder) throws EmfException {
        if (folder == null || folder.trim().isEmpty())
            throw new EmfException("Please select a valid folder to export.");
        
        if (folder.contains("/home/") || folder.endsWith("/home"))
            throw new EmfException("Export data into user's home directory is not allowed.");
    }

    public void notifyScenarioRun(SectorScenario sectorScenario) {
        if (sectorScenario.getDeleteResults()) {
            refresh(sectorScenario, new SectorScenarioOutput[] {});
        }
    }

    public void viewOnly() {
        // NOTE Auto-generated method stub
        
    }
}
