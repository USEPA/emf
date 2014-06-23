package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class EditQAEmissionsWindow extends DisposableInteralFrame implements EditQAEmissionsView {
    
    protected AddRemoveDatasetWidget datasetWidget;
    
    protected EmfConsole parentConsole;
    
    protected JPanel layout;
    
    protected EditQAEmissionsPresenter presenter1;
    
    protected ListWidget invTable;
    
    protected EmfSession session;
    
    protected SingleLineMessagePanel messagePanel;
    
    protected EmfDataset[] inventories;
    
    protected EmfDataset[] invTables;
    
    protected ComboBox summaryTypes;
    
    protected String summaryType; 
    
    protected String program;
    
    
        
    public EditQAEmissionsWindow(DesktopManager desktopManager, String program, EmfSession session, EmfDataset[] inventories, EmfDataset [] invTables, String summaryType) {
        
        super("Emissions Inventories Editor", new Dimension(600, 300), desktopManager);
        this.program=program; 
        this.session = session;
        this.inventories = inventories;
        this.invTables = invTables;
        this.summaryType =summaryType;
    }


    public void display(EmfDataset dataset, QAStep qaStep) {
        super.setTitle("Setup "+qaStep.getName()+": " + dataset.getName() + "_" + qaStep.getId() );
        super.display();
        this.getContentPane().add(createLayout(dataset));
    }

    public void observe(EditQAEmissionsPresenter presenter1) {
        this.presenter1 = presenter1;
    }
    
    // A JList with Add and Remove buttons for the Emission .
    // A Text Field for Adding the Inventory Table with a Select button
    // OK and Cancel buttons.

    private JPanel createLayout(EmfDataset dataset) {
        
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add( createLabelInPanel("Emission inventories: ", 130,30));
        panel.add( emisinv(dataset));
        //panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        layout.add( panel);
        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add( createLabelInPanel("Inventory table:      ", 130,30));
        panel.add( invTablePanel());
        //panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setMaximumSize( new Dimension(1500, 30));
        layout.add( panel);
        
        summaryTypeCombo();
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add( createLabelInPanel("Summary Type:         ", 130,30));
        panel.add( summaryTypes);
        //panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        layout.add( panel);        
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
        
        
//        System.out.println( label.getWidth());
//        System.out.println( panel.getWidth());
//        panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        
        return panel;
    }
    
    protected JPanel emisinv(EmfDataset dataset) {
        datasetWidget = new AddRemoveDatasetWidget(this, program, parentConsole, session);
        datasetWidget.setPreferredSize(new Dimension(350,250));
        if(inventories != null && inventories.length > 0)
            datasetWidget.setDatasetsFromStepWindow(inventories);
        else 
            datasetWidget.setDatasetsFromStepWindow(new EmfDataset[] {dataset});
        return datasetWidget;
    }
    
    protected JPanel invTablePanel() {
        
        invTable = new ListWidget(new EmfDataset[0]);
        if(!(invTables==null) && (invTables.length > 0))
            setDatasetsFromStepWindow(invTables);
        
        JScrollPane pane = new JScrollPane(invTable);
        pane.setPreferredSize(new Dimension(800, 25));
        invTable.setToolTipText("The inventory table dataset.  Press select button to choose from a list.");
        pane.setMaximumSize(new Dimension(1500, 25));
        
        Button addButton = new AddButton("Select", addAction());
        addButton.setPreferredSize(new Dimension(75, 25));
        addButton.setMaximumSize(new Dimension(75, 25));
        addButton.setMargin(new Insets(1, 2, 1, 2));
        
        
        JPanel invPanel = new JPanel();
        invPanel.setMaximumSize( new Dimension(1500,30));
        invPanel.setLayout( new BoxLayout(invPanel, BoxLayout.X_AXIS));
        
        invPanel.add(pane);
        invPanel.add(addButton);
        
        return invPanel;
    }
    
    protected void summaryTypeCombo() {
        String [] values= new String[]{"State", "State+SCC", "County", "County+SCC", "SCC"};
        summaryTypes = new ComboBox("Not Selected", values);
        summaryTypes.setPreferredSize(new Dimension(350, 25));
        if(!(summaryType==null) && (summaryType.trim().length()>0))
            summaryTypes.setSelectedItem(summaryType);
        
        summaryTypes.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                summaryTypes.getSelectedItem();
            }
        });
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new OKButton(okAction()));
        panel.add(new CancelButton(cancelAction()));
        return panel;
    }
    
    protected Action addAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddWindow();
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
                if (!validateValues()){
                    messagePanel.setError("Please select inventories or summary type");
                    return; 
                }
                presenter1.updateInventories(datasetWidget.getDatasets(), getInvTableDatasets(), getSummaryType() );
                dispose();
                disposeView();
            }
        };
    }
    
    private boolean validateValues(){
        if (datasetWidget.getDatasets().length ==0 
                || getSummaryType().trim().equals(""))
            return false; 
        return true; 
    }
    
    protected void doAddWindow() {
        List<DatasetType> datasetTypeList = new ArrayList<DatasetType>();
        try {
            DatasetType[] allDatasetTypes = session.getLightDatasetTypes();
            for (int i = 0; i < allDatasetTypes.length; i++) {
             // Only get the dataset type INVTABLE
                if (allDatasetTypes[i].getName().equals("Inventory Table Data (INVTABLE)"))
                    datasetTypeList.add(allDatasetTypes[i]);
            }
            DatasetType[] datasetTypes = datasetTypeList.toArray(new DatasetType[0]);
            InputDatasetSelectionDialog view = new InputDatasetSelectionDialog (parentConsole);
            InputDatasetSelectionPresenter presenter = new InputDatasetSelectionPresenter(view, session, datasetTypeList.toArray(new DatasetType[0]));
            if (datasetTypes.length == 1)
                presenter.display(datasetTypes[0], true);
            else
                presenter.display(null, true);
            if (view.shouldCreate())
                setDatasets(presenter.getDatasets());
        } catch (Exception e) {
             messagePanel.setError(e.getMessage());
        }
    }
    
    protected void setDatasetsFromStepWindow(EmfDataset [] datasets){
        invTable.removeAll();
        for (int i = 0; i < datasets.length; i++) {
            //System.out.println(" Inv dataset is: " + datasets[i]);
            invTable.addElement(datasets[i]);
        }
    }
    
    protected void setDatasets(EmfDataset [] datasets) {
        invTable.removeAllElements();
        for (int i = 0; i < datasets.length; i++) {
           //System.out.println(" Inv dataset is: " + datasets[i]);
           invTable.addElement(datasets[i]);
        }
        
    }
    
    protected Object[] getInvTableDatasets() {
        return invTable.getAllElements();
   }
   
    protected String getSummaryType(){
       if (summaryTypes.getSelectedItem()==null)
           return ""; 
       return summaryTypes.getSelectedItem().toString();
   }
   
}
