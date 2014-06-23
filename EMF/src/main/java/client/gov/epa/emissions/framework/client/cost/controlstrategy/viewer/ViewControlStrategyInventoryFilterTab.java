package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.BorderlessButton;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.DisabledBorderlessButton;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.framework.client.EmfPanel;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.ControlStrategyInputDatasetTableData;
import gov.epa.emissions.framework.client.data.viewer.DataViewPresenter;
import gov.epa.emissions.framework.client.data.viewer.DataViewer;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

public class ViewControlStrategyInventoryFilterTab extends EmfPanel implements ViewControlStrategyTabView {

    private TextArea filter;

    private ControlStrategy controlStrategy;

    private ControlStrategyInputDatasetTableData tableData;

    private SelectableSortFilterWrapper table;

    private JPanel tablePanel;

    private ViewControlStrategyPresenter viewControlStrategyPresenter;

    private ComboBox version;

    private ComboBox dataset;

    private JCheckBox mergeInventories;

    public ViewControlStrategyInventoryFilterTab(ControlStrategy controlStrategy, MessagePanel messagePanel,
            EmfConsole parentConsole, DesktopManager desktopManager,
            ViewControlStrategyPresenter viewControlStrategyPresenter) throws EmfException {

        super("csFilter", parentConsole, desktopManager, messagePanel);

        this.controlStrategy = controlStrategy;
        this.viewControlStrategyPresenter = viewControlStrategyPresenter;

        doLayout(controlStrategy.getControlStrategyInputDatasets());
    }

    private void doLayout(ControlStrategyInputDataset[] controlStrategyInputDatasets) throws EmfException {

        this.tableData = new ControlStrategyInputDatasetTableData(controlStrategyInputDatasets);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createMiddleSection(controlStrategy), BorderLayout.CENTER);

        setLayout(new BorderLayout(5, 5));
        add(panel, BorderLayout.SOUTH);

        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.add(buildSortFilterPanel(), BorderLayout.CENTER);

        notifyStrategyTypeChange(controlStrategy.getStrategyType());
    }

    private JPanel buildSortFilterPanel() {

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new Border("Inventories to Process"));
        panel.add(tablePanel(), BorderLayout.CENTER);
        panel.add(buttonPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel tablePanel() {

        this.tablePanel = new JPanel(new BorderLayout());
        this.table = new SelectableSortFilterWrapper(this.getParentConsole(), this.tableData, null);
        this.tablePanel.add(this.table);

        return tablePanel;
    }

    private JPanel buttonPanel() {

        JPanel panel = new JPanel();
        JButton addButton = new DisabledBorderlessButton("Add");
        panel.add(addButton);

        JButton editButton = new DisabledBorderlessButton("Set Version");
        panel.add(editButton);

        JButton removeButton = new DisabledBorderlessButton("Remove");
        panel.add(removeButton);

        Button viewButton = new BorderlessButton("View", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    viewAction();
                } catch (EmfException e) {
                    showError("Error viewing dataset: " + e.getMessage());
                }
            }
        });
        panel.add(viewButton);

        Button viewDataButton = new BorderlessButton("View Data", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    viewDataAction();
                } catch (EmfException e) {
                    showError("Error viewing dataset data: " + e.getMessage());
                }
            }
        });
        panel.add(viewDataButton);

        JPanel rightPanel = new JPanel();
        mergeInventories = new JCheckBox("Merge Inventories", null,
                controlStrategy.getMergeInventories() != null ? controlStrategy.getMergeInventories() : true);
        mergeInventories.setEnabled(false);
        rightPanel.add(mergeInventories);
        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.LINE_START);
        container.add(rightPanel, BorderLayout.LINE_END);

        return container;
    }

    private void viewDataAction() throws EmfException {

        clearMessage();
        List selected = table.selected();

        if (selected.size() == 0) {
            showError("Please select an item to view.");
            return;
        }

        for (int i = 0; i < selected.size(); i++) {
            EmfDataset dataset = viewControlStrategyPresenter
                    .getDataset(((ControlStrategyInputDataset) selected.get(i)).getInputDataset().getId());
            showDatasetDataViewer(dataset);
        }
    }

    private void refresh() {

        this.table.refresh(tableData);
        panelRefresh();
    }

    private void panelRefresh() {

        this.tablePanel.removeAll();
        this.tablePanel.add(table);
        this.validate();
    }

    private void fillVersions(EmfDataset dataset, Integer versionNumber) throws EmfException{
        version.setEnabled(true);

        if (dataset != null && dataset.getName().equals("None")) dataset = null;
        Version[] versions = viewControlStrategyPresenter.getVersions(dataset);
        version.removeAllItems();
        version.setModel(new DefaultComboBoxModel(versions));
        version.revalidate();
        if (versions.length > 0) 
            version.setSelectedIndex(getVersionIndex(versions, dataset, versionNumber));

    }
    
    private int getVersionIndex(Version[] versions, EmfDataset dataset, Integer version) {
//        int defaultversion = dataset.getDefaultVersion();
        
        if (version != null) {
            for (int i = 0; i < versions.length; i++)
                if (version == versions[i].getVersion())
                    return i;
        }

        return 0;
    }

    private void viewAction() throws EmfException {

        clearMessage();

        List selected = table.selected();
        if (selected.size() == 0) {
            showError("Please select an item to view.");
            return;
        }

        for (int i = 0; i < selected.size(); i++) {

            EmfSession session = this.getSession();
            PropertiesViewPresenter presenter = new PropertiesViewPresenter(viewControlStrategyPresenter
                    .getDataset(((ControlStrategyInputDataset) selected.get(i)).getInputDataset().getId()), session);
            DatasetPropertiesViewer view = new DatasetPropertiesViewer(session, this.getParentConsole(), this
                    .getDesktopManager(), true);
            presenter.doDisplay(view);
        }
    }

    private void viewCountyDataset() throws EmfException {

        clearMessage();

        EmfDataset countyDataset = (EmfDataset) dataset.getSelectedItem();
        if (countyDataset == null) {
            showError("Please select an item to view.");
            return;
        }

        EmfSession session = this.getSession();
        PropertiesViewPresenter presenter = new PropertiesViewPresenter(countyDataset, session);
        // editControlStrategyPresenter.getDataset(countyDataset.getId()), session);
        DatasetPropertiesViewer view = new DatasetPropertiesViewer(session, this.getParentConsole(), this
                .getDesktopManager(), true);
        presenter.doDisplay(view);
    }

    private void viewCountyDatasetData() {

        clearMessage();

        EmfDataset countyDataset = (EmfDataset) dataset.getSelectedItem();
        if (countyDataset == null) {
            showError("Please select an item to view.");
            return;
        }

        showDatasetDataViewer(countyDataset);
    }

    private JPanel createMiddleSection(ControlStrategy controlStrategy) throws EmfException {

        JPanel middlePanel = new JPanel(new SpringLayout());
        middlePanel.setBorder(new Border("Filters"));

        String value = controlStrategy.getFilter();
        if (value == null)
            value = "";

        filter = new TextArea("filter", value, 40, 2);
        filter
                .setToolTipText("Enter a filter that could be entered as a SQL where clause (e.g., ANN_EMIS>5000 and SCC like '30300%')");
        filter.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(filter);

        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        layoutGenerator.addLabelWidgetPair("Inventory Filter:", scrollPane, middlePanel);

        EmfDataset[] datasets = viewControlStrategyPresenter.getDatasets(viewControlStrategyPresenter
                .getDatasetType("List of Counties (CSV)"));
        // String width = EmptyStrings.create(80);
        // Dimension size=new Dimension(500, 13);

        dataset = new ComboBox("Not selected", datasets);
        if (controlStrategy.getCountyDataset() != null) {
            dataset.setSelectedItem(controlStrategy.getCountyDataset());
        }

        dataset.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    EmfDataset countyDataset = (EmfDataset)dataset.getSelectedItem();
                    Integer versionNumber = (countyDataset != null ? countyDataset.getDefaultVersion() : null);

                    fillVersions(countyDataset, versionNumber);
                } catch (EmfException e1) {
                    // NOTE Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });

        version = new ComboBox(new Version[0]);

        try {
            EmfDataset countyDataset = controlStrategy.getCountyDataset();
            Integer versionNumber = (countyDataset != null ? controlStrategy.getCountyDatasetVersion() : null);
            fillVersions(countyDataset, versionNumber);
        } catch (EmfException e1) {
            // NOTE Auto-generated catch block
            e1.printStackTrace();
        }

        if (controlStrategy.getCountyDataset() != null) {
            version.setSelectedItem(controlStrategy.getCountyDatasetVersion());
        }

        layoutGenerator.addLabelWidgetPair("County Dataset:", datasetPanel(), middlePanel);
        layoutGenerator.addLabelWidgetPair("County Dataset Version:", version, middlePanel);

        layoutGenerator.makeCompactGrid(middlePanel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 5);// xPad, yPad

        return middlePanel;
    }

    private JPanel datasetPanel() {

        Button viewButton = new BorderlessButton("View", viewDatasetAction());
        JPanel invPanel = new JPanel(new BorderLayout(5, 0));

        invPanel.add(dataset, BorderLayout.LINE_START);
        invPanel.add(viewButton);
        Button viewDataButton = new BorderlessButton("View Data", viewCountyDatasetDataAction());
        invPanel.add(viewDataButton, BorderLayout.LINE_END);
        return invPanel;
    }

    private Action viewDatasetAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    viewCountyDataset();
                } catch (EmfException e) {
                    showError("Error viewing dataset: " + e.getMessage());
                }
            }
        };
    }

    private Action viewCountyDatasetDataAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                viewCountyDatasetData();
            }
        };
    }

    public void save(ControlStrategy controlStrategy) throws EmfException {
        String value = filter.getText().trim();

        controlStrategy.setFilter(value);
        // controlStrategy.setCountyFile(countyFileTextField.getText().trim());
        EmfDataset ds = (EmfDataset) dataset.getSelectedItem();
        if (ds == null) {
            ds = null;
        }
        controlStrategy.setCountyDataset(ds);
        Version ver = (ds != null ? (Version) version.getSelectedItem() : null);
        Integer verValue = (ver != null ? ver.getVersion() : null);
        controlStrategy.setCountyDatasetVersion(verValue);

        ControlStrategyInputDataset[] inputDatasets = {};
        if (tableData != null) {
            inputDatasets = new ControlStrategyInputDataset[tableData.rows().size()];
            for (int i = 0; i < tableData.rows().size(); i++) {
                inputDatasets[i] = (ControlStrategyInputDataset) tableData.element(i);
            }
            controlStrategy.setControlStrategyInputDatasets(inputDatasets);
        }
        controlStrategy.setMergeInventories(mergeInventories.isSelected());
        // make sure if there are multiple inventories for the least cost strategies,
        // then enforce merging datasets, this type of strategy only takes one input inventory.
        if (inputDatasets.length > 1
                && (controlStrategy.getStrategyType().getName().equals(StrategyType.leastCost) || controlStrategy
                        .getStrategyType().getName().equals(StrategyType.leastCostCurve))
                && !controlStrategy.getMergeInventories())
            throw new EmfException(
                    "Inventories Tab: Multiple inventories must be merged.  Check the Merge Inventories checkbock.");
    }

    public void refresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        tableData.add(controlStrategy.getControlStrategyInputDatasets());
        refresh();
    }

    public void notifyStrategyTypeChange(StrategyType strategyType) {

        if (strategyType != null
                && (strategyType.getName().equals(StrategyType.leastCost) || strategyType.getName().equals(
                        StrategyType.leastCostCurve)))
            mergeInventories.setVisible(true);
        else
            mergeInventories.setVisible(false);
    }

    public void notifyStrategyRun(ControlStrategy controlStrategy) {
        // NOTE Auto-generated method stub

    }

    private void showDatasetDataViewer(EmfDataset dataset) {
        try {

            EmfSession session = this.getSession();
            EmfConsole parentConsole = this.getParentConsole();
            DesktopManager desktopManager = this.getDesktopManager();

            Version[] versions = viewControlStrategyPresenter.getVersions(dataset);
            // if just one version, then go directly to the dataviewer
            if (versions.length == 1) {

                DataViewer dataViewerView = new DataViewer(dataset, parentConsole, desktopManager, true);
                DataViewPresenter dataViewPresenter = new DataViewPresenter(dataset, versions[0],
                        getTableName(dataset), dataViewerView, session);
                dataViewPresenter.display();
                // else goto to dataset editior and display different version to display
            } else {

                DatasetPropertiesViewer datasetPropertiesViewerView = new DatasetPropertiesViewer(session,
                        parentConsole, desktopManager, true);
                viewControlStrategyPresenter.doDisplayPropertiesView(datasetPropertiesViewerView, dataset);
                datasetPropertiesViewerView.setDefaultTab(1);
            }
            // presenter.doView(version, table, view);
        } catch (EmfException e) {
            // displayError(e.getMessage());
        }
    }

    protected String getTableName(Dataset dataset) {
        InternalSource[] internalSources = dataset.getInternalSources();
        String tableName = "";
        if (internalSources.length > 0)
            tableName = internalSources[0].getTable();
        return tableName;
    }

    public void run(ControlStrategy controlStrategy) {
        // NOTE Auto-generated method stub

    }
}