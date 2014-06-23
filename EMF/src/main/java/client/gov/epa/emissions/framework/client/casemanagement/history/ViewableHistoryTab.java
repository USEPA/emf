package gov.epa.emissions.framework.client.casemanagement.history;

import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ViewableHistoryTab extends JPanel implements RefreshObserver {
    private EmfConsole parentConsole;

    private ViewableHistoryTabPresenter presenter;

    private int caseId;

    private JobMessagesTableData tableData;

    private SelectableSortFilterWrapper table;

    private JPanel tablePanel;

    private MessagePanel messagePanel;

    private EmfSession session;

    private ComboBox jobCombo;

    private List<CaseJob> caseJobs; 

    private CaseJob selectedJob=null;

    public ViewableHistoryTab(EmfConsole parentConsole, MessagePanel messagePanel, EmfSession session) {
        super.setName("viewCaseHistoryTab");
        this.parentConsole = parentConsole;
        this.messagePanel = messagePanel;
        this.session = session;
        super.setLayout(new BorderLayout());
    }

    public void display(EmfSession session, int caseId, ViewableHistoryTabPresenter presenter) {
        super.removeAll();

        this.caseId = caseId;
        this.presenter = presenter;
        try {
            getAllJobs();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
        try {
            super.add(createLayout(new JobMessage[0], parentConsole), BorderLayout.CENTER);
        } catch (Exception e) {
            messagePanel.setError("Cannot retrieve all case histories. " + e.getMessage());
        }

  //      kickPopulateThread(new JobMessage[0]);
    }

    private void kickPopulateThread() {
        Thread populateThread = new Thread(new Runnable() {
            public void run() {
                retrieveJobMsgs();
            }
        });
        populateThread.start();
    }
    
    private synchronized void refreshJobList() throws EmfException {
        getAllJobs();
        jobCombo.resetModel(caseJobs.toArray(new CaseJob[0]));
        jobCombo.setSelectedItem(getCaseJob(caseJobs, this.selectedJob));
    }
    
    private CaseJob getCaseJob(List<CaseJob> allJobs, CaseJob selectedJob) {
        if (selectedJob == null)
            return null;
        
        for (Iterator<CaseJob> iter = allJobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();
            
            if (selectedJob.getId() == job.getId())
                return job;
        }

        return null;
    }


    public synchronized void retrieveJobMsgs() {
        try {
            messagePanel.setMessage("Please wait while retrieving all case histories...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            refreshJobList();
            
            if (selectedJob == null || selectedJob.getName().equalsIgnoreCase("Select one")){
                doRefresh(new JobMessage[0]);
                return; 
            }
            
            JobMessage[] msgs=presenter.getJobMessages(caseId, selectedJob.getId());
            doRefresh(msgs);
        } catch (Exception e) {
            e.printStackTrace();
            messagePanel.setError("Cannot retrieve all case histories. " + e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private JPanel createLayout(JobMessage[] msgs, EmfConsole parentConsole) throws Exception {
        final JPanel layout = new JPanel(new BorderLayout());
        layout.add(createTopPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(msgs, parentConsole), BorderLayout.CENTER);
        return layout;
    }
    
    private void getAllJobs() throws EmfException {
        this.caseJobs = new ArrayList<CaseJob>();
        caseJobs.add(new CaseJob("All"));
        caseJobs.addAll(Arrays.asList(presenter.getCaseJobs()));
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        jobCombo=new ComboBox("Select One", caseJobs.toArray(new CaseJob[0]));
        jobCombo.setPreferredSize(new Dimension(550,20));
        
        if (selectedJob!=null){
            jobCombo.setSelectedItem(selectedJob);
        }
        jobCombo.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                selectedJob=getSelectedJob();
                retrieveJobMsgs();
            }
        });
        
        layoutGenerator.addLabelWidgetPair("Job: ", jobCombo, panel);
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
        100, 15, // initialX, initialY
        5, 15);// xPad, yPad
        return panel;
    }
    
    private CaseJob getSelectedJob() {
        Object selected = jobCombo.getSelectedItem();

        if (selected == null)
            return new CaseJob("Select one");

        return (CaseJob) selected;
    }

    private JPanel tablePanel(JobMessage[] msgs, EmfConsole parentConsole) {
        setupTableModel(msgs);

        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());
        tablePanel.add(table, BorderLayout.CENTER);

        return tablePanel;
    }
    
    private void setupTableModel(JobMessage[] msgs){
        tableData = new JobMessagesTableData(msgs, session);
    }

    private SortCriteria sortCriteria() {
        String[] columnNames = { "Received Date"};
        return new SortCriteria(columnNames, new boolean[] {false}, new boolean[] {false});
    }

    public int numberOfRecord() {
        return tableData.sources().length;
    }

    public void clearMessage() {
        messagePanel.clear();
    }

    public void doRefresh(JobMessage[] msgs) throws Exception {
        clearMessage();
        setupTableModel(msgs);
        table.refresh(tableData);
        panelRefresh();
    }
    
    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.validate();
    }

    public void doRefresh() throws EmfException {
        // note that this will get called when the case is save
        try {
            kickPopulateThread();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

}
