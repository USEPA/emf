package gov.epa.emissions.framework.client.meta.summary;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.intendeduse.IntendedUse;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.swingworker.RefreshSwingWorkerTasks;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class SummaryTab extends JPanel implements SummaryTabView, RefreshObserver {

    private EmfDataset dataset;
    private Version version; 
    private SummaryTabPresenter presenter; 
    private MessagePanel messagePanel;

    public SummaryTab(EmfDataset dataset, Version version, MessagePanel messagePanel) {
        super.setName("summary");
        this.dataset = dataset;
        this.version = version; 
        this.messagePanel = messagePanel;

        setLayout();
    }
    
    private void setLayout(){
        super.setLayout(new BorderLayout());
        super.add(createOverviewSection(), BorderLayout.PAGE_START);
        super.add(createLowerSection(), BorderLayout.CENTER);
    }

    private JPanel createLowerSection() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        container.add(createTimeSpaceSection());
        container.add(createStatusSection());

        panel.add(container, BorderLayout.LINE_START);

        return panel;
    }

    private JPanel createStatusSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        panel.add(createStatusDatesPanel(), BorderLayout.PAGE_START);

        return panel;
    }

    private JPanel createStatusDatesPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Status:", new Label("Status", dataset.getStatus()), panel);
        layoutGenerator.addLabelWidgetPair("Last Modified Date:", new Label("Last modified date", formatDate(dataset
                .getModifiedDateTime())), panel);
        layoutGenerator.addLabelWidgetPair("Last Accessed Date:", new Label("Last accessed date", formatDate(dataset
                .getAccessedDateTime())), panel);
        layoutGenerator.addLabelWidgetPair("Creation Date:", new Label("Creation date", formatDate(dataset
                .getCreatedDateTime())), panel);

        IntendedUse intendedUse = dataset.getIntendedUse();
        String intendedUseName = (intendedUse != null) ? intendedUse.getName() : "";
        layoutGenerator.addLabelWidgetPair("Intended Use:", new Label("Intended use", intendedUseName), panel);

        layoutGenerator.addLabelWidgetPair("Default Version:", new Label("Default version", ""
                + (version != null ? version.toString() : "" )), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel createTimeSpaceSection() {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // time period
        layoutGenerator.addLabelWidgetPair("Time Period Start:", new Label("Time period start", formatDate(dataset.getStartDateTime())),
                panel);
        layoutGenerator
                .addLabelWidgetPair("Time Period End:", new Label("Time period end", formatDate(dataset.getStopDateTime())), panel);
        layoutGenerator.addLabelWidgetPair("Temporal Resolution:", new Label("Temporal resolution", dataset.getTemporalResolution()), panel);
        Sector[] sectors = dataset.getSectors();
        String sectorLabel = "";
        if (sectors != null && sectors.length > 0) {
            sectorLabel = sectors[0].toString();
        }
        layoutGenerator.addLabelWidgetPair("Sector:", new Label("Sector", sectorLabel), panel);
        Region region = dataset.getRegion();
        String regionName = (region != null) ? region.getName() : "";
        layoutGenerator.addLabelWidgetPair("Region:", new Label("Region", regionName), panel);

        Country country = dataset.getCountry();
        String countryName = (country != null) ? country.getName() : "";
        layoutGenerator.addLabelWidgetPair("Country:", new Label("Country", countryName), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private String formatDate(Date date) {
        return CustomDateFormat.format_MM_DD_YYYY_HH_mm(date);
    }

    private JPanel createOverviewSection() {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // name
//      layoutGenerator.addLabelWidgetPair("Name:", new JLabel(dataset.getName()), panel);
        JTextField nameField = new JTextField(dataset.getName() + " (ID = " + dataset.getId() + ")");
        nameField.setEditable(false);
        layoutGenerator.addLabelWidgetPair("Name:", 
                nameField //new JLabel(dataset.getName() + " (ID = " + dataset.getId() + ")")
                , panel);


        // description
        TextArea description = new TextArea("description", dataset.getDescription());
        description.setEditable(false);
        ScrollableComponent viewableTextArea = new ScrollableComponent(description);
        viewableTextArea.setPreferredSize(new Dimension(575,100));
        layoutGenerator.addLabelWidgetPair("Description:", viewableTextArea, panel);

        //old version of command before changes
        //layoutGenerator.addLabelWidgetPair("Description:", new ScrollableComponent(description), panel);

        Project project = dataset.getProject();
        String projectName = (project != null) ? project.getName() : "";
        layoutGenerator.addLabelWidgetPair("Project:", new Label("Project", projectName), panel);
        layoutGenerator.addLabelWidgetPair("Creator:", new Label("Creator", getFullName()), panel);
        layoutGenerator.addLabelWidgetPair("Dataset Type:", new Label("Dataset type", dataset.getDatasetTypeName()), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }
    
    public void doRefresh() throws EmfException {
        
        try {
            new RefreshSwingWorkerTasks(this, messagePanel, presenter).execute();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }  
    }
    
    public void doRefresh(EmfDataset dataset, Version version){
        this.dataset = dataset;
        this.version = version;
        super.removeAll();
        setLayout();
        super.validate();    
    }

    public void observe(SummaryTabPresenter presenter) {
        this.presenter = presenter;   
    }
    
    private String getFullName(){
        String fullName = dataset.getCreatorFullName();
        if ((fullName == null) || (fullName.trim().equalsIgnoreCase("")))
            fullName = dataset.getCreator();
        else
            fullName= fullName+ " ("+dataset.getCreator()+")";
        return fullName;
    }   

}
