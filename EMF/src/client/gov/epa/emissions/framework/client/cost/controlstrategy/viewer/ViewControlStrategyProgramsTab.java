package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.DisabledButton;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.framework.client.EmfPanel;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlprogram.viewer.ControlProgramView;
import gov.epa.emissions.framework.client.cost.controlprogram.viewer.ControlProgramWindow;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.ControlStrategyProgramTableData;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.ui.MessagePanel;
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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ViewControlStrategyProgramsTab extends EmfPanel implements ViewControlStrategyTabView {

    private JPanel tablePanel;

    private SelectableSortFilterWrapper table;

    private ControlStrategyProgramTableData tableData;

    private ViewControlStrategyProgramsTabPresenter presenter;
    
    private SingleLineMessagePanel messagePanel;

    public ViewControlStrategyProgramsTab(MessagePanel messagePanel, EmfConsole parentConsole,
            DesktopManager desktopManager) {
        super("csPrograms", parentConsole, desktopManager, messagePanel);
    }

    public void display(ControlStrategy controlStrategy) {
        setupLayout(controlStrategy);
    }

    private void setupLayout(ControlStrategy controlStrategy) {

        try {
            tableData = new ControlStrategyProgramTableData(controlStrategy.getControlPrograms());
        } catch (Exception e) {
            showError(e.getMessage());
        }

        this.setLayout(new BorderLayout(5, 5));
        this.add(mainPanel(), BorderLayout.CENTER);
    }

    private SortCriteria sortCriteria() {

        String[] columnNames = { "Name" };
        return new SortCriteria(columnNames, new boolean[] { true }, new boolean[] { true });
    }

    private JPanel buttonPanel() {

        String message = "You have asked to open a lot of windows. Do you want proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        JPanel panel = new JPanel();

        JButton addButton = new DisabledButton("Add");
        addButton.setMargin(new Insets(2, 5, 2, 5));
        panel.add(addButton);

        JButton removeButton = new DisabledButton("Remove");
        removeButton.setMargin(new Insets(2, 5, 2, 5));
        panel.add(removeButton);

        JButton editButton = new DisabledButton("Edit");
        editButton.setMargin(new Insets(2, 5, 2, 5));
        panel.add(editButton);

        SelectAwareButton viewButton = new SelectAwareButton("View", viewAction(), table, confirmDialog);
        viewButton.setMargin(new Insets(2, 5, 2, 5));
        panel.add(viewButton);

        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.LINE_START);

        return container;
    }

    private Action viewAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewControlPrograms();
            }
        };
    }

    private JPanel mainPanel() {

        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.removeAll();
        mainPanel.add(new JLabel("Programs to Include:"), BorderLayout.NORTH);
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

    public void observe(ViewControlStrategyProgramsTabPresenter presenter) {
        this.presenter = presenter;
    }

    public void notifyStrategyTypeChange(StrategyType strategyType) {
        // no-op
    }

    public void refresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        // no-op
    }

    public void refresh(ControlStrategy controlStrategy) {
        try {
            tableData = new ControlStrategyProgramTableData(controlStrategy.getControlPrograms());
        } catch (Exception e) {
            messagePanel.setError(e.getMessage());
        }

        table.refresh(tableData);
        tablePanel.removeAll();
        tablePanel.add(table);
        super.validate();
    }
    

    private void viewControlPrograms() {

        List controlPrograms = selected();

        if (controlPrograms.isEmpty()) {
            showMessage("Please select one or more Control Programs");
            return;
        }

        for (int i = 0; i < controlPrograms.size(); i++) {

            ControlProgram controlProgram = (ControlProgram) controlPrograms.get(i);
            ControlProgramView view = new ControlProgramWindow(this.getDesktopManager(), this.getSession(), this
                    .getParentConsole(), controlProgram.getControlMeasures());

            try {
                presenter.doView(view, controlProgram);
            } catch (EmfException e) {
                showError(e.getMessage());
            }
        }
    }

    private List selected() {
        return table.selected();
    }

    public void notifyStrategyRun(ControlStrategy controlStrategy) {
        // NOTE Auto-generated method stub

    }

    public void run(ControlStrategy controlStrategy) throws EmfException {
        if (controlStrategy.getStrategyType().getName().equalsIgnoreCase(StrategyType.projectFutureYearInventory)) {
            if (tableData.rows().size() == 0)
                throw new EmfException(
                        "Control Programs Tab: The strategy must have at least one control program specified for the run.");
        }
    }
}