package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.AddRemoveRegionsWidget;
import gov.epa.emissions.framework.client.swingworker.RefreshSwingWorkerTasks;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ViewableCaseSummaryTab extends EditableCaseSummaryTab {

  
    public ViewableCaseSummaryTab(Case caseObj, MessagePanel messagePanel,
            EmfConsole parentConsole) {
        super(messagePanel, parentConsole);
        super.setName("viewSummary");
        this.caseObj = caseObj;
    }

    public void display() throws EmfException {
        setLayout();
        super.validate();
        viewOnly();
    }

    private void viewOnly(){
        name.setEditable(false);
        futureYear.setEditable(false);
        template.setEditable(false);
        description.setEditable(false);
        isFinal.setEnabled(false);
        isTemplate.setEnabled(false);
        startDate.setEditable(false);
        endDate.setEditable(false);
        numMetLayers.setEditable(false);
        numEmissionLayers.setEditable(false);
        description.setEditable(false);
        abbreviationsCombo.setEditable(false);
        airQualityModelsCombo.setEditable(false);
        emissionsYearCombo.setEditable(false);
        meteorlogicalYearCombo.setEditable(false);
        speciationCombo.setEditable(false);
       
    }


    private void setLayout() throws EmfException {
        super.setLayout(new BorderLayout());
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        layout.add(createOverviewSection());
        layout.add(createLowerSection());

        super.add(layout, BorderLayout.CENTER);
    } 

    public void doRefresh() throws EmfException {
        new RefreshSwingWorkerTasks(layout, messagePanel, presenter).execute();
    }
    
    public void refresh(Case caseObj){
        this.caseObj = caseObj;
        super.removeAll();
        try {
            setLayout();
            super.validate();
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        viewOnly();
    }
    
    private JPanel createOverviewSection() throws EmfException {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

        JPanel container = new JPanel();
        container.setLayout(new GridLayout(1, 2));
        container.add(createLeftOverviewSection());
        container.add(createRightOverviewSection());

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }
    
    private JPanel createLowerSection() throws EmfException {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

        JPanel container = new JPanel();
        container.setLayout(new GridLayout(1, 2));
        container.add(createLowerLeftSection());
        container.add(createLowerRightSection());

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }
    
    private JPanel createLowerLeftSection() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Model & Version:", modelToRun(), panel);
        layoutGenerator.addLabelWidgetPair("Modeling Region:", modRegions(), panel);
        layoutGenerator.addLabelWidgetPair("<html>Regions:<br><br><br></html>", regions(), panel);
        layoutGenerator.addLabelWidgetPair("Met/Emis Layers:", metEmisLayers(), panel);
        layoutGenerator.addLabelWidgetPair("Start Date & Time: ", startDate(), panel);

        layoutGenerator.makeCompactGrid(panel, 5, 2, 10, 10, 5, 10);

        return panel;
    }
    
    protected JPanel createRightOverviewSection() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Abbreviation:", abbreviations(), panel);
        JPanel finalTemplatePanel = new JPanel(new GridLayout(1, 2));
        finalTemplatePanel.add(isFinal());
        finalTemplatePanel.add(isTemplate());
        layoutGenerator.addLabelWidgetPair("Is Final:", finalTemplatePanel, panel);
        layoutGenerator.addLabelWidgetPair("<html>Sectors:<br><br><br></html>", sectors(), panel);
        layoutGenerator.addLabelWidgetPair("Copied From:", template(), panel);
        layoutGenerator.addLabelWidgetPair("Last Modified By:     ", creator(), panel);

        layoutGenerator.makeCompactGrid(panel, 5, 2, 10, 10, 5, 10);

        return panel;
    }
    
    private JPanel sectors() {
        sectorsWidget = new AddRemoveSectorWidget();
        if (caseObj.getSectors() == null )
            sectorsWidget.setSectors(new Sector[0]);
        else 
            sectorsWidget.setSectors(caseObj.getSectors());
        sectorsWidget.setPreferredSize(new Dimension(255, 80));
        return sectorsWidget;
    }
    
    private JPanel regions() throws EmfException {
        regionsWidget = new AddRemoveRegionsWidget(presenter.getAllGeoRegions());
        regionsWidget.setRegions(caseObj.getRegions());
        regionsWidget.setPreferredSize(new Dimension(255, 80));
        regionsWidget.setDesktopManager(desktopManager);
        regionsWidget.setEmfSession(session);
        regionsWidget.observeParentPresenter(presenter);
        return regionsWidget;
    }
 
}
