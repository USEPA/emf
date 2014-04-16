package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;

public class QAECControlScenarioWindow extends DisposableInteralFrame  {
    
    protected AddRemoveDatasetWidget datasetWidget;
    
    protected EmfConsole parentConsole;
    
    protected JPanel layout;
    
    protected EditQAECControlScenarioPresenter presenter1;
    
    protected ListWidget inventoryListWidget;
    
    protected ListWidget gsproDatasetListWidget;

    private ListWidget detailedResultDatasetListWidget;
    
    private ListWidget gsrefDatasetListWidget;
    
    protected EmfSession session;
    
    protected SingleLineMessagePanel messagePanel;
    
    private EmfDataset[] gsrefDatasets;
        
    private EmfDataset[] gsproDatasets;
    
    protected DatasetType orlPointDST;
    
    protected DatasetType orlNonpointDST;
    
    protected DatasetType orlNonroadDST;
    
    protected DatasetType orlOnroadDST;

    private DatasetType gsrefDST;
    
    private DatasetType gsproDST;
    
    private DatasetType detailedResultDST;
    
    private EmfDataset inventory;

    private EmfDataset detailedResultDataset;

//    private String program;
    
    public QAECControlScenarioWindow(DesktopManager desktopManager, 
            String program, 
            EmfSession session, 
            EmfDataset inventory, 
            EmfDataset detailedResultDataset,
            EmfDataset[] gsrefDatasets,
            EmfDataset[] gsproDatasets) {
        
        super("EC Control Scenario", new Dimension(750, 450), desktopManager);
//        this.program = program;
        this.session = session;
        this.gsrefDatasets = gsrefDatasets;
        this.gsproDatasets = gsproDatasets;
        this.inventory = inventory;
        this.detailedResultDataset = detailedResultDataset;
    }

    public void display(EmfDataset dataset, QAStep qaStep) {
        try {
            this.orlPointDST = presenter1.getDatasetType(DatasetType.orlPointInventory);
            this.orlNonpointDST = presenter1.getDatasetType(DatasetType.orlNonpointInventory);
            this.orlNonroadDST = presenter1.getDatasetType(DatasetType.orlNonroadInventory);
            this.orlOnroadDST = presenter1.getDatasetType(DatasetType.orlOnroadInventory);
            this.gsrefDST = presenter1.getDatasetType(DatasetType.chemicalSpeciationCrossReferenceGSREF);
            this.gsproDST = presenter1.getDatasetType(DatasetType.chemicalSpeciationProfilesGSPRO);
            this.detailedResultDST = presenter1.getDatasetType(DatasetType.strategyDetailedResult);
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        super.setTitle("Setup "+qaStep.getName()+": " + dataset.getName() + "_" + qaStep.getId() );
        super.display();
        this.getContentPane().add(createLayout(dataset));
    }

    public void observe(EditQAECControlScenarioPresenter presenter1) {
        this.presenter1 = presenter1;
    }
    
    private JPanel createLayout(EmfDataset dataset) {
        
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        JPanel content = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        
        // TODO: JIZHEN BUG3548
       
        layoutGenerator.addLabelWidgetPair("ORL Inventory:", inventoryPanel(dataset), content);
        layoutGenerator.addLabelWidgetPair("<html>" + DatasetType.strategyDetailedResult.replace("Detailed Result", "Detailed<br/>Result") + " Dataset:</html>",  detailedResultDatasetPanel(), content);
        layoutGenerator.addLabelWidgetPair("<html>" + DatasetType.chemicalSpeciationCrossReferenceGSREF.replace("Speciation Cross", "Speiation<br/>Cross").replace("GSREF)", "GSREF)<br/>") + " Datasets:</html>",  gsrefDatasetsPanel(), content);
        layoutGenerator.addLabelWidgetPair("<html>" + DatasetType.chemicalSpeciationProfilesGSPRO.replace("Speciation Profiles", "Speciation<br/>Profiles") + " Datasets:</html>",  gsproDatasetsPanel(), content);
        
        layoutGenerator.makeCompactGrid(content, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad*/
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        layout.add(content);
        layout.add(buttonPanel());
        
        return layout;
    }
    
    private JPanel inventoryPanel(EmfDataset dataset) {
      
        inventoryListWidget = new ListWidget(new EmfDataset[0]);
        inventoryListWidget.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        if(!(inventory == null))
            setDatasetsFromStepWindow(inventoryListWidget, new EmfDataset[] {inventory});
        
        JScrollPane pane = new JScrollPane(inventoryListWidget);
        pane.setPreferredSize(new Dimension(450, 25));
        inventoryListWidget.setToolTipText("The inventory datasets.  Press select button to choose from a list.");
       
        Button addButton = new AddButton("Select", addInventoryAction() );
        addButton.setMargin(new Insets(1, 2, 1, 2));

        JPanel container = new JPanel(new FlowLayout());

        container.add(pane);
        container.add(addButton);
        
        return container;
    }
    
    private JPanel detailedResultDatasetPanel() {

        detailedResultDatasetListWidget = new ListWidget(new EmfDataset[0]);
        detailedResultDatasetListWidget.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        if(detailedResultDataset != null)
            setDatasetsFromStepWindow(detailedResultDatasetListWidget, new EmfDataset[] {detailedResultDataset});
        JScrollPane pane = new JScrollPane(detailedResultDatasetListWidget);
        pane.setPreferredSize(new Dimension(450, 25));
        detailedResultDatasetListWidget.setToolTipText("The " + DatasetType.strategyDetailedResult + " dataset.  Press add button to choose from a list.");
        
        Button addButton = new AddButton("Select", addDetailedResultAction());
        addButton.setMargin(new Insets(1, 2, 1, 2));
        
        JPanel container = new JPanel(new FlowLayout());

        container.add(pane);
        container.add(addButton);
        
        return container;
    }
   
    private JPanel gsproDatasetsPanel() {
        
        gsproDatasetListWidget = new ListWidget(new EmfDataset[0]);
        gsproDatasetListWidget.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        if(!(gsproDatasets==null))
            setDatasetsFromStepWindow(gsproDatasetListWidget, gsproDatasets);
        
        JScrollPane pane = new JScrollPane(gsproDatasetListWidget);
        pane.setPreferredSize(new Dimension(450, 75));
        gsproDatasetListWidget.setToolTipText("The " + DatasetType.chemicalSpeciationProfilesGSPRO + " datasets.  Press select button to choose from a list.");
        
        Button addButton = new AddButton("Select", addGSPRODatasetsAction());
        addButton.setMargin(new Insets(1, 2, 1, 2));
        
        JPanel container = new JPanel(new FlowLayout());

        container.add(pane);
        container.add(addButton);
        
        return container;
    }
    
    private JPanel gsrefDatasetsPanel() {
        
        gsrefDatasetListWidget = new ListWidget(new EmfDataset[0]);
        gsrefDatasetListWidget.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        if(!(gsrefDatasets==null))
            setDatasetsFromStepWindow(gsrefDatasetListWidget, gsrefDatasets);
        
        JScrollPane pane = new JScrollPane(gsrefDatasetListWidget);
        pane.setPreferredSize(new Dimension(450, 75));
        gsrefDatasetListWidget.setToolTipText("The " + DatasetType.chemicalSpeciationCrossReferenceGSREF + " datasets.  Press select button to choose from a list.");
        
        Button addButton = new AddButton("Select", addGSREFDatasetsAction());
        addButton.setMargin(new Insets(1, 2, 1, 2));
        
        JPanel container = new JPanel(new FlowLayout());

        container.add(pane);
        container.add(addButton);
        
        return container;
    }
    
    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new OKButton(okAction()));
        panel.add(new CancelButton(cancelAction()));
        return panel;
    }
    
    private Action addInventoryAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddWindow(inventoryListWidget, new DatasetType[] {orlNonpointDST, orlNonroadDST, orlOnroadDST, orlPointDST}, orlPointDST, false);
            }
        };
    }
  
    private Action addGSPRODatasetsAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddWindow(gsproDatasetListWidget, new DatasetType[] {gsproDST}, gsproDST, false);
            }
        };
    }
    
    private Action addGSREFDatasetsAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddWindow(gsrefDatasetListWidget, new DatasetType[] {gsrefDST}, gsrefDST, false);
            }
        };
    }
    
    private Action addDetailedResultAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddWindow(detailedResultDatasetListWidget, new DatasetType[] {detailedResultDST}, detailedResultDST, false);
            }
        };
    }
    
    protected Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                disposeView();
            }

        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //validate dataset selections
                Object inventory = getInventoryDataset();
                Object detailedResult = getDetailedResultDataset();
                Object[] gsproDatasets = getGSPRODatasets();
                Object[] gsrefDatasets = getGSREFDatasets();
                String errors = "";

                if (gsproDatasets == null || gsproDatasets.length == 0) {
                    errors = "Missing " + DatasetType.chemicalSpeciationProfilesGSPRO + " datasets. ";
                }
                if (gsrefDatasets == null || gsrefDatasets.length == 0) {
                    errors += "Missing " + DatasetType.chemicalSpeciationCrossReferenceGSREF + " Datasets. ";
                }
                if (inventory == null ) {
                    errors += "Missing Inventory Dataset. ";
                }
                if (detailedResult == null ) {
                    errors += "Missing " + DatasetType.strategyDetailedResult + " Dataset. ";
                }
                
                if (errors.length() > 0) {
                    messagePanel.setError(errors);
                    return;
                }
                presenter1.updateECControlScenarioArguments(inventory, detailedResult, gsrefDatasets, gsproDatasets);
                dispose();
                disposeView();
            }
        };
    }
    
    protected void doAddWindow(ListWidget listWidget, DatasetType[] datasetTypes, DatasetType defaultDatasetType, boolean selectSingle) {
        try {
            InputDatasetSelectionDialog view = new InputDatasetSelectionDialog (parentConsole);
            InputDatasetSelectionPresenter presenter = new InputDatasetSelectionPresenter(view, session, datasetTypes);
            presenter.display(defaultDatasetType, selectSingle);
            if (view.shouldCreate())
                setDatasets(listWidget, presenter.getDatasets());
        } catch (Exception e) {
             messagePanel.setError(e.getMessage());
        }
    }
    
    protected void setDatasetsFromStepWindow(ListWidget listWidget, EmfDataset[] datasets){
        listWidget.removeAll();
        for (int i = 0; i < datasets.length; i++) {
            listWidget.addElement(datasets[i]);
        }
    }
    
    protected void setDatasets(ListWidget listWidget, EmfDataset [] datasets) {
        listWidget.removeAllElements();
        for (int i = 0; i < datasets.length; i++) {
            if (!listWidget.contains(datasets[i])) 
           //System.out.println(" Inv dataset is: " + datasets[i]);
                listWidget.addElement(datasets[i]);
        }
        
    }
    
    protected Object[] getGSPRODatasets() {
        return (gsproDatasetListWidget.getAllElements() != null && gsproDatasetListWidget.getAllElements().length > 0) ? gsproDatasetListWidget.getAllElements() : null;
    }
    
    protected Object getInventoryDataset() {
        return (inventoryListWidget.getAllElements() != null && inventoryListWidget.getAllElements().length > 0) ? inventoryListWidget.getAllElements()[0] : null;
    }
    
    protected Object getDetailedResultDataset() {
        return (detailedResultDatasetListWidget.getAllElements() != null && detailedResultDatasetListWidget.getAllElements().length > 0) ? detailedResultDatasetListWidget.getAllElements()[0] : null;
    }
    
    protected Object[] getGSREFDatasets() {
        return (gsrefDatasetListWidget.getAllElements() != null && gsrefDatasetListWidget.getAllElements().length > 0) ? gsrefDatasetListWidget.getAllElements() : null;
    }
}
