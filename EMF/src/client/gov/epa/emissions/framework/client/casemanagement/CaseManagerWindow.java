package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.CopyButton;
import gov.epa.emissions.commons.gui.buttons.ImportButton;
import gov.epa.emissions.commons.gui.buttons.NewButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditor;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseViewer;
import gov.epa.emissions.framework.client.casemanagement.sensitivity.SensitivityWindow;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlstrategy.AnalysisEngineTableApp;
import gov.epa.emissions.framework.client.swingworker.EditSwingWorkerTasks;
import gov.epa.emissions.framework.client.swingworker.SwingWorkerTasks;
import gov.epa.emissions.framework.client.swingworker.ViewSwingWorkerTasks;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;
import gov.epa.emissions.framework.client.swingworker.RefreshSwingWorkerTasks;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class CaseManagerWindow extends DisposableInteralFrame implements CaseManagerView, RefreshObserver {

    private CaseManagerPresenter presenter;

    private JPanel mainPanel;

    private CasesTableData tableData;

    private SelectableSortFilterWrapper table;

    private JPanel layout;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private List<Case> cases;
    
    private TextField nameFilter;

    private List<CaseCategory> categories = new ArrayList<CaseCategory>();

    private CaseCategory selectedCategory;

    private EmfSession session;
    
    //boolean isAdmin; 

    private ComboBox categoriesBox;

    public CaseManagerWindow(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager) {
        super("Case Manager", new Dimension(900, 400), desktopManager);

        this.session = session;
        this.parentConsole = parentConsole;
        //this.isAdmin = session.user().isAdmin();
        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void observe(CaseManagerPresenter presenter) {
        this.presenter = presenter;
    }
    
    public void display(){
        new SwingWorkerTasks(layout, presenter).execute();
    }

    public void display(CaseCategory[] catetories){
        this.categories = Arrays.asList(catetories);
        doLayout(new Case[0]);
        super.display();
    }

    public void refresh(Case[] cases) {
        setupTableModel(cases);
        table.refresh(tableData);
        panelRefresh();
        categoriesBox.requestFocusInWindow();
    }

    private void panelRefresh() {
        mainPanel.removeAll();
        mainPanel.add(table);
        super.refreshLayout();
    }

    public void refreshWithLastCategory() throws EmfException {
        doLayout(presenter.getCases(selectedCategory, nameFilter.getText()));
        super.refreshLayout();
    }

    private void doLayout(Case[] cases) {
        messagePanel = new SingleLineMessagePanel();
        createCategoriesComboBox();
        setupTableModel(cases);
        createLayout(layout, mainPanel(parentConsole));
        super.refreshLayout();
    }

    private void setupTableModel(Case[] cases) {
        tableData = new CasesTableData(cases);
    }

    private JPanel mainPanel(EmfConsole parentConsole) {
        this.mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());
        table.getTable().getAccessibleContext().setAccessibleName("List of cases");
        mainPanel.add(table);

        return mainPanel;
    }

    private SortCriteria sortCriteria() {
        String[] columnNames = { "Last Modified Date" };
        return new SortCriteria(columnNames, new boolean[] { false }, new boolean[] { false });
    }

    private void createCategoriesComboBox() { 
        categoriesBox = new ComboBox("Select one", categories.toArray(new CaseCategory[0]), "Select one category to filter the cases on");
        categoriesBox.setPreferredSize(new Dimension(360, 20));
        
        if (selectedCategory != null)
            categoriesBox.setSelectedItem(selectedCategory);

        categoriesBox.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                new RefreshSwingWorkerTasks(layout, messagePanel, presenter).execute();
            }
        });
    }

    public CaseCategory getSelectedCategory() {
        this.selectedCategory = (CaseCategory) categoriesBox.getSelectedItem();
        return selectedCategory;
    }
    
    public String getNameFilter(){
        return nameFilter.getText();
    }

    public void setSelectedCategory() {
        categoriesBox.setSelectedIndex(1);
    }

    private void createLayout(JPanel layout, JPanel table) {
        layout.removeAll();
        layout.setLayout(new BorderLayout());

        layout.add(createTopPanel(), BorderLayout.NORTH);
        layout.add(table, BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel msgRefreshPanel = new JPanel(new BorderLayout());
        msgRefreshPanel.add(messagePanel, BorderLayout.CENTER);
        Button button = new RefreshButton(this, "Refresh Cases along with lists of sectors and other items", 
                messagePanel);
        msgRefreshPanel.add(button, BorderLayout.EAST);

        JPanel topPanel = new JPanel(new BorderLayout());
        nameFilter = new TextField("textfilter", 10, "Name contains text filter");
        nameFilter.setEditable(true);
        nameFilter.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                new RefreshSwingWorkerTasks(layout, messagePanel, presenter).execute();
            }
        });
        
        JPanel advPanel = new JPanel(new BorderLayout(5, 2));
        JLabel jlabel = new JLabel("Name Contains:");
        jlabel.setHorizontalAlignment(JLabel.RIGHT);
        jlabel.setLabelFor(nameFilter);
        advPanel.add(jlabel, BorderLayout.WEST);
        advPanel.add(nameFilter, BorderLayout.CENTER);
        advPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 30));
        
        topPanel.add(getCategoryPanel("Show Cases of Category:", categoriesBox), BorderLayout.LINE_START);
        topPanel.add(advPanel, BorderLayout.EAST);
        
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(msgRefreshPanel);
        panel.add(topPanel);

        return panel;
    }

    private JPanel getCategoryPanel(String label, JComboBox box) {
        JPanel panel = new JPanel(new BorderLayout(5, 2));
        JLabel jlabel = new JLabel(label);
        jlabel.setHorizontalAlignment(JLabel.RIGHT);
        jlabel.setLabelFor(box);
        panel.add(jlabel, BorderLayout.WEST);
        panel.add(box, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(3, 50, 5, 5));

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel crudPanel = createCrudPanel();

        JPanel closePanel = new JPanel();
        Button importButton = new ImportButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                importCase();
            }
        });
        importButton.setToolTipText("Import a case");
        closePanel.add(importButton);
        
        Button closeButton = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        });
        closePanel.add(closeButton);
        getRootPane().setDefaultButton(closeButton);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        controlPanel.add(crudPanel, BorderLayout.WEST);
        controlPanel.add(closePanel, BorderLayout.EAST);

        return controlPanel;
    }

    private JPanel createCrudPanel() {
        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout());

        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        crudPanel.add(viewButton(confirmDialog));
        crudPanel.add(editButton(confirmDialog));

        Button newButton = new NewButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMsgPanel();
                createNewCase();
            }
        });
        crudPanel.add(newButton);

        Button removeButton = new RemoveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMsgPanel();
                removeSelectedCases();
            }
        });
        crudPanel.add(removeButton);
        if (!session.user().isAdmin())
            removeButton.setEnabled(false);

        Button copyButton = new CopyButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMsgPanel();
                copySelectedCases();
            }
        });
        crudPanel.add(copyButton);

        Button sensitivityButton = new Button("Sensitivity", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMsgPanel();
                sensitivityCase();
            }
        });
        sensitivityButton.setMnemonic(KeyEvent.VK_S);
        crudPanel.add(sensitivityButton);
        
        Button compareButton = new Button("Compare", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMsgPanel();
                try {
                    compareCases();
                } catch (EmfException e1) {
                    // NOTE Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });
        crudPanel.add(compareButton);
        compareButton.setMnemonic(KeyEvent.VK_O);
        
        Button qaButton = new Button("Compare Reports", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMsgPanel();
                try {
                    compareCasesOutputs();
                } catch (EmfException e1) {
                    // NOTE Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });
        crudPanel.add(qaButton);
        qaButton.setMnemonic(KeyEvent.VK_P);
        
        return crudPanel;
    }

    protected void compareCases() throws EmfException {
        cases = selected();

        if (cases.isEmpty()) {
            messagePanel.setMessage("Please select at least one case to compare");
            return;
        }

        int[] ids = new int[cases.size()];
        
        for (int i = 0; i < cases.size(); ++i) {
            ids[i] = cases.get(i).getId();
        }
        presenter.viewCaseComparisonResult(ids, "");
        presenter.viewCaseComparisonDatasetRevisionResult(ids, "");
    }
    
    protected void compareCasesOutputs() throws EmfException {
        cases = selected();

        if (cases.isEmpty()) {
            messagePanel.setMessage("Please select at least one case to compare");
            return;
        }
        
        int[] ids = new int[cases.size()];
        Case[] selectedCases = new Case[cases.size()];
        
        for (int i = 0; i < cases.size(); ++i) {
            ids[i] = cases.get(i).getId();
            selectedCases[i] = cases.get(i);
        }
        CompareCaseWindow view = new CompareCaseWindow(desktopManager, selectedCases, parentConsole, session );
        presenter.doQA(ids, view);
    }

    private SelectAwareButton editButton(ConfirmDialog confirmDialog) {
        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMsgPanel();
                cases = selected();
                if (cases.isEmpty()) {
                    messagePanel.setMessage("Please select one or more Cases to edit");
                    return;
                }
                new EditSwingWorkerTasks(layout,messagePanel, presenter).execute();
            }

        };
        SelectAwareButton editButton = new SelectAwareButton("Edit", editAction, table, confirmDialog);
        return editButton;
    }

    private SelectAwareButton viewButton(ConfirmDialog confirmDialog) {
        Action viewAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMsgPanel();
                cases = selected();
                if (cases.isEmpty()){ 
                    messagePanel.setMessage("Please select one or more Cases to view");
                    return;
                }
                new ViewSwingWorkerTasks(layout,messagePanel, presenter).execute();
            }
        };

        SelectAwareButton viewButton = new SelectAwareButton("View", viewAction, table, confirmDialog);
        return viewButton;
    }

    public Case[] getSCases(){
        return cases.toArray(new Case[0]);
    }
    
    public CaseViewer getCViewer(){
        return new CaseViewer(parentConsole, session, desktopManager);
    }
    
    public CaseEditor getCEditor(){
        return new CaseEditor(parentConsole, session, desktopManager);
    }

    private void createNewCase() {
        NewCaseWindow view = new NewCaseWindow(desktopManager);
        presenter.doNew(view);
    }

    private void sensitivityCase() {
        cases = selected();

        if (cases.isEmpty() || cases.size() != 1) {
            messagePanel.setMessage("Please select a single case to use as the Parent Case for the sensitivity");
            return;
        }

        SensitivityWindow view = new SensitivityWindow(desktopManager, parentConsole, categories);
        presenter.doSensitivity(view, (Case) cases.get(0));
    }

    private void removeSelectedCases() {
        clearMsgPanel();
        List selected = selected();

        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more cases to remove.");
            return;
        }

        int selection = showWarningMsg("Warning", "Are you sure you want to remove " + selected.size()+ (selected.size()>1? " cases?":" case?"));
        
        if (selection == JOptionPane.NO_OPTION) {
            return;
        }

        try {
            for (Iterator iter = selected.iterator(); iter.hasNext();) {
                Case element = (Case) iter.next();

                if (isParentCase(element)) {
                    clearMsgPanel();
                    break;
                }

                messagePanel.setMessage("Please wait while removing cases...");
                 
                presenter.doRemove(element);
                doRefresh();
                clearMsgPanel();
                messagePanel.setMessage("Finished removing cases.");
            }
        } catch (EmfException e) {
            showError(e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private boolean isParentCase(Case element) throws EmfException {
        String msg = presenter.checkParentCase(element);

        if (msg.isEmpty())
            return false;
        
        int selection = showWarningMsg("Warning", msg + "\nAre you sure you want to remove the selected case " + element.getName() + "?");

        if (selection == JOptionPane.NO_OPTION) {
            return true;
        }

        return false;
    }

    private int showWarningMsg(String title, String msg) {
        return JOptionPane.showConfirmDialog(parentConsole, msg, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
    }

    private void copySelectedCases() {
        cases = selected();

        if (cases.isEmpty() ) {
            messagePanel.setMessage("Please select one or more case to copy");
            return;
        }

        //messagePanel.setMessage("Please wait while copying cases...");
        //setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        int[] caseIds = new int[cases.size()];

        for (int i = 0; i < caseIds.length; i++)
            caseIds[i] = cases.get(i).getId();

        try {
            presenter.doCopyCases(caseIds);
            //doRefresh();
            //clearMsgPanel();
            //messagePanel.setMessage("Finished copying cases.");
        } catch (Exception e) {
            showError("Could not copy cases." + e.getMessage());
        }

    }

    private void showError(String message) {
        messagePanel.setError(message);
    }

    private List<Case> selected() {
        return (List<Case>) table.selected();
    }

    public EmfConsole getParentConsole() {
        return parentConsole;
    }

    private void clearMsgPanel() {
        messagePanel.clear();
    }
    
    

    public void doRefresh(){
        new RefreshSwingWorkerTasks(layout,messagePanel, presenter).execute();
    }
        

    public void addNewCaseToTableData(Case newCase) {
        List<Case> cases = new ArrayList<Case>();
        cases.addAll(Arrays.asList(tableData.sources()));
        cases.add(newCase);
        refresh(cases.toArray(new Case[0]));
    }
    
    private void importCase() {
        ImportCaseView importView = new ImportCaseWindow(session.dataCommonsService(), desktopManager, parentConsole);

        ImportCasePresenter importPresenter = new ImportCasePresenter(session);
        importPresenter.display(importView);
    }
    
    public void setMessage(String message) {
        messagePanel.setMessage(message);

    }

    public void displayCaseComparisonResult(String qaStepName, String exportedFileName) {
        AnalysisEngineTableApp app = new AnalysisEngineTableApp("View QA Step Results: " + qaStepName, new Dimension(500, 500), desktopManager, parentConsole);
        app.display(new String[] { exportedFileName });
    }


    public void displayCaseComparisonDatasetRevisionResult(String qaStepName, String exportedFileName) {
        AnalysisEngineTableApp app = new AnalysisEngineTableApp("View QA Step Results: " + qaStepName, new Dimension(500, 500), desktopManager, parentConsole);
        app.display(new String[] { exportedFileName });
    }
}
