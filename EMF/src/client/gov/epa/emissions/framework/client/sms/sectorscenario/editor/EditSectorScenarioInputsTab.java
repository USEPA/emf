package gov.epa.emissions.framework.client.sms.sectorscenario.editor;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.BorderlessButton;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionView;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.client.sms.sectorscenario.SectorScenarioInputDatasetTableData;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.sms.SectorScenario;
import gov.epa.emissions.framework.services.sms.SectorScenarioInventory;
import gov.epa.emissions.framework.services.sms.SectorScenarioOutput;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class EditSectorScenarioInputsTab extends JPanel implements EditSectorScenarioInputsTabView {

//    private TextField countyFileTextField;

    private MessagePanel messagePanel;
    
    private SectorScenario sectorScenario;
    
    private EmfSession session;
    
    protected EmfConsole parentConsole;

    private ManageChangeables changeablesList;

    private SectorScenarioInputDatasetTableData tableData;

    private SelectableSortFilterWrapper table;

    private JPanel tablePanel;

    //private JPanel mainPanel;

    private DesktopManager desktopManager;
    
    private EditSectorScenarioInputsTabPresenter presenter;
    
    private EditSectorScenarioPresenter editPresenter;
    
    private ComboBox eecsMappingDataset;
    
    private ComboBox eecsMappingDatasetVersion;

    private ComboBox sectorMappingDataset;

    private ComboBox sectorMappingDatasetVersion;

    private BorderlessButton addButton;

    private BorderlessButton editButton;

    private BorderlessButton removeButton;

    private BorderlessButton viewButton;

    private JCheckBox straightEECSMatch;
    
    public EditSectorScenarioInputsTab(SectorScenario sectorScenario, ManageChangeables changeablesList, 
            MessagePanel messagePanel, EmfConsole parentConsole, 
            EmfSession session, DesktopManager desktopManager,
            EditSectorScenarioPresenter editPresenter){
        super.setName("sectorscenarioinputs");
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.sectorScenario = sectorScenario;
        this.parentConsole = parentConsole;
        this.session = session;
        this.desktopManager = desktopManager;
        this.editPresenter = editPresenter; 
    }


    public void observe(EditSectorScenarioInputsTabPresenter presenter){
        this.presenter = presenter;
    }

    public void display() throws EmfException {
        SectorScenarioInventory[] sectorScenarioInventories = sectorScenario.getInventories();
        tableData = new SectorScenarioInputDatasetTableData(sectorScenarioInventories);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createMiddleSection(), BorderLayout.CENTER);
        
        setLayout(new BorderLayout(5, 5));
        add(panel,BorderLayout.NORTH);
        // mainPanel.add(buttonPanel(), BorderLayout.SOUTH);
        //mainPanel = new JPanel(new BorderLayout(10, 10));
        //buildSortFilterPanel();
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.add(buildSortFilterPanel(), BorderLayout.CENTER);
        
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
        addButton = new BorderlessButton("Add", new AbstractAction() {
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
        editButton = new BorderlessButton("Set Version", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                    setVersionAction();
            }
        });
        panel.add(editButton);
        removeButton = new BorderlessButton("Remove", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {           
                    removeAction();
            }
        });
        panel.add(removeButton);
        viewButton = new BorderlessButton("View", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    viewAction();
                } catch (EmfException e) {
                    messagePanel.setError("Error viewing dataset: " + e.getMessage());
                }
            }
        });
        panel.add(viewButton);
//        Button viewDataButton = new BorderlessButton("View Data", new AbstractAction() {
//            public void actionPerformed(ActionEvent event) {
//                try {
//                    viewDataAction();
//                } catch (EmfException e) {
//                    messagePanel.setError("Error viewing dataset data: " + e.getMessage());
//                }
//            }
//        });
//        panel.add(viewDataButton);
        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.LINE_START);

        return container;
    }


//    private void viewDataAction() throws EmfException {
//        messagePanel.clear();
//        List selected = table.selected();
//
//        if (selected.size() == 0) {
//            messagePanel.setError("Please select an item to view.");
//            return;
//        }
//
//        for (int i = 0; i < selected.size(); i++) {
//            EmfDataset dataset = editPresenter.getDataset(((SectorScenarioInventory)selected.get(i)).getDataset().getId());
//            showDatasetDataViewer(dataset);
//        }
//    }

    private void addAction() throws EmfException {
        InputDatasetSelectionView view = new InputDatasetSelectionDialog(parentConsole);
        InputDatasetSelectionPresenter selPresenter = new InputDatasetSelectionPresenter(view, session,
                new DatasetType[] { 
                    presenter.getDatasetType(DatasetType.ORL_POINT_NATA),
                    presenter.getDatasetType(DatasetType.ORL_POINT_NATA_SECTOR_ANNOTATED),
                    presenter.getDatasetType(DatasetType.NOF_POINT),
                    presenter.getDatasetType(DatasetType.NOF_NONPOINT)
                });
        try {
            selPresenter.display(null, false);
            if (view.shouldCreate()){
                EmfDataset[] inputDatasets = selPresenter.getDatasets();
                SectorScenarioInventory[] sectorScenarioInventories = new SectorScenarioInventory[inputDatasets.length];
                for (int i = 0; i < inputDatasets.length; i++) {
                    sectorScenarioInventories[i] = new SectorScenarioInventory(inputDatasets[i]);
                    sectorScenarioInventories[i].setVersion(inputDatasets[i].getDefaultVersion());
                }
                tableData.add(sectorScenarioInventories);
                if (inputDatasets.length > 0) editPresenter.fireTracking();
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
        SectorScenarioInventory[] sectorScenarioInventories = (SectorScenarioInventory[]) selected.toArray(new SectorScenarioInventory[0]);

        EmfDataset dataset=sectorScenarioInventories[0].getDataset();
        //Show select version dialog
        SSInventoryEditDialog dialog=new SSInventoryEditDialog(parentConsole, dataset, presenter, this);
        dialog.run();
        
    }
    
    public void editVersion(Version version, EmfDataset dataset) {
        messagePanel.clear();
        //get all measures
        SectorScenarioInventory[] datasets =tableData.sources();
        //get versions of selected item
        if (version != null) {
            //validate value
            
            //only update items that have been selected          
            for (int j = 0; j < datasets.length; j++) {
                if (dataset.equals(datasets[j].getDataset())) {
                    datasets[j].setVersion(version.getVersion());
                }
            }
            //repopulate the tabe data
            tableData = new SectorScenarioInputDatasetTableData(datasets);
            
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
    
    private int getDefaultVersionIndex(Version[] versions, EmfDataset dataset) {
        int defaultversion = dataset.getDefaultVersion();

        for (int i = 0; i < versions.length; i++)
            if (defaultversion == versions[i].getVersion())
                return i;

        return 0;
    }
 
    protected void removeAction() {
        messagePanel.clear();
        List selected = table.selected();

        if (selected.size() == 0) {
            messagePanel.setError("Please select an inventory to remove.");
            return;
        }

        SectorScenarioInventory[] SectorScenarioInventories = (SectorScenarioInventory[]) selected.toArray(new SectorScenarioInventory[0]);

        if (SectorScenarioInventories.length == 0)
            return;

        String title = "Warning";
        String message = "Are you sure you want to remove the selected inventories?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            tableData.remove(SectorScenarioInventories);
            if (SectorScenarioInventories.length > 0) editPresenter.fireTracking();
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
            PropertiesViewPresenter viewPresenter = new PropertiesViewPresenter(
                    editPresenter.getDataset(((SectorScenarioInventory)selected.get(i)).getDataset().getId()), session);
            DatasetPropertiesViewer view = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
            viewPresenter.doDisplay(view);
        }
    }
    
    private void viewDataset(ComboBox datasetCom) throws EmfException{
        messagePanel.clear();
        EmfDataset dataset = (EmfDataset) datasetCom.getSelectedItem();
        if (dataset == null){
            messagePanel.setError("Please select an item to view.");
            return;
        }
        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        DatasetPropertiesViewer view = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
        presenter.doDisplay(view);
        
    }
    
//    private void viewDatasetData(ComboBox datasetCom) throws EmfException {
//        messagePanel.clear();
//        EmfDataset dataset = (EmfDataset) datasetCom.getSelectedItem();
//        if (dataset == null){
//            messagePanel.setError("Please select an item to view.");
//            return;
//        }
//        dataset = editPresenter.getDataset(dataset.getId());
//        showDatasetDataViewer(dataset);
//    }
//    
    private JPanel createMiddleSection() throws EmfException {
        Dimension dimension = new Dimension(400, 15);
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(new Border("Mappings"));

        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        sectorMappingDataset = new ComboBox("Not selected", presenter.getDatasets(presenter.getDatasetType(DatasetType.SECTOR_MAPPING)));
        sectorMappingDataset.setPreferredSize(dimension);
        if (sectorScenario.getSectorMapppingDataset() != null )
            sectorMappingDataset.setSelectedItem(sectorScenario.getSectorMapppingDataset());
        
        sectorMappingDataset.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    fillVersions(sectorMappingDatasetVersion, (EmfDataset)sectorMappingDataset.getSelectedItem());
                } catch (EmfException e1) {
                    // NOTE Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });

        sectorMappingDatasetVersion = new ComboBox(new Version[0]);      
//      version.setPrototypeDisplayValue(width);
        try {
            fillVersions(sectorMappingDatasetVersion, (EmfDataset)sectorMappingDataset.getSelectedItem());
            Integer sectorMapppingDatasetVersion = sectorScenario.getSectorMapppingDatasetVersion();
            if (sectorMapppingDatasetVersion != null ) 
                sectorMappingDatasetVersion.setSelectedIndex(sectorMapppingDatasetVersion);
        } catch (EmfException e1) {
            // NOTE Auto-generated catch block
            e1.printStackTrace();
        }

        layoutGenerator.addLabelWidgetPair("Sector Mapping Dataset:", datasetPanel(sectorMappingDataset), panel);
        layoutGenerator.addLabelWidgetPair("Dataset Version:", sectorMappingDatasetVersion, panel);

        Boolean straightEECSMatchValue = sectorScenario.getStraightEecsMatch() != null ? sectorScenario.getStraightEecsMatch() : false;
        straightEECSMatch = new JCheckBox("", null, straightEECSMatchValue);
        straightEECSMatch.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                enableStraightEECSMatchFields(straightEECSMatch.isSelected());
            }
        });

        eecsMappingDataset = new ComboBox("Not selected", presenter.getDatasets(presenter.getDatasetType(DatasetType.EECS_MAPPING)));
        eecsMappingDataset.setPreferredSize(dimension);
        if (sectorScenario.getEecsMapppingDataset() != null )
            eecsMappingDataset.setSelectedItem(sectorScenario.getEecsMapppingDataset());
        
        eecsMappingDataset.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    fillVersions(eecsMappingDatasetVersion, (EmfDataset)eecsMappingDataset.getSelectedItem());
                } catch (EmfException e1) {
                    // NOTE Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });
        
        eecsMappingDatasetVersion =new ComboBox(new Version[0]);      
//      version.setPrototypeDisplayValue(width);
        try {       
            fillVersions(eecsMappingDatasetVersion, (EmfDataset)eecsMappingDataset.getSelectedItem());
            
            Integer eecsMapppingDatasetVersion = sectorScenario.getEecsMapppingDatasetVersion();  
            if (eecsMapppingDatasetVersion != null ) {
                //System.out.print("eccs mapping is " + eecsMapppingDatasetVersion + "\n");
                eecsMappingDatasetVersion.setSelectedIndex(eecsMapppingDatasetVersion);
            }
        } catch (EmfException e1) {
            // NOTE Auto-generated catch block
            e1.printStackTrace();
        }

        enableStraightEECSMatchFields(straightEECSMatchValue);
        
        layoutGenerator.addLabelWidgetPair("Straight Match:", straightEECSMatch, panel);
        layoutGenerator.addLabelWidgetPair("EECS Mapping Dataset:", datasetPanel(eecsMappingDataset), panel);
        layoutGenerator.addLabelWidgetPair("Dataset Version:", eecsMappingDatasetVersion, panel);

        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 10, // initialX, initialY
                5, 5);// xPad, yPad
        return panel; 

    }

    private void enableStraightEECSMatchFields(Boolean straightEECSMatchValue) {
        eecsMappingDataset.setEnabled(straightEECSMatchValue);
        eecsMappingDatasetVersion.setEnabled(straightEECSMatchValue);
    }


    private JPanel datasetPanel(ComboBox datasetCom) {

        changeablesList.addChangeable(datasetCom);
        datasetCom.setToolTipText("Press select button to choose from a dataset list.");
        Button viewButton = new BorderlessButton("View", viewDatasetAction(datasetCom)); 
        JPanel invPanel = new JPanel(new BorderLayout(5,0));

        invPanel.add(datasetCom, BorderLayout.LINE_START);
        invPanel.add(viewButton);
//        Button viewDataButton = new BorderlessButton("View Data", viewDatasetDataAction(datasetCom)); 
//        invPanel.add(viewDataButton, BorderLayout.LINE_END );
        return invPanel;
    }
    
    private Action viewDatasetAction(final ComboBox datasetCom) {
        return new AbstractAction(){
            public void actionPerformed(ActionEvent event) {
                try {
                    viewDataset(datasetCom);
                } catch (EmfException e) {
                    messagePanel.setError("Error viewing dataset: " + e.getMessage());
                }
            }
        };
    }
    
//    private Action viewDatasetDataAction(final ComboBox datasetCom) {
//        return new AbstractAction(){
//            public void actionPerformed(ActionEvent event) {
//                try {
//                    viewDatasetData(datasetCom);
//                } catch (EmfException e) {
//                    messagePanel.setError("Error viewing dataset data: " + e.getMessage());
//                }
//            }
//        };
//    }
    
    public void save(SectorScenario sectorScenario) throws EmfException{
        
        //perform some basic validation...
        Boolean straightEECSMatchValue = straightEECSMatch.isSelected();
        sectorScenario.setStraightEecsMatch(straightEECSMatchValue);
   
        EmfDataset ds =(EmfDataset) eecsMappingDataset.getSelectedItem();
        if (straightEECSMatchValue && ds == null)
            throw new EmfException("Inputs Tab: Missing " + DatasetType.EECS_MAPPING + " dataset.");
        sectorScenario.setEecsMapppingDataset(ds);
        Version ver = (ds !=null ? (Version) eecsMappingDatasetVersion.getSelectedItem(): null);
        Integer verValue = (ver !=null? ver.getVersion(): null);
        sectorScenario.setEecsMapppingDatasetVersion(verValue);
        
        EmfDataset sectorDS =(EmfDataset) sectorMappingDataset.getSelectedItem();
        if (sectorDS == null)
            throw new EmfException("Inputs Tab: Missing " + DatasetType.SECTOR_MAPPING + " dataset.");
        sectorScenario.setSectorMapppingDataset(sectorDS);
        Version sectorVer = (sectorDS !=null ? (Version) sectorMappingDatasetVersion.getSelectedItem(): null);
        Integer sectorVerValue = (sectorVer !=null? sectorVer.getVersion(): null);
        sectorScenario.setSectorMapppingDatasetVersion(sectorVerValue);
        
        SectorScenarioInventory[] inputDatasets = {};
        if (tableData != null) {
            inputDatasets = new SectorScenarioInventory[tableData.rows().size()];
            for (int i = 0; i < tableData.rows().size(); i++) {
                inputDatasets[i] = (SectorScenarioInventory)tableData.element(i);
            }
            sectorScenario.setInventories(inputDatasets);
        }
    }

    
//    private void showDatasetDataViewer(EmfDataset dataset) {
//        try {
//            Version[] versions = presenter.getVersions(dataset);
//            //if just one version, then go directly to the dataviewer
//            if (versions.length == 1) {
//                DataViewer dataViewerView = new DataViewer(dataset, parentConsole, desktopManager);
//                DataViewPresenter dataViewPresenter = new DataViewPresenter(dataset, versions[0], getTableName(dataset), dataViewerView, session);
//                dataViewPresenter.display();
//            //else goto to dataset editior and display different version to display
//            } else {
//                DatasetPropertiesViewer datasetPropertiesViewerView = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
//                presenter.doDisplayPropertiesView(datasetPropertiesViewerView, dataset);
//                datasetPropertiesViewerView.setDefaultTab(1);
//            }
////            presenter.doView(version, table, view);
//        } catch (EmfException e) {
////            displayError(e.getMessage());
//        }
//    }
    
    protected String getTableName(Dataset dataset) {
        InternalSource[] internalSources = dataset.getInternalSources();
        String tableName = "";
        if (internalSources.length > 0)
            tableName = internalSources[0].getTable();
        return tableName;
    }
    
    private void fillVersions(ComboBox version, EmfDataset dataset) throws EmfException{
        version.setEnabled(true);

        if (dataset != null && dataset.getName().equals("None")) dataset = null;
        Version[] versions = presenter.getVersions(dataset);
        version.removeAllItems();
        version.setModel(new DefaultComboBoxModel(versions));
        version.revalidate();
        if (versions.length > 0)
            version.setSelectedIndex(getDefaultVersionIndex(versions, dataset));

    }


    public void refresh(SectorScenario sectorScenario, SectorScenarioOutput[] sectorScenarioOutputs) {
        // NOTE Auto-generated method stub
        
    }

    public void viewOnly() {
        editButton.setVisible(false);
        addButton.setVisible(false);
        removeButton.setVisible(false);
    }   
}