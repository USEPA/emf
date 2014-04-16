package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
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

public class EditQAEmissionsColumnBasedWindow extends EditQAEmissionsWindow implements EditQAEmissionsView {
    
    private ComboBox emissionTypes;
    
    private String emissionType; 
    
    public EditQAEmissionsColumnBasedWindow(DesktopManager desktopManager, String program, EmfSession session, 
            EmfDataset[] inventories, EmfDataset [] invTables, String summaryType, String emissionType) {
        super(desktopManager, program, session, inventories, invTables, summaryType);
        this.emissionType = emissionType; 
    }


    public void display(EmfDataset dataset, QAStep qaStep) {
        super.setTitle("Setup "+qaStep.getName()+": " + dataset.getName() + "_" + qaStep.getId() );
        super.display();
        this.getContentPane().add(createLayout(dataset));
    }
    
    // A JList with Add and Remove buttons for the Emission Inventories.
    // A Text Field for Adding the Inventory Table with a Select button
    // OK and Cancel buttons.

    private JPanel createLayout(EmfDataset dataset) {
        
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        
//        JPanel panel = new JPanel();
//        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
//        JLabel label = new JLabel("Emission inventories: ");
//        panel.add( label);
//        panel.add( emisinv(dataset));
//        panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
//        layout.add( panel);
//        
//        panel = new JPanel();
//        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
//        label = new JLabel("Inventory table:      ");
//        panel.add( label);
//        panel.add( invTablePanel());
//        panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
//        layout.add( panel);
//        
//        emissionTypeCombo();        
//        panel = new JPanel();
//        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
//        label = new JLabel("Emission Type:        ");
//        panel.add( label);
//        panel.add( emissionTypes);
//        panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
//        layout.add( panel);
//        
//        summaryTypeCombo();
//        panel = new JPanel();
//        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
//        label = new JLabel("Summary Type:         ");
//        panel.add( label);
//        panel.add( summaryTypes);
//        panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
//        layout.add( panel);
        
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
        
        emissionTypeCombo();        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add( createLabelInPanel("Emission Type:        ", 130,30));
        panel.add( emissionTypes);
        //panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        layout.add( panel);        
        
        summaryTypeCombo();
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add( createLabelInPanel("Summary Type:         ", 130,30));
        panel.add( summaryTypes);
        //panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        layout.add( panel);          
        
//        JPanel content = new JPanel(new SpringLayout());
//        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
//       
//        layoutGenerator.addLabelWidgetPair("Emission inventories:", emisinv(dataset), content);
//        layoutGenerator.addLabelWidgetPair("Inventory table:", invTablePanel(), content);
//        emissionTypeCombo();
//        layoutGenerator.addLabelWidgetPair("Emission Type:", emissionTypes, content);
//        
//        summaryTypeCombo();
//        layoutGenerator.addLabelWidgetPair("Summary Type:", summaryTypes, content);
//        layoutGenerator.makeCompactGrid(content, 4, 2, // rows, cols
//                5, 5, // initialX, initialY
//                10, 10);// xPad, yPad*/
//        layout.add(content);
        
        layout.add(buttonPanel());
        
        return layout;
    }
    
    
    private void emissionTypeCombo() {
        String [] values= new String[]{"Annual emissions", "Average day emissions"};
        emissionTypes = new ComboBox("Not Selected", values);
        emissionTypes.setPreferredSize(new Dimension(350, 25));
        if(!(emissionType==null) && (emissionType.trim().length()>0)){
            if (emissionType.trim().startsWith("Average"))
            emissionTypes.setSelectedItem("Average day emissions");
            if (emissionType.trim().startsWith("Annual"))
                emissionTypes.setSelectedItem("Annual emissions");
        }
        emissionTypes.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                emissionTypes.getSelectedItem();
            }
        });
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new OKButton(okAction()));
        panel.add(new CancelButton(cancelAction()));
        return panel;
    }
    

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!validateValues()){
                    messagePanel.setError("Please select inventories, emission type or summary type");
                    return; 
                }
                presenter1.updateInventories(datasetWidget.getDatasets(), getInvTableDatasets(), getSummaryType(), getEmissionType() );
                dispose();
                disposeView();
            }
        };
    }
    
    private boolean validateValues(){
        if (datasetWidget.getDatasets().length ==0 
                || getEmissionType().trim().equals("")
                || getSummaryType().trim().equals(""))
            return false; 
        return true; 
    }
    
   private String getEmissionType(){
       if (emissionTypes.getSelectedItem()==null)
           return ""; 
       return emissionTypes.getSelectedItem().toString();
   }
   
}
