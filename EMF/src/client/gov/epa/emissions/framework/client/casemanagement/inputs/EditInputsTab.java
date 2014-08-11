package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
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
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.data.EmfDataset;
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
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;

public class EditInputsTab extends JPanel implements EditInputsTabView, RefreshObserver {

    protected EmfConsole parentConsole;

    protected EditInputsTabPresenter presenter;

    protected Case caseObj;

    protected int caseId;

    protected Sector selectedSector;

    protected InputsTableData tableData;

    protected JPanel mainPanel;

    protected SelectableSortFilterWrapper table;

    protected MessagePanel messagePanel;

    protected DesktopManager desktopManager;

    protected TextField inputDir;
    
    protected TextField envVarContains;

    protected ComboBox sectorsComboBox;

    protected JCheckBox showAll;

    protected EmfSession session;

    private ManageChangeables changeables;
    protected JPanel layout;

    public EditInputsTab(EmfConsole parentConsole, ManageChangeables changeables, MessagePanel messagePanel,
            DesktopManager desktopManager) {
        this(parentConsole, messagePanel, desktopManager);
        super.setName("editInputsTab");
        this.changeables = changeables;
        super.setLayout(new BorderLayout());
    }
    
    public EditInputsTab(EmfConsole parentConsole, MessagePanel messagePanel,
            DesktopManager desktopManager) {      
        this.parentConsole = parentConsole;
        this.messagePanel = messagePanel;
        this.desktopManager = desktopManager;       
    }
    
    public void doDisplay(EditInputsTabPresenter presenter){
        this.presenter = presenter;
        new SwingWorkerTasks(this, presenter).execute();
    }
    

    public void display(EmfSession session, Case caseObj) {
        super.removeAll();
        this.caseObj = caseObj;
        this.caseId = caseObj.getId();
        this.session = session;
        this.inputDir = new TextField("inputdir", 50);
        inputDir.setText(caseObj.getInputFileDir());
        this.changeables.addChangeable(inputDir);
        super.add(createLayout(new CaseInput[0], presenter, parentConsole), BorderLayout.CENTER);
        messagePanel.setMessage("Please select a sector to retrieve case inputs.");
    }
 

    protected void doRefresh(CaseInput[] inputs) throws Exception {
        String inputFileDir = caseObj.getInputFileDir();

        if (!inputDir.getText().equalsIgnoreCase(inputFileDir))
            inputDir.setText(inputFileDir);

//        sectorsComboBox.resetModel(presenter.getAllSetcors());
//        sectorsComboBox.setSelectedItem(this.selectedSector);
        setupTableModel(inputs);
        table.refresh(tableData);
        panelRefresh();
    }

    protected void panelRefresh() {
        mainPanel.removeAll();
        mainPanel.add(table);
        super.validate();
    }

    protected void setupTableModel(CaseInput[] inputs) {
        tableData = new InputsTableData(inputs, session);
    }

    private JPanel createLayout(CaseInput[] inputs, EditInputsTabPresenter presenter, EmfConsole parentConsole){
        layout = new JPanel(new BorderLayout());

        layout.add(createFolderNSectorPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(inputs, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(presenter), BorderLayout.PAGE_END);

        return layout;
    }

    protected JPanel tablePanel(CaseInput[] inputs, EmfConsole parentConsole) {
        setupTableModel(inputs);

        mainPanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());
        mainPanel.add(table);

        return mainPanel;
    }

    protected SortCriteria sortCriteria() {
        String[] columnNames = { "Envt. Var.", "Sector", "Input", "Job" };
        return new SortCriteria(columnNames, new boolean[] { true, true, true, true }, new boolean[] { false, false,
                false, false });
    }

    protected JPanel createFolderNSectorPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Input Folder:", getFolderChooserPanel(inputDir,
                "Select the base Input Folder for the Case"), panel);

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

    protected AbstractAction filterAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    new RefreshSwingWorkerTasks(layout, messagePanel, presenter).execute();
                    messagePanel.clear();
                } catch (Exception exc) {
                    setErrorMessage(exc.getMessage());
                }
            }
        };
    }
    
    protected AbstractAction filterActionByName() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    if (envVarContains.getText() == null || envVarContains.getText().trim().isEmpty())
                        return;
                    
                    if (sectorsComboBox != null)
                        sectorsComboBox.setSelectedItem(new Sector("All", "All"));
                    
                    new RefreshSwingWorkerTasks(layout, messagePanel, presenter).execute();
                    messagePanel.clear();
                } catch (Exception exc) {
                    setErrorMessage(exc.getMessage());
                }
            }
        };
    }

    protected JPanel getFolderChooserPanel(final JTextField dir, final String title) {
        Button browseButton = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                clearMessage();
                selectFolder(dir, title);
            }
        });
        JPanel folderPanel = new JPanel(new BorderLayout(2, 0));
        folderPanel.add(dir, BorderLayout.LINE_START);
        folderPanel.add(browseButton, BorderLayout.LINE_END);

        return folderPanel;
    }

    protected void selectFolder(JTextField dir, String title) {
        EmfFileInfo initDir = new EmfFileInfo(dir.getText(), true, true);
        EmfFileChooser chooser = new EmfFileChooser(initDir, new EmfFileSystemView(session.dataCommonsService()));
        chooser.setTitle(title);
        int option = chooser.showDialog(parentConsole, "Select a folder");

        EmfFileInfo file = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedDir() : null;
        if (file == null)
            return;

        if (file.isDirectory()) {
            caseObj.setInputFileDir(file.getAbsolutePath());
            dir.setText(file.getAbsolutePath());
        }
    }

    private JPanel controlPanel(final EditInputsTabPresenter presenter) {
        Insets insets = new Insets(1, 2, 1, 2);
        JPanel container = new JPanel();

        Button add = new AddButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                try {
                    doNewInput(presenter);
                } catch (EmfException e1) {
                    messagePanel.setError(e1.getMessage());
                }
            }
        });
        add.setMargin(insets);
        container.add(add);

        Button remove = new RemoveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                try {
                    doRemove(presenter);
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

        Button copy = new Button("Copy", copyAction(presenter));
        copy.setMargin(insets);
        container.add(copy);

        Button view = new ViewButton("View Dataset", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doDisplayInputDatasetsPropertiesViewer();
            }
        });
        view.setMargin(insets);
        container.add(view);

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
                    new RefreshSwingWorkerTasks(layout, messagePanel, presenter).execute();
                    clearMessage();
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

    protected Action copyAction(final EditInputsTabPresenter localPresenter) {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    clearMessage();
                    checkModelToRun();
                    copyInputs(localPresenter);
                } catch (Exception ex) {
                    messagePanel.setError(ex.getMessage());
                    //ex.printStackTrace();
                }
            }
        };
    }

    private Action editAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clearMessage();
                    doEditInput(presenter);
                } catch (EmfException ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        };
        return action;
    }

    protected void doNewInput(EditInputsTabPresenter presenter) throws EmfException {
        checkModelToRun();
        NewInputDialog view = new NewInputDialog(parentConsole);
        try {
            CaseInput newInput = new CaseInput();
            newInput.setCaseID(caseId);
            newInput.setRequired(true);
            newInput.setLocal(true);
            presenter.addNewInputDialog(view, newInput);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    protected void doRemove(EditInputsTabPresenter presenter) throws EmfException {
        CaseInput[] inputs = getSelectedInputs().toArray(new CaseInput[0]);

        if (inputs.length == 0) {
            messagePanel.setMessage("Please select input(s) to remove.");
            return;
        }

        String title = "Warning";
        String message = "Are you sure you want to remove the selected input(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            tableData.remove(inputs);
            refresh(tableData.sources());
            try {
                presenter.removeInputs(inputs);
                setMessage(" Click Refresh to see case updates.");
            }catch (EmfException e){
                //e.printStackTrace();
                throw new EmfException(e.getMessage());
            }
        }
    }

    private void doEditInput(EditInputsTabPresenter presenter) throws EmfException {
        checkModelToRun();

        List<CaseInput> inputs = getSelectedInputs();

        if (inputs.size() == 0) {
            messagePanel.setMessage("Please select input(s) to edit.");
            return;
        }

        for (Iterator<CaseInput> iter = inputs.iterator(); iter.hasNext();) {
            CaseInput input = iter.next();
            String title = "Edit Case Input: " + input.getName() + "(" + input.getId() + ")(" + caseObj.getName() + ")";
            EditCaseInputView inputEditor = new EditCaseInputWindow(title, desktopManager, parentConsole);
            presenter.doEditInput(input, inputEditor);
        }
    }

    private void checkModelToRun() throws EmfException {
        if (caseObj.getModel() == null || caseObj.getModel().getId() == 0)
            throw new EmfException("Please specify model to run on summary tab. ");
    }

    protected void copyInputs(EditInputsTabPresenter presenter) throws Exception {
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
                return;
            }
            
            showEditor(presenter, inputs, selectedCase);
        }
    }

    private void showEditor(EditInputsTabPresenter presenter, List<CaseInput> inputs, String selectedCase)
            throws Exception {
        for (Iterator<CaseInput> iter = inputs.iterator(); iter.hasNext();) {
            CaseInput input = iter.next();
            NewInputDialog view = new NewInputDialog(parentConsole);
            view.setModal(false);
            view.setLocationByPlatform(true);
            presenter.copyInput(input, view);
        }
    }

    protected void doDisplayInputDatasetsPropertiesViewer() {
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
                //e.printStackTrace();
            }
        }
    }

    protected void doExportInputDatasets(List inputlist) {
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

    protected boolean checkExportDir(String exportDir) throws EmfException {
        if (exportDir == null || exportDir.equals("")) {
            messagePanel.setMessage("Please specify the input folder before exporting the case inputs.");
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

    private int checkOverWrite() {
        // String title = "Message";
        // String message = "Would you like to remove previously exported files prior to export?";
        // return JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
        // JOptionPane.QUESTION_MESSAGE);
        // FIXME: Temporal setting till gets back from Marc on this policy 11/09/2007 Qun
        return JOptionPane.YES_OPTION;
    }

    protected List<EmfDataset> getSelectedDatasets(List inputlist) {
        List<EmfDataset> datasetList = new ArrayList<EmfDataset>();

        for (int i = 0; i < inputlist.size(); i++) {
            EmfDataset dataset = ((CaseInput) inputlist.get(i)).getDataset();
            if (dataset != null)
                datasetList.add(dataset);
        }

        return datasetList;
    }

    public void addInput(CaseInput input) {
        tableData.add(input);
        setMessage("Added \"" + input.getName() + "\".  Click Refresh to see it in the table.");
    }

    protected List<CaseInput> getSelectedInputs() {
        return (List<CaseInput>) table.selected();
    }

    public Sector getSelectedSector() {
        this.selectedSector = (Sector) sectorsComboBox.getSelectedItem();
        if ( selectedSector == null )
            setMessage("Please select a sector to see list of inputs.");        
        return this.selectedSector;     
    }

    public CaseInput[] caseInputs() {
        return tableData.sources();
    }

    public String getCaseInputFileDir() {
        if (inputDir == null)
            return null;
        return inputDir.getText();
    }

    private int getCaseId(String selectedCase) {
        int index1 = selectedCase.indexOf("(") + 1;
        int index2 = selectedCase.indexOf(")");

        return Integer.parseInt(selectedCase.substring(index1, index2));
    }

    
    public Boolean isShowAll(){
        return showAll.isSelected();
    }

    public String nameContains() {
        return envVarContains == null ? "" : envVarContains.getText().trim();
    }

    public void refresh(CaseInput[] caseInputs) {
        // note that this will get called when the case is save
        try {
            if (caseInputs != null) // it's still null if you've never displayed this tab
                doRefresh(caseInputs);
        } catch (Exception e) {
            setErrorMessage("Cannot refresh inputs tab. " + e.getMessage());
        }
    }

    public int numberOfRecord() {
        return tableData.sources().length;
    }

    public void clearMessage() {
        messagePanel.clear();
    }

    public void setErrorMessage(String message) {
        messagePanel.setError(message);
    }

    public void setMessage(String message) {
        messagePanel.setMessage(message);
    }

    public void doRefresh() throws EmfException {
        try {
            new RefreshSwingWorkerTasks(layout, messagePanel, presenter).execute();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    protected void viewCasesReleatedToDataset() {
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
}
