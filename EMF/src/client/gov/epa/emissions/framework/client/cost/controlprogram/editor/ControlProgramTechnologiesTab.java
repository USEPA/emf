package gov.epa.emissions.framework.client.cost.controlprogram.editor;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlProgramType;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ControlProgramTechnologiesTab extends JPanel implements ControlProgramTabView, ControlTechnologySelectorView {

    private ControlProgramTechnologiesTabPresenter presenter;

    private ManageChangeables changeablesList;

    private JPanel tablePanel; 

    private SingleLineMessagePanel messagePanel;

    private SelectableSortFilterWrapper table;

    private ControlProgramTechnologyTableData tableData;

    private EmfConsole parent;

    private Button addButton = new AddButton(addAction());
    
    private EmfSession session;
    
    public ControlProgramTechnologiesTab(ControlProgram controlProgram, ManageChangeables changeablesList,
            SingleLineMessagePanel messagePanel, EmfConsole parentConsole, EmfSession session) {
//        this.controlProgram = controlProgram;
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.parent = parentConsole;
        this.session = session;
    }

    public void display(ControlProgram controlProgram) throws EmfException {
        setupLayout(changeablesList, controlProgram);
    }

    private void setupLayout(ManageChangeables changeables,
            ControlProgram controlProgram) throws EmfException {
        try {
//            controlMeasures = presenter.getControlMeasures();
            tableData = new ControlProgramTechnologyTableData(controlProgram.getTechnologies());
        } catch (Exception e) {
            messagePanel.setError(e.getMessage());
        }
        this.setLayout(new BorderLayout(5, 5));
        // this.setBorder(BorderFactory.createEmptyBorder(50,50,0,300));
        //buildSortFilterPanel();
        this.add(mainPanel(), BorderLayout.CENTER);
    }


    private SortCriteria sortCriteria() {
        String[] columnNames = { "Name" };
        return new SortCriteria(columnNames, new boolean[] {true }, new boolean[] { true });
    }

    private JPanel buttonPanel() throws EmfException {
        JPanel panel = new JPanel();
        if (presenter.getAllControlTechnologies().length == 0)
            addButton.setEnabled(false);
        addButton.setMargin(new Insets(2, 5, 2, 5));
        panel.add(addButton);
        Button removeButton = new RemoveButton(removeAction());
        removeButton.setMargin(new Insets(2, 5, 2, 5));
        panel.add(removeButton);

        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.LINE_START);

        return container;
    }

    private Action addAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                selectionView();
            }
        };
    }

    private void selectionView() {
        ControlTechnologySelectionView view = new ControlTechnologySelectionDialog(parent, changeablesList);
        ControlTechnologySelectionPresenter presenter = new ControlTechnologySelectionPresenter(this, view, session);
        try {
            presenter.display(view);
        } catch (Exception exp) {
            messagePanel.setError(exp.getMessage());
        }
    }

    private Action removeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                remove();
            }
        };
    }

    protected void remove() {
        messagePanel.clear();
        List selected = table.selected();

        if (selected.size() == 0) {
            messagePanel.setError("Please select an item to remove.");
            return;
        }

        ControlTechnology[] records = (ControlTechnology[]) selected.toArray(new ControlTechnology[0]);

        if (records.length == 0)
            return;

        String title = "Warning";
        String message = "Are you sure you want to remove the "+records.length+" selected items?";
        int selection = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            tableData.remove(records);
            refresh();
        }
    }

    private JPanel mainPanel() throws EmfException {
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      
        mainPanel.removeAll();
        mainPanel.add(new JLabel("Technologies to Include:"), BorderLayout.NORTH);
        mainPanel.add(tablePanel());
        mainPanel.add(buttonPanel(), BorderLayout.SOUTH);
        return mainPanel; 
    }
    
    private JPanel tablePanel() {
        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parent, tableData, sortCriteria());
        tablePanel.add(table);

        return tablePanel;
    }
    
    private void refresh(){
        table.refresh(tableData);
        panelRefresh();
    }
    
    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.validate();
    }

    public void observe(ControlProgramTechnologiesTabPresenter presenter) {
        this.presenter = presenter;
    }

    public void add(ControlTechnology[] controlTechnologies) {
        messagePanel.clear();
        if (controlTechnologies.length > 0 ) {
            tableData.add(controlTechnologies);

            refresh();

        }
    }

    public void notifyControlProgramTypeChange(ControlProgramType controlProgramType) {
        // NOTE Auto-generated method stub
    }

    public void save(ControlProgram controlProgram) {
        ControlTechnology[] controlTechnologies = controlProgram.getTechnologies();
        if (tableData != null) {
            controlTechnologies = new ControlTechnology[tableData.rows().size()];
            for (int i = 0; i < tableData.rows().size(); i++) {
                controlTechnologies[i] = (ControlTechnology)tableData.element(i);
            }
        }
        controlProgram.setTechnologies(controlTechnologies);
    }
}