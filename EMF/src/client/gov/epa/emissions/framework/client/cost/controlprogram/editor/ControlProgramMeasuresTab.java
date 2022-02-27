package gov.epa.emissions.framework.client.cost.controlprogram.editor;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlProgramType;
import gov.epa.emissions.framework.ui.ListWidget;
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

public class ControlProgramMeasuresTab extends JPanel implements ControlProgramTabView, ControlMeasureSelectorView {
    private ListWidget classesList;

//    private ControlProgramMeasuresTabPresenter presenter;

    private ManageChangeables changeablesList;

    private JPanel tablePanel; 

    private SingleLineMessagePanel messagePanel;

    private SelectableSortFilterWrapper table;

    private ControlProgramMeasureTableData tableData;

    //private SortFilterSelectModel sortFilterSelectModel;

    private EmfConsole parent;

    private Button addButton = new AddButton(addAction());
    
    private EmfSession session;
    
    private ControlMeasure[] controlMeasures = new ControlMeasure[] {};
    
    public ControlProgramMeasuresTab(ControlProgram controlProgram, ManageChangeables changeablesList,
            SingleLineMessagePanel messagePanel, EmfConsole parentConsole, EmfSession session, ControlMeasure[] controlMeasures) {
//        this.controlProgram = controlProgram;
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.parent = parentConsole;
        this.session = session;
        this.controlMeasures = controlMeasures;
    }

    public void display(ControlProgram controlProgram) {
        setupLayout(changeablesList, controlProgram);
    }

    private void setupLayout(ManageChangeables changeables,
            ControlProgram controlProgram) {
        try {
//            controlMeasures = presenter.getControlMeasures();
            tableData = new ControlProgramMeasureTableData(controlProgram.getControlMeasures());
        } catch (Exception e) {
            messagePanel.setError(e.getMessage());
        }
        this.setLayout(new BorderLayout(5, 5));
        // this.setBorder(BorderFactory.createEmptyBorder(50,50,0,300));
        //buildSortFilterPanel();
        this.add(mainPanel(), BorderLayout.CENTER);
    }


    private SortCriteria sortCriteria() {
        String[] columnNames = { "Abbrev", "Name"  };
        return new SortCriteria(columnNames, new boolean[] {true, true }, new boolean[] { true, true });
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
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
        ControlMeasureSelectionView view = new ControlMeasureSelectionDialog(parent, changeablesList, controlMeasures);
        ControlMeasureSelectionPresenter presenter = new ControlMeasureSelectionPresenter(this, view, 
                session);
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

        ControlMeasure[] records = (ControlMeasure[]) selected.toArray(new ControlMeasure[0]);

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

        // disable class filter, if there are measures selected, or enable if no
        // measures are selected
        if (table.getModel().getRowCount() == 0) {
            classesList.setEnabled(true);
        } else {
            classesList.setEnabled(false);
        }

    }

    private JPanel mainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      
        mainPanel.removeAll();
        mainPanel.add(new JLabel("Measures to Include:"), BorderLayout.NORTH);
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

    public void observe(ControlProgramMeasuresTabPresenter presenter) {
//        this.presenter = presenter;
    }

    public void add(ControlMeasure[] measures) {
        messagePanel.clear();
        if (measures.length > 0 ) {
            tableData.add(measures);

            refresh();

        }
    }

    public void startControlMeasuresRefresh() {
        if (addButton != null) addButton.setEnabled(false);
    }

    public void signalControlMeasuresAreLoaded(ControlMeasure[] controlMeasures) {
        this.controlMeasures = controlMeasures;
        if (addButton != null) addButton.setEnabled(true);
    }

    public void notifyControlProgramTypeChange(ControlProgramType controlProgramType) {
        // NOTE Auto-generated method stub
        
    }

    public void save(ControlProgram controlProgram) {
        ControlMeasure[] cms = controlProgram.getControlMeasures();
        if (tableData != null) {
            cms = new ControlMeasure[tableData.rows().size()];
            for (int i = 0; i < tableData.rows().size(); i++) {
                cms[i] = (ControlMeasure)tableData.element(i);
            }
        }
        controlProgram.setControlMeasures(cms);
    }
}