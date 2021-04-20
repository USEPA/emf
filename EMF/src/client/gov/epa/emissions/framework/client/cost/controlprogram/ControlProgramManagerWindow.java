package gov.epa.emissions.framework.client.cost.controlprogram;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CopyButton;
import gov.epa.emissions.commons.gui.buttons.NewButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlprogram.editor.ControlProgramView;
import gov.epa.emissions.framework.client.cost.controlprogram.editor.ControlProgramWindow;
import gov.epa.emissions.framework.client.cost.controlprogram.editor.NewControlProgramWindow;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.BasicSearchFilter;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlStrategyProgramFilter;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;

public class ControlProgramManagerWindow extends DisposableInteralFrame implements ControlProgramManagerView,
        RefreshObserver, Runnable {

    private ControlProgramManagerPresenter presenter;

    private ControlProgramsTableData tableData;

    private JPanel tablePanel;
    
    private SelectableSortFilterWrapper table;

    //private EmfTableModel model;

    private JPanel layout;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private EmfSession session;
    
    private List<ControlProgramView> editControlProgramViewList;

    private volatile Thread populateThread;

    private TextField simpleTextFilter;
    private ComboBox filterFieldsComboBox;

    public ControlProgramManagerWindow(EmfConsole parentConsole, EmfSession session, DesktopManager desktopManager) {
        super("Control Program Manager", new Dimension(850, 400), desktopManager);
        
        this.session = session;
        this.parentConsole = parentConsole;

        layout = new JPanel();
        this.getContentPane().add(layout);
        this.editControlProgramViewList = new ArrayList<ControlProgramView>();
    }

    public void observe(ControlProgramManagerPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(ControlProgram[] controlPrograms) {
        doLayout(controlPrograms, this.session);
        super.display();
        //refresh control measures...
        this.populateThread = new Thread(this);
        populateThread.start();
    }

    public void run() {
        //refresh Edit Control Program windows...
        for (int i = 0; i < editControlProgramViewList.size(); i++) {
            ControlProgramView view = editControlProgramViewList.get(i);
            view.startControlMeasuresRefresh();
        }
        try {
            presenter.loadControlMeasures();
        } catch (Exception e) {
            messagePanel.setError("Cannot retrieve all control measures.");
        }
        //refresh Edit Control Program windows...
        for (int i = 0; i < editControlProgramViewList.size(); i++) {
            ControlProgramView view = editControlProgramViewList.get(i);
            view.signalControlMeasuresAreLoaded(presenter.getControlMeasures());
        }
        this.populateThread = null;
    }

    public void refresh(ControlProgram[] controlPrograms) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setupTableModel(controlPrograms);
        table.refresh(tableData);
        panelRefresh();
        super.refreshLayout();
        setCursor(Cursor.getDefaultCursor());
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

    private void doLayout(ControlProgram[] controlPrograms, EmfSession session) {
        layout.removeAll();
        layout.setLayout(new BorderLayout());

        layout.add(createTopPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(controlPrograms, parentConsole, session), BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel tablePanel(ControlProgram[] controlPrograms, EmfConsole parentConsole, EmfSession session) {
        setupTableModel(controlPrograms);
        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());
        table.getTable().getAccessibleContext().setAccessibleName("List of control programs");
        tablePanel.add(table, BorderLayout.CENTER);

        return tablePanel;
    }
    
    private void setupTableModel(ControlProgram[] controlPrograms){
        tableData = new ControlProgramsTableData(controlPrograms);
    }
    
    private SortCriteria sortCriteria() {
        String[] columnNames = { "Start" };
        return new SortCriteria(columnNames, new boolean[] { false }, new boolean[] { true });
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        Button button = new RefreshButton(this, "Refresh Control Programs", messagePanel);
        panel.add(button, BorderLayout.EAST);
        JPanel topPanel = new JPanel(new BorderLayout());
        simpleTextFilter = new TextField("textfilter", 25);
        simpleTextFilter.setPreferredSize(new Dimension(360, 25));
        simpleTextFilter.setEditable(true);
        simpleTextFilter.addActionListener(simpleFilterTypeAction());


        JPanel advPanel = new JPanel(new BorderLayout(5, 2));

        //get table column names
//        String[] columns = new String[] {"Module Name", "Composite?", "Final?", "Tags", "Project", "Module Type", "Version", "Creator", "Date", "Lock Owner", "Lock Date", "Description" };//(new ModulesTableData(new ConcurrentSkipListMap<Integer, LiteModule>())).columns();

        filterFieldsComboBox = new ComboBox("Select one", (new ControlStrategyProgramFilter()).getFilterFieldNames());
        filterFieldsComboBox.setSelectedIndex(1);
        filterFieldsComboBox.setPreferredSize(new Dimension(180, 25));
        filterFieldsComboBox.addActionListener(simpleFilterTypeAction());

        advPanel.add(getFilterFieldsComboBoxPanel("Filter Fields:", filterFieldsComboBox), BorderLayout.LINE_START);
        advPanel.add(simpleTextFilter, BorderLayout.EAST);
//        advPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));

        topPanel.add(advPanel, BorderLayout.EAST);

        JPanel mainPanel = new JPanel(new GridLayout(2, 1));
        mainPanel.add(panel);
        mainPanel.add(topPanel);

        return mainPanel;
    }

    private Action simpleFilterTypeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
//                DatasetType type = getSelectedDSType();
//                try {
//                    // count the number of datasets and do refresh
//                    if (dsTypesBox.getSelectedIndex() > 0)
                try {
                    doRefresh();
                } catch (EmfException e1) {
                    e1.printStackTrace();
                }
//                } catch (EmfException e1) {
////                    messagePanel.setError("Could not retrieve all modules " /*+ type.getName()*/);
//                }
            }
        };
    }

    private JPanel getFilterFieldsComboBoxPanel(String label, JComboBox box) {
        JPanel panel = new JPanel(new BorderLayout(5, 2));
        JLabel jlabel = new JLabel(label);
        jlabel.setHorizontalAlignment(JLabel.RIGHT);
        panel.add(jlabel, BorderLayout.WEST);
        panel.add(box, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 10));

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
        closeButton.setMnemonic('l');
        closePanel.add(closeButton);
        getRootPane().setDefaultButton(closeButton);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        controlPanel.add(crudPanel, BorderLayout.WEST);
        controlPanel.add(closePanel, BorderLayout.EAST);

        return controlPanel;
    }

    public BasicSearchFilter getSearchFilter() {

        BasicSearchFilter searchFilter = new BasicSearchFilter();
        String fieldName = (String)filterFieldsComboBox.getSelectedItem();
        if (fieldName != null) {
            searchFilter.setFieldName(fieldName);
            searchFilter.setFieldValue(simpleTextFilter.getText());
        }
        return searchFilter;
    }

    private JPanel createCrudPanel() {
        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout());

        String message = "You have asked to open a lot of windows. Do you want to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        crudPanel.add(viewButton(confirmDialog));
        crudPanel.add(editButton(confirmDialog));

        Button newButton = new NewButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                createNewProgram();
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
                        copySelectedProgram();
                    } catch (EmfException excp) {
                        messagePanel.setError("Error copying control Programs: " + excp.getMessage());
                    }
            }
        });
        crudPanel.add(copyButton);


        return crudPanel;
    }

    private SelectAwareButton editButton(ConfirmDialog confirmDialog) {
        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editControlPrograms();
            }

        };
        SelectAwareButton editButton = new SelectAwareButton("Edit", editAction, table, confirmDialog);
        return editButton;
    }

    private SelectAwareButton viewButton(ConfirmDialog confirmDialog) {
        Action viewAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewControlPrograms();
            }
        };

        SelectAwareButton viewButton = new SelectAwareButton("View", viewAction, table, confirmDialog);
        viewButton.setEnabled(false);
        return viewButton;
    }

    private void viewControlPrograms() {
        tempMessage();
    }

    private void editControlPrograms() {
        List controlPrograms = selected();
        if (controlPrograms.isEmpty()) {
            messagePanel.setMessage("Please select one or more Control Programs");
            return;
        }
        for (int i = 0; i < controlPrograms.size(); i++) {
            ControlProgram controlProgram = (ControlProgram) controlPrograms.get(i);
            ControlProgramView view = new ControlProgramWindow(desktopManager, session, parentConsole, presenter.getControlMeasures());
            editControlProgramViewList.add(view);
            try {
                presenter.doEdit(view, controlProgram);
            } catch (EmfException e) {
                messagePanel.setError(e.getMessage());
            }
        }
    }

    protected void doRemove() throws EmfException {
        messagePanel.clear();
        ControlProgram[] records = (ControlProgram[])selected().toArray(new ControlProgram[0]);

        if (records.length == 0) {
            messagePanel.setError("Please select an item to remove.");
            return;
        }

        String title = "Warning";
        String message = "Are you sure you want to remove the "+records.length+" selected control program(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

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
    
    private void copySelectedProgram() throws EmfException {
        boolean error = false;
        messagePanel.clear();
        List programs = selected();
        if (programs.isEmpty()) {
            messagePanel.setMessage("Please select one or more control strategies.");
            return;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        for (Iterator iter = programs.iterator(); iter.hasNext();) {
            ControlProgram element = (ControlProgram) iter.next();
            
            try {
                presenter.doSaveCopiedPrograms(element.getId(), session.user());
//                ControlProgram coppied = (ControlProgram)DeepCopy.copy(element);
//                coppied.setName("Copy of " + element.getName());
//                presenter.doSaveCopiedPrograms(coppied, element.getName());
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

    private void createNewProgram() {
        ControlProgramView view = new NewControlProgramWindow(desktopManager, session, parentConsole, presenter.getControlMeasures());
        editControlProgramViewList.add(view);
        presenter.doNew(view);   
    }

    public EmfConsole getParentConsole() {
        return parentConsole;
    }

    private void tempMessage() {
        messagePanel.clear();
        messagePanel.setMessage("Under construction");
    }
}
