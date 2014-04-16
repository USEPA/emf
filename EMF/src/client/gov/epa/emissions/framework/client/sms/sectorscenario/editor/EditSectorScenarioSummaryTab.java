package gov.epa.emissions.framework.client.sms.sectorscenario.editor;

import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.sms.SectorScenario;
import gov.epa.emissions.framework.services.sms.SectorScenarioOutput;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class EditSectorScenarioSummaryTab extends JPanel implements EditSectorScenarioSummaryTabView, RefreshObserver {

    protected SectorScenario sectorScenario;

    protected TextField name;
    
    protected TextField abbrev;

    protected TextArea description;

    protected ComboBox projectsCombo;

    protected JLabel startDate;

    protected JLabel completionDate;

    protected JLabel user;
    
    private ManageChangeables changeablesList;
    
    protected MessagePanel messagePanel; 

    private  EditSectorScenarioSummaryTabPresenter presenter;
    
    protected EmfConsole parentConsole;

    public EditSectorScenarioSummaryTab(SectorScenario sectorScenario,EmfSession session, ManageChangeables changeablesList,
            MessagePanel messagePanel, EmfConsole parentConsole) {

        super.setName("scenarioSummary");
        this.sectorScenario = sectorScenario;
        this.changeablesList = changeablesList;
        this.parentConsole = parentConsole;
        this.messagePanel = messagePanel;
    }

    public void display()  {
        setLayout();
    }
    
    private void setLayout(){

        super.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getBorderedPanel(createmMainSection(), ""), BorderLayout.NORTH);
        panel.add(createLowerSection(), BorderLayout.SOUTH);
        super.add(panel, BorderLayout.CENTER);
    }

    private JPanel createmMainSection(){

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel panelTop = new JPanel(new SpringLayout());
        JPanel panelBottom = new JPanel(new GridLayout(1, 2, 5, 5));

        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Name:", name(), panelTop);
        layoutGenerator.addLabelWidgetPair("Description:", new ScrollableComponent(description()), panelTop);

        JPanel middleLeftPanel = new JPanel(new SpringLayout());

        SpringLayoutGenerator middleLeftLayoutGenerator = new SpringLayoutGenerator();
        middleLeftLayoutGenerator.addLabelWidgetPair("Creator:", creator(), middleLeftPanel);
        middleLeftLayoutGenerator.addLabelWidgetPair("Abbreviation:", abbrev(), middleLeftPanel);
        middleLeftLayoutGenerator.makeCompactGrid(middleLeftPanel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        JPanel middleRightPanel = new JPanel(new SpringLayout());

        SpringLayoutGenerator middleRightLayoutGenerator = new SpringLayoutGenerator();
        middleRightLayoutGenerator.addLabelWidgetPair("Last Modified Date: ", lastModifiedDate(), middleRightPanel);

        String copiedFrom = this.sectorScenario.getCopiedFrom();
        if (copiedFrom == null) {
            copiedFrom = "";
        }

        middleRightLayoutGenerator.addLabelWidgetPair("Copied From:", this.createLeftAlignedLabel(copiedFrom),
                middleRightPanel);
        middleRightLayoutGenerator.makeCompactGrid(middleRightPanel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                30, 10);// xPad, yPad

        try {
            layoutGenerator.addLabelWidgetPair("Project:", projects(), panelTop);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
        panelBottom.add(middleLeftPanel);
        panelBottom.add(middleRightPanel);

        layoutGenerator.makeCompactGrid(panelTop, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad
        panel.add(panelTop);
        panel.add(panelBottom);

        return panel;
    }
    
    private JComponent createLowerSection(){

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(resultsPanel(), BorderLayout.CENTER);

        return panel;
    }

    private JComponent getBorderedPanel(JComponent component, String border) {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new Border(border));
        panel.add(component);

        return panel;
    }

    private JComponent lastModifiedDate() {
        return createLeftAlignedLabel(CustomDateFormat.format_MM_DD_YYYY_HH_mm(sectorScenario.getLastModifiedDate()));
    }

    private JComponent creator() {
        return createLeftAlignedLabel(sectorScenario.getCreator().getName());
    }

    private JComponent description() {

        this.description = new TextArea("description", sectorScenario.getDescription(), 40, 3);
        this.description.setEditable(true);
        changeablesList.addChangeable(description);

        return this.description;
    }

    private JComponent name() {

        this.name = new TextField("name", 40);
        this.name.setEditable(true);
        this.name.setText(sectorScenario.getName());
        this.name.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(name);

        return this.name;
    }
    
    private JComponent abbrev() {
        this.abbrev = new TextField("abbrev", 15);
        this.abbrev.setEditable(true);
        this.abbrev.setText(sectorScenario.getAbbreviation());
        changeablesList.addChangeable(abbrev);

        return abbrev;
    }

    private Project[] getProjects() throws EmfException {
        return presenter.getProjects();
    }

    private JComponent projects() throws EmfException {
        projectsCombo = new ComboBox(getProjects());
        projectsCombo.setSelectedItem(sectorScenario.getProject());
        projectsCombo.setPreferredSize(name.getPreferredSize());
        changeablesList.addChangeable(projectsCombo);

        return projectsCombo;
    }


    private JComponent createLeftAlignedLabel(String name) {
        JLabel label = new JLabel(name);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }
    
    private JComponent resultsPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Results"));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        user = new JLabel("");
        user.setBackground(Color.white);

        startDate = new JLabel("");
        startDate.setBackground(Color.white);

        completionDate = new JLabel("");
        completionDate.setBackground(Color.white);

        updateSummaryResultPanel();

        layoutGenerator.addLabelWidgetPair("Start Date:", startDate, panel);
        layoutGenerator.addLabelWidgetPair("Completion Date:", completionDate, panel);
        layoutGenerator.addLabelWidgetPair("Running User:", user, panel);
       
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    public void save(SectorScenario sectorScenario) throws EmfException {
        messagePanel.clear();
        
        //perform some basic validation...
        if (name.getText().trim().length() == 0)
            throw new EmfException("Summary Tab: Missing name.");
        if (abbrev.getText().trim().length() == 0)
            throw new EmfException("Summary Tab: Missing abbreviation.");
        
        sectorScenario.setName(name.getText());
        sectorScenario.setAbbreviation(abbrev.getText());
        sectorScenario.setDescription(description.getText());
        sectorScenario.setProject((Project)projectsCombo.getSelectedItem());
        sectorScenario.setLastModifiedDate(new Date());
    }

    public void refresh(SectorScenario sectorScenario, SectorScenarioOutput[] sectorScenarioOutputs) {
        this.sectorScenario = sectorScenario;
        updateSummaryResultPanel();

        String runStatus = sectorScenario.getRunStatus().toLowerCase();

        if (runStatus.indexOf("running") == -1 && runStatus.indexOf("waiting") == -1) {
            clearMessage();
            //presenter.resetButtons(true);
        }
    }

    private void updateSummaryResultPanel() {

        String runStatus = sectorScenario.getRunStatus().toLowerCase(); 

        updateStartDate();

        String completionTime;
        if (runStatus.indexOf("finished") == -1)
            completionTime = runStatus;
        else
            completionTime = CustomDateFormat.format_MM_DD_YYYY_HH_mm_ss(sectorScenario.getCompletionDate());
        String userName = sectorScenario.getCreator().getName();
        
        updateSummaryPanelValuesExceptStartDate(completionTime, userName);
    }

    private void updateStartDate() {
        String startDateString = CustomDateFormat.format_MM_DD_YYYY_HH_mm_ss(sectorScenario.getStartDate());
        startDate.setText((startDateString == null || startDateString.trim() == "" ? "Not started" : startDateString));
    }

    private void updateSummaryPanelValuesExceptStartDate(String closeDate, String userName) {
        completionDate.setText(closeDate);
        user.setText(userName);
    }

    public void stopRun() {
        // NOTE Auto-generated method stub
    }

    public void run(SectorScenario sectorScenario) {
        // NOTE Auto-generated method stub
        
    }

    protected void clearMessage(){
        messagePanel.clear();
    }

    public void setRunMessage(SectorScenario sectorScenario) {
        messagePanel.clear();
//        updateStartDate(sectorScenario);
//        updateSummaryPanelValuesExceptStartDate("Running", "", null, null);
    }

    public void observe(EditSectorScenarioSummaryTabPresenter presenter) {
        this.presenter = presenter;     
    }

    public void doRefresh() {
//        presenter.refreshObjectManager();
//        checkIfLockedByCurrentUser();
//        super.removeAll();
//        setLayout();
//        super.validate();
//        changeablesList.resetChanges();
    }

    public void viewOnly() {
        // NOTE Auto-generated method stub
        
    }

}
