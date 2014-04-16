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
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.border.EtchedBorder;

public class QACompareAnnualStateSummariesWindow extends DisposableInteralFrame implements EditQAEmissionsView {
    
    protected AddRemoveDatasetWidget datasetWidget;
    
    protected EmfConsole parentConsole;
    
    protected JPanel layout;
    
    protected EditQAEmissionsPresenter presenter1;
    
    protected ListWidget inventoryListWidget;
    
    protected ListWidget smkRptDatasetsListWidget;

    private ListWidget invTableDatasetListWidget;
    
    private ListWidget toleranceDatasetListWidget;
    
    protected EmfSession session;
    
    protected SingleLineMessagePanel messagePanel;
    
    private EmfDataset[] inventories;
        
    private EmfDataset[] smkRpts;
    
    protected DatasetType orlPointDST;
    
    protected DatasetType orlNonpointDST;
    
    protected DatasetType orlNonroadDST;
    
    protected DatasetType orlOnroadDST;

    private DatasetType smkRptDST;
    
    private DatasetType invTableDST;
    
    private EmfDataset invTableDataset;

    private EmfDataset toleranceDataset;

    private DatasetType stateComparisonToleranceDST;

    private DatasetType countryStateCountyNamesAndDataCOSTCYDST;

    private ListWidget coStCyDatasetListWidget;

    private EmfDataset coStCyDataset;

//    private String program;
    
    public QACompareAnnualStateSummariesWindow(DesktopManager desktopManager, 
            String program, 
            EmfSession session, 
            EmfDataset[] inventories, 
            EmfDataset[] smkRptDatasets,
            EmfDataset invTableDataset,
            EmfDataset toleranceDataset, EmfDataset coStCyDataset) {
        
        super("Compare annual state summaries", new Dimension(800, 450), desktopManager);
//        this.program = program;
        this.session = session;
        this.inventories = inventories;
        this.smkRpts = smkRptDatasets;
        this.invTableDataset = invTableDataset;
        this.toleranceDataset = toleranceDataset;
        this.coStCyDataset = coStCyDataset;
    }

    public void display(EmfDataset dataset, QAStep qaStep) {
        try {
            this.orlPointDST = presenter1.getDatasetType(DatasetType.orlPointInventory);
            this.orlNonpointDST = presenter1.getDatasetType(DatasetType.orlNonpointInventory);
            this.orlNonroadDST = presenter1.getDatasetType(DatasetType.orlNonroadInventory);
            this.orlOnroadDST = presenter1.getDatasetType(DatasetType.orlOnroadInventory);
            this.smkRptDST = presenter1.getDatasetType(DatasetType.smkmergeRptStateAnnualSummary);
            this.invTableDST = presenter1.getDatasetType(DatasetType.invTable);
            this.stateComparisonToleranceDST = presenter1.getDatasetType(DatasetType.stateComparisonTolerance);
            this.countryStateCountyNamesAndDataCOSTCYDST = presenter1.getDatasetType(DatasetType.countryStateCountyNamesAndDataCOSTCY);
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        super.setTitle("Setup "+qaStep.getName()+": " + dataset.getName() + "_" + qaStep.getId() );
        super.display();
        this.getContentPane().add(createLayout(dataset));
        //this.pack();
        //this.setVisible( true);
    }

    public void observe(EditQAEmissionsPresenter presenter1) {
        this.presenter1 = presenter1;
    }
    
    private JPanel createLayout(EmfDataset dataset) {
        
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        messagePanel = new SingleLineMessagePanel();
        messagePanel.setMaximumSize( new Dimension(10000,30));
        layout.add(messagePanel);
        
        boolean boxlayout = true;
        
        if ( boxlayout) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add( createLabelInPanel("ORL Inventories:", 250,30));
            panel.add( inventoryPanel(dataset));
            //panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            layout.add( panel);

            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add( createLabelInPanel("<html>" + DatasetType.smkmergeRptStateAnnualSummary.replaceFirst("annual summary", "annual<br/>summary") + " Datasets:</html>", 250,30));
            panel.add( smokeReportDatasetsPanel());
            //panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            //panel.setMaximumSize( new Dimension(1500, 1000));
            layout.add( panel);

            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add( createLabelInPanel(DatasetType.invTable + " Dataset:", 250,30));
            panel.add( invTableDatasetPanel());
            //panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            //panel.setMaximumSize( new Dimension(1500, 1000));
            layout.add( panel);

            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add( createLabelInPanel(DatasetType.stateComparisonTolerance + " Dataset:", 250,30));
            panel.add( toleranceDatasetPanel());
            //panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            //panel.setMaximumSize( new Dimension(1500, 1000));
            layout.add( panel);

            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add( createLabelInPanel("<html>" + DatasetType.countryStateCountyNamesAndDataCOSTCY.replaceFirst("names and", "names<br/>and") + " Dataset:</html>", 250,30));
            panel.add( coStCyDatasetPanel());
            //panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            //panel.setMaximumSize( new Dimension(1500, 1000));
            layout.add( panel);
        } else {        
            JPanel content = new JPanel(new SpringLayout());
            SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

            layoutGenerator.addLabelWidgetPair("ORL Inventories:", inventoryPanel(dataset), content);
            layoutGenerator.addLabelWidgetPair("<html>" + DatasetType.smkmergeRptStateAnnualSummary.replaceFirst("annual summary", "annual<br/>summary") + " Datasets:</html>",  smokeReportDatasetsPanel(), content);
            layoutGenerator.addLabelWidgetPair(DatasetType.invTable + " Dataset:",  invTableDatasetPanel(), content);
            layoutGenerator.addLabelWidgetPair(DatasetType.stateComparisonTolerance + " Dataset:",  toleranceDatasetPanel(), content);
            layoutGenerator.addLabelWidgetPair("<html>" + DatasetType.countryStateCountyNamesAndDataCOSTCY.replaceFirst("names and", "names<br/>and") + " Dataset:</html>",  coStCyDatasetPanel(), content);

            layoutGenerator.makeCompactGrid(content, 5, 2, // rows, cols
                    5, 5, // initialX, initialY
                    10, 10);// xPad, yPad*/
            layout.add(content);
        }
        
        layout.add(buttonPanel());
        
        return layout;
    }
    
    protected JPanel createLabelInPanel( String lbl, int width, int height) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setSize(width, height);
        panel.setMinimumSize( new Dimension(width, height));
        panel.setMaximumSize( new Dimension(width, height));
        panel.setPreferredSize( new Dimension(width, height));
        
        JLabel label = new JLabel( lbl);
        label.setSize(width, height);
        label.setMinimumSize( new Dimension(width, height));
        label.setMaximumSize( new Dimension(width, height));   
        label.setPreferredSize( new Dimension(width, height));
        panel.add( label);
        
        return panel;
    }    
    
    private JPanel inventoryPanel(EmfDataset dataset) {
//        datasetWidget = new AddRemoveDatasetWidget(this, program, parentConsole, session);
//        datasetWidget.setPreferredSize(new Dimension(350,250));
//        if(!(inventories==null))
//            datasetWidget.setDatasetsFromStepWindow(inventories);
//        return datasetWidget;
        
        inventoryListWidget = new ListWidget(new EmfDataset[0]);
        inventoryListWidget.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        if(!(inventories == null))
            setDatasetsFromStepWindow(inventoryListWidget, inventories);
        
        JScrollPane pane = new JScrollPane(inventoryListWidget);
        pane.setPreferredSize(new Dimension(450, 75));
        inventoryListWidget.setToolTipText("The inventory datasets.  Press select button to choose from a list.");
       
        Button addButton = new AddButton("Add", addInventoryAction() );
        addButton.setMargin(new Insets(1, 2, 1, 2));
        Button removeButton = new AddButton("Remove", removeInventoryAction() );
        removeButton.setMargin(new Insets(1, 2, 1, 2));
        
//        JPanel container = new JPanel(new FlowLayout());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonPanel.setPreferredSize( new Dimension(75,60));
        
        JPanel invPanel = new JPanel();
        //invPanel.setMaximumSize( new Dimension(1500,30));
        invPanel.setLayout( new BoxLayout(invPanel, BoxLayout.X_AXIS));
        
        invPanel.add(pane);
        invPanel.add(buttonPanel);
        
        return invPanel;
    }
    
    private JPanel invTableDatasetPanel() {

        invTableDatasetListWidget = new ListWidget(new EmfDataset[0]);
        invTableDatasetListWidget.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        if(!(invTableDataset==null))
            setDatasetsFromStepWindow(invTableDatasetListWidget, new EmfDataset[] {invTableDataset});
        JScrollPane pane = new JScrollPane(invTableDatasetListWidget);
        pane.setPreferredSize(new Dimension(450, 25));
        invTableDatasetListWidget.setToolTipText("The invtable dataset.  Press add button to choose from a list.");
        
        Button addButton = new AddButton("Select", addInvTableAction());
        addButton.setMargin(new Insets(1, 2, 1, 2));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(addButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonPanel.setPreferredSize( new Dimension(75,30));
        
        //JPanel container = new JPanel(new FlowLayout());
        
        JPanel container = new JPanel();
        //invPanel.setMaximumSize( new Dimension(1500,30));
        container.setLayout( new BoxLayout(container, BoxLayout.X_AXIS));

        container.add(pane);
        container.add(buttonPanel);
        
        return container;
    }
    
    private JPanel coStCyDatasetPanel() {
        
        coStCyDatasetListWidget = new ListWidget(new EmfDataset[0]);
        coStCyDatasetListWidget.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        if(!(coStCyDataset==null))
            setDatasetsFromStepWindow(coStCyDatasetListWidget, new EmfDataset[] {coStCyDataset});
        
        JScrollPane pane = new JScrollPane(coStCyDatasetListWidget);
        pane.setPreferredSize(new Dimension(450, 25));
        coStCyDatasetListWidget.setToolTipText("The " + DatasetType.countryStateCountyNamesAndDataCOSTCY + " dataset.  Press select button to choose from a list.");
        
        Button addButton = new AddButton("Select", addCoStCyDatasetAction());
        addButton.setMargin(new Insets(1, 2, 1, 2));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(addButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonPanel.setPreferredSize( new Dimension(75,30));
        
        //JPanel container = new JPanel(new FlowLayout());
        JPanel container = new JPanel();
        //invPanel.setMaximumSize( new Dimension(1500,30));
        container.setLayout( new BoxLayout(container, BoxLayout.X_AXIS));

        container.add(pane);
        container.add(buttonPanel);
        
        return container;
    }
    
    private JPanel toleranceDatasetPanel() {
        
        toleranceDatasetListWidget = new ListWidget(new EmfDataset[0]);
        toleranceDatasetListWidget.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        if(!(toleranceDataset==null))
            setDatasetsFromStepWindow(toleranceDatasetListWidget, new EmfDataset[] {toleranceDataset});
        
        JScrollPane pane = new JScrollPane(toleranceDatasetListWidget);
        pane.setPreferredSize(new Dimension(450, 25));
        toleranceDatasetListWidget.setToolTipText("The tolerance dataset.  Press select button to choose from a list.");
        
        Button addButton = new AddButton("Select", addToleranceAction());
        addButton.setMargin(new Insets(1, 2, 1, 2));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(addButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonPanel.setPreferredSize( new Dimension(75,30));
        
        //JPanel container = new JPanel(new FlowLayout());
        JPanel container = new JPanel();
        //invPanel.setMaximumSize( new Dimension(1500,30));
        container.setLayout( new BoxLayout(container, BoxLayout.X_AXIS));

        container.add(pane);
        container.add(buttonPanel);
        
        return container;
    }
    
    private JPanel smokeReportDatasetsPanel() {
        
        smkRptDatasetsListWidget = new ListWidget(new EmfDataset[0]);
        smkRptDatasetsListWidget.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        if(!(smkRpts==null))
            setDatasetsFromStepWindow(smkRptDatasetsListWidget, smkRpts);
        
        JScrollPane pane = new JScrollPane(smkRptDatasetsListWidget);
        pane.setPreferredSize(new Dimension(450, 75));
        smkRptDatasetsListWidget.setToolTipText("The " + DatasetType.smkmergeRptStateAnnualSummary + " datasets.  Press select button to choose from a list.");
//        speciationProfileWeightDatasetsListWidget.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        Button addButton = new AddButton("Select", addSmkRptDatasetsAction());
        addButton.setMargin(new Insets(1, 2, 1, 2));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(addButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonPanel.setPreferredSize( new Dimension(75,30));
        
        //JPanel container = new JPanel(new FlowLayout());
        JPanel container = new JPanel();
        //invPanel.setMaximumSize( new Dimension(1500,30));
        container.setLayout( new BoxLayout(container, BoxLayout.X_AXIS));

        container.add(pane);
        container.add(buttonPanel);
        
        return container;
    }
    
    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new OKButton(okAction()));
        panel.add(new CancelButton(cancelAction()));
        panel.setMaximumSize( new Dimension(10000,30));
        return panel;
    }
    
    private Action removeInventoryAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                inventoryListWidget.removeSelectedElements();
            }
        };
    }
  
    private Action addInventoryAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddWindow(inventoryListWidget, new DatasetType[] {orlNonpointDST, orlNonroadDST, orlOnroadDST, orlPointDST}, orlPointDST, false);
            }
        };
    }
  
    private Action addSmkRptDatasetsAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddWindow(smkRptDatasetsListWidget, new DatasetType[] {smkRptDST}, smkRptDST, false);
            }
        };
    }
    
    private Action addInvTableAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddWindow(invTableDatasetListWidget, new DatasetType[] {invTableDST}, invTableDST, false);
            }
        };
    }
    
    private Action addCoStCyDatasetAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddWindow(coStCyDatasetListWidget, new DatasetType[] {countryStateCountyNamesAndDataCOSTCYDST}, countryStateCountyNamesAndDataCOSTCYDST, false);
            }
        };
    }
    
    private Action addToleranceAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddWindow(toleranceDatasetListWidget, new DatasetType[] {stateComparisonToleranceDST}, stateComparisonToleranceDST, false);
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
                Object[] inventories = getInventoryDatasets();
                Object[] smkRpts = getSmkRptDatasets();
                Object invTable = getInvTableDataset();
                Object tolerance = getToleranceDataset();
                Object coStCy = getCoStCyDataset();
                String errors = "";

                if (inventories == null || inventories.length == 0) {
                    errors = "Missing inventory datasets. ";
                }
                if (smkRpts == null || smkRpts.length == 0) {
                    errors += "Missing SMOKE Report Dataset(s). ";
                }
                if (invTable == null ) {
                    errors += "Missing InvTable Dataset. ";
                }
                if (tolerance == null ) {
                    errors += "Missing Tolerance Dataset. ";
                }
                if (coStCy == null ) {
                    errors += "Missing " + DatasetType.countryStateCountyNamesAndDataCOSTCY + " Dataset. ";
                }
                
                if (errors.length() > 0) {
                    messagePanel.setError(errors);
                    return;
                }
                presenter1.updateCompareAnnualStateSummariesDatasets(inventories, 
                        smkRpts,
                        invTable,
                        tolerance,
                        coStCy);
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
//        listWidget.removeAllElements();
        for (int i = 0; i < datasets.length; i++) {
            if (!listWidget.contains(datasets[i])) 
           //System.out.println(" Inv dataset is: " + datasets[i]);
                listWidget.addElement(datasets[i]);
        }
        
    }
    
    protected Object[] getInventoryDatasets() {
        return (inventoryListWidget.getAllElements() != null && inventoryListWidget.getAllElements().length > 0) ? inventoryListWidget.getAllElements() : null;
    }
    
    protected Object getInvTableDataset() {
        return (invTableDatasetListWidget.getAllElements() != null && invTableDatasetListWidget.getAllElements().length > 0) ? invTableDatasetListWidget.getAllElements()[0] : null;
    }
    
    protected Object getCoStCyDataset() {
        return (coStCyDatasetListWidget.getAllElements() != null && coStCyDatasetListWidget.getAllElements().length > 0) ? coStCyDatasetListWidget.getAllElements()[0] : null;
    }
    
    protected Object getToleranceDataset() {
        return (toleranceDatasetListWidget.getAllElements() != null && toleranceDatasetListWidget.getAllElements().length > 0) ? toleranceDatasetListWidget.getAllElements()[0] : null;
    }
    
    protected Object[] getSmkRptDatasets() {
        return (smkRptDatasetsListWidget.getAllElements() != null && smkRptDatasetsListWidget.getAllElements().length > 0) ? smkRptDatasetsListWidget.getAllElements() : null;
    }
}
