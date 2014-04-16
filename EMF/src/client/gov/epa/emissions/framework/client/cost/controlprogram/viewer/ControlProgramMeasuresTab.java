package gov.epa.emissions.framework.client.cost.controlprogram.viewer;

import gov.epa.emissions.commons.gui.DisabledButton;
import gov.epa.emissions.framework.client.EmfPanel;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlprogram.editor.ControlProgramMeasureTableData;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlProgramType;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ControlProgramMeasuresTab extends EmfPanel implements ControlProgramTabView {

    private ListWidget classesList;

    private JPanel tablePanel;

    private SelectableSortFilterWrapper table;

    private ControlProgramMeasureTableData tableData;

    public ControlProgramMeasuresTab(MessagePanel messagePanel, EmfConsole parentConsole, DesktopManager desktopManager) {
        super("measures", parentConsole, desktopManager, messagePanel);
    }

    public void display(ControlProgram controlProgram) {
        setupLayout(controlProgram);
    }

    private void setupLayout(ControlProgram controlProgram) {

        try {
            // controlMeasures = presenter.getControlMeasures();
            tableData = new ControlProgramMeasureTableData(controlProgram.getControlMeasures());
        } catch (Exception e) {
            this.showError(e.getMessage());
        }

        this.setLayout(new BorderLayout(5, 5));
        this.add(mainPanel(), BorderLayout.CENTER);
    }

    private SortCriteria sortCriteria() {
        String[] columnNames = { "Abbrev", "Name" };
        return new SortCriteria(columnNames, new boolean[] { true, true }, new boolean[] { true, true });
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();

        JButton addButton = new DisabledButton("Add");
        addButton.setMargin(new Insets(2, 5, 2, 5));
        panel.add(addButton);

        JButton removeButton = new DisabledButton("Remove");
        removeButton.setMargin(new Insets(2, 5, 2, 5));
        panel.add(removeButton);

        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.LINE_START);

        return container;
    }

    protected void remove() {
        this.clearMessage();
        List selected = table.selected();

        if (selected.size() == 0) {
            this.showError("Please select an item to remove.");
            return;
        }

        ControlMeasure[] records = (ControlMeasure[]) selected.toArray(new ControlMeasure[0]);

        if (records.length == 0)
            return;

        String title = "Warning";
        String message = "Are you sure you want to remove the " + records.length + " selected items?";
        int selection = JOptionPane.showConfirmDialog(this.getParentConsole(), message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

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
        table = new SelectableSortFilterWrapper(this.getParentConsole(), tableData, sortCriteria());
        tablePanel.add(table);

        return tablePanel;
    }

    private void refresh() {
        table.refresh(tableData);
        panelRefresh();
    }

    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.validate();
    }

    public void observe(ControlProgramMeasuresTabPresenter presenter) {
        // this.presenter = presenter;
    }

    public void add(ControlMeasure[] measures) {
        this.clearMessage();
        if (measures.length > 0) {
            tableData.add(measures);

            refresh();

        }
    }

    public void notifyControlProgramTypeChange(ControlProgramType controlProgramType) {
        // no-op
    }

    public void save(ControlProgram controlProgram) {

        ControlMeasure[] cms = controlProgram.getControlMeasures();
        if (tableData != null) {

            cms = new ControlMeasure[tableData.rows().size()];
            for (int i = 0; i < tableData.rows().size(); i++) {
                cms[i] = (ControlMeasure) tableData.element(i);
            }
        }

        controlProgram.setControlMeasures(cms);
    }
}