package gov.epa.emissions.framework.client.tempalloc.editor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.viewer.DataViewPresenter;
import gov.epa.emissions.framework.client.data.viewer.DataViewer;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesEditor;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.client.meta.PropertiesEditorPresenter;
import gov.epa.emissions.framework.client.meta.PropertiesEditorPresenterImpl;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocation;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocationOutput;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class TemporalAllocationOutputTab extends JPanel implements TemporalAllocationTabView {
    
    private TemporalAllocation temporalAllocation;

    private ManageChangeables changeablesList;

    private EmfSession session;

    private MessagePanel messagePanel;
    
    private EmfConsole parentConsole;
    
    private DesktopManager desktopManager;
    
    private TemporalAllocationPresenter presenter;
    
    private SelectableSortFilterWrapper table;
    
    public TemporalAllocationOutputTab(TemporalAllocation temporalAllocation, EmfSession session, 
            ManageChangeables changeablesList, SingleLineMessagePanel messagePanel, 
            EmfConsole parentConsole, DesktopManager desktopManager,
            TemporalAllocationPresenter presenter) {
        super.setName("output");
        this.temporalAllocation = temporalAllocation;
        this.session = session;
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManager;
        this.presenter = presenter;
    }
    
    public void setTemporalAllocation(TemporalAllocation temporalAllocation) {
        this.temporalAllocation = temporalAllocation;
    }
    
    public void display() {
        super.setLayout(new BorderLayout());
        super.add(tablePanel(), BorderLayout.CENTER);
        super.add(buttonPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel tablePanel() {
        TemporalAllocationOutput[] outputs = null;
        try {
            outputs = session.temporalAllocationService().getTemporalAllocationOutputs(temporalAllocation);
        } catch (Exception e) {
            //
        }
        TemporalAllocationOutputTableData tableData = new TemporalAllocationOutputTableData(outputs);
        table = new SelectableSortFilterWrapper(parentConsole, tableData, null);
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(table);
        return tablePanel;
    }
    
    private JPanel buttonPanel() {
        JPanel buttonPanel = new JPanel();
        Button viewButton = new Button("View Properties", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                messagePanel.clear();
                try {
                    viewDataset();
                } catch (EmfException e) {
                    messagePanel.setError("Error viewing dataset: " + e.getMessage());
                }
            }
        });
        viewButton.setMnemonic(KeyEvent.VK_V);
        buttonPanel.add(viewButton);
        Button viewDataButton = new Button("View Data", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                messagePanel.clear();
                viewData();
            }
        });
        viewDataButton.setMnemonic(KeyEvent.VK_D);
        buttonPanel.add(viewDataButton);
        Button summarizeButton = new Button("Summarize", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                messagePanel.clear();
                try {
                    summarize();
                } catch (EmfException e) {
                    messagePanel.setError("Error viewing dataset: " + e.getMessage());
                }
            }
        });
        summarizeButton.setMnemonic(KeyEvent.VK_Z);
        buttonPanel.add(summarizeButton);
        return buttonPanel;
    }
    
    private TemporalAllocationOutput[] getSelectedOutputs() {
        return table.selected().toArray(new TemporalAllocationOutput[0]);
    }
    
    private void viewDataset() throws EmfException {
        TemporalAllocationOutput[] selectedOutputs = getSelectedOutputs();
        if (selectedOutputs.length == 0) {
            messagePanel.setError("Please select at least one item.");
            return;
        }
        
        for (TemporalAllocationOutput output : selectedOutputs) {
            EmfDataset dataset = output.getOutputDataset();
            if (dataset != null) {
                PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
                DatasetPropertiesViewer view = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
                presenter.doDisplay(view);
            }
        }
    }
    
    private void viewData() {
        TemporalAllocationOutput[] selectedOutputs = getSelectedOutputs();
        if (selectedOutputs.length == 0) {
            messagePanel.setError("Please select at least one item.");
            return;
        }
        
        for (TemporalAllocationOutput output : selectedOutputs) {
            EmfDataset dataset = output.getOutputDataset();
            if (dataset != null) {
                try {
                    Version[] versions = presenter.getVersions(dataset);
                    //if just one version, then go directly to the dataviewer
                    if (versions.length == 1) {
                        DataViewer dataViewerView = new DataViewer(dataset, parentConsole, desktopManager);
                        DataViewPresenter dataViewPresenter = new DataViewPresenter(dataset, versions[0], presenter.getTableName(dataset), dataViewerView, session);
                        dataViewPresenter.display();
                    //else goto to dataset editior and display different version to display
                    } else {
                        DatasetPropertiesViewer datasetPropertiesViewerView = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
                        presenter.doDisplayPropertiesView(datasetPropertiesViewerView, dataset);
                        datasetPropertiesViewerView.setDefaultTab(1);
                    }
//                    presenter.doView(version, table, view);
                } catch (EmfException e) {
//                    displayError(e.getMessage());
                }
            }
        }
    }
    
    private void summarize() throws EmfException {
        TemporalAllocationOutput[] selectedOutputs = getSelectedOutputs();
        if (selectedOutputs.length == 0) {
            messagePanel.setError("Please select at least one item.");
            return;
        }
        
        for (TemporalAllocationOutput output : selectedOutputs) {
            EmfDataset dataset = output.getOutputDataset();
            if (dataset != null) {
                DatasetPropertiesEditor view = new DatasetPropertiesEditor(session, parentConsole, desktopManager);
                PropertiesEditorPresenter presenter = new PropertiesEditorPresenterImpl(dataset, view, session);
                presenter.doDisplay();
                view.setDefaultTab(7);
            }
        }
        
    }
    
    public void save() {
        
    }
    
    public void refresh() {
        TemporalAllocationOutput[] outputs = null;
        try {
            outputs = session.temporalAllocationService().getTemporalAllocationOutputs(temporalAllocation);
        } catch (Exception e) {
            //
        }
        TemporalAllocationOutputTableData tableData = new TemporalAllocationOutputTableData(outputs);
        table.refresh(tableData);
    }
    
    public void prepareRun() throws EmfException {
        // check if run has existing outputs
        TemporalAllocationOutput[] outputs = null;
        try {
            outputs = session.temporalAllocationService().getTemporalAllocationOutputs(temporalAllocation);
        } catch (Exception e) {
            throw new EmfException("Could not check existing run outputs");
        }

        if (outputs != null && outputs.length > 0) {
            String title = "Warning";
            String message = "There are results available for this temporal allocation.\n"
                    + "Re-running it will delete the existing outputs and any QA steps.\n"
                    + "Are you sure you want to re-run?";
            int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (selection == JOptionPane.CANCEL_OPTION) {
                throw new EmfException("Run cancelled.");
            }
        }    
    }
}
