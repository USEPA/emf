package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class SetjobsStatusDialog extends JDialog{

    private EditJobsTabPresenter presenter;

    private ComboBox status; 
    
    private CaseJob[] jobs; 
    
    private JobsTabView view; 
    
    public SetjobsStatusDialog(EmfConsole parentConsole, JobsTabView view, CaseJob[] jobs, EditJobsTabPresenter presenter) {
        super(parentConsole);
        this.presenter = presenter; 
        this.jobs =jobs; 
        this.view=view; 
    }


    public void run(){
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 10));
  
        try {
            contentPane.add(createSection(), BorderLayout.CENTER);
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        setTitle("Set Status ");
        this.pack();
        this.setSize(450,140);
        this.setLocation(ScreenUtils.getPointToCenter(this));
        this.setVisible(true);
    }
    
    

    private JPanel createSection() throws EmfException {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createPropertySection(), BorderLayout.CENTER);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        return panel;  
    }
    
    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new OKButton(okAction()));
        panel.add(new CancelButton(cancelAction()));
        return panel;
    }
    
    private JPanel createPropertySection() throws EmfException{
        JPanel panel = new JPanel(new SpringLayout());

        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        status =new ComboBox(presenter.getRunStatuses());           
        status.setPreferredSize(new Dimension(300,15));

        layoutGenerator.addLabelWidgetPair("Status:", status, panel);
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                55, 15, // initialX, initialY
                5, 15);// xPad, yPad
        return panel;
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e){  
                setField();
                try {
                    presenter.doSave(jobs);
                    view.refresh();
                } catch (EmfException e1) {
                    // NOTE Auto-generated catch block
                    e1.printStackTrace();
                }
                dispose();
            }
        };
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e){            
                dispose();
            }
        };
    }
    
    private void setField(){
        for (CaseJob job : jobs) {
            job.setRunstatus((JobRunStatus) (status.getSelectedItem()));
        }
    }
}
