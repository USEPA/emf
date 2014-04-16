package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.border.EtchedBorder;

public class EditControlDetailedDiffWindow extends DisposableInteralFrame implements EditQAEmissionsView {
    
    private AddRemoveDatasetWidget datasetWidgetBase;
    private AddRemoveDatasetWidget datasetWidgetCompare;
    
    private EmfConsole parentConsole;
    
    private JPanel layout;
    
    private EditQAEmissionsPresenter presenter;
    
    private EmfSession session;
    
    private SingleLineMessagePanel messagePanel;
    
    private EmfDataset[] resultBase;
    
    private EmfDataset[] resultCompare;
    
    private ComboBox summaryTypes;
    
    private String summaryType; 
    
    private String program;
        
    public EditControlDetailedDiffWindow(DesktopManager desktopManager, String program, 
            EmfSession session, EmfDataset[] invBase, EmfDataset[] invCompare, 
            String summaryType) {
        
        super("Contorled Strategy Detailed Editor", new Dimension(600, 400), desktopManager);
        this.program=program; 
        this.session = session;
        this.resultBase = invBase;
        this.resultCompare = invCompare;
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
        layout.add(messagePanel);
        
        boolean boxlayout = true;
        
        if ( boxlayout) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add( createLabelInPanel("Base Result:", 110,30));
            panel.add( emisinvBase(dataset));
            //panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            layout.add( panel);

            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add( createLabelInPanel("Compare Result:", 110,30));
            panel.add( emisinvCompare());
            //panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            //panel.setMaximumSize( new Dimension(1500, 1000));
            layout.add( panel);
            
            summaryTypeCombo();
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add( createLabelInPanel("Summary Type:", 110,30));
            panel.add( summaryTypes);
            //panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            //panel.setMaximumSize( new Dimension(1500, 1000));
            layout.add( panel);            
        } else {
            JPanel content = new JPanel(new SpringLayout());
            SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
           
            layoutGenerator.addLabelWidgetPair("Base Result:", emisinvBase(dataset), content);
            layoutGenerator.addLabelWidgetPair("Compare Result:", emisinvCompare(), content);
            summaryTypeCombo();
            layoutGenerator.addLabelWidgetPair("Summary Type:", summaryTypes, content);
            layoutGenerator.makeCompactGrid(content, 3, 2, // rows, cols
                    5, 5, // initialX, initialY
                    10, 10);// xPad, yPad*/
            layout.add(content);            
        }
        
        layout.add(buttonPanel());
        
        return layout;
    }
    
    private JPanel emisinvBase(EmfDataset dataset) {
        datasetWidgetBase = new AddRemoveDatasetWidget(this, program, parentConsole, session);
        datasetWidgetBase.setPreferredSize(new Dimension(420,250));
        if(resultBase != null && resultBase.length > 0)
            datasetWidgetBase.setDatasetsFromStepWindow(resultBase);
        else 
            datasetWidgetBase.setDatasetsFromStepWindow(new EmfDataset[] {dataset});
        return datasetWidgetBase;
    }
    
    private JPanel emisinvCompare() {
        datasetWidgetCompare = new AddRemoveDatasetWidget(this, program, parentConsole, session);
        datasetWidgetCompare.setPreferredSize(new Dimension(420,250));
        if(resultCompare != null && resultCompare.length > 0)
            datasetWidgetCompare.setDatasetsFromStepWindow(resultCompare);
        return datasetWidgetCompare;
    }
    
    private void summaryTypeCombo() {
        String [] values = null; 
        values= new String[]{"Plant", "State", "County", "SCC"};
        summaryTypes = new ComboBox("Not Selected", values);
        summaryTypes.setPreferredSize(new Dimension(420, 25));
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
                    messagePanel.setError("Please specify one base result, one result for comparison, and the summary type");
                    return; 
                }
                presenter.updateInventories(datasetWidgetBase.getDatasets(),datasetWidgetCompare.getDatasets(),new Object[]{}, getSummaryType() );
                dispose();
                disposeView();
            }
        };
    }
    
    private boolean validateValues(){
        if (datasetWidgetBase.getDatasets().length !=1 
                || datasetWidgetCompare.getDatasets().length !=1 
                || getSummaryType().trim().equals(""))
            return false; 
        return true; 
    }
    
//    private void doAddWindow() {
//        List<DatasetType> datasetTypeList = new ArrayList<DatasetType>();
//        try {
//            DatasetType[] allDatasetTypes = session.dataCommonsService().getDatasetTypes();
//            for (int i = 0; i < allDatasetTypes.length; i++) {
//             // Only get the dataset type INVTABLE
//                if (allDatasetTypes[i].getName().equals("Inventory Table Data (INVTABLE)"))
//                    datasetTypeList.add(allDatasetTypes[i]);
//            }
//            DatasetType[] datasetTypes = datasetTypeList.toArray(new DatasetType[0]);
//            InputDatasetSelectionDialog view = new InputDatasetSelectionDialog (parentConsole, this);
//            InputDatasetSelectionPresenter presenter = new InputDatasetSelectionPresenter(view, session, datasetTypes);
//            if (datasetTypes.length == 1)
//                presenter.display(datasetTypes[0], true);
//            else
//                presenter.display(null, true);
//            if (view.shouldCreate())
//                setDatasets(presenter.getDatasets());
//        } catch (Exception e) {
//             messagePanel.setError(e.getMessage());
//        }
//    }
    
//    private void setDatasetsFromStepWindow(EmfDataset [] datasets){
//        invTable.removeAll();
//        for (int i = 0; i < datasets.length; i++) {
//            //System.out.println(" Inv dataset is: " + datasets[i]);
//            invTable.addElement(datasets[i]);
//        }
//    }
//    
//    private void setDatasets(EmfDataset [] datasets) {
//        invTable.removeAllElements();
//        for (int i = 0; i < datasets.length; i++) {
//           //System.out.println(" Inv dataset is: " + datasets[i]);
//           invTable.addElement(datasets[i]);
//        }
//        
//    }
//    
//   private Object[] getInvTableDatasets() {
//        return invTable.getAllElements();
//   }
   
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
