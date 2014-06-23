package gov.epa.emissions.framework.client.casemanagement.sensitivity;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class SetCaseParameterPanel extends JPanel{

    private TextField envValue;
    
    private TextArea purpose;
    
    private CaseParameter param; 
    
    private ManageChangeables changeablesList;
    
    //private Dimension preferredSize = new Dimension(380, 20);
    
    public SetCaseParameterPanel(MessagePanel messagePanel, ManageChangeables changeablesList,
            EmfConsole parentConsole) {
        this.changeablesList = changeablesList;
    }

    public void display(CaseParameter param, JComponent container, EmfSession session, String jobName){
        this.param =param;
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createFolders(jobName),BorderLayout.NORTH);
        container.add(panel);
    }
    
    private JPanel createFolders(String jobName){
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        Dimension preferredSize = new Dimension(380, 25);
        
        JLabel parameterName = new JLabel(param.getParameterName().toString());
        layoutGenerator.addLabelWidgetPair("Parameter Name:", parameterName, panel);

        JLabel envtVar = new JLabel(param.getEnvVar()==null? "":param.getEnvVar().toString());
        layoutGenerator.addLabelWidgetPair("Envt. Variable:", envtVar, panel);
        
        JLabel sector = new JLabel(param.getSector()==null? "All jobs for sector" :param.getSector().toString());
        layoutGenerator.addLabelWidgetPair("Sector:", sector, panel);
        
        JLabel job = new JLabel(jobName);
        layoutGenerator.addLabelWidgetPair("Job:", job, panel);
        
        JLabel varTypes = new JLabel(param.getType()==null? "":param.getType().toString());
        layoutGenerator.addLabelWidgetPair("Type:", varTypes, panel);
        
        envValue = new TextField("value", param.getValue(), 34);
        envValue.setPreferredSize(preferredSize);
        changeablesList.addChangeable(envValue);
        layoutGenerator.addLabelWidgetPair("Value:", envValue, panel);
        
        purpose = new TextArea("Information", param.getPurpose(), 34, 3);
        purpose.setEditable(false);
        ScrollableComponent scrolpane = new ScrollableComponent(purpose);
        scrolpane.setPreferredSize(new Dimension(380, 100));
        layoutGenerator.addLabelWidgetPair("Information:", scrolpane, panel);

        JLabel required = new JLabel(param.isRequired()? "True" : "False" );
        layoutGenerator.addLabelWidgetPair("Required?", required, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 8, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }
    
    public void setFields() {
        param.setValue(envValue.getText() == null ? "" : envValue.getText().trim());
    }
    
}
