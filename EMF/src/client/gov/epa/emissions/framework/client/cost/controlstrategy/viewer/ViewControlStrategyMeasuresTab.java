package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.commons.gui.DisabledButton;
import gov.epa.emissions.framework.client.EmfPanel;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.ControlStrategyMeasureTableData;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyMeasure;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;
import gov.epa.emissions.commons.CommonDebugLevel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ViewControlStrategyMeasuresTab extends EmfPanel implements ViewControlStrategyMeasuresTabView {

    private ListWidget classesList;

    private ControlMeasureClass[] allClasses;

    private ControlMeasureClass[] classes;

    private ViewControlStrategyMeasuresTabPresenter presenter;

    private ControlMeasureClass defaultClass = new ControlMeasureClass("Known");

    private JPanel tablePanel;

    private JPanel classesPanel;

    private SelectableSortFilterWrapper table;

    private ControlStrategyMeasureTableData tableData;

    private ControlStrategyMeasure[] controlStrategyMeasures;

    public ViewControlStrategyMeasuresTab(MessagePanel messagePanel, EmfConsole parentConsole,
            DesktopManager desktopManager) {
        super("csMeasures", parentConsole, desktopManager, messagePanel);
    }

    public void display(ControlStrategy strategy) throws EmfException {

        this.allClasses = presenter.getAllClasses();
        this.classes = presenter.getControlMeasureClasses();
        setupLayout();
    }

    private void setupLayout() {

        try {
            controlStrategyMeasures = presenter.getControlMeasures();
            if ( controlStrategyMeasures != null) {
                tableData = new ControlStrategyMeasureTableData(controlStrategyMeasures);
            } else {
                throw new EmfException( "controlStrategyMeasures is null!");
            }
        } catch (Exception e) {
            if ( CommonDebugLevel.DEBUG_CMIMPORT) {
                System.out.println("=== Exception occured: ");
                e.printStackTrace();
            }
            showError(e.getMessage());
            return;
        }

        this.setLayout(new BorderLayout(5, 5));
        // this.setBorder(BorderFactory.createEmptyBorder(50,50,0,300));
        classesPanel = createClassesPanel();
        this.add(classesPanel, BorderLayout.NORTH);
        // buildSortFilterPanel();
        this.add(mainPanel(), BorderLayout.CENTER);

        // disable class filter since there are measures selected
        if (table.getModel().getRowCount() > 0) {
            classesPanel.setVisible(false);// classesList.setVisible(false);
        } else {
            classesPanel.setVisible(true);// classesList.setVisible(false);
        }
    }

    private SortCriteria sortCriteria() {

        String[] columnNames = { "Order", "Name" };
        return new SortCriteria(columnNames, new boolean[] { true, true }, new boolean[] { true, true });
    }

    private JPanel buttonPanel() {

        JPanel panel = new JPanel();

        JButton addButton = new DisabledButton("Add");
        addButton.setMargin(new Insets(2, 5, 2, 5));
        panel.add(addButton);

        JButton editButton = new DisabledButton("Edit");
        panel.add(editButton);

        JButton removeButton = new DisabledButton("Remove");
        removeButton.setMargin(new Insets(2, 5, 2, 5));
        panel.add(removeButton);

        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.LINE_START);

        return container;
    }

    private JPanel mainPanel() {

        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(new Border("Measures to Include"));

        mainPanel.add(tablePanel(), BorderLayout.CENTER);
        mainPanel.add(buttonPanel(), BorderLayout.SOUTH);

        return mainPanel;
    }

    private JPanel tablePanel() {

        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(this.getParentConsole(), tableData, sortCriteria());
        tablePanel.add(table);

        return tablePanel;
    }

    private JPanel createClassesPanel() {

        // get all measure classes and cs classes
        // and add default "ALL" to both lists
        List allClassesList = new ArrayList(Arrays.asList(allClasses));
        // allClassesList.add(0, defaultClass);
        allClasses = (ControlMeasureClass[]) allClassesList.toArray(new ControlMeasureClass[0]);
        if (classes.length == 0) {
            List selClassesList = new ArrayList();
            selClassesList.add(defaultClass);
            classes = (ControlMeasureClass[]) selClassesList.toArray(new ControlMeasureClass[0]);
        }
        // build list widget
        this.classesList = new ListWidget(allClasses, classes);
        this.classesList.setToolTipText("Use Ctrl or Shift to select multiple classes");

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 100, 0, 300));
        JLabel label = new JLabel("Classes to Include:");
        JScrollPane scrollPane = new JScrollPane(classesList);
        scrollPane.setPreferredSize(new Dimension(20, 100));
        panel.add(label, BorderLayout.NORTH);
        JPanel scrollPanel = new JPanel(new BorderLayout());
        scrollPanel.add(scrollPane, BorderLayout.NORTH);
        panel.add(scrollPanel);
        return panel;
    }

    public void refresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        // NOTE Auto-generated method stub

    }

    public void observe(ViewControlStrategyMeasuresTabPresenter presenter) {
        this.presenter = presenter;
    }

    public void notifyStrategyTypeChange(StrategyType strategyType) {
        // NOTE Auto-generated method stub

    }

    public void notifyStrategyRun(ControlStrategy controlStrategy) {
        // NOTE Auto-generated method stub

    }

    public void run(ControlStrategy controlStrategy) {
        // NOTE Auto-generated method stub

    }
}