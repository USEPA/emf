package gov.epa.emissions.framework.client.casemanagement.sensitivity;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class JobsChooserDialog extends JDialog {

    private JList jobsList;

    private CaseJob[] selectedJobs;
    
    private SensitivityWindow view;
    
    private MessagePanel messagePanel; 

    JobsChooserDialog(SensitivityWindow view, EmfConsole console) {
        super(console);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        this.view = view; 
        setLocation(ScreenUtils.getPointToCenter(console));
        setModal(true);
    }
    
    public void display(CaseJob[] jobs){
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));
        messagePanel = new SingleLineMessagePanel();
        contentPane.add(messagePanel, BorderLayout.NORTH);
        contentPane.add(buildjobsPanel(jobs));
        contentPane.add(buttonPanel(), BorderLayout.SOUTH);
        
        setTitle("Select jobs for ("+view.getName()+")");
        this.pack();
        this.setSize(500, 400);
        this.setLocation(ScreenUtils.getPointToCenter(this));
        this.setVisible(true);
    }
    
//
//    public CaseJob getSelectedJobs() {
//        return selectedJobs;
//    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(15);
        layout.setVgap(10);
        container.setLayout(layout);

        Button wizardButton = new Button("Wizard", setAction());
        container.add(wizardButton);
        Button editButton = new OKButton("Edit Case", editAction());
        container.add(editButton);
        container.add(new CancelButton(cancelAction()));
        getRootPane().setDefaultButton(wizardButton);

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    private Action editAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setSelectedJobs();
                if (selectedJobs.length == 0){
                    messagePanel.setMessage("Please select at least one job. ");
                    return; 
                }
                JobsChooserDialog.this.setVisible(false);
                JobsChooserDialog.this.dispose();
                view.editAction(selectedJobs);
            }
        };
    }
    
    private Action setAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setSelectedJobs();
                if (selectedJobs.length == 0){
                    messagePanel.setMessage("Please select at least one job. ");
                    return; 
                }
                JobsChooserDialog.this.setVisible(false);
                JobsChooserDialog.this.dispose();
                view.setAction(selectedJobs);
            }
        };
    }

    private JPanel buildjobsPanel(CaseJob[] jobs) {
      jobsList = new JList();
      jobsList.setListData(jobs);
      jobsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      JScrollPane scrollPane = new JScrollPane(jobsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
              JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      scrollPane.setPreferredSize(new Dimension(300, 100));
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(scrollPane);
        return panel;
    }
    
    private void setSelectedJobs(){
        List<CaseJob> list = new ArrayList<CaseJob>(jobsList.getSelectedValues().length);
        for (int i = 0; i < jobsList.getSelectedValues().length; i++)
            list.add((CaseJob) jobsList.getSelectedValues()[i]);
        selectedJobs = list.toArray(new CaseJob[0]);
    }
    
    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        };
    }

}
