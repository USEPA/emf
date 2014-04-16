package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
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
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
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
import javax.swing.SpringLayout;
import javax.swing.border.EtchedBorder;

public class EditMultiInvDiffWindow extends DisposableInteralFrame implements EditQAEmissionsView {
    
    private AddRemoveDatasetWidget datasetWidgetBase;
    private AddRemoveDatasetWidget datasetWidgetCompare;
    
    private EmfConsole parentConsole;
    
    private JPanel layout;
    
    private EditQAEmissionsPresenter presenter;
    
    private ListWidget invTable;
    
    private EmfSession session;
    
    private SingleLineMessagePanel messagePanel;
    
    private EmfDataset[] invBase;
    
    private EmfDataset[] invCompare;
    
    private EmfDataset[] invTables;
    
    private ComboBox summaryTypes;
    
    private String summaryType; 
    
    private String program;
        
    public EditMultiInvDiffWindow(DesktopManager desktopManager, String program, 
            EmfSession session, EmfDataset[] invBase, EmfDataset[] invCompare, 
            EmfDataset [] invTables, String summaryType) {
        
        super("Emissions Inventories Editor", new Dimension(600, 400), desktopManager);
        this.program=program; 
        this.session = session;
        this.invBase = invBase;
        this.invCompare = invCompare;
        this.invTables = invTables;
        this.summaryType =summaryType;
    }


    public void display(EmfDataset dataset, QAStep qaStep) {
        super.setTitle("Setup "+qaStep.getName()+": " + dataset.getName() + "_" + qaStep.getId() );
        super.display();
        this.getContentPane().add(createLayout(dataset));
    }

    public void observe(EditQAEmissionsPresenter presenter) {
        this.presenter = presenter;
    }
    
    // A JList with Add and Remove buttons for the Emission Inventories.
    // A Text Field for Adding the Inventory Table with a Select button
    // OK and Cancel buttons.

    private JPanel createLayout(EmfDataset dataset) {
        
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        messagePanel = new SingleLineMessagePanel();
        messagePanel.setMaximumSize( new Dimension(10000,30));
        layout.add(messagePanel);
        
        boolean boxlayout = true;
        if ( boxlayout) {
            int w = 150, h = 30;
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add( createLabelInPanel("Base inventories:", w,h));
            panel.add( emisinvBase(dataset));
            //panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            layout.add( panel);
            
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add( createLabelInPanel("Compare inventories:", w,h));
            panel.add( emisinvCompare());
            //panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            layout.add( panel);
            
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add( createLabelInPanel("Inventory table:", w,h));
            panel.add( invTablePanel());
            //panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            layout.add( panel);
            
            summaryTypeCombo();
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add( createLabelInPanel("Summary Type:", w,h));
            panel.add( summaryTypes);
            //panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            layout.add( panel);
            
        } else {
            JPanel content = new JPanel(new SpringLayout());
            SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

            layoutGenerator.addLabelWidgetPair("Base inventories:", emisinvBase(dataset), content);
            layoutGenerator.addLabelWidgetPair("Compare inventories:", emisinvCompare(), content);
            layoutGenerator.addLabelWidgetPair("Inventory table:", invTablePanel(), content);
            summaryTypeCombo();
            layoutGenerator.addLabelWidgetPair("Summary Type:", summaryTypes, content);
            layoutGenerator.makeCompactGrid(content, 4, 2, // rows, cols
                    5, 5, // initialX, initialY
                    10, 10);// xPad, yPad*/

            layout.add(content);
        }
        layout.add(buttonPanel());
        
        return layout;
    }
    
    private JPanel emisinvBase(EmfDataset dataset) {
        datasetWidgetBase = new AddRemoveDatasetWidget(this, program, parentConsole, session);
        datasetWidgetBase.setPreferredSize(new Dimension(350,250));
        if(invBase != null && invBase.length > 0)
            datasetWidgetBase.setDatasetsFromStepWindow(invBase);
        else 
            datasetWidgetBase.setDatasetsFromStepWindow(new EmfDataset[] {dataset});
        return datasetWidgetBase;
    }
    
    private JPanel emisinvCompare() {
        datasetWidgetCompare = new AddRemoveDatasetWidget(this, program, parentConsole, session);
        datasetWidgetCompare.setPreferredSize(new Dimension(350,250));
        if(invCompare != null && invCompare.length > 0)
            datasetWidgetCompare.setDatasetsFromStepWindow(invCompare);
        return datasetWidgetCompare;
    }
    
    private JPanel invTablePanel() {
        
        invTable = new ListWidget(new EmfDataset[0]);
        if(!(invTables==null) && (invTables.length > 0))
            setDatasetsFromStepWindow(invTables);
        
        JScrollPane pane = new JScrollPane(invTable);
        pane.setPreferredSize(new Dimension(350, 25));
        invTable.setToolTipText("The inventory table dataset.  Press select button to choose from a list.");
       
        Button addButton = new AddButton("Select", addAction());
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
    
    private void summaryTypeCombo() {
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
        panel.setMaximumSize( new Dimension(10000,30));
        return panel;
    }
    
    private Action addAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddWindow();
            }
        };
    }
  

    private Action cancelAction() {
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
                    messagePanel.setError("Please select base or compare inventories, or summary type");
                    return; 
                }
                presenter.updateInventories(datasetWidgetBase.getDatasets(),datasetWidgetCompare.getDatasets(), getInvTableDatasets(), getSummaryType() );
                dispose();
                disposeView();
            }
        };
    }
    
    private boolean validateValues(){
        if (datasetWidgetBase.getDatasets().length ==0 
                || datasetWidgetCompare.getDatasets().length ==0 
                || getSummaryType().trim().equals(""))
            return false; 
        return true; 
    }
    
    private void doAddWindow() {
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
            InputDatasetSelectionPresenter presenter = new InputDatasetSelectionPresenter(view, session, datasetTypes);
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
    
    private void setDatasetsFromStepWindow(EmfDataset [] datasets){
        invTable.removeAll();
        for (int i = 0; i < datasets.length; i++) {
            //System.out.println(" Inv dataset is: " + datasets[i]);
            invTable.addElement(datasets[i]);
        }
    }
    
    private void setDatasets(EmfDataset [] datasets) {
        invTable.removeAllElements();
        for (int i = 0; i < datasets.length; i++) {
           //System.out.println(" Inv dataset is: " + datasets[i]);
           invTable.addElement(datasets[i]);
        }
        
    }
    
   private Object[] getInvTableDatasets() {
        return invTable.getAllElements();
   }
   
   private String getSummaryType(){
       if (summaryTypes.getSelectedItem()==null)
           return ""; 
       return summaryTypes.getSelectedItem().toString();
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
   
}
