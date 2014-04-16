package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.RunButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.casemanagement.CaseSelectionDialog;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ViewableJobsTab extends JPanel implements JobsTabView, RefreshObserver {

    private EmfConsole parentConsole;

    private ViewableJobsTabPresenterImpl presenter;

    private Case caseObj;

    private CaseJobsTableData tableData;

    private SelectableSortFilterWrapper table;

    private JPanel tablePanel;

    private MessagePanel messagePanel;

    private TextField outputDir;

    private EmfSession session;
   
    private DesktopManager desktopManager;

    public ViewableJobsTab(EmfConsole parentConsole, MessagePanel messagePanel,
            DesktopManager desktopManager, EmfSession session) {
        super.setName("viewJobsTab");
        this.parentConsole = parentConsole;
        this.messagePanel = messagePanel;
        this.desktopManager = desktopManager;
        this.session = session;

        super.setLayout(new BorderLayout());
    }

    public void display(Case caseObj, ViewableJobsTabPresenterImpl presenter) {
        super.removeAll();
        this.outputDir = new TextField("outputdir", 50);
        outputDir.setText(caseObj.getOutputFileDir());
        outputDir.setEditable(false);
        this.caseObj = caseObj;
        this.presenter = presenter;

        try {
            super.add(createLayout(new CaseJob[0], parentConsole), BorderLayout.CENTER);
        } catch (Exception e) {
            messagePanel.setError("Cannot retrieve all case jobs.");
        }

        kickPopulateThread();
    }

    private void kickPopulateThread() {
        Thread populateThread = new Thread(new Runnable() {
            public void run() {
                retrieveJobs();
            }
        });
        populateThread.start();
    }

    public synchronized void retrieveJobs() {
        try {
            messagePanel.setMessage("Please wait while retrieving all case jobs...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            doRefresh(presenter.getCaseJobs());
            messagePanel.clear();
        } catch (Exception e) {
            messagePanel.setError("Cannot retrieve all case jobs.");
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void doRefresh(CaseJob[] jobs) throws Exception {
        String outputFileDir = caseObj.getOutputFileDir();

        if (!outputDir.getText().equalsIgnoreCase(outputFileDir))
            outputDir.setText(outputFileDir);

        setupTableModel(jobs);
        table.refresh(tableData);
        panelRefresh();    
    }
    
    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.validate();
    }

    private JPanel createLayout(CaseJob[] jobs, EmfConsole parentConsole) throws Exception {
        final JPanel layout = new JPanel(new BorderLayout());

        layout.add(createFolderPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(jobs, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(), BorderLayout.PAGE_END);

        return layout;
    }

    private JPanel createFolderPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Output Folder:", outputDir, panel);
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }


    private JPanel tablePanel(CaseJob[] jobs, EmfConsole parentConsole) {
        setupTableModel(jobs);

        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());

        tablePanel.add(table, BorderLayout.CENTER);

        return tablePanel;
    }
    
    private void setupTableModel(CaseJob[] jobs){
        tableData = new CaseJobsTableData(jobs);
    }
    

    private SortCriteria sortCriteria() {
        String[] columnNames = { "Order", "Sector", "Name", "Executable" };
        return new SortCriteria(columnNames, new boolean[] { true, true, true, true }, new boolean[] { false, false,
                false, false });
    }

    private JPanel controlPanel() {
        Insets insets = new Insets(1, 2, 1, 2);
        JPanel container = new JPanel();
        
        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);
        SelectAwareButton view = new SelectAwareButton("View", viewAction(), table, confirmDialog);
        view.setMargin(insets);
        container.add(view);

        Button run = new RunButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clearMessage();
                    runJobs();
                } catch (Exception ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        });
        run.setMargin(insets);
        container.add(run);
        
        Button copy = new Button("Copy", copyAction());
        copy.setMargin(insets);
        container.add(copy);

        Button validate = new Button("Validate", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clearMessage();
                    validateJobDatasets();
                } catch (Exception ex) {
                   // ex.printStackTrace();
                    messagePanel.setError(ex.getMessage());
                }
            }
        });
        validate.setMargin(insets);
        container.add(validate);
        
        Button set = new Button("Set Status", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                setStatus();
            }
        });
        set.setMargin(insets);
        container.add(set);
        
        Button cancelJobs = new Button("Cancel", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clearMessage();
                    kickOfCancelJobs();
                } catch (Exception ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        });
        cancelJobs.setMargin(insets);
        container.add(cancelJobs);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }
    
    private Action viewAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clearMessage();
                    viewJobs();
                } catch (EmfException ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        };
        return action; 
    }

    private void viewJobs() throws EmfException {
        List jobs = getSelectedJobs();

        if (jobs.size() == 0) {
            messagePanel.setMessage("Please select job(s) to edit.");
            return;
        }

        for (Iterator iter = jobs.iterator(); iter.hasNext();) {
            CaseJob job = (CaseJob) iter.next();
            String title = "View Case Job : " + job.getName() + " (" + job.getId() + ") (" + caseObj.getName() + ")";
            EditCaseJobView jobEditor = new EditCaseJobWindow(true, title, desktopManager, parentConsole, session);
            presenter.editJob(job, jobEditor);
        }
    }
    
    private Action copyAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    clearMessage();
                    copyJobs();
                } catch (Exception ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        };
    }
    
    private void copyJobs() throws Exception {
        List<CaseJob> jobs = getSelectedJobs();

        if (jobs.size() == 0) {
            messagePanel.setMessage("Please select job(s) to copy.");
            return;
        }

        String[] caseIds = (String[]) presenter.getAllCaseNameIDs();
        
        CaseSelectionDialog view = new CaseSelectionDialog(parentConsole, caseIds);
        String title = "Copy " + jobs.size()+" case job(s) to case: ";
        
        view.display(title, true);
        
        if (view.shouldCopy()){
            String selectedCase=view.getCases()[0];
            processCopyjobs(getCaseId(selectedCase), jobs);
        }
    }
 
    private void processCopyjobs(int caseId, List<CaseJob> jobs) throws Exception {
        if (caseId != this.caseObj.getId()) {
            GeoRegion[] regions = presenter.getGeoregion(jobs);
            if (regions.length >0 ){
                String message= presenter.isGeoRegionInSummary(caseId, regions);
                if (message.trim().length()>0){
                    message = "Add the regions " + message + " to Case (" +
                    caseId + ")? \n Note: if you don't add the region, the copy will be canceled. ";
                          
                    int selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if (selection == JOptionPane.YES_OPTION) 
                        presenter.copyJobs(caseId, jobs);
                    return; 
                }
                presenter.copyJobs(caseId, jobs);
                return; 
            }
            presenter.copyJobs(caseId, jobs);   
        }
    }
     
    private int getCaseId(String selectedCase) {
        int index1 = selectedCase.indexOf("(") + 1;
        int index2 = selectedCase.indexOf(")");
         
        return Integer.parseInt(selectedCase.substring(index1, index2));
    }

    private void validateJobDatasets() throws EmfException {
        CaseJob[] jobs = getSelectedJobs().toArray(new CaseJob[0]);

        if (jobs.length == 0) {
            messagePanel.setMessage("Please select one or more jobs to run.");
            return;
        }

        String validationMsg = presenter.validateJobs(jobs);
        int width = 50;
        int height = validationMsg.length() / 50;
        
        if (height > 30)
            height = 30;
        
        String title = "Possible Issues with Datasets Selected for Job Inputs";

        showMessageDialog(createMsgScrollPane(validationMsg, width, height), title);
    }

    private void setStatus() {
        CaseJob[] jobs = getSelectedJobs().toArray(new CaseJob[0]);

        if (jobs.length == 0) {
            messagePanel.setMessage("Please select one or more jobs to set status.");
            return;
        }

        SetjobsStatusDialog setDialog = new  SetjobsStatusDialog(parentConsole, this, jobs, presenter);
        setDialog.run();    
    }
            
    private void runJobs() throws Exception {
        CaseJob[] jobs = getSelectedJobs().toArray(new CaseJob[0]);

        if (jobs.length == 0) {
            messagePanel.setMessage("Please select one or more jobs to run.");
            return;
        }

        try {
            String msg = presenter.getJobsStatus(jobs);
            int option = JOptionPane.NO_OPTION;
            String lineSeparator = System.getProperty("line.separator");

            if (msg.equalsIgnoreCase("OK")) {
                option = validateJobs(jobs, lineSeparator);

                if (option == JOptionPane.YES_OPTION)
                    proceedRunningJobs(jobs);

                return;
            }

            if (msg.equalsIgnoreCase("CANCEL")) {
                setMessage("One or more of the selected jobs is already running.");
                return;
            }

            if (msg.equalsIgnoreCase("WARNING"))
                option = showDialog("Are you sure you want to rerun the selected job" + (jobs.length > 1 ? "s" : "")
                        + "?", "Warning");

            if (option == JOptionPane.YES_OPTION) {
                option = validateJobs(jobs, lineSeparator);
            }

            if (option == JOptionPane.YES_OPTION)
                proceedRunningJobs(jobs);
        } catch (Exception e) {
            throw e;
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private int validateJobs(CaseJob[] jobs, String ls) throws EmfException {
        String validationMsg = presenter.validateJobs(jobs);

        if (validationMsg.isEmpty())
            // there are no nonfinal dataset versions used, so return yes
            return JOptionPane.YES_OPTION; 
        
        String finalMsg = validationMsg + ls + "ARE YOU SURE YOU WANT TO RUN THE SELECTED JOB" + 
                    (jobs.length > 1 ? "S" : "") + "?";
        int width = 50;
        int height = (validationMsg.length() / 50)+3;
        
        if (height > 30)
            height = 30;
        
        ScrollableComponent msgArea = createMsgScrollPane(finalMsg, width, height);
        
        return showDialog(msgArea, "Confirm Running Jobs");
    }

    private void proceedRunningJobs(CaseJob[] jobs) throws Exception {
        setMessage("Please wait while submitting all case jobs...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        presenter.runJobs(jobs);
        setMessage("Finished submitting jobs to run.");
    }

    private void kickOfCancelJobs() {
        Thread populateThread = new Thread(new Runnable() {
            public void run() {
                startCancelJobs();
            }
        });
        populateThread.start();
    }
    
    private synchronized void startCancelJobs() {
        try {
            messagePanel.setMessage("Please wait while all selected jobs are being canceled...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            String msg = cancelJobs();

            if (msg != null && !msg.trim().isEmpty()){
                refresh();
                messagePanel.setMessage(msg);
            }
                
        } catch (Exception e) {
            messagePanel.setError(e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    private String cancelJobs() throws EmfException {
        List<CaseJob> jobs = getSelectedJobs();

        if (jobs == null || jobs.size() == 0)
            throw new EmfException("Please select a job to cancel.");

        return presenter.cancelJobs(jobs);
    }
    
    private List<CaseJob> getSelectedJobs() {
        return (List<CaseJob>) table.selected();
    }

    private int showDialog(Object msg, String title) {
        return JOptionPane.showConfirmDialog(parentConsole, msg, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
    }

    private void showMessageDialog(Object msg, String title) {
        JOptionPane.showMessageDialog(parentConsole, msg, title, JOptionPane.WARNING_MESSAGE);
    }

    private ScrollableComponent createMsgScrollPane(String msg, int width, int height) {
        TextArea message = new TextArea("msgArea", msg, width, height);
        message.setEditable(false);
        ScrollableComponent descScrollableTextArea = new ScrollableComponent(message);
        return descScrollableTextArea;
    }

    public void refresh() {
        // note that this will get called when the case is save
        try {
            if (tableData != null) // it's still null if you've never displayed this tab
                doRefresh(presenter.getCaseJobs());
        } catch (Exception e) {
            messagePanel.setError("Cannot refresh current tab. " + e.getMessage());
        }
    }

    public int numberOfRecord() {
        return tableData.sources().length;
    }

    public void clearMessage() {
        messagePanel.clear();
    }

    private void setMessage(String msg) {
        messagePanel.setMessage(msg);
    }

    public CaseJob[] caseJobs() {
        return tableData.sources();
    }

    public String getCaseOutputFileDir() {
        if (outputDir == null)
            return null;
        return outputDir.getText();
    }

    public void doRefresh() throws EmfException {
        try {
            kickPopulateThread();
        } catch (RuntimeException e) {
            throw new EmfException(e.getMessage());
        }
    }
    
}
