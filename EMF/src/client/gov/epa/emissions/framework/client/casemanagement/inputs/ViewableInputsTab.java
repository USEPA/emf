package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.commons.gui.buttons.ViewButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.casemanagement.CaseSelectionDialog;
import gov.epa.emissions.framework.client.casemanagement.editor.FindCaseWindow;
import gov.epa.emissions.framework.client.casemanagement.editor.RelatedCaseView;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.client.swingworker.RefreshSwingWorkerTasks;
import gov.epa.emissions.framework.client.swingworker.SwingWorkerTasks;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;

public class ViewableInputsTab extends JPanel implements RefreshObserver {

    private EmfConsole parentConsole;

    private ViewableInputsTabPresenter presenter;

    private Case caseObj;

    private int caseId;

    private InputsTableData tableData;

    private JPanel mainPanel;
    
    private JPanel layout;

    private SelectableSortFilterWrapper table;

    private MessagePanel messagePanel;

    private DesktopManager desktopManager;

    private TextField inputDir;

    private JCheckBox showAll;

    private ComboBox sectorsComboBox;

    private EmfSession session;

    private Sector selectedSector;

    private TextField envVarContains;

    public ViewableInputsTab(EmfConsole parentConsole, MessagePanel messagePanel, DesktopManager desktopManager) {
        super.setName("viewInputsTab");
        this.parentConsole = parentConsole;
        this.messagePanel = messagePanel;
        this.desktopManager = desktopManager;
        super.setLayout(new BorderLayout());
    }

    public void display(EmfSession session, Case caseObj) {
        super.removeAll();

        this.caseObj = caseObj;
        this.caseId = caseObj.getId();
         
        this.session = session;
        this.inputDir = new TextField("inputdir", 50);
        inputDir.setText(caseObj.getInputFileDir());
        inputDir.setEditable(false);

        super.add(createLayout(new CaseInput[0], parentConsole), BorderLayout.CENTER);
        messagePanel.setMessage("Please select a sector to see full list of inputs.");

    }
    
    public void doDisplay(ViewableInputsTabPresenter presenter){
        this.presenter = presenter;
        new SwingWorkerTasks(this, presenter).execute();
    }

    private void doRefresh(CaseInput[] inputs) throws Exception {
        String inputFileDir = caseObj.getInputFileDir();
        if (!inputDir.getText().equalsIgnoreCase(inputFileDir))
            inputDir.setText(inputFileDir);
        setupTableModel(inputs);
        table.refresh(tableData);
        panelRefresh();
    }

    private void panelRefresh() {
        mainPanel.removeAll();
        mainPanel.add(table);
        super.validate();
    }

    private JPanel createLayout(CaseInput[] inputs, EmfConsole parentConsole) {
        layout = new JPanel(new BorderLayout());

        layout.add(createFolderNSectorPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(inputs, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(), BorderLayout.PAGE_END);

        return layout;
    }

    private JPanel tablePanel(CaseInput[] inputs, EmfConsole parentConsole) {
        setupTableModel(inputs);

        mainPanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());
        mainPanel.add(table);

        return mainPanel;
    }

    private void setupTableModel(CaseInput[] inputs) {
        tableData = new InputsTableData(inputs, session);
    }

    private JPanel createFolderNSectorPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Input Folder:", inputDir, panel);

        sectorsComboBox = new ComboBox("Select a Sector", presenter.getAllSetcors());
        sectorsComboBox.addActionListener(filterAction());
        
        envVarContains = new TextField("envVarFilter", 10);
        envVarContains.setToolTipText("Environment variable name filter. Press enter to refresh.");
        KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
        envVarContains.registerKeyboardAction(filterActionByName(), keystroke, JComponent.WHEN_FOCUSED);
        
        JPanel selection = new JPanel(new BorderLayout(20, 0));
        selection.add(sectorsComboBox, BorderLayout.LINE_START);
        selection.add(new JLabel("Environment Variable Contains:"));
        selection.add(envVarContains, BorderLayout.LINE_END);

        layoutGenerator.addLabelWidgetPair("Sector:", selection, panel);
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }
    
    private AbstractAction filterAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    new RefreshSwingWorkerTasks(layout, messagePanel, presenter).execute();
                    clearMessage();
                } catch (Exception exc) {
                    setErrorMessage(exc.getMessage());
                }
            }
        };
    }
    
    private AbstractAction filterActionByName() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    if (envVarContains.getText() == null || envVarContains.getText().trim().isEmpty())
                        return;
                    
                    if (sectorsComboBox != null)
                        sectorsComboBox.setSelectedItem(new Sector("All", "All"));
                    
                    new RefreshSwingWorkerTasks(layout, messagePanel, presenter).execute();
                    clearMessage();
                } catch (Exception exc) {
                    setErrorMessage(exc.getMessage());
                }
            }
        };
    }

    private SortCriteria sortCriteria() {
        String[] columnNames = { "Envt. Var.", "Sector", "Input", "Job" };
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

        Button copy = new Button("Copy", copyAction(presenter));
        copy.setMargin(insets);
        container.add(copy);

        Button viewDS = new ViewButton("View Dataset", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doDisplayInputDatasetsPropertiesViewer();
            }
        });
        viewDS.setMargin(insets);
        container.add(viewDS);

        Button export = new ExportButton("Export Inputs", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doExportInputDatasets(getSelectedInputs());
            }
        });
        export.setMargin(insets);
        container.add(export);

        Button findRelated = new Button("Find", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewCasesReleatedToDataset();
            }
        });
        findRelated.setMargin(insets);
        container.add(findRelated);

        showAll = new JCheckBox("Show All", false);
        showAll.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                try {
                    if (sectorsComboBox != null)
                        sectorsComboBox.setSelectedItem(new Sector("All", "All"));
                    new RefreshSwingWorkerTasks(layout, messagePanel, presenter).execute();
                    messagePanel.clear();
                } catch (Exception ex) {
                    setErrorMessage(ex.getMessage());
                }
            }
        });
        container.add(showAll);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }

    private Action viewAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clearMessage();
                    doView();
                } catch (EmfException ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        };
        return action;
    }

    private void doView() throws EmfException {
        List inputs = getSelectedInputs();

        if (inputs.size() == 0) {
            messagePanel.setMessage("Please select input(s) to edit.");
            return;
        }

        for (Iterator iter = inputs.iterator(); iter.hasNext();) {
            CaseInput input = (CaseInput) iter.next();
            String title = "View Case Input:" + input.getName() + "(" + input.getId() + ")(" + caseObj.getName() + ")";
            EditCaseInputView inputEditor = new EditCaseInputWindow(title, desktopManager, parentConsole);
            presenter.doEditInput(input, inputEditor);
            inputEditor.viewOnly(title);
        }
    }

    private Action copyAction(final ViewableInputsTabPresenter localPresenter) {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    clearMessage();
                    copyInputs(localPresenter);
                } catch (Exception ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        };
    }
    
    private void copyInputs(ViewableInputsTabPresenter presenter) throws Exception {
        checkModelToRun();
        List<CaseInput> inputs = getSelectedInputs();

        if (inputs.size() == 0) {
            messagePanel.setMessage("Please select input(s) to copy.");
            return;
        }

        String[] caseIds = (String[]) presenter.getAllCaseNameIDs();      
        CaseSelectionDialog view = new CaseSelectionDialog(parentConsole, caseIds);
        String title = "Copy " + inputs.size()+" case input(s) to case: ";
        
        view.display(title, true);
        
        if (view.shouldCopy()){
            String selectedCase=view.getCases()[0];
            int selectedCaseId = getCaseId(selectedCase);
            if (selectedCaseId != this.caseId) {
                GeoRegion[] regions = presenter.getGeoregion(inputs);
                if (regions.length >0 ){
                    String message= presenter.isGeoRegionInSummary(selectedCaseId, regions);
                    if (message.trim().length()>0){
                        message = "Add the region " + message + " to Case (" +
                        selectedCase + ")? \n Note: if you don't add the region, the copy will be canceled. ";

                        int selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                        if (selection == JOptionPane.YES_OPTION) 
                            presenter.copyInput(selectedCaseId, inputs);
                        return; 
                    }
                    presenter.copyInput(selectedCaseId, inputs);
                    return; 
                }
                presenter.copyInput(selectedCaseId, inputs); 
            }
        }
    }

    private void checkModelToRun() throws EmfException {
        if (caseObj.getModel() == null || caseObj.getModel().getId() == 0)
            throw new EmfException("Please specify model to run on summary tab. ");
    }
    
    private int getCaseId(String selectedCase) {
        int index1 = selectedCase.indexOf("(") + 1;
        int index2 = selectedCase.indexOf(")");

        return Integer.parseInt(selectedCase.substring(index1, index2));
    }
    
    private void doDisplayInputDatasetsPropertiesViewer() {
        List<EmfDataset> datasets = getSelectedDatasets(getSelectedInputs());
        if (datasets.isEmpty()) {
            messagePanel.setMessage("Please select one or more inputs with datasets specified to view.");
            return;
        }
        for (Iterator<EmfDataset> iter = datasets.iterator(); iter.hasNext();) {
            DatasetPropertiesViewer view = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
            EmfDataset dataset = iter.next();
            try {
                presenter.doDisplayPropertiesView(view, dataset);
            } catch (EmfException e) {
                messagePanel.setError(e.getMessage());
            }
        }
    }

    private void doExportInputDatasets(List inputlist) {
        if (inputlist.size() == 0) {
            messagePanel.setMessage("Please select input(s) to export.");
            return;
        }

        int numberToExport = checkToWriteStartMessage(inputlist);

        try {
            if (!checkExportDir(inputDir.getText()) || !checkDatasets(inputlist) || numberToExport < 1)
                return;

            int ok = checkOverWrite();
            String purpose = "Used by case: " + this.caseObj.getName() + ".";

            if (ok != JOptionPane.YES_OPTION) {
                presenter.exportCaseInputs(inputlist, purpose);
            } else {
                presenter.exportCaseInputsWithOverwrite(inputlist, purpose);
            }
            messagePanel.setMessage("Started export of " + numberToExport
                    + " input datasets.  Please see the Status Window for additional information.");
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private boolean checkExportDir(String exportDir) throws EmfException {
        if (exportDir == null || exportDir.equals("")) {
            messagePanel
                    .setMessage("Please specify/save the input folder through case editor before exporting the case inputs.");
            return false;
        }

        if (exportDir.contains("/home/") || exportDir.endsWith("/home")) {
            throw new EmfException("Export data into user's home directory is not allowed.");
        }

        return true;
    }

    private boolean checkDatasets(List inputList) {
        CaseInput[] inputs = (CaseInput[]) inputList.toArray(new CaseInput[0]);

        for (int i = 0; i < inputs.length; i++)
            if (inputs[i].isRequired() && inputs[i].getDataset() == null) {
                messagePanel.setMessage("Please specify a dataset for the required input \"" + inputs[i].getName()
                        + "\".");
                return false;
            }

        return true;
    }

    // returns the number of datasets that will actually be exported
    private int checkToWriteStartMessage(List inputList) {
        CaseInput[] inputs = (CaseInput[]) inputList.toArray(new CaseInput[0]);
        int count = 0;

        for (int i = 0; i < inputs.length; i++) {
            DatasetType type = inputs[i].getDatasetType();
            EmfDataset dataset = inputs[i].getDataset();
            if (type != null && dataset != null)
                count++;
        }

        if (count == 0)
            messagePanel.setMessage("Please make sure the selected inputs have datasets in them).");

        return count;
    }

    private void viewCasesReleatedToDataset() {
        List<CaseInput> inputlist = getSelectedInputs();
        if (inputlist == null || inputlist.size() != 1) {
            messagePanel.setMessage("Please select one input. ");
            return;
        }

        EmfDataset dataset = inputlist.get(0).getDataset();
        if (dataset == null) {
            messagePanel.setMessage("No dataset available. ");
            return;
        }

        try {
            Case[] casesByInputDataset = presenter.getCasesByInputDataset(dataset.getId());
            Case[] casesByOutputDataset = presenter.getCasesByOutputDatasets(new int[] { dataset.getId() });
            String title = "Find Uses of Dataset: " + dataset.getName();
            RelatedCaseView view = new FindCaseWindow(title, session, parentConsole, desktopManager);
            presenter.doViewRelated(view, casesByOutputDataset, casesByInputDataset);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }

    }

    private int checkOverWrite() {
        // FIXME: Temporal setting till gets back from Marc on this policy 11/09/2007 Qun
        return JOptionPane.YES_OPTION;
    }

    private List<EmfDataset> getSelectedDatasets(List inputlist) {
        List<EmfDataset> datasetList = new ArrayList<EmfDataset>();

        for (int i = 0; i < inputlist.size(); i++) {
            EmfDataset dataset = ((CaseInput) inputlist.get(i)).getDataset();
            if (dataset != null)
                datasetList.add(dataset);
        }

        return datasetList;
    }

    private CaseInput[] listFreshInputs() throws EmfException {
        this.selectedSector = getSelectedSector();
        CaseInput[] freshList = presenter.getCaseInput(caseId, selectedSector, nameContains(), showAll.isSelected());

        if (selectedSector == null && freshList.length == presenter.getPageSize())
            setMessage("Please select a sector to see full list of inputs.");
        else
            messagePanel.clear();

        return freshList;
    }
    
    public String nameContains() {
        return envVarContains == null ? "" : envVarContains.getText().trim();
    }

    public Sector getSelectedSector() {
        return (Sector) sectorsComboBox.getSelectedItem();
    }
    
    public Boolean isShowAll(){
        return showAll.isSelected();
    }

    public void setMessage(String message) {
        messagePanel.setMessage(message);
    }

    public void setErrorMessage(String message) {
        messagePanel.setError(message);
    }

    public void addInput(CaseInput note) {
        tableData.add(note);
        table.refresh(tableData);
        panelRefresh();
    }

    private List<CaseInput> getSelectedInputs() {
        return (List<CaseInput>)table.selected();
    }

    public void refresh(CaseInput[] caseInputs) {
        // note that this will get called when the case is save
        try {
            if (caseInputs != null) // it's still null if you've never displayed this tab
                doRefresh(caseInputs);
        } catch (Exception e) {
            setErrorMessage("Cannot refresh current tab. " + e.getMessage());
        }
    }

    public int numberOfRecord() {
        return tableData.sources().length;
    }

    public void clearMessage() {
        messagePanel.clear();
    }

    public void doRefresh() throws EmfException {
        try {
            new RefreshSwingWorkerTasks(layout, messagePanel, presenter).execute();
            clearMessage();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

}
