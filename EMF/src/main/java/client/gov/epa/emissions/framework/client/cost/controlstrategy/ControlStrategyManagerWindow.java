package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.buttons.CopyButton;
import gov.epa.emissions.commons.gui.buttons.NewButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyView;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyWindow;
import gov.epa.emissions.framework.client.cost.controlstrategy.viewer.ViewControlStrategyView;
import gov.epa.emissions.framework.client.cost.controlstrategy.viewer.ViewControlStrategyWindow;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.table.format.FormattedCellRenderer;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;
import gov.epa.emissions.commons.CommonDebugLevel;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class ControlStrategyManagerWindow extends ReusableInteralFrame implements ControlStrategyManagerView,
        RefreshObserver, Runnable {

    private ControlStrategiesManagerPresenter presenter;

    private ControlStrategiesTableData tableData;

    private JPanel tablePanel;
    
    private SelectableSortFilterWrapper table;

    //private EmfTableModel model;

    private JPanel layout;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private EmfSession session;
    
    private EditControlStrategyView[] editControlStrategyViews = {};

    private ViewControlStrategyView[] viewControlStrategyViews = {};

    private volatile Thread populateThread;

    private static final List<String> COLUMN_NAMES_TO_FORMAT = new ArrayList<String>();
    
    static {
        COLUMN_NAMES_TO_FORMAT.add("Total Cost");
        COLUMN_NAMES_TO_FORMAT.add("Reduction (tons)");
        COLUMN_NAMES_TO_FORMAT.add("Average Cost Per Ton");
    }
     
    public ControlStrategyManagerWindow(EmfConsole parentConsole, EmfSession session, DesktopManager desktopManager) {
        super("Control Strategy Manager", new Dimension(850, 400), desktopManager);
        
        this.session = session;
        this.parentConsole = parentConsole;

        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void observe(ControlStrategiesManagerPresenterImpl presenter) {
        this.presenter = presenter;
    }

    public void display(ControlStrategy[] controlStrategies) {
        doLayout(controlStrategies, this.session);
        super.display();
        //refresh control measures...
        this.populateThread = new Thread(this);
        populateThread.start();
    }

    public void run() {
        try {
            presenter.loadControlMeasures();
        } catch (Exception e) {
            messagePanel.setError("Cannot retrieve all control measures.");
        }
        //refresh Edit Control Strategy windows...
        for (int i = 0; i < editControlStrategyViews.length; i++) {
            EditControlStrategyView view = editControlStrategyViews[i];
            if (view != null) view.endControlMeasuresRefresh();
        }
        this.populateThread = null;
    }

    public void refresh(ControlStrategy[] controlStrategies) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setupTableModel(controlStrategies);
        table.refresh(tableData);
        panelRefresh();
        super.refreshLayout();
        setCursor(Cursor.getDefaultCursor());
        //refresh Edit Control Strategy windows...
        for (int i = 0; i < editControlStrategyViews.length; i++) {
            EditControlStrategyView view = editControlStrategyViews[i];
            if (editControlStrategyViews != null) {
                view.startControlMeasuresRefresh();
            }
        }
        //refresh control measures...
        this.populateThread = new Thread(this);
        populateThread.start();
    }
    
    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.refreshLayout();
    }

    public void doRefresh() throws EmfException {
        presenter.doRefresh();
    }

    private void doLayout(ControlStrategy[] controlStrategies, EmfSession session) {
        layout.removeAll();
        layout.setLayout(new BorderLayout());

        layout.add(createTopPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(controlStrategies, parentConsole, session), BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel tablePanel(ControlStrategy[] controlStrategies, EmfConsole parentConsole, EmfSession session) {

        setupTableModel(controlStrategies);
        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());

        JTable innerTable = table.getTable();
        TableColumnModel columnModel = innerTable.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {

            TableColumn column = columnModel.getColumn(i);
            String[] headerValue = (String[]) column.getHeaderValue();
            if (headerValue != null && headerValue.length > 0 && headerValue[0] != null
                    && COLUMN_NAMES_TO_FORMAT.contains(headerValue[0])) {

                ControlStrategyCustomFormat format = new ControlStrategyCustomFormat("#,##0");
                FormattedCellRenderer formattedCellRenderer = new FormattedCellRenderer(format, SwingConstants.CENTER);
                column.setCellRenderer(formattedCellRenderer);
            }
        }

        tablePanel.add(table, BorderLayout.CENTER);

        return tablePanel;
    }
    
    private void setupTableModel(ControlStrategy[] controlStrategies){
        tableData = new ControlStrategiesTableData(controlStrategies);
    }
    
    private SortCriteria sortCriteria() {
        String[] columnNames = { "Last Modified" };
        return new SortCriteria(columnNames, new boolean[] { false }, new boolean[] { true });
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        Button button = new RefreshButton(this, "Refresh Control Strategies", messagePanel);
        panel.add(button, BorderLayout.EAST);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel crudPanel = createCrudPanel();

        JPanel closePanel = new JPanel();
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
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

        String message = "You have asked to open a lot of windows. Do you want proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        crudPanel.add(viewButton(confirmDialog));
        crudPanel.add(editButton(confirmDialog));

        Button newButton = new NewButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                createNewStrategy();
            }
        });
        crudPanel.add(newButton);

        Button removeButton = new RemoveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    doRemove();
                } catch (EmfException exception) {
                    messagePanel.setError(exception.getMessage());
                }
            }
        });
        crudPanel.add(removeButton);
        
        Button copyButton = new CopyButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                
                    try {
                        copySelectedStrategy();
                    } catch (EmfException excp) {
                        messagePanel.setError("Error copying control strategies: " + excp.getMessage());
                    }
            }
        });
        crudPanel.add(copyButton);

        Button compareButton = new Button("Compare", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                try {
                    compareControlStrategies();
                } catch (EmfException e1) {
                    // NOTE Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });
        crudPanel.add(compareButton);
        

        return crudPanel;
    }

    private SelectAwareButton editButton(ConfirmDialog confirmDialog) {
        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editControlStrategies();
            }

        };
        SelectAwareButton editButton = new SelectAwareButton("Edit", editAction, table, confirmDialog);
        return editButton;
    }

    private SelectAwareButton viewButton(ConfirmDialog confirmDialog) {
        Action viewAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewControlStrategies();
            }
        };

        SelectAwareButton viewButton = new SelectAwareButton("View", viewAction, table, confirmDialog);
        return viewButton;
    }

    private void editControlStrategies() {
        messagePanel.clear();
        List controlStrategies = selected();
        if (controlStrategies.isEmpty()) {
            messagePanel.setMessage("Please select one or more Control Strategies");
            return;
        }
        editControlStrategyViews = new EditControlStrategyView[controlStrategies.size()]; 
        for (int i = 0; i < controlStrategies.size(); i++) {
            ControlStrategy controlStrategy = (ControlStrategy) controlStrategies.get(i);
            EditControlStrategyView view = new EditControlStrategyWindow(desktopManager, session, parentConsole);
            editControlStrategyViews[i] = view;
            try {
                presenter.doEdit(view, controlStrategy);
            } catch (EmfException e) {
                messagePanel.setError(e.getMessage());
            }
        }
    }

    private void viewControlStrategies() {
        
        this.messagePanel.clear();
        
        List controlStrategies = selected();
        if (controlStrategies.isEmpty()) {
            this.messagePanel.setMessage("Please select one or more Control Strategies");
        }
        else {

            this.viewControlStrategyViews = new ViewControlStrategyView[controlStrategies.size()]; 
            
            for (int i = 0; i < controlStrategies.size(); i++) {
                
                ControlStrategy controlStrategy = (ControlStrategy) controlStrategies.get(i);
                ViewControlStrategyView view = new ViewControlStrategyWindow(desktopManager, session, parentConsole);
                this.viewControlStrategyViews[i] = view;
                
                try {
                    presenter.doView(view, controlStrategy);
                } catch (EmfException e) {
                    if ( CommonDebugLevel.DEBUG_CMIMPORT) {
                        System.out.println(" === Exception 1 ===");
                        e.printStackTrace();
                    }
                    
                    messagePanel.setError("Exception occured: " + e.getMessage());
                } catch ( Exception e) {
                    if ( CommonDebugLevel.DEBUG_CMIMPORT) {
                        System.out.println(" === Exception 2 ===");
                        e.printStackTrace();
                    }
                    
                    messagePanel.setError("Exception occured: " + e.getMessage());
                }
            }
        }
        
    }
    
    protected void doRemove() throws EmfException {
        messagePanel.clear();
        ControlStrategy[] records = (ControlStrategy[])selected().toArray(new ControlStrategy[0]);

        if (records.length == 0) {
            messagePanel.setError("Please select an item to remove.");
            return;
        }

        String title = "Warning";
        String message = (records.length == 1) ? 
                "Are you sure you want to remove the selected strategy?" :
                "Are you sure you want to remove the "+records.length+" selected strategies?";
       int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (selection == JOptionPane.YES_OPTION) {
            int[] ids = new int[records.length];
            for (int i = 0; i < records.length; i++) {
                ids[i] = records[i].getId(); 
            }
            try {
                presenter.doRemove(ids);
            } catch (EmfException ex) {
                throw ex;
            } finally {
                doRefresh();
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }
    
    private void compareControlStrategies() throws EmfException {
        List strategies = selected();

        if (strategies.isEmpty()) {
            messagePanel.setMessage("Please select at least one control strategy to compare");
            return;
        }

        int[] ids = new int[strategies.size()];
        
        for (int i = 0; i < strategies.size(); ++i) {
            ids[i] = ((ControlStrategy)strategies.get(i)).getId();
        }
        presenter.viewControlStrategyComparisonResult(ids, "");
    }

    private void copySelectedStrategy() throws EmfException {
        boolean error = false;
        messagePanel.clear();
        List strategies = selected();
        if (strategies.isEmpty()) {
            messagePanel.setMessage("Please select one or more control strategies.");
            return;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        for (Iterator iter = strategies.iterator(); iter.hasNext();) {
            ControlStrategy element = (ControlStrategy) iter.next();
            
            try {
                presenter.doSaveCopiedStrategies(element.getId(), session.user());
//                ControlStrategy coppied = (ControlStrategy)DeepCopy.copy(element);
//                coppied.setName("Copy of " + element.getName());
//                presenter.doSaveCopiedStrategies(coppied, element.getName());
            } catch (Exception e) {
//                setCursor(Cursor.getDefaultCursor());
                messagePanel.setError(e.getMessage());
                error = true;
            }
        }
        if (!error) doRefresh();
        setCursor(Cursor.getDefaultCursor());
    }

    private List selected() {
        return table.selected();
    }

    private void createNewStrategy() {
        ControlStrategyView view = new ControlStrategyWindow(parentConsole, session, desktopManager);
        presenter.doNew(view);   
    }

    public EmfConsole getParentConsole() {
        return parentConsole;
    }
    
    public void displayControlStrategyComparisonResult(String qaStepName, String exportedFileName) {
        AnalysisEngineTableApp app = new AnalysisEngineTableApp("View QA Step Results: " + qaStepName, new Dimension(500, 500), desktopManager, parentConsole);
        app.display(new String[] { exportedFileName });
    }   

}
