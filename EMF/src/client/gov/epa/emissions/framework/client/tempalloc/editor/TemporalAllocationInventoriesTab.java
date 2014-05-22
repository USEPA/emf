package gov.epa.emissions.framework.client.tempalloc.editor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.BorderlessButton;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocation;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocationInputDataset;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import javax.swing.AbstractAction;
import javax.swing.JPanel;

public class TemporalAllocationInventoriesTab extends JPanel implements TemporalAllocationTabView {
    
    private TemporalAllocation temporalAllocation;

    private ManageChangeables changeablesList;

    private EmfSession session;

    private MessagePanel messagePanel;
    
    private EmfConsole parentConsole;
    
    private TemporalAllocationInventoriesTableData tableData;

    private SelectableSortFilterWrapper table;

    private JPanel tablePanel;
    
    public TemporalAllocationInventoriesTab(TemporalAllocation temporalAllocation, EmfSession session, ManageChangeables changeablesList, SingleLineMessagePanel messagePanel, EmfConsole parentConsole) {
        super.setName("inventories");
        this.temporalAllocation = temporalAllocation;
        tableData = new TemporalAllocationInventoriesTableData(temporalAllocation.getTemporalAllocationInputDatasets());
        this.session = session;
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.parentConsole = parentConsole;
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
                //if (inputDatasets.length > 0) editControlStrategyPresenter.fireTracking();
                //refresh();
            }
        } catch (Exception exp) {
            messagePanel.setError(exp.getMessage());
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
}
