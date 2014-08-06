package gov.epa.emissions.framework.client.tempalloc.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.BorderlessButton;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.viewer.DataViewPresenter;
import gov.epa.emissions.framework.client.data.viewer.DataViewer;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocation;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class TemporalAllocationProfilesTab extends JPanel implements TemporalAllocationTabView {
    
    private TemporalAllocation temporalAllocation;

    private ManageChangeables changeablesList;

    private EmfSession session;

    private MessagePanel messagePanel;
    
    private EmfConsole parentConsole;
    
    private DesktopManager desktopManager;
    
    private TemporalAllocationPresenter presenter;
    
    private ComboBox xrefDataset, monthlyProfileDataset, weeklyProfileDataset, dailyProfileDataset;
    
    private ComboBox xrefVersion, monthlyProfileVersion, weeklyProfileVersion, dailyProfileVersion;
    
    public TemporalAllocationProfilesTab(TemporalAllocation temporalAllocation, EmfSession session, 
            ManageChangeables changeablesList, SingleLineMessagePanel messagePanel,
            EmfConsole parentConsole, DesktopManager desktopManager,
            TemporalAllocationPresenter presenter) {
        super.setName("profiles");
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
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        xrefDataset = new ComboBox("Not selected", new EmfDataset[0]);
        xrefVersion = new ComboBox(new Version[0]);
        super.add(datasetPanel(session.getLightDatasetType("Temporal Cross Reference (CSV)"), 
                "Cross-Reference Dataset", 
                xrefDataset, temporalAllocation.getXrefDataset(), 
                xrefVersion, temporalAllocation.getXrefDatasetVersion()));
        
        monthlyProfileDataset = new ComboBox("Not selected", new EmfDataset[0]);
        monthlyProfileVersion = new ComboBox(new Version[0]);
        super.add(datasetPanel(session.getLightDatasetType("Temporal Profile Monthly (CSV)"), 
                "Year-to-Month Profile Dataset", 
                monthlyProfileDataset, temporalAllocation.getMonthlyProfileDataset(),
                monthlyProfileVersion, temporalAllocation.getMonthlyProfileDatasetVersion()));
        
        weeklyProfileDataset = new ComboBox("Not selected", new EmfDataset[0]);
        weeklyProfileVersion = new ComboBox(new Version[0]);
        super.add(datasetPanel(session.getLightDatasetType("Temporal Profile Weekly (CSV)"), 
                "Week-to-Day Profile Dataset", 
                weeklyProfileDataset, temporalAllocation.getWeeklyProfileDataset(),
                weeklyProfileVersion, temporalAllocation.getWeeklyProfileDatasetVersion()));
        
        dailyProfileDataset = new ComboBox("Not selected", new EmfDataset[0]);
        dailyProfileVersion = new ComboBox(new Version[0]);
        super.add(datasetPanel(session.getLightDatasetType("Temporal Profile Daily (CSV)"), 
                "Month-to-Day Profile Dataset", 
                dailyProfileDataset, temporalAllocation.getDailyProfileDataset(),
                dailyProfileVersion, temporalAllocation.getDailyProfileDatasetVersion()));
    }
    
    private JPanel datasetPanel(DatasetType datasetType, String label, ComboBox selectDataset, 
            EmfDataset currentDataset, ComboBox selectVersion, Integer currentVersion) {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(new Border(label));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        
        EmfDataset[] datasets = null;
        try {
            datasets = session.dataService().getDatasets(datasetType);
        } catch (EmfException e) {
            messagePanel.setMessage(e.getMessage());
        }
        selectDataset.resetModel(datasets);
        selectDataset.setPreferredSize(new Dimension(500, selectDataset.getHeight()));
        if (currentDataset != null) {
            selectDataset.setSelectedItem(currentDataset);
        }
        changeablesList.addChangeable(selectDataset);
        layoutGenerator.addLabelWidgetPair("Dataset:", selectDataset, panel);
        
        selectVersion.setPreferredSize(new Dimension(200, selectVersion.getHeight()));
        if (currentDataset != null) {
            fillVersions(selectVersion, currentDataset);
        }
        if (currentVersion != null) {
            selectVersion.setSelectedItem(currentVersion);
        }
        changeablesList.addChangeable(selectVersion);
        
        JPanel versionPanel = new JPanel(new BorderLayout(5, 0));
        versionPanel.add(selectVersion, BorderLayout.LINE_START);

        Button viewButton = new BorderlessButton("View", new ViewDatasetAction(selectDataset));
        versionPanel.add(viewButton);

        viewButton = new BorderlessButton("View Data", new ViewDataAction(selectDataset));
        versionPanel.add(viewButton, BorderLayout.LINE_END);
        
        layoutGenerator.addLabelWidgetPair("Version:", versionPanel, panel);
        
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 5);// xPad, yPad
        
        selectDataset.addActionListener(new SelectDatasetAction(selectDataset, selectVersion));
        
        return panel;
    }
    
    public void save() {
        temporalAllocation.setXrefDataset((EmfDataset)xrefDataset.getSelectedItem());
        if (temporalAllocation.getXrefDataset() != null &&
            xrefVersion.getSelectedItem() != null) {
            Integer version = ((Version)xrefVersion.getSelectedItem()).getVersion();
            temporalAllocation.setXrefDatasetVersion(version);
        } else {
            temporalAllocation.setXrefDatasetVersion(null);
        }

        temporalAllocation.setMonthlyProfileDataset((EmfDataset)monthlyProfileDataset.getSelectedItem());
        if (temporalAllocation.getMonthlyProfileDataset() != null &&
            monthlyProfileVersion.getSelectedItem() != null) {
            Integer version = ((Version)monthlyProfileVersion.getSelectedItem()).getVersion();
            temporalAllocation.setMonthlyProfileDatasetVersion(version);
        } else {
            temporalAllocation.setMonthlyProfileDatasetVersion(null);
        }

        temporalAllocation.setWeeklyProfileDataset((EmfDataset)weeklyProfileDataset.getSelectedItem());
        if (temporalAllocation.getWeeklyProfileDataset() != null &&
            weeklyProfileVersion.getSelectedItem() != null) {
            Integer version = ((Version)weeklyProfileVersion.getSelectedItem()).getVersion();
            temporalAllocation.setWeeklyProfileDatasetVersion(version);
        } else {
            temporalAllocation.setWeeklyProfileDatasetVersion(null);
        }

        temporalAllocation.setDailyProfileDataset((EmfDataset)dailyProfileDataset.getSelectedItem());
        if (temporalAllocation.getDailyProfileDataset() != null &&
            dailyProfileVersion.getSelectedItem() != null) {
            Integer version = ((Version)dailyProfileVersion.getSelectedItem()).getVersion();
            temporalAllocation.setDailyProfileDatasetVersion(version);
        } else {
            temporalAllocation.setDailyProfileDatasetVersion(null);
        }
    }
    
    private void fillVersions(ComboBox versionSelector, EmfDataset dataset) {
        try {
            Version[] versions = presenter.getVersions(dataset);
            versionSelector.removeAllItems();
            versionSelector.setModel(new DefaultComboBoxModel(versions));
            versionSelector.revalidate();
        } catch (EmfException e) {
            //
        }
    }
    
    private class SelectDatasetAction extends AbstractAction {
        
        ComboBox datasetSelector, versionSelector;

        public SelectDatasetAction(ComboBox datasetSelector, ComboBox versionSelector) {
            this.datasetSelector = datasetSelector;
            this.versionSelector = versionSelector;
        }
        
        public void actionPerformed(ActionEvent event) {
            EmfDataset dataset = (EmfDataset)datasetSelector.getSelectedItem();
            fillVersions(versionSelector, dataset);
            if (dataset != null) {
                versionSelector.setSelectedItem(dataset.getDefaultVersion());
            }
        }
    }
    
    private class ViewDatasetAction extends AbstractAction {
        ComboBox datasetSelector;
        
        public ViewDatasetAction(ComboBox datasetSelector) {
            this.datasetSelector = datasetSelector;
        }
        
        public void actionPerformed(ActionEvent event) {
            messagePanel.clear();
            
            if (datasetSelector.getSelectedItem() != null) {
                EmfDataset dataset = (EmfDataset)datasetSelector.getSelectedItem();
                PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
                DatasetPropertiesViewer view = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
                try {
                    presenter.doDisplay(view);
                } catch (EmfException e) {
                    messagePanel.setError("Error viewing dataset: " + e.getMessage());
                }
            } else {
                messagePanel.setError("Please select a dataset to view.");
            }
        }
    }
    
    private class ViewDataAction extends AbstractAction {
        ComboBox datasetSelector;
        
        public ViewDataAction(ComboBox datasetSelector) {
            this.datasetSelector = datasetSelector;
        }
        
        public void actionPerformed(ActionEvent event) {
            messagePanel.clear();
            
            if (datasetSelector.getSelectedItem() != null) {
                EmfDataset dataset = (EmfDataset)datasetSelector.getSelectedItem();
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
                } catch (EmfException e) {
                    messagePanel.setError("Error viewing data: " + e.getMessage());
                }
            } else {
                messagePanel.setError("Please select a dataset to view.");
            }
            
        }
    }
}
