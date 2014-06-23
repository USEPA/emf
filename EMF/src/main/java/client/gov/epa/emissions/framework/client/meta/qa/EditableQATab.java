package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.BorderlessButton;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlstrategy.AnalysisEngineTableApp;
import gov.epa.emissions.framework.client.data.dataset.CopyQAStepToDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.CopyQAStepToDatasetSelectionPresenter;
import gov.epa.emissions.framework.client.data.dataset.CopyQAStepToDatasetSelectionView;
import gov.epa.emissions.framework.client.meta.versions.VersionsSet;
import gov.epa.emissions.framework.client.preference.DefaultUserPreferences;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.ui.InfoDialog;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class EditableQATab extends JPanel implements EditableQATabView, RefreshObserver {

    private EditableQATabPresenter presenter;

    private EditableQAStepsTableData tableData;

    private SelectableSortFilterWrapper table;

    private JPanel tablePanel;

    private EmfConsole parentConsole;

    private VersionsSet versions;

    private MessagePanel messagePanel;

    private DesktopManager desktop;

    private int datasetID;

    private Dataset dataset;

    private EmfSession session;
    
    public EditableQATab(EmfSession session, EmfConsole parent, DesktopManager desktop, MessagePanel messagePanel) {
        this.parentConsole = parent;
        this.desktop = desktop;
        this.messagePanel = messagePanel;
        this.session = session;
    }

    public void display(Dataset dataset, QAStep[] steps, QAStepResult[] qaStepResults, Version[] versions) {
        this.datasetID = dataset.getId(); // for uniqueness of window naming
        this.dataset = dataset;
        this.versions = new VersionsSet(versions);
        
        createLayout(steps, qaStepResults);
 
    }
    
    private void createLayout(QAStep[] steps, QAStepResult[] qaStepResults) {
        super.setLayout(new BorderLayout());
        super.add(tablePanel(steps, qaStepResults), BorderLayout.CENTER);
        super.add(createButtonsSection(), BorderLayout.PAGE_END);
        super.setSize(new Dimension(700, 300));
    }

    protected JPanel tablePanel(QAStep[] steps, QAStepResult[] qaStepResults) {
        setupTableModel(steps, qaStepResults);
        if (table == null){
            tablePanel = new JPanel(new BorderLayout());
            table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());
            tablePanel.add(table, BorderLayout.CENTER);
        }else {
            refreshTable(steps, qaStepResults);
        }
        return tablePanel;
    }
    
    private void setupTableModel(QAStep[] steps, QAStepResult[] qaStepResults) {
        tableData = new EditableQAStepsTableData(steps, qaStepResults);
    }
    
    private void refreshTable(QAStep[] steps, QAStepResult[] qaStepResults){
        table.refresh(tableData);
        super.validate();
    }  

    private SortCriteria sortCriteria() {
        String[] columnNames = { "Version", "Order", "Name" };
        return new SortCriteria(columnNames, new boolean[] { false, true, true }, new boolean[] { true, true, true });
        //return new SortCriteria(columnNames, new boolean[] { false, false }, new boolean[] { true, true });
    }

    private JPanel createButtonsSection() {
        JPanel container = new JPanel();

        Button add = new BorderlessButton("Add from Template", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAddUsingTemplate();
            }
        });
        container.add(add);

        Button remove = new BorderlessButton("Add Custom", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doAddCustom();
            }
        });
        container.add(remove);

        Button edit = new BorderlessButton("Edit", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doEdit();
            }
        });
        container.add(edit);

        Button copy = new BorderlessButton("Copy", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doCopy();
            }
        });
        container.add(copy);
        
        Button delete = new BorderlessButton("Delete", new AbstractAction() { // BUG3615
            public void actionPerformed(ActionEvent event) {
                doDelete();
            }

        });
        container.add(delete);

        Button status = new BorderlessButton("Set Status", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doSetStatus();
            }
        });
        container.add(status);
        
        Button runStatus = new BorderlessButton("Run", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                runStatus();
            }
        });
        container.add(runStatus);
        JButton viewResults = new BorderlessButton("View Results", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                try {
                    viewResults();
                } catch (EmfException exc) {
                    messagePanel.setError(exc.getMessage());
                }
            }
        });
        container.add(viewResults);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }

    public void observe(EditableQATabPresenter presenter) {
        this.presenter = presenter;
    }

    public QAStep[] steps() {
        return tableData.sources();
    }

    private void doAddUsingTemplate() {
        clearMessage();
        presenter.doAddUsingTemplate(new NewQAStepDialog(parentConsole, versions.all()));
    }

    private void doAddCustom() {
        clearMessage();
        try {
            presenter.doAddCustomized(new NewCustomQAStepWindow(desktop));
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void doEdit() {
        clearMessage();

        List selected = table.selected();
        if (selected == null || selected.size() == 0) {
            messagePanel.setMessage("Please select a QA step.");
            return;
        }

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            QAStep step = (QAStep) iter.next();
            EditQAStepWindow view = new EditQAStepWindow(desktop, parentConsole);
            try {
                presenter.doEdit(step, view, versions.name(step.getVersion()));
            } catch (EmfException e) {
                messagePanel.setError(e.getMessage());
            }
        }
    }

    private void doCopy() {
        clearMessage();

        List selected = table.selected();
        if (selected == null || selected.size() == 0) {
            messagePanel.setMessage("Please select a QA step.");
            return;
        }

        int answer = JOptionPane.showConfirmDialog(this, "Copy to the current dataset?");
        if (answer == JOptionPane.CANCEL_OPTION) {
            return;
        } else if ( answer == JOptionPane.YES_OPTION) {
            int[] datasetIds = new int[1];
            datasetIds[0] = dataset.getId();
            try {
                this.presenter.doCopyQASteps((QAStep[])selected.toArray(new QAStep[0]), datasetIds, false);
                messagePanel.setMessage("Copied " + selected.size() + " QA Steps to current dataset.");
            } catch (EmfException e) {
                // NOTE Auto-generated catch block
                e.printStackTrace();
                messagePanel.setError(e.getMessage());
            }
        } else {

            CopyQAStepToDatasetSelectionView view = new CopyQAStepToDatasetSelectionDialog(parentConsole);
            CopyQAStepToDatasetSelectionPresenter presenter = new CopyQAStepToDatasetSelectionPresenter(view, session);
            try {
                presenter.display(dataset.getDatasetType(), false);
                Dataset[] datasets = presenter.getDatasets();
                //            boolean copyToExistingDatasetType = false;
                if (datasets.length > 0) {
                    int[] datasetIds = new int[datasets.length];
                    String datasetNameList = "";
                    for (int i = 0; i < datasets.length; i++) {
                        datasetIds[i] = datasets[i].getId();
                        datasetNameList = datasetNameList + (i > 0 ? ", " : "") + datasets[i].getName();
                        //                    if (datasets[i].getId() == datasetID) copyToExistingDatasetType = true;
                    }
                    this.presenter.doCopyQASteps((QAStep[])selected.toArray(new QAStep[0]), datasetIds, presenter.shouldReplace());
                    //if copied to self, then a lock would have been lose, and needs to be reclaimed
                    //also, refresh the templates table
                    //                if (copyToExistingDatasetType) {
                    //                    this.type = this.presenter.obtainLockedDatasetType(session.user(), this.type);
                    //                    tableData.removeAll();
                    //                    for (QAStepTemplate template : this.type.getQaStepTemplates())
                    //                        tableData.add(template);
                    //
                    ////                    tableData = new EditableQAStepTemplateTableData(this.type.getQaStepTemplates());
                    ////                    tableModel.refresh(tableData);
                    //                    
                    //                    refresh();
                    //                }
                    messagePanel.setMessage("Copied " + selected.size() + " QA Steps to Datasets: " + datasetNameList + ".");
                }
            } catch (Exception exp) {
                messagePanel.setError(exp.getMessage());
            }
        }
    }

    private void doDelete() { //BUG3615
        clearMessage();

        List selected = table.selected();
        if (selected == null || selected.size() == 0) {
            messagePanel.setMessage("Please select a QA step.");
            return;
        }
        
        String message = "Are you sure you want to remove the selected " + selected.size() + " QA step(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            this.presenter.doDelete((QAStep[])selected.toArray(new QAStep[0]));
            messagePanel.setMessage("Deleting the QA steps, please watch status windows for detailed information.");

        } catch (Exception exp) {
            messagePanel.setError(exp.getMessage());
        }
        
    }

    private void clearMessage() {
        messagePanel.clear();
    }

    public void addFromTemplate(QAStep[] steps) {
        QAStep[] newSteps = filterDuplicates(steps);
        try {
            newSteps = presenter.addFromTemplates(newSteps);
            addNewStepsToTable(newSteps);
        } catch (Exception e) {
            messagePanel.setError(e.getMessage());
        }
    }

    public void addCustomQAStep(QAStep step) {
        QAStep[] steps = new QAStep[] { step };
        steps = filterDuplicates(steps);
        addNewStepsToTable(steps);
    }


    private QAStep[] filterDuplicates(QAStep[] steps) {
        QASteps qaSteps = new QASteps(tableData.sources());
        QAStep[] newSteps = qaSteps.filterDuplicates(steps);
        for (int i = 0; i < newSteps.length; i++)
            newSteps[i].setStatus("Not Started");
        return newSteps;
    }
    
    private void addNewStepsToTable(QAStep[] newSteps) {
        for (int i = 0; i < newSteps.length; i++)
            tableData.add(newSteps[i]);
        refresh();
    }

    public void refresh(QAStep step, QAStepResult result) {
        tableData.add(step, result);
        refresh();
    }
    
    public void refresh() {
        tableData.refresh();
        table.refresh(tableData);
        panelRefresh();
    }
    
    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.validate();
    }

    private void doSetStatus() {
        clearMessage();

        List selected = table.selected();
        QAStep[] steps = (QAStep[]) selected.toArray(new QAStep[0]);
        if (steps.length > 0)
            presenter.doSetStatus(new SetQAStatusWindow(desktop, datasetID), steps);
        else
            messagePanel.setMessage("Please select a QA step.");
    }
    
    private void runStatus() {
        clearMessage();

        List selected = table.selected();
        QAStep[] steps = (QAStep[]) selected.toArray(new QAStep[0]);
        if (steps==null ||steps.length == 0) {
            messagePanel.setMessage("Please select a QA step.");
            return; 
        }
        if (steps.length > 3 ){
            messagePanel.setMessage("You may only run three at the same time.");
            return;
        }
        for (int i=0; i<steps.length; i++){
            try {
                messagePanel.setMessage("Started " + (steps.length==1? " One Run. " : steps.length+" Runs. ")+" Please monitor the Status Window. ");
                 presenter.runStatus(steps[i]);
            } catch (EmfException e) {
                messagePanel.setMessage(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void informLackOfTemplatesForAddingNewSteps(DatasetType type) {
        String message = "Dataset has no templates to choose from. Please add templates to Dataset Type: "
                + type.getName();
        InfoDialog dialog = new InfoDialog(this, "Message", message);
        dialog.confirm();
    }

    public void doRefresh() {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            messagePanel.setMessage("Please wait while loading dataset QA...");
            super.removeAll();
            presenter.display();
            super.validate();
            messagePanel.setMessage("Finished loading dataset QA steps.");
        } catch (Exception e) {
            messagePanel.setError(e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
            try {
                presenter.checkIfLockedByCurrentUser();
            } catch (Exception e) {
                messagePanel.setMessage(e.getMessage());
            }
        }
        
    }

    public void displayResultsTable(String qaStepName, String exportedFileName) {
        AnalysisEngineTableApp app = new AnalysisEngineTableApp("View QA Step Results: " + qaStepName, new Dimension(500, 500), desktop, parentConsole);
        app.display(new String[] { exportedFileName });
    }
    
    private void viewResults() throws EmfException {
        clearMessage();

        List selected = table.selected();
        if (selected == null || selected.size() == 0) {
            messagePanel.setMessage("Please select a QA step.");
            return;
        }

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            QAStep step = (QAStep) iter.next();
            QAStepResult stepResult = presenter.getStepResult(step);
            if (stepResult == null)
                throw new EmfException("Please run the QA step, " + step.getName() + ", before trying to view.");
        }

        Thread viewResultsThread = new Thread(new Runnable() {
            public void run() {
                try {
                    List selected = table.selected();
                    for (Iterator iter = selected.iterator(); iter.hasNext();) {

                        QAStep step = (QAStep) iter.next();
                        try {
                            QAStepResult stepResult = presenter.getStepResult(step);
                            clearMessage();
                            
                            DefaultUserPreferences userPref = new DefaultUserPreferences();
                            String sLimit = userPref.property("View_QA_results_limit");
                            long rlimit;
                            if ( sLimit == null ){
                                JOptionPane.showMessageDialog(parentConsole, 
                                        "View_QA_results_limit is not specified in EMFPrefs.txt, default value is 50000.", "Warning", JOptionPane.WARNING_MESSAGE);
                                rlimit = 50000;
                            }
                            else   
                                try {
                                    rlimit = Integer.parseInt(sLimit.trim());
                                } catch (NumberFormatException e) {
                                    //just default if they entered a non number string
                                    rlimit = 50000;
                                }
                          
                            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                            long records = presenter.getTableRecordCount(stepResult);
                            long viewCount =  records;
                            if ( records > rlimit ){
                                messagePanel.setMessage("Total records: " + records + ", limit: "+rlimit );
                                ViewQAResultDialg dialog = new ViewQAResultDialg(step.getName(), parentConsole);
                                dialog.run();
                                 
                                if ( dialog.shouldViewNone() )
                                    return; 
                                else if ( !dialog.shouldViewall()){ 
                                    viewCount = dialog.getLines();
                                } 
                                if ( viewCount > records ) viewCount = records;
                                if ( viewCount > 100000) {
                                    String title = "Warning";
                                    String message = "Are you sure you want to view more than 100,000 records?  It could take several minutes to load the data.";
                                    int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                                            JOptionPane.QUESTION_MESSAGE);

                                    if (selection == JOptionPane.NO_OPTION) {
                                        return;
                                    }
                                }
                            }
                            presenter.viewResults(step, viewCount);
                        } catch (EmfException e) {
                            try  {
                                //dataset.
                                //if ( presenter.checkBizzareCharInColumn(step, "plant")) { // sniff the msg to see if xml related, then check column name, then check
                                if ( e.getMessage().contains("Invalid XML character")) {
                                    messagePanel.setError("There are bizarre characters in the dataset." + 
                                            ((dataset.getDatasetType().getName().equals(DatasetType.FLAT_FILE_2010_POINT) || 
                                              dataset.getDatasetType().getName().equals(DatasetType.orlPointInventory)) 
                                              ? ", please run a QA step Detect Bizarre Characters." : "."));                                    
                                } else {
                                    messagePanel.setError(e.getMessage());
                                }
                            } catch (Exception e2) {
                                messagePanel.setError(e2.getMessage());
                            }
                        } finally {
                            setCursor(Cursor.getDefaultCursor());
                        }
                    }
                } catch ( Exception e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        });

        viewResultsThread.start();
    }
    
}
