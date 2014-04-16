package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.commons.gui.buttons.RunButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.casemanagement.CaseSelectionDialog;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorPresenter;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.ui.EmfFileChooser;
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
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class EditJobsTab extends JPanel implements EditJobsTabView, RefreshObserver {

    private EmfConsole parentConsole;

    private EditJobsTabPresenter presenter;

    private Case caseObj;

    private CaseJobsTableData tableData;

    private SelectableSortFilterWrapper table;

    private JPanel mainPanel;

    private MessagePanel messagePanel;

    private ManageChangeables changeables;

    private TextField outputDir;

    private EmfSession session;

    private DesktopManager desktopManager;

    public EditJobsTab(EmfConsole parentConsole, ManageChangeables changeables, MessagePanel messagePanel,
            DesktopManager desktopManager, EmfSession session) {
        super.setName("editJobsTab");
        this.parentConsole = parentConsole;
        this.messagePanel = messagePanel;
        this.desktopManager = desktopManager;
        this.session = session;
        this.changeables = changeables;

        super.setLayout(new BorderLayout());
    }

    public void display(EmfSession session, Case caseObj, EditJobsTabPresenter presenter,
            CaseEditorPresenter parentPresenter) {
        super.removeAll();
        this.outputDir = new TextField("outputdir", 50);
        outputDir.setText(caseObj.getOutputFileDir());
        this.changeables.addChangeable(outputDir);
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

    private void kickOfCancelJobs() {
        Thread populateThread = new Thread(new Runnable() {
            public void run() {
                startCancelJobs();
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

            try {
                presenter.checkIfLockedByCurrentUser();
            } catch (Exception e) {
                messagePanel.setMessage(e.getMessage());
            }
        }
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

    private void doRefresh(CaseJob[] jobs) throws Exception {
        // super.removeAll();
        String outputFileDir = caseObj.getOutputFileDir();

        if (!outputDir.getText().equalsIgnoreCase(outputFileDir))
            outputDir.setText(outputFileDir);

        setupTableModel(jobs);
        table.refresh(tableData);
        panelRefresh();
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

        layoutGenerator.addLabelWidgetPair("Output Job Scripts Folder:", getFolderChooserPanel(outputDir,
                "Select the base Output Job Scripts Folder for the Case"), panel);
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }

    private JPanel getFolderChooserPanel(final JTextField dir, final String title) {
        Button browseButton = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                selectFolder(dir, title);
            }
        });
        JPanel folderPanel = new JPanel(new BorderLayout(2, 0));
        folderPanel.add(dir, BorderLayout.LINE_START);
        folderPanel.add(browseButton, BorderLayout.LINE_END);

        return folderPanel;
    }

    private void selectFolder(JTextField dir, String title) {
        EmfFileInfo initDir = new EmfFileInfo(dir.getText(), true, true);

        EmfFileChooser chooser = new EmfFileChooser(initDir, new EmfFileSystemView(session.dataCommonsService()));
        chooser.setTitle(title);
        int option = chooser.showDialog(parentConsole, "Select a folder");

        EmfFileInfo file = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedDir() : null;
        if (file == null)
            return;

        if (file.isDirectory()) {
            caseObj.setOutputFileDir(file.getAbsolutePath());
            dir.setText(file.getAbsolutePath());
        }
    }

    private JPanel tablePanel(CaseJob[] jobs, EmfConsole parentConsole) {
        setupTableModel(jobs);

        mainPanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());
        mainPanel.add(table);
        return mainPanel;
    }

    private void setupTableModel(CaseJob[] jobs) {
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

        Button add = new AddButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                addNewJob();
            }
        });
        add.setMargin(insets);
        container.add(add);

        Button remove = new RemoveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                try {
                    removeJobs();
                } catch (EmfException exc) {
                    messagePanel.setError(exc.getMessage());
                }
            }
        });
        remove.setMargin(insets);
        container.add(remove);

        String message1 = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog1 = new ConfirmDialog(message1, "Warning", this);
        SelectAwareButton edit = new SelectAwareButton("Edit", editAction(), table, confirmDialog1);
        edit.setMargin(insets);
        container.add(edit);

        Button copy = new Button("Copy", copyAction());
        copy.setMargin(insets);
        container.add(copy);

        Button modify = new Button("Modify", modifyAction());
        modify.setMargin(insets);
        container.add(modify);

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

    private Action editAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clearMessage();
                    editJobs(getSelectedJobs());
                } catch (EmfException ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        };
        return action;
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

    private Action modifyAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    clearMessage();
                    modifyJobs();
                } catch (Exception ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        };
    }

    private void addNewJob() {
        NewJobDialog view = new NewJobDialog(parentConsole, caseObj, session);
        try {
            presenter.addNewJobDialog(view);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void removeJobs() throws EmfException {
        CaseJob[] jobs = getSelectedJobs().toArray(new CaseJob[0]);

        if (jobs.length == 0) {
            messagePanel.setMessage("Please select job(s) to remove.");
            return;
        }

        String title = "Warning";

        if (presenter.jobsUsed(jobs)) {
            int selection1 = showDialog("Selected job(s) are used by case inputs or parameters.\n "
                    + "Would you like to remove the selected job(s) and the associated inputs or parameters?", title);

            if (selection1 != JOptionPane.YES_OPTION)
                return;

            removeSelectedJobs(jobs);
            return;
        }

        int selection2 = showDialog("Are you sure you want to remove the selected job(s)?", title);

        if (selection2 == JOptionPane.YES_OPTION) {
            removeSelectedJobs(jobs);
        }
    }

    private void removeSelectedJobs(CaseJob[] jobs) throws EmfException {
        try {
            presenter.removeJobs(jobs);
        } catch (EmfException e) {
            throw new EmfException(e.getMessage());
        }
        refresh();
    }

    private void editJobs(List<CaseJob> jobs) throws EmfException {
        if (jobs.size() == 0) {
            messagePanel.setMessage("Please select job(s) to edit.");
            return;
        }

        for (Iterator<CaseJob> iter = jobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();
            String title = getWindowName(job);
            EditCaseJobView jobEditor = new EditCaseJobWindow(title, desktopManager, parentConsole, session);
            presenter.editJob(job, jobEditor);
        }
    }

    private String getWindowName(CaseJob job) {
        return "Edit Case Job: " + job.getName() + " (" + job.getId() + ") (" + caseObj.getName() + ")";
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
        if (caseId == this.caseObj.getId()) {
            List<CaseJob> copied = presenter.copyJobs2CurrentCase(caseId, jobs);
            
            if (copied.size() > 5) {
                int option = showDialog("There are more than 5 job editors to be opened." +
                        System.getProperty("line.separator") + "Are you sure you want to continue to open them?", "Warning");
                if (option == JOptionPane.NO_OPTION)
                    return;
            }
            
            editJobs(copied);
            return;
        }
        
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

    private void modifyJobs() throws Exception {
        List<CaseJob> jobs = getSelectedJobs();

        if (jobs.size() == 0) {
            messagePanel.setMessage("Please select job(s) to modify.");
            return;
        }

        for (CaseJob job : jobs) {
            if (desktopManager.getWindow(getWindowName(job)) != null) {
                JOptionPane.showMessageDialog(parentConsole, "Please close the editor for job '" + job.getName() + "'.",
                        "Close Job Editors", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        ModifyJobsDialog dialog = new ModifyJobsDialog(this.parentConsole, jobs.toArray(new CaseJob[0]), session);
        presenter.modifyJobs(dialog);
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
        int height = (validationMsg.length() / 50) + 3;

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

        SetjobsStatusDialog setDialog = new SetjobsStatusDialog(parentConsole, this, jobs, presenter);
        setDialog.run();
    }

    private void runJobs() throws Exception {
        CaseJob[] jobs = getSelectedJobs().toArray(new CaseJob[0]);

        if (jobs.length == 0) {
            messagePanel.setMessage("Please select one or more jobs to run.");
            return;
        }

        try {
            if (!presenter.passwordRegistered()) {
                SMKLoginDialog login = new SMKLoginDialog(parentConsole);
                login.register(presenter);
                login.display();
            }
                
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

        String finalMsg = validationMsg + ls + "ARE YOU SURE YOU WANT TO RUN THE SELECTED JOB"
                + (jobs.length > 1 ? "S" : "") + "?";
        int width = 50;
        int height = validationMsg.length() / 50;

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
        // descScrollableTextArea.setMinimumSize(new Dimension(width * 3, height * 2));
        return descScrollableTextArea;
    }

    private String cancelJobs() throws EmfException {
        List<CaseJob> jobs = getSelectedJobs();

        if (jobs == null || jobs.size() == 0)
            throw new EmfException("Please select a job to cancel.");

        return presenter.cancelJobs(jobs);
    }

    public void refresh() {
        // note that this will get called when the case is save
        try {
            if (tableData != null) // it's still null if you've never displayed this tab
                doRefresh(presenter.getCaseJobsFromManager());
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

    public void setMessage(String msg) {
        messagePanel.setMessage(msg);
    }

    private void panelRefresh() {
        mainPanel.removeAll();
        mainPanel.add(table);
        super.validate();
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
