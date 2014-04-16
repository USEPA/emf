package gov.epa.emissions.framework.client.casemanagement.parameters;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.casemanagement.CaseSelectionDialog;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
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

public class ViewableParametersTab extends JPanel implements RefreshObserver {

    private EmfConsole parentConsole;

    private ViewableParametersTabPresenterImpl presenter;

    private Case caseObj;

    private int caseId;

    private ParametersTableData tableData;

    private SelectableSortFilterWrapper table;

    private JPanel tablePanel;
    
    private ComboBox sectorsComboBox;

    private JCheckBox showAll;

    private MessagePanel messagePanel;

    private DesktopManager desktopManager;

    private EmfSession session;

    private TextField envVarContains;

    public ViewableParametersTab(EmfConsole parentConsole, MessagePanel messagePanel, DesktopManager desktopManager) {
        super.setName("viewParametersTab");
        this.parentConsole = parentConsole;
        this.messagePanel = messagePanel;
        this.desktopManager = desktopManager;
        super.setLayout(new BorderLayout());
    }

    public void display(EmfSession session, Case caseObj, ViewableParametersTabPresenterImpl presenter) {
        super.removeAll();

        this.caseObj = caseObj;
        this.caseId = caseObj.getId();
        this.presenter = presenter;
        this.session = session;

        try {
            super.add(createLayout(new CaseParameter[0], parentConsole), BorderLayout.CENTER);
        } catch (Exception e) {
            messagePanel.setError("Cannot retrieve all case parameters.");
        }

        kickPopulateThread();
    }

    private void kickPopulateThread() {
        Thread populateThread = new Thread(new Runnable() {
            public void run() {
                retrieveParams();
            }
        });

        populateThread.start();
    }

    public synchronized void retrieveParams() {
        try {
            messagePanel.setMessage("Please wait while retrieving all case parameters...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            doRefresh(listFreshParameters());
        } catch (Exception e) {
            messagePanel.setError("Cannot retrieve all case parameters: " + e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void doRefresh(CaseParameter[] params) throws Exception {
        //super.removeAll();
        setupTableModel(params);
        table.refresh(tableData);
        panelRefresh();
    }
    
    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.validate();
    }

    private JPanel createLayout(CaseParameter[] params, EmfConsole parentConsole)
            throws Exception {
        final JPanel layout = new JPanel(new BorderLayout());

        layout.add(createSectorPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(params, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(), BorderLayout.PAGE_END);

        return layout;
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
                    doRefresh(listFreshParameters());
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
                    
                    doRefresh(listFreshParameters());
                } catch (Exception exc) {
                    setErrorMessage(exc.getMessage());
                }
            }
        };
    }
    
    private JPanel tablePanel(CaseParameter[] params, EmfConsole parentConsole) {
        setupTableModel(params);
        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());
        tablePanel.add(table, BorderLayout.CENTER);

        return tablePanel;
    }
    
    private void setupTableModel(CaseParameter[] params){
        tableData = new ParametersTableData(params, session);
    }


    private SortCriteria sortCriteria() {
        String[] columnNames = { "Order", "Envt. Var.", "Sector", "Parameter", "Job" };
        return new SortCriteria(columnNames, new boolean[] { true, true, true, true, true }, new boolean[] { false, false,
                false, false, false });
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

        showAll = new JCheckBox("Show All", false);
        showAll.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                try {
                    doRefresh(listFreshParameters());
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

    private Action viewAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clearMessage();
                    viewParameter();
                } catch (EmfException ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        };
        return action; 
    }

    private void viewParameter() throws EmfException {
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
            parameterEditor.viewOnly(title);
        }
    }

    private Action copyAction(final ViewableParametersTabPresenterImpl localPresenter) {
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
    
    private void checkModelToRun() throws EmfException{
        if (caseObj.getModel() == null ||caseObj.getModel().getId() == 0 )
            throw new EmfException("Please specify model to run on summary tab. ");
    }
    
    private void copyParameters(ViewableParametersTabPresenterImpl presenter) throws Exception {
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
                                JOptionPane.QUESTION_MESSAGE);
                        if (selection == JOptionPane.YES_OPTION) 
                            presenter.copyParameter(selectedCaseId, params);
                        return;
                    }
                    presenter.copyParameter(selectedCaseId, params);
                    return;
                }
                presenter.copyParameter(selectedCaseId, params);
            }
        }
    }
    
    private int getCaseId(String selectedCase) {
        int index1 = selectedCase.indexOf("(") + 1;
        int index2 = selectedCase.indexOf(")");

        return Integer.parseInt(selectedCase.substring(index1, index2));
    }
    
    private List getSelectedParameters() {
        return table.selected();
    }

    public CaseParameter[] caseParameters() {
        return tableData.sources();
    }


    public int numberOfRecord() {
        return tableData.sources().length;
    }

    public void clearMessage() {
        messagePanel.clear();
    }

    private void setErrorMessage(String msg) {
        messagePanel.setError(msg);
    }
    
    private void setMessage(String msg) {
        messagePanel.setMessage(msg);
    }
    
    private CaseParameter[] listFreshParameters() throws EmfException {
        CaseParameter[] freshList = presenter.getCaseParameters(caseId, getSelectedSector(), nameContains(), showAll.isSelected());
        
        if (getSelectedSector() == null && freshList.length == presenter.getPageSize())
            setMessage("Please select a sector to see full list of parameters.");
        else
            messagePanel.clear();
        
        return freshList;
    }
    
    private String nameContains() {
        return envVarContains == null ? "" : envVarContains.getText().trim();
    }

    private Sector getSelectedSector() {
        return (Sector) sectorsComboBox.getSelectedItem();
    }

    public void doRefresh() throws EmfException {
        try {
            kickPopulateThread();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

}
