package gov.epa.emissions.framework.client.cost.controlprogram.viewer;

import gov.epa.emissions.commons.gui.DisabledButton;
import gov.epa.emissions.framework.client.EmfPanel;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlprogram.editor.ControlProgramTechnologyTableData;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlProgramType;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ControlProgramTechnologiesTab extends EmfPanel implements ControlProgramTabView {

    private JPanel tablePanel;

    private SelectableSortFilterWrapper table;

    private ControlProgramTechnologyTableData tableData;

    public ControlProgramTechnologiesTab(MessagePanel messagePanel, EmfConsole parentConsole,
            DesktopManager desktopManager) {
        super("technologies", parentConsole, desktopManager, messagePanel);
    }

    public void display(ControlProgram controlProgram) {
        setupLayout(controlProgram);
    }

    private void setupLayout(ControlProgram controlProgram) {

        try {
            tableData = new ControlProgramTechnologyTableData(controlProgram.getTechnologies());
        } catch (Exception e) {
            this.showError(e.getMessage());
        }

        this.setLayout(new BorderLayout(5, 5));
        this.add(mainPanel(), BorderLayout.CENTER);
    }

    private SortCriteria sortCriteria() {

        String[] columnNames = { "Name" };
        return new SortCriteria(columnNames, new boolean[] { true }, new boolean[] { true });
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

    private JPanel mainPanel() {
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

    public void add(ControlTechnology[] controlTechnologies) {

        this.clearMessage();
        if (controlTechnologies.length > 0) {
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
                controlTechnologies[i] = (ControlTechnology) tableData.element(i);
            }
        }
        controlProgram.setTechnologies(controlTechnologies);
    }
}