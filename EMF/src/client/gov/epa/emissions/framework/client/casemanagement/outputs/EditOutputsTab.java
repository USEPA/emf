package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.commons.gui.buttons.ViewButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.casemanagement.editor.FindCaseWindow;
import gov.epa.emissions.framework.client.casemanagement.editor.RelatedCaseView;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.YesNoDialog;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class EditOutputsTab extends JPanel implements EditOutputsTabView, RefreshObserver {

    private EmfConsole parentConsole;

    private EditOutputsTabPresenterImpl presenter;
    
    private MessagePanel messagePanel;

    private OutputsTableData tableData;

    private ManageChangeables changeables;

    private SelectableSortFilterWrapper table;

    private JPanel tablePanel;
    
    private Case caseObj;
    
    private EmfSession session; 
    
    private ComboBox jobCombo;
    
    private List<CaseJob> caseJobs; 
    
    private CaseJob selectedJob=null;
    
    private CaseOutput selectedOutput;
    
    private DesktopManager desktopManager;


    public EditOutputsTab(EmfConsole parentConsole, ManageChangeables changeables, MessagePanel messagePanel,
            DesktopManager desktopManager, EmfSession session) {
        super.setName("editOutputsTab");
        this.parentConsole = parentConsole;
        this.changeables = changeables;
        this.session=session; 
        this.desktopManager=desktopManager;
        this.messagePanel=messagePanel;
 
        super.setLayout(new BorderLayout());
    }

    public void display() {
        super.removeAll();
        CaseOutput[] outputs = new CaseOutput[0];
        try {
            getAllJobs();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
        super.add(createLayout(outputs), BorderLayout.CENTER);
    }

    private void doRefresh(CaseOutput[] outputs){
        messagePanel.clear();
        selectedJob=(CaseJob) jobCombo.getSelectedItem();
        setupTableModel(outputs);
        table.refresh(tableData);
        panelRefresh();
    }

    private JPanel createLayout(CaseOutput[] outputs){
        JPanel layout = new JPanel(new BorderLayout());
        layout.add(createTopPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(outputs, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(), BorderLayout.PAGE_END);
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
        if (selectedJob!=null)
            jobCombo.setSelectedItem(selectedJob);
            
        jobCombo.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                CaseJob job=(CaseJob) jobCombo.getSelectedItem();
                
                try {
                    if (job == null){
                        doRefresh(new CaseOutput[0]);
                        return; 
                    }
                    CaseOutput[] outputs=presenter.getCaseOutputs(caseObj.getId(),job.getId());
                    doRefresh(outputs);
                } catch (EmfException exc) {
                    messagePanel.setError("Could not retrieve all outputs for job " + (job != null ? job.getName() : job) + ".");
                }
            }
        });
        layoutGenerator.addLabelWidgetPair("Job: ", jobCombo, panel);
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                100, 15, // initialX, initialY
                5, 15);// xPad, yPad
        return panel;
    }

    private JPanel tablePanel(CaseOutput[] outputs, EmfConsole parentConsole){
        setupTableModel(outputs);
        changeables.addChangeable(tableData);
        
        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());
        tablePanel.add(table, BorderLayout.CENTER);
        return tablePanel;
    }
    
    private void setupTableModel(CaseOutput[] outputs){
        tableData = new OutputsTableData(outputs, session);
    }

    private SortCriteria sortCriteria() {
        String[] columnNames = { "Sector", "Output name", "Job" };
        return new SortCriteria(columnNames, new boolean[] { true, true, true }, new boolean[] { false, false,
                false });
    }

    private JPanel controlPanel() {
        JPanel container = new JPanel();
        Insets insets = new Insets(1, 2, 1, 2);
        
        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        Button add = new AddButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                doNewOutput(presenter);
            }
        });
        add.setMargin(insets);
        container.add(add);
        
        Button remove = new RemoveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                removeSelectedOutput();
            }
        });
        remove.setMargin(insets);
        container.add(remove);

        SelectAwareButton edit = new SelectAwareButton("Edit", editAction(), table, confirmDialog);
        edit.setMargin(insets);
        container.add(edit);
        
        Button view = new ViewButton("View Dataset", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    displayOutputDatasetsPropertiesViewer();
                } catch (Exception e1) {
                    messagePanel.setError("Could not get dataset for output " + selectedOutput.getName() + "." 
                            + (e1.getMessage() == null ? "" : e1.getMessage()));
                }
            }
        });
        view.setMargin(insets);
        container.add(view);
        
        Button export = new ExportButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //
            }
        });
        export.setMargin(insets);
        export.setEnabled(false);
        container.add(export);
        
        Button findRelated = new Button("Find", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewCasesReleatedToDataset();
            }
        });
        findRelated.setMargin(insets);
        container.add(findRelated);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);
        return panel;
    }
 
    private Action editAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clearMessage();
                    editOutput();
                } catch (EmfException ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        };
        return action; 
    }
    
    private void doNewOutput(EditOutputsTabPresenter presenter) {
        NewOutputDialog view = new NewOutputDialog(parentConsole);
        try {
            CaseOutput newOutput = new CaseOutput();
            presenter.addNewOutputDialog(view, newOutput);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }
    protected void editOutput() throws EmfException {
        List outputs = table.selected();
        if (outputs.size() == 0) {
            messagePanel.setMessage("Please select output(s) to edit.");
            return;
        }
        for (Iterator iter = outputs.iterator(); iter.hasNext();) {
            CaseOutput output = (CaseOutput) iter.next();
            String title = "Edit Case Output: " + output.getName()+"("+output.getId()+ ")(" + caseObj.getName() + ")";
            EditCaseOutputView outputEditor = new EditCaseOutputWindow(title, desktopManager, parentConsole);
            presenter.editOutput(output, outputEditor);
        }
    }

    protected void displayOutputDatasetsPropertiesViewer() throws EmfException {
        messagePanel.clear();
        List selected = table.selected();
        
        if (selected.size() == 0) {
            messagePanel.setMessage("Please select one or more outputs to view.");
            return;
        }
        
        for (int i=0; i<selected.size(); i++) {
            selectedOutput = (CaseOutput) selected.get(i);
            if (selectedOutput == null){ 
                throw new EmfException("Output is null "); 
            }
            int id = selectedOutput.getDatasetId();
            EmfDataset dataset = presenter.getDataset(id);
            PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
            DatasetPropertiesViewer view = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
            presenter.doDisplay(view);
        }
    }

    private void removeSelectedOutput(){
        messagePanel.clear();
        CaseOutput[] selected =table.selected().toArray(new CaseOutput[0]);

        if (selected.length==0) {
            messagePanel.setMessage("Please select one or more outputs to remove.");
            return;
        }

        String title = "Warning";
        String message = "Are you sure you want to remove the selected output(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        String messageDS = "Would you also like to delete the Datasets"+"\n associated with these Outputs?";
        String titleDS = "Discard dataset?";
        if (selection == JOptionPane.YES_OPTION) {
            YesNoDialog dialog = new YesNoDialog(parentConsole, titleDS, messageDS);
            boolean deleteDS=dialog.confirm();
            tableData.remove(selected);
            refresh();
            
            try {
                presenter.doRemove(selected,deleteDS);
                messagePanel.setMessage(selected.length
                        + " outputs have been removed. Please Refresh to see the revised list of Outputs.");
            }catch (EmfException e){
                messagePanel.setError(e.getMessage());
            }
        }
    }
    public void refresh(){
        // note that this will get called when the case is save
            if (tableData != null) {// it's still null if you've never displayed this tab
                doRefresh(tableData.sources());
            }
    }

    public void doRefresh() throws EmfException {
        try {
            kickPopulateThread();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }


    public void observe(EditOutputsTabPresenterImpl presenter) {
        this.presenter = presenter;
        this.caseObj=presenter.getCaseObj();
    }
    
    private void kickPopulateThread() {
        Thread populateThread = new Thread(new Runnable() {
            public void run() {
                retrieveOutputs();
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

    private synchronized void retrieveOutputs() {
        try {
            messagePanel.setMessage("Please wait while retrieving all outputs...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            refreshJobList();
            
            if ( selectedJob != null)
                doRefresh(presenter.getCaseOutputs(caseObj.getId(), selectedJob.getId()));
            
            clearMessage();
        } catch (Exception e) {
            messagePanel.setError("Cannot retrieve all outputs.");
        } finally {
            setCursor(Cursor.getDefaultCursor());
            
            try {
                presenter.checkIfLockedByCurrentUser();
            } catch (Exception e) {
                messagePanel.setMessage(e.getMessage());
            }
        }
    }

    public void clearMessage() {
        messagePanel.clear();
    }
    
    public void setMessage(String msg) {
        messagePanel.setMessage(msg);
    }

    public void addOutput(CaseOutput addCaseOutput) {
        tableData.add(addCaseOutput);
        setMessage("Added \"" + addCaseOutput.getName() 
                + "\".  Click Refresh to see it in the table.");
    }
    
    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.validate();
    }
    
    private void viewCasesReleatedToDataset() {
        List outputlist = table.selected();
        if (outputlist == null || outputlist.size() != 1 ){
            messagePanel.setMessage("Please select one output.");
            return; 
        }
        
        int datasetId = ((CaseOutput)outputlist.get(0)).getDatasetId();
        
        if (datasetId == 0 ){
            messagePanel.setMessage("No dataset available. ");
            return; 
        }
        
        try {
            Case[] casesByInputDataset = presenter.getCasesByInputDataset(datasetId);
            Case[] casesByOutputDataset  = presenter.getCasesByOutputDatasets(new int[] {datasetId});
            String datasetName = presenter.getDataset(datasetId).getName();
            String title = "Find Uses of Dataset: " + datasetName;
            RelatedCaseView view = new FindCaseWindow(title, session, parentConsole, desktopManager);
            presenter.doViewRelated(view, casesByOutputDataset, casesByInputDataset);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }

    } 

    
}
