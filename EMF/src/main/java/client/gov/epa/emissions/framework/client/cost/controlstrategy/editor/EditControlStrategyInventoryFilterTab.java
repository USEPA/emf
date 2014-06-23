package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.BorderlessButton;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionView;
import gov.epa.emissions.framework.client.data.viewer.DataViewPresenter;
import gov.epa.emissions.framework.client.data.viewer.DataViewer;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

public class EditControlStrategyInventoryFilterTab extends JPanel implements EditControlStrategyTabView {

//    private TextField countyFileTextField;
    
    private TextArea filter;
    
    private MessagePanel messagePanel;
    
    private ControlStrategy controlStrategy;
    
    private EmfSession session;
    
    protected EmfConsole parentConsole;

    private ManageChangeables changeablesList;

    private ControlStrategyInputDatasetTableData tableData;

    private SelectableSortFilterWrapper table;

    private JPanel tablePanel;

    //private JPanel mainPanel;

    private DesktopManager desktopManager;
    
    private EditControlStrategyPresenter editControlStrategyPresenter;
    
    private ComboBox version, dataset;
    
    private JCheckBox mergeInventories;

    private JPanel invFilterCountyDatasetPanel;
    
    public EditControlStrategyInventoryFilterTab(ControlStrategy controlStrategy, ManageChangeables changeablesList, 
            MessagePanel messagePanel, EmfConsole parentConsole, 
            EmfSession session, DesktopManager desktopManager,
            EditControlStrategyPresenter editControlStrategyPresenter) throws EmfException {
        super.setName("csFilter");
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.controlStrategy = controlStrategy;
        this.parentConsole = parentConsole;
        this.session = session;
        this.desktopManager = desktopManager;
        this.editControlStrategyPresenter = editControlStrategyPresenter;
        doLayout(controlStrategy.getControlStrategyInputDatasets());
    }

    private void doLayout(ControlStrategyInputDataset[] controlStrategyInputDatasets) throws EmfException {
        tableData = new ControlStrategyInputDatasetTableData(controlStrategyInputDatasets);
        JPanel panel = new JPanel(new BorderLayout());
        invFilterCountyDatasetPanel = createMiddleSection(controlStrategy);
        panel.add(invFilterCountyDatasetPanel, BorderLayout.CENTER);
        
        setLayout(new BorderLayout(5, 5));
        add(panel,BorderLayout.SOUTH);
        // mainPanel.add(buttonPanel(), BorderLayout.SOUTH);
        //mainPanel = new JPanel(new BorderLayout(10, 10));
        //buildSortFilterPanel();
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.add(buildSortFilterPanel(), BorderLayout.CENTER);
        notifyStrategyTypeChange(controlStrategy.getStrategyType());
    }

    private JPanel buildSortFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new Border("Inventories to Process"));
        //SortFilterSelectionPanel sfpanel = sortFilterPanel();
        panel.add(tablePanel(), BorderLayout.CENTER);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        return panel; 
    }
    
    private JPanel tablePanel() {
        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, tableData, null);
        tablePanel.add(table);

        return tablePanel;
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        Button addButton = new BorderlessButton("Add", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    addAction();
                } catch (EmfException e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        panel.add(addButton);
        Button editButton = new BorderlessButton("Set Version", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                    setVersionAction();
            }
        });
        panel.add(editButton);
        Button removeButton = new BorderlessButton("Remove", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {           
                    removeAction();
            }
        });
        panel.add(removeButton);
        Button viewButton = new BorderlessButton("View", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    viewAction();
                } catch (EmfException e) {
                    messagePanel.setError("Error viewing dataset: " + e.getMessage());
                }
            }
        });
        panel.add(viewButton);
        Button viewDataButton = new BorderlessButton("View Data", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    viewDataAction();
                } catch (EmfException e) {
                    messagePanel.setError("Error viewing dataset data: " + e.getMessage());
                }
            }
        });
        panel.add(viewDataButton);

        JPanel rightPanel = new JPanel();
        mergeInventories = new JCheckBox("Merge Inventories", null, controlStrategy.getMergeInventories() != null ? controlStrategy.getMergeInventories() : true);
        mergeInventories.setEnabled(false);
        rightPanel.add(mergeInventories);
        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.LINE_START);
        container.add(rightPanel, BorderLayout.LINE_END);

        return container;
    }


    private void viewDataAction() throws EmfException {
        messagePanel.clear();
        List selected = table.selected();

        if (selected.size() == 0) {
            messagePanel.setError("Please select an item to view.");
            return;
        }

        for (int i = 0; i < selected.size(); i++) {
            EmfDataset dataset = editControlStrategyPresenter.getDataset(((ControlStrategyInputDataset)selected.get(i)).getInputDataset().getId());
            showDatasetDataViewer(dataset);
        }
    }

    private void addAction() throws EmfException {
        InputDatasetSelectionView view = new InputDatasetSelectionDialog(parentConsole);
        InputDatasetSelectionPresenter presenter = new InputDatasetSelectionPresenter(view, session,
                (controlStrategy.getStrategyType() != null && controlStrategy.getStrategyType().getName().equals(StrategyType.projectFutureYearInventory) 
                        ?
                        new DatasetType[] { 
                            editControlStrategyPresenter.getDatasetType(DatasetType.orlPointInventory),
                            editControlStrategyPresenter.getDatasetType(DatasetType.orlNonpointInventory),
                            editControlStrategyPresenter.getDatasetType(DatasetType.orlNonroadInventory),
                            editControlStrategyPresenter.getDatasetType(DatasetType.orlOnroadInventory),
                            editControlStrategyPresenter.getDatasetType(DatasetType.FLAT_FILE_2010_POINT),
                            editControlStrategyPresenter.getDatasetType(DatasetType.FLAT_FILE_2010_NONPOINT)
                        }
                        :
                            (controlStrategy.getStrategyType() != null 
                                    && 
                                    (
                                        controlStrategy.getStrategyType().getName().equals(StrategyType.maxEmissionsReduction)
                                        || controlStrategy.getStrategyType().getName().equals(StrategyType.MULTI_POLLUTANT_MAX_EMISSIONS_REDUCTION)
                                        || controlStrategy.getStrategyType().getName().equals(StrategyType.leastCost)
                                        || controlStrategy.getStrategyType().getName().equals(StrategyType.leastCostCurve)
                                    )
                                    ?
                                    new DatasetType[] { 
                                        editControlStrategyPresenter.getDatasetType(DatasetType.orlPointInventory),
                                        editControlStrategyPresenter.getDatasetType(DatasetType.orlNonpointInventory),
                                        editControlStrategyPresenter.getDatasetType(DatasetType.orlNonroadInventory),
                                        editControlStrategyPresenter.getDatasetType(DatasetType.orlOnroadInventory),
                                        editControlStrategyPresenter.getDatasetType(DatasetType.FLAT_FILE_2010_POINT),
                                        editControlStrategyPresenter.getDatasetType(DatasetType.FLAT_FILE_2010_NONPOINT)
                                    }
                                    :
                                    new DatasetType[] { 
                                        editControlStrategyPresenter.getDatasetType(DatasetType.orlPointInventory),
                                        editControlStrategyPresenter.getDatasetType(DatasetType.orlNonpointInventory),
                                        editControlStrategyPresenter.getDatasetType(DatasetType.orlNonroadInventory),
                                        editControlStrategyPresenter.getDatasetType(DatasetType.orlOnroadInventory)
                                    }
                            )
                )
            );
        try {
            presenter.display(null, false);
            if (view.shouldCreate()){
                EmfDataset[] inputDatasets = presenter.getDatasets();
                ControlStrategyInputDataset[] controlStrategyInputDatasets = new ControlStrategyInputDataset[inputDatasets.length];
                for (int i = 0; i < inputDatasets.length; i++) {
                    controlStrategyInputDatasets[i] = new ControlStrategyInputDataset(inputDatasets[i]);
                    controlStrategyInputDatasets[i].setVersion(inputDatasets[i].getDefaultVersion());
                }
                tableData.add(controlStrategyInputDatasets);
                if (inputDatasets.length > 0) editControlStrategyPresenter.fireTracking();
                refresh();
            }
        } catch (Exception exp) {
            messagePanel.setError(exp.getMessage());
        }
    }

    private void setVersionAction(){
        messagePanel.clear();
        //get a single selected item
        List selected = table.selected();
        if (selected.size() != 1) {
            messagePanel.setMessage("Please select only a single inventory to set its version.");
            return;
        }
        ControlStrategyInputDataset[] controlStrategyInputDatasets = (ControlStrategyInputDataset[]) selected.toArray(new ControlStrategyInputDataset[0]);

        EmfDataset dataset=controlStrategyInputDatasets[0].getInputDataset();
        //Show select version dialog
        CSInventoryEditDialog dialog=new CSInventoryEditDialog(parentConsole, dataset, editControlStrategyPresenter, this);
        dialog.run();
        
    }
    
    public void editVersion(Version version, EmfDataset dataset) {
        messagePanel.clear();
        //get all measures
        ControlStrategyInputDataset[] datasets =tableData.sources();
        //get versions of selected item
        if (version != null) {
            //validate value
            
            //only update items that have been selected          
            for (int j = 0; j < datasets.length; j++) {
                if (dataset.equals(datasets[j].getInputDataset())) {
                    datasets[j].setVersion(version.getVersion());
                }
            }
            //repopulate the tabe data
            tableData = new ControlStrategyInputDatasetTableData(datasets);
            //rebuild the sort filter panelControlStrategyInputDatasetTableData
            refresh();
        }
    }
    
    private void refresh(){
        table.refresh(tableData);
        panelRefresh();
    }
    
    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.validate();
    }

    private void fillVersions(EmfDataset dataset, Integer versionNumber) throws EmfException{
        version.setEnabled(true);

        if (dataset != null && dataset.getName().equals("None")) dataset = null;
        Version[] versions = editControlStrategyPresenter.getVersions(dataset);
        version.removeAllItems();
        version.setModel(new DefaultComboBoxModel(versions));
        version.revalidate();
        if (versions.length > 0) 
            version.setSelectedIndex(getVersionIndex(versions, dataset, versionNumber));

    }
    
    private int getVersionIndex(Version[] versions, EmfDataset dataset, Integer version) {
//        int defaultversion = dataset.getDefaultVersion();
        
        if (version != null) {
            for (int i = 0; i < versions.length; i++)
                if (version == versions[i].getVersion())
                    return i;
        }

        return 0;
    }
 
    protected void removeAction() {
        messagePanel.clear();
        List selected = table.selected();

        if (selected.size() == 0) {
            messagePanel.setError("Please select an inventory to remove.");
            return;
        }

        ControlStrategyInputDataset[] controlStrategyInputDatasets = (ControlStrategyInputDataset[]) selected.toArray(new ControlStrategyInputDataset[0]);

        if (controlStrategyInputDatasets.length == 0)
            return;

        String title = "Warning";
        String message = "Are you sure you want to remove the selected inventories?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            tableData.remove(controlStrategyInputDatasets);
            if (controlStrategyInputDatasets.length > 0) editControlStrategyPresenter.fireTracking();
            refresh();
        }
    }

    private void viewAction() throws EmfException {
        messagePanel.clear();
        List selected = table.selected();

        if (selected.size() == 0) {
            messagePanel.setError("Please select an item to view.");
            return;
        }

        for (int i = 0; i < selected.size(); i++) {
            PropertiesViewPresenter presenter = new PropertiesViewPresenter(
                    editControlStrategyPresenter.getDataset(((ControlStrategyInputDataset)selected.get(i)).getInputDataset().getId()), session);
            DatasetPropertiesViewer view = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
            presenter.doDisplay(view);
        }
    }
    
    private void viewCountyDataset() throws EmfException{
        messagePanel.clear();
        EmfDataset countyDataset = (EmfDataset) dataset.getSelectedItem();
        if (countyDataset == null){
            messagePanel.setError("Please select an item to view.");
            return;
        }
        PropertiesViewPresenter presenter = new PropertiesViewPresenter(countyDataset, session);
                //editControlStrategyPresenter.getDataset(countyDataset.getId()), session);
        DatasetPropertiesViewer view = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
        presenter.doDisplay(view);
        
    }
    
    private void viewCountyDatasetData() {
        messagePanel.clear();
        EmfDataset countyDataset = (EmfDataset) dataset.getSelectedItem();
        if (countyDataset == null){
            messagePanel.setError("Please select an item to view.");
            return;
        }
        showDatasetDataViewer(countyDataset);
    }
    
    
    private JPanel createMiddleSection(ControlStrategy controlStrategy) throws EmfException {
        JPanel middlePanel = new JPanel(new SpringLayout());
        middlePanel.setBorder(new Border("Filters"));

        String value = controlStrategy.getFilter();
        if (value == null)
            value = "";
        
        filter = new TextArea("filter", value, 40, 2);
        filter.setToolTipText("Enter a filter that could be entered as a SQL where clause (e.g., ANN_EMIS>5000 and SCC like '30300%')");
        JScrollPane scrollPane = new JScrollPane(filter);
        changeablesList.addChangeable(filter);
        
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        layoutGenerator.addLabelWidgetPair("Inventory Filter:", scrollPane, middlePanel);

        EmfDataset[] datasets = editControlStrategyPresenter.getDatasets( editControlStrategyPresenter.getDatasetType("List of Counties (CSV)") );
//        String width = EmptyStrings.create(80);
//        Dimension size=new Dimension(500, 13);
        
        dataset = new ComboBox("Not selected", datasets);
        if (controlStrategy.getCountyDataset() != null) dataset.setSelectedItem(controlStrategy.getCountyDataset());

        dataset.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    EmfDataset countyDataset = (EmfDataset)dataset.getSelectedItem();
                    Integer versionNumber = (countyDataset != null ? countyDataset.getDefaultVersion() : null);

                    fillVersions(countyDataset, versionNumber);
                } catch (EmfException e1) {
                    // NOTE Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });

        version =new ComboBox(new Version[0]);           
//        version.setPrototypeDisplayValue(width);
        try {
            EmfDataset countyDataset = controlStrategy.getCountyDataset();
            Integer versionNumber = (countyDataset != null ? controlStrategy.getCountyDatasetVersion() : null);
            fillVersions(countyDataset, versionNumber);
        } catch (EmfException e1) {
            // NOTE Auto-generated catch block
            e1.printStackTrace();
        }
//        if (controlStrategy.getCountyDataset() != null) version.setSelectedItem(controlStrategy.getCountyDatasetVersion());
        
        layoutGenerator.addLabelWidgetPair("County Dataset:", datasetPanel(), middlePanel);
        layoutGenerator.addLabelWidgetPair("County Dataset Version:", version, middlePanel);

        layoutGenerator.makeCompactGrid(middlePanel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 5);// xPad, yPad

        return middlePanel;
    }
    
    private JPanel datasetPanel() {

        changeablesList.addChangeable(dataset);
        dataset.setToolTipText("Press select button to choose from a dataset list.");
        Button viewButton = new BorderlessButton("View", viewDatasetAction()); 
        JPanel invPanel = new JPanel(new BorderLayout(5,0));

        invPanel.add(dataset, BorderLayout.LINE_START);
        invPanel.add(viewButton);
        Button viewDataButton = new BorderlessButton("View Data", viewCountyDatasetDataAction()); 
        invPanel.add(viewDataButton, BorderLayout.LINE_END );
        return invPanel;
    }
    
    private Action viewDatasetAction() {
        return new AbstractAction(){
            public void actionPerformed(ActionEvent event) {
                try {
                    viewCountyDataset();
                } catch (EmfException e) {
                    messagePanel.setError("Error viewing dataset: " + e.getMessage());
                }
            }
        };
    }
    
    private Action viewCountyDatasetDataAction() {
        return new AbstractAction(){
            public void actionPerformed(ActionEvent event) {
                viewCountyDatasetData();
            }
        };
    }
    
    public void save(ControlStrategy controlStrategy) throws EmfException {
        String value = filter.getText().trim();

        controlStrategy.setFilter(value);
//        controlStrategy.setCountyFile(countyFileTextField.getText().trim());
        EmfDataset ds =(EmfDataset) dataset.getSelectedItem();
        if (ds == null) {
            ds = null;
        }
        controlStrategy.setCountyDataset(ds);
        Version ver = (ds !=null ? (Version) version.getSelectedItem(): null);
        Integer verValue = (ver !=null? ver.getVersion(): null);
        controlStrategy.setCountyDatasetVersion(verValue);

        
        ControlStrategyInputDataset[] inputDatasets = {};
        if (tableData != null) {
            inputDatasets = new ControlStrategyInputDataset[tableData.rows().size()];
            for (int i = 0; i < tableData.rows().size(); i++) {
                inputDatasets[i] = (ControlStrategyInputDataset)tableData.element(i);
            }
            controlStrategy.setControlStrategyInputDatasets(inputDatasets);
        }
        controlStrategy.setMergeInventories(mergeInventories.isSelected());
        //make sure if there are multiple inventories for the least cost strategies,
        //then enforce merging datasets, this type of strategy only takes one input inventory.
        if (inputDatasets.length > 1
                && (controlStrategy.getStrategyType().getName().equals(StrategyType.leastCost) 
                        || controlStrategy.getStrategyType().getName().equals(StrategyType.leastCostCurve))
                && !controlStrategy.getMergeInventories()) 
            throw new EmfException("Inventories Tab: Multiple inventories must be merged.  Check the Merge Inventories checkbock.");
    }

    public void refresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        tableData.add(controlStrategy.getControlStrategyInputDatasets());
        refresh();
    }

    public void notifyStrategyTypeChange(StrategyType strategyType) {
        
        if (strategyType != null && (strategyType.getName().equals(StrategyType.leastCost) || strategyType.getName().equals(StrategyType.leastCostCurve))) {            
            mergeInventories.setVisible(true);
            invFilterCountyDatasetPanel.setVisible(true);
        }
        else if (strategyType != null && (strategyType.getName().equals(StrategyType.MULTI_POLLUTANT_MAX_EMISSIONS_REDUCTION))) {
            mergeInventories.setVisible(true);
            invFilterCountyDatasetPanel.setVisible(false);
        }
        else {
            mergeInventories.setVisible(false);
            invFilterCountyDatasetPanel.setVisible(true);
        }
    }

    public void notifyStrategyRun(ControlStrategy controlStrategy) {
        // NOTE Auto-generated method stub
        
    }
    
    private void showDatasetDataViewer(EmfDataset dataset) {
        try {
            Version[] versions = editControlStrategyPresenter.getVersions(dataset);
            //if just one version, then go directly to the dataviewer
            if (versions.length == 1) {
                DataViewer dataViewerView = new DataViewer(dataset, parentConsole, desktopManager);
                DataViewPresenter dataViewPresenter = new DataViewPresenter(dataset, versions[0], getTableName(dataset), dataViewerView, session);
                dataViewPresenter.display();
            //else goto to dataset editior and display different version to display
            } else {
                DatasetPropertiesViewer datasetPropertiesViewerView = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
                editControlStrategyPresenter.doDisplayPropertiesView(datasetPropertiesViewerView, dataset);
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

    public void run(ControlStrategy controlStrategy) {
        // NOTE Auto-generated method stub
        
    }

    public void setTargetPollutants(Pollutant[] pollutants) {
        // NOTE Auto-generated method stub
        
    }

    public void fireStrategyTypeChanges(StrategyType strategyType) {
        notifyStrategyTypeChange(strategyType);
    }
}