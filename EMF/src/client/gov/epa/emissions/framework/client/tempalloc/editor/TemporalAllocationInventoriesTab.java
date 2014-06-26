package gov.epa.emissions.framework.client.tempalloc.editor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.BorderlessButton;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionView;
import gov.epa.emissions.framework.client.data.viewer.DataViewPresenter;
import gov.epa.emissions.framework.client.data.viewer.DataViewer;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocation;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocationInputDataset;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class TemporalAllocationInventoriesTab extends JPanel implements TemporalAllocationTabView {
    
    private TemporalAllocation temporalAllocation;

    private ManageChangeables changeablesList;

    private EmfSession session;

    private MessagePanel messagePanel;
    
    private EmfConsole parentConsole;
    
    private DesktopManager desktopManager;
    
    private TemporalAllocationInventoriesTableData tableData;

    private SelectableSortFilterWrapper table;

    private JPanel tablePanel;
    
    public TemporalAllocationInventoriesTab(TemporalAllocation temporalAllocation, EmfSession session, 
            ManageChangeables changeablesList, SingleLineMessagePanel messagePanel, 
            EmfConsole parentConsole, DesktopManager desktopManager) {
        super.setName("inventories");
        this.temporalAllocation = temporalAllocation;
        tableData = new TemporalAllocationInventoriesTableData(temporalAllocation.getTemporalAllocationInputDatasets());
        this.session = session;
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManager;
    }
    
    public void display() {
        super.setLayout(new BorderLayout());
        super.add(buildSortFilterPanel(), BorderLayout.CENTER);
    }

    private JPanel buildSortFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new Border("Inventories to Process"));
        panel.add(tablePanel(), BorderLayout.CENTER);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        return panel; 
    }
    
    private JPanel tablePanel() {
        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, tableData, null);
        tablePanel.add(table);

        return tablePanel;
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        Button addButton = new BorderlessButton("Add", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    addAction();
                } catch (EmfException e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        panel.add(addButton);
        Button editButton = new BorderlessButton("Set Version", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                    //setVersionAction();
            }
        });
        panel.add(editButton);
        Button removeButton = new BorderlessButton("Remove", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {           
                    removeAction();
            }
        });
        panel.add(removeButton);
        Button viewButton = new BorderlessButton("View", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    viewAction();
                } catch (EmfException e) {
                    messagePanel.setError("Error viewing dataset: " + e.getMessage());
                }
            }
        });
        panel.add(viewButton);
        Button viewDataButton = new BorderlessButton("View Data", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    viewDataAction();
                } catch (EmfException e) {
                    messagePanel.setError("Error viewing dataset data: " + e.getMessage());
                }
            }
        });
        panel.add(viewDataButton);

        return panel;
    }

    private void addAction() throws EmfException {
        InputDatasetSelectionView view = new InputDatasetSelectionDialog(parentConsole);
        InputDatasetSelectionPresenter presenter = new InputDatasetSelectionPresenter(view, session,
                new DatasetType[] { 
                    session.getLightDatasetType(DatasetType.orlPointInventory),
                    session.getLightDatasetType(DatasetType.orlPointInventory),
                    session.getLightDatasetType(DatasetType.orlNonpointInventory),
                    session.getLightDatasetType(DatasetType.orlNonroadInventory),
                    session.getLightDatasetType(DatasetType.orlOnroadInventory),
                    session.getLightDatasetType(DatasetType.FLAT_FILE_2010_POINT),
                    session.getLightDatasetType(DatasetType.FLAT_FILE_2010_NONPOINT)
                });
        try {
            presenter.display(null, false);
            if (view.shouldCreate()){
                EmfDataset[] inputDatasets = presenter.getDatasets();
                TemporalAllocationInputDataset[] temporalAllocationInputDatasets = new TemporalAllocationInputDataset[inputDatasets.length];
                for (int i = 0; i < inputDatasets.length; i++) {
                    temporalAllocationInputDatasets[i] = new TemporalAllocationInputDataset(inputDatasets[i]);
                    temporalAllocationInputDatasets[i].setVersion(inputDatasets[i].getDefaultVersion());
                }
                tableData.add(temporalAllocationInputDatasets);
                //if (inputDatasets.length > 0) editPresenter.fireTracking();
                refresh();
            }
        } catch (Exception exp) {
            messagePanel.setError(exp.getMessage());
        }
    }

    private void viewAction() throws EmfException {
        messagePanel.clear();
        List selected = table.selected();

        if (selected.size() == 0) {
            messagePanel.setError("Please select an item to view.");
            return;
        }

        for (int i = 0; i < selected.size(); i++) {
            TemporalAllocationInputDataset inputDataset = (TemporalAllocationInputDataset)selected.get(i);
            PropertiesViewPresenter presenter = new PropertiesViewPresenter(inputDataset.getInputDataset(), session);
            DatasetPropertiesViewer view = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
            presenter.doDisplay(view);
        }
    }

    private void viewDataAction() throws EmfException {
        messagePanel.clear();
        List selected = table.selected();

        if (selected.size() == 0) {
            messagePanel.setError("Please select an item to view.");
            return;
        }

        for (int i = 0; i < selected.size(); i++) {
            TemporalAllocationInputDataset inputDataset = (TemporalAllocationInputDataset)selected.get(i);
            showDatasetDataViewer(inputDataset.getInputDataset());
        }
    }
    
    private void showDatasetDataViewer(EmfDataset dataset) {
//        try {
//            Version[] versions = editPresenter.getVersions(dataset);
//            //if just one version, then go directly to the dataviewer
//            if (versions.length == 1) {
//                DataViewer dataViewerView = new DataViewer(dataset, parentConsole, desktopManager);
//                DataViewPresenter dataViewPresenter = new DataViewPresenter(dataset, versions[0], getTableName(dataset), dataViewerView, session);
//                dataViewPresenter.display();
//            //else goto to dataset editior and display different version to display
//            } else {
//                DatasetPropertiesViewer datasetPropertiesViewerView = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
//                editControlStrategyPresenter.doDisplayPropertiesView(datasetPropertiesViewerView, dataset);
//                datasetPropertiesViewerView.setDefaultTab(1);
//            }
////            presenter.doView(version, table, view);
//        } catch (EmfException e) {
////            displayError(e.getMessage());
//        }
    }
 
    protected void removeAction() {
        messagePanel.clear();
        List selected = table.selected();

        if (selected.size() == 0) {
            messagePanel.setError("Please select an inventory to remove.");
            return;
        }

        TemporalAllocationInputDataset[] inputDatasets = (TemporalAllocationInputDataset[])selected.toArray(new TemporalAllocationInputDataset[0]);

        if (inputDatasets.length == 0)
            return;

        String title = "Warning";
        String message = "Are you sure you want to remove the selected inventories?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            tableData.remove(inputDatasets);
            //if (inputDatasets.length > 0) editPresenter.fireTracking();
            refresh();
        }
    }
    
    public void save() {
        TemporalAllocationInputDataset[] inputDatasets = {};
        if (tableData != null) {
            inputDatasets = new TemporalAllocationInputDataset[tableData.rows().size()];
            for (int i = 0; i < tableData.rows().size(); i++) {
                inputDatasets[i] = (TemporalAllocationInputDataset)tableData.element(i);
            }
            temporalAllocation.setTemporalAllocationInputDatasets(inputDatasets);
        }
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
}
