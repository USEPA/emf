package gov.epa.emissions.framework.client.casemanagement.history;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.swingworker.RefreshSwingWorkerTasks;
import gov.epa.emissions.framework.client.swingworker.SwingWorkerTasks;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;

import javax.swing.JPanel;

public class ViewableHistoryTab extends ShowHistoryTab {
  
 
    public ViewableHistoryTab(EmfConsole parentConsole, MessagePanel messagePanel, EmfSession session) {
        super(parentConsole, messagePanel, session);
        super.setName("viewCaseHistoryTab");
        super.setLayout(new BorderLayout());
    }
    
    public void doDisplay(ShowHistoryTabPresenter presenter, int caseId){
        super.setLayout(new BorderLayout());
        super.setName("showCaseHistoryTab");
        this.presenter = presenter;
        //this.caseId = caseId;
        new SwingWorkerTasks(this, presenter).execute();
    }

    public void display(CaseJob[] jobs) {
        super.setLayout(new BorderLayout());
        super.removeAll();
        getAllJobs(jobs);

        super.add(createLayout(new JobMessage[0], parentConsole), BorderLayout.CENTER);
        super.validate();
    }


    private JPanel createLayout(JobMessage[] msgs, EmfConsole parentConsole){
        layout = new JPanel(new BorderLayout());
        layout.add(createTopPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(msgs, parentConsole), BorderLayout.CENTER);
        return layout;
    }


    public void doRefresh() throws EmfException {
        // note that this will get called when the case is save
        try {
            refreshJobList(presenter.getCaseJobs());
            new RefreshSwingWorkerTasks(layout, messagePanel, presenter).execute();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

}
