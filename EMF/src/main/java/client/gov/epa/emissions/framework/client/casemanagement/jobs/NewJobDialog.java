package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.Changeable;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.ui.Dialog;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class NewJobDialog extends Dialog implements NewJobView, ManageChangeables {

    protected boolean shouldCreate;

    protected EditJobsTabPresenterImpl presenter;

    private MessagePanel messagePanel;
    
    private JobFieldsPanel jobFieldsPanel;
    
    private EmfConsole parent;
    
    private EmfSession session;
    
    private CaseJob newjob;
    
    public NewJobDialog(EmfConsole parent, Case caseObj, EmfSession session) {
        super("Add a Job to " + caseObj.getName(), parent);
        super.setSize(new Dimension(600, 450));
        super.center();
        
        this.parent = parent;
        this.session = session;
        this.newjob = new CaseJob();
        newjob.setUser(session.user());
    }

    public void display() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(inputPanel());
        panel.add(buttonsPanel());

        super.getContentPane().add(panel);
        super.display();
    }

    private JPanel inputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        
        jobFieldsPanel = new JobFieldsPanel(true, messagePanel, this, parent, session);

        try {
            presenter.addJobFields(newjob, panel, jobFieldsPanel);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
        
        return panel;
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button ok = new SaveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    cleareMsg();
                    addNewJob();
                } catch (EmfException exc) {
                    messagePanel.setError(exc.getMessage());
                }
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        Button cancel = new CancelButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                shouldCreate = false;
                close();
            }
        });
        panel.add(cancel);
        
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

        return panel;
    }

    private void addNewJob() throws EmfException {
        try {
            jobFieldsPanel.validateFields();
        } catch (RuntimeException e) {
            messagePanel.setError(e.getMessage());
        }
        
        shouldCreate = true;
        CaseJob job = jobFieldsPanel.setFields();
        presenter.addNewJob(job);
        close();
    }

    public boolean shouldCreate() {
        return shouldCreate;
    }

    public void register(Object presenter) {
        this.presenter = (EditJobsTabPresenterImpl) presenter;
    }
    
    public void addChangeable(Changeable changeable) {
        // NOTE Auto-generated method stub
    }

    private void cleareMsg() {
        this.messagePanel.clear();
    }

    public void resetChanges() {
        // NOTE Auto-generated method stub
        
    }

}
