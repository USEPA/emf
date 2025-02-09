package gov.epa.emissions.framework.client.casemanagement.parameters;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.casemanagement.CaseSelectionDialog;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.swingworker.RefreshSwingWorkerTasks;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
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

public class EditParametersTab extends JPanel implements EditCaseParametersTabView, RefreshObserver {

    protected EmfConsole parentConsole;

    private EditParametersTabPresenter presenter;

    protected Case caseObj;

    protected int caseId;

    protected ParametersTableData tableData;

    protected SelectableSortFilterWrapper table;

    protected JPanel tablePanel;
    
    protected JPanel layout;

    protected ComboBox sectorsComboBox;

    protected JCheckBox showAll;

    protected MessagePanel messagePanel;

    protected DesktopManager desktopManager;

    protected EmfSession session;

    protected Sector selectedSector;

    protected TextField envVarContains;

    public EditParametersTab(EmfConsole parentConsole, MessagePanel messagePanel, DesktopManager desktopManager) {
        super.setName("editParametersTab");
        this.parentConsole = parentConsole;
        this.messagePanel = messagePanel;
        this.desktopManager = desktopManager;
    }

    public void display(EmfSession session, Case caseObj, EditParametersTabPresenter presenter) {
        super.setLayout(new BorderLayout());
        super.removeAll();

        this.caseObj = caseObj;
        this.caseId = caseObj.getId();
        this.presenter = presenter;
        this.session = session;
        
        super.add(createLayout(new CaseParameter[0], presenter, parentConsole), BorderLayout.CENTER);
        setMessage("Please select a sector to see list of parameters.");  
    }

 
    protected void doRefresh(CaseParameter[] params) throws Exception {
        setupTableModel(params);
        table.refresh(tableData);
        panelRefresh();
    }

    private JPanel createLayout(CaseParameter[] params, EditParametersTabPresenter presenter, 
            EmfConsole parentConsole)  {
        layout = new JPanel(new BorderLayout());
        layout.add(createSectorPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(params, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(presenter), BorderLayout.PAGE_END);

        return layout;
    }

    protected JPanel tablePanel(CaseParameter[] params, EmfConsole parentConsole) {
        setupTableModel(params);
        tablePanel = new JPanel(new BorderLayout());

        table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());
        tablePanel.add(table, BorderLayout.CENTER);

        return tablePanel;
    }

    protected void setupTableModel(CaseParameter[] params) {
        tableData = new ParametersTableData(params, session);
    }

    protected SortCriteria sortCriteria() {
        String[] columnNames = { "Order", "Envt. Var.", "Sector", "Parameter", "Job" };
        return new SortCriteria(columnNames, new boolean[] { true, true, true, true, true }, new boolean[] { false, false,
                false, false, false });
    }

    private JPanel createSectorPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
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
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
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

    private JPanel controlPanel(final EditParametersTabPresenter presenter) {
        Insets insets = new Insets(1, 2, 1, 2);
        JPanel container = new JPanel();
        
        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        Button add = new AddButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                doNewInput(presenter);
            }
        });
        add.setMargin(insets);
        container.add(add);

        Button remove = new RemoveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                try {
                    removeParameter(presenter);
                } catch (EmfException exc) {
                    setErrorMessage(exc.getMessage());
                }
            }
        });
        remove.setMargin(insets);
        container.add(remove);

        SelectAwareButton edit = new SelectAwareButton("Edit", editAction(), table, confirmDialog);
        //edit.setMnemonic(KeyEvent.VK_I);
        edit.setMargin(insets);
        container.add(edit);

        Button copy = new Button("Copy", copyAction(presenter));
        copy.setMnemonic(KeyEvent.VK_C);
        copy.setMargin(insets);
        container.add(copy);

        showAll = new JCheckBox("Show All", false);
        showAll.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                try {
                    new RefreshSwingWorkerTasks(layout, messagePanel, presenter).execute();
                } catch (Exception e1) {
                    setErrorMessage(e1.getMessage());
                }
            }
        });
        container.add(showAll);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }

    private Action editAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clearMessage();
                    checkModelToRun();
                    editParameter(presenter);
                } catch (EmfException ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        };
        return action; 
    }



    protected Action copyAction(final EditParametersTabPresenter  localPresenter) {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    clearMessage();
                    checkModelToRun();
                    copyParameters(localPresenter);
                } catch (Exception ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        };
    }

    protected void doNewInput(EditParametersTabPresenter presenter) {
        try {
            checkModelToRun();
            NewCaseParameterDialog view = new NewCaseParameterDialog(parentConsole);
            CaseParameter newParameter = new CaseParameter();
            newParameter.setCaseID(caseId);
            newParameter.setLocal(true);
            newParameter.setRequired(true);
            presenter.addNewParameterDialog(view, newParameter);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }
    
    private void checkModelToRun() throws EmfException{
        if (caseObj.getModel() == null ||caseObj.getModel().getId() == 0 )
            throw new EmfException("Please specify model to run on summary tab. ");
    }
    
    protected void removeParameter(EditParametersTabPresenter presenter) throws EmfException {
        CaseParameter[] params = getSelectedParameters().toArray(new CaseParameter[0]);

        if (params.length == 0) {
            messagePanel.setMessage("Please select parameter(s) to remove.");
            return;
        }

        String title = "Warning";
        String message = "Are you sure you want to remove the selected parameter(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            tableData.remove(params);
            refresh(tableData.sources());
            presenter.removeParameters(params);
            //messagePanel.setMessage("Click Refresh to see case updates.");
        }
    }

    private void editParameter(EditParametersTabPresenter presenter) throws EmfException {
        List params = getSelectedParameters();

        if (params.size() == 0) {
            messagePanel.setMessage("Please select parameter(s) to edit.");
            return;
        }

        for (Iterator iter = params.iterator(); iter.hasNext();) {
            CaseParameter param = (CaseParameter) iter.next();
            String title = param.getName() + "(" + param.getId() + ")(" + caseObj.getName() + ")";
            EditCaseParameterView parameterEditor = new EditCaseParameterWindow(title, desktopManager);
            presenter.editParameter(param, parameterEditor);
        }
    }

    private void copyParameters(EditParametersTabPresenter presenter) throws Exception {
        List<CaseParameter> params = getSelectedParameters();

        if (params.size() == 0) {
            messagePanel.setMessage("Please select parameter(s) to copy.");
            return;
        }

        String[] caseIds = (String[]) presenter.getAllCaseNameIDs();      
        CaseSelectionDialog view = new CaseSelectionDialog(parentConsole, caseIds);
        String title = "Copy " + params.size()+" case parameter(s) to case: ";
        
        view.display(title, true);
        
        if (view.shouldCopy()){
            String selectedCase=view.getCases()[0];
            int selectedCaseId = getCaseId(selectedCase);
            if (selectedCaseId != this.caseId) {
                GeoRegion[] regions = presenter.getGeoregion(params);
                if (regions.length >0 ) {
                    String message = presenter.isGeoRegionInSummary(selectedCaseId, regions);
                    if (message.trim().length()>0){
                        message = "Add the region " + message + " to Case (" +
                        selectedCase + ")? \n Note: if you don't add the region, the copy will be canceled. ";

                        int selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                        if (selection == JOptionPane.YES_OPTION) 
                            presenter.copyParameter(selectedCaseId, params);
                        return;
                    }
                    presenter.copyParameter(selectedCaseId, params);
                    return;
                }
                presenter.copyParameter(selectedCaseId, params);
                return;
            }
            showEditor(presenter, params, selectedCase);
        }
    }

    private void showEditor(EditParametersTabPresenter presenter, List<CaseParameter> params, String selectedCase)
            throws Exception {
        for (Iterator<CaseParameter> iter = params.iterator(); iter.hasNext();) {
            CaseParameter param = iter.next();
            NewCaseParameterDialog view = new NewCaseParameterDialog(parentConsole);
            view.setModal(false);
            view.setLocationByPlatform(true);
            presenter.copyParameter(view, param);
        }
    }

    public void addParameter(CaseParameter param) {
        tableData.add(param);
        setMessage("Added \"" + param.getName() + "\".  Click Refresh to see it in the table.");
    }

    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.validate();
    }

    protected List<CaseParameter> getSelectedParameters() {
        return (List<CaseParameter>) table.selected();
    }

//    public CaseParameter[] caseParameters() {
//        return tableData.sources();
//    }

    protected int getCaseId(String selectedCase) {
        int index1 = selectedCase.indexOf("(") + 1;
        int index2 = selectedCase.indexOf(")");

        return Integer.parseInt(selectedCase.substring(index1, index2));
    }


    public void refresh(CaseParameter[] caseParas) {
        // note that this will get called when the case is save
        try {
            if (caseParas != null) // it's still null if you've never displayed this tab
                doRefresh(caseParas);
        } catch (Exception e) {
            messagePanel.setError("Cannot refresh parameters tab. " + e.getMessage());
        }
    }

    public int numberOfRecord() {
        return tableData.sources().length;
    }

    public void clearMessage() {
        messagePanel.clear();
    }

    protected void setErrorMessage(String msg) {
        messagePanel.setError(msg);
    }
    
    public void setMessage(String msg) {
        messagePanel.setMessage(msg);
    }
    
    
    public Sector getSelectedSector(){       
        this.selectedSector = (Sector) sectorsComboBox.getSelectedItem();
        if ( selectedSector == null )
            setMessage("Please select a sector to see list of parameters.");        
        return this.selectedSector;
    }
    
    public String nameContains() {
        return envVarContains == null ? "" : envVarContains.getText().trim();
    }

    public void doRefresh() throws EmfException {
        try {
            new RefreshSwingWorkerTasks(layout, messagePanel, presenter).execute();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    @Override
    public Boolean isShowAll() {
        return showAll.isSelected();
    }
}
