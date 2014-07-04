package gov.epa.emissions.framework.client.tempalloc.editor;

import java.awt.BorderLayout;

import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocation;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class TemporalAllocationProfilesTab extends JPanel implements TemporalAllocationTabView {
    
    private TemporalAllocation temporalAllocation;

    private ManageChangeables changeablesList;

    private EmfSession session;

    private MessagePanel messagePanel;
    
    private ComboBox xrefDataset, monthlyProfileDataset, weeklyProfileDataset, dailyProfileDataset;
    
    private ComboBox xrefVersion, monthlyProfileVersion, weeklyProfileVersion, dailyProfileVersion;
    
    public TemporalAllocationProfilesTab(TemporalAllocation temporalAllocation, EmfSession session, ManageChangeables changeablesList, SingleLineMessagePanel messagePanel) {
        super.setName("profiles");
        this.temporalAllocation = temporalAllocation;
        this.session = session;
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
    }
    
    public void setTemporalAllocation(TemporalAllocation temporalAllocation) {
        this.temporalAllocation = temporalAllocation;
    }
    
    public void display() {
        super.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new SpringLayout());
        
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        
        EmfDataset[] datasets = null;
        try {
            datasets = session.dataService().getDatasets(session.getLightDatasetType("Temporal Cross Reference (CSV)"));
        } catch (EmfException e) {
            messagePanel.setMessage(e.getMessage());
        }
        xrefDataset = new ComboBox("Not selected", datasets);
        if (temporalAllocation.getXrefDataset() != null) {
            xrefDataset.setSelectedItem(temporalAllocation.getXrefDataset());
        }
        changeablesList.addChangeable(xrefDataset);
        layoutGenerator.addLabelWidgetPair("Cross-Reference Dataset:", xrefDataset, panel);
        
        try {
            datasets = session.dataService().getDatasets(session.getLightDatasetType("Temporal Profile Monthly (CSV)"));
        } catch (EmfException e) {
            messagePanel.setMessage(e.getMessage());
        }
        monthlyProfileDataset = new ComboBox("Not selected", datasets);
        if (temporalAllocation.getMonthlyProfileDataset() != null) {
            monthlyProfileDataset.setSelectedItem(temporalAllocation.getMonthlyProfileDataset());
        }
        changeablesList.addChangeable(monthlyProfileDataset);
        layoutGenerator.addLabelWidgetPair("Year-To-Month Profile Dataset:", monthlyProfileDataset, panel);

        try {
            datasets = session.dataService().getDatasets(session.getLightDatasetType("Temporal Profile Weekly (CSV)"));
        } catch (EmfException e) {
            messagePanel.setMessage(e.getMessage());
        }
        weeklyProfileDataset = new ComboBox("Not selected", datasets);
        if (temporalAllocation.getWeeklyProfileDataset() != null) {
            weeklyProfileDataset.setSelectedItem(temporalAllocation.getWeeklyProfileDataset());
        }
        changeablesList.addChangeable(weeklyProfileDataset);
        layoutGenerator.addLabelWidgetPair("Week-To-Day Profile Dataset:", weeklyProfileDataset, panel);

        try {
            datasets = session.dataService().getDatasets(session.getLightDatasetType("Temporal Profile Daily (CSV)"));
        } catch (EmfException e) {
            messagePanel.setMessage(e.getMessage());
        }
        dailyProfileDataset = new ComboBox("Not selected", datasets);
        if (temporalAllocation.getDailyProfileDataset() != null) {
            dailyProfileDataset.setSelectedItem(temporalAllocation.getDailyProfileDataset());
        }
        changeablesList.addChangeable(dailyProfileDataset);
        layoutGenerator.addLabelWidgetPair("Month-to-Day Profile Dataset:", dailyProfileDataset, panel);

        layoutGenerator.makeCompactGrid(panel, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 5);// xPad, yPad
        
        super.add(panel, BorderLayout.NORTH);
    }
    
    public void save() {
        temporalAllocation.setXrefDataset((EmfDataset)xrefDataset.getSelectedItem());
        temporalAllocation.setMonthlyProfileDataset((EmfDataset)monthlyProfileDataset.getSelectedItem());
        temporalAllocation.setWeeklyProfileDataset((EmfDataset)weeklyProfileDataset.getSelectedItem());
        temporalAllocation.setDailyProfileDataset((EmfDataset)dailyProfileDataset.getSelectedItem());
    }
}
