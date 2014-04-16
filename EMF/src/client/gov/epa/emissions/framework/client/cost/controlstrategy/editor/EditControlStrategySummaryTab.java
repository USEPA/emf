package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlstrategy.TargetPollutantListWidget;
import gov.epa.emissions.framework.client.data.Projects;
import gov.epa.emissions.framework.client.data.region.Regions;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyTargetPollutant;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlmeasure.YearValidation;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.DoubleTextField;
import gov.epa.emissions.framework.ui.IntTextField;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.NumberFieldVerifier;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class EditControlStrategySummaryTab extends JPanel implements EditControlStrategySummaryTabView {

    private ControlStrategy controlStrategy;

    private ManageChangeables changeablesList;

    private TextField name;
    
    private TextField multiPollField;

    private TextArea description;

    private DoubleTextField discountRate;

    private EditableComboBox projectsCombo;

    private EmfSession session;

    private Dimension comboSize = new Dimension(200, 20);

    private MessagePanel messagePanel;

    private IntTextField costYear;

    private EditableComboBox regionsCombo;

    private IntTextField inventoryYear;

    private ComboBox majorPollutant;
    
    private JPanel pollutantsPanel;
    
    private Pollutant[] targetPollutants;

    protected EmfConsole parentConsole;

    private JLabel startDate, completionDate, user, costValue, emissionReductionValue;

    private ComboBox strategyTypeCombo;

    protected JCheckBox useCostEquationCheck;
    
    protected JCheckBox makeFinalCheck;

    private JCheckBox includeUnspecifiedCostsCheck;

    private ControlStrategyResult[] controlStrategyResults;

    private DecimalFormat decFormat;

    private CostYearTable costYearTable;

    private NumberFieldVerifier verifier;
    
    private EditControlStrategyPresenter presenter;

    private JCheckBox applyCAPMeasureOnHAPPollCheck;

    public EditControlStrategySummaryTab(ControlStrategy controlStrategy,
            ControlStrategyResult[] controlStrategyResults, EmfSession session, ManageChangeables changeablesList,
            MessagePanel messagePanel, EmfConsole parentConsole, CostYearTable costYearTable, EditControlStrategyPresenter presenter) throws EmfException {
        super.setName("summary");
        this.controlStrategy = controlStrategy;
        this.controlStrategyResults = controlStrategyResults;
        this.session = session;
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.parentConsole = parentConsole;
//        this.decFormat = new DecimalFormat("0.###E0");
        this.decFormat = new DecimalFormat("#,##0");
        this.costYearTable = costYearTable;
        this.verifier = new NumberFieldVerifier("Summary tab: ");
        this.presenter = presenter;
        this.pollutantsPanel = new JPanel(new BorderLayout());
        setLayout();
    }

    private void setLayout() throws EmfException {
        super.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getBorderedPanel(createmMainSection(), ""), BorderLayout.NORTH);
        panel.add(createLowerSection(), BorderLayout.SOUTH);
        super.add(panel, BorderLayout.CENTER);
    }

    private JPanel createmMainSection() throws EmfException {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JPanel panelTop = new JPanel(new SpringLayout());
        JPanel panelBottom = new JPanel(new GridLayout(1,2,5,5));
        // panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Name:", name(), panelTop);
        layoutGenerator.addLabelWidgetPair("Description:", new ScrollableComponent(description()), panelTop);
//      JPanel subPanel = new JPanel(new BorderLayout());
//      subPanel.add(new JLabel("Project:"));
//      subPanel.add(projects());
        layoutGenerator.addLabelWidgetPair("Project:", projects(), panelTop);
//      layoutGenerator.addWidgetPair(middleLeftPanel, middleRightPanel, panelBottom);
//      layoutGenerator.addLabelWidgetPair("Creator:", creator(), panelTop);
//      layoutGenerator.addLabelWidgetPair("Last Modified Date: ", lastModifiedDate(), panelTop);
//      layoutGenerator.addLabelWidgetPair("Copied From:", new JLabel("   "), panelTop);
//      layoutGenerator.addLabelWidgetPair("Type of Analysis:", typeOfAnalysis(), panelTop);
        layoutGenerator.makeCompactGrid(panelTop, 3, 2, // rows, cols
              5, 5, // initialX, initialY
              10, 10);// xPad, yPad
      
        JPanel middleLeftPanel = new JPanel(new SpringLayout());
        // panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator middleLeftLayoutGenerator = new SpringLayoutGenerator();
        middleLeftLayoutGenerator.addLabelWidgetPair("Creator:", creator(), middleLeftPanel);
        middleLeftLayoutGenerator.addLabelWidgetPair("Type of Analysis:", typeOfAnalysis(), middleLeftPanel);
        middleLeftLayoutGenerator.addLabelWidgetPair("Is Final:", makeFinal(), middleLeftPanel);
        middleLeftLayoutGenerator.makeCompactGrid(middleLeftPanel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                30, 10);// xPad, yPad

        JPanel middleRightPanel = new JPanel(new SpringLayout());
        // panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator middleRightLayoutGenerator = new SpringLayoutGenerator();
        middleRightLayoutGenerator.addLabelWidgetPair("Last Modified Date: ", lastModifiedDate(), middleRightPanel);
        
        String copiedFrom = this.controlStrategy.getCopiedFrom();
        if (copiedFrom == null) {
            copiedFrom = "";
        }

        middleRightLayoutGenerator.addLabelWidgetPair("Copied From:", this.createLeftAlignedLabel(copiedFrom),
                middleRightPanel);
        middleRightLayoutGenerator.addLabelWidgetPair("", new JLabel(""),
                middleRightPanel);

        middleRightLayoutGenerator.makeCompactGrid(middleRightPanel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        panelBottom.add(middleLeftPanel);
        panelBottom.add(middleRightPanel);
        
        panel.add(panelTop);
        panel.add(panelBottom);
        return panel;
    }

    private ComboBox typeOfAnalysis() throws EmfException {
        StrategyType[] types = session.controlStrategyService().getStrategyTypes();
        strategyTypeCombo = new ComboBox("Choose a strategy type", types);
        strategyTypeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                StrategyType strategyType = (StrategyType)strategyTypeCombo.getSelectedItem();
                presenter.doChangeStrategyType(strategyType);
                
                try {
                    updatePollutantsPanel(strategyType);
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
                
                EditControlStrategySummaryTab.this.revalidate();
            }
        });
        strategyTypeCombo.setSelectedItem(controlStrategy.getStrategyType());
        changeablesList.addChangeable(strategyTypeCombo);
        
        return strategyTypeCombo;
    }
    
    private JCheckBox makeFinal() {

        makeFinalCheck = new JCheckBox("", null, (controlStrategy.getIsFinal() != null ? controlStrategy.getIsFinal() : false)); // need to check with database to see if it is final
        return makeFinalCheck;
    }    

    private JPanel createLowerSection() throws EmfException {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getBorderedPanel(createLowerLeftSection(), "Parameters"), BorderLayout.WEST);
        panel.add(resultsPanel(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel getBorderedPanel(JPanel component, String border) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new Border(border));
        panel.add(component);

        return panel;
    }

    private JPanel createLowerLeftSection() throws EmfException {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        JPanel panel = new JPanel(new SpringLayout());
        JPanel panelBottom = new JPanel(new BorderLayout());
        // panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        
        updatePollutantsPanel(controlStrategy.getStrategyType());

        // layoutGenerator.addLabelWidgetPair("Discount Rate:", discountRateTextField(), panel);
        layoutGenerator.addLabelWidgetPair("Cost Year:", costYearTextField(), panel);
        layoutGenerator.addLabelWidgetPair("Target Year:", inventoryYearTextField(), panel);
        layoutGenerator.addLabelWidgetPair("Region:", regions(), panel);
        layoutGenerator.addLabelWidgetPair("Target Pollutant:", pollutantsPanel, panel);
        layoutGenerator.addLabelWidgetPair("Discount Rate (%):", discountRate(), panel);

        JPanel middleLeftPanel = new JPanel(new SpringLayout());
        // panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator middleLeftLayoutGenerator = new SpringLayoutGenerator();
        middleLeftLayoutGenerator.addLabelWidgetPair("Use Cost Equations:", useCostEquation(), middleLeftPanel);
        middleLeftLayoutGenerator.addLabelWidgetPair("<html>Apply CAP measures<br/>on HAP Pollutants:</html>:", applyCAPMeasureOnHAPPoll(), middleLeftPanel);

        middleLeftLayoutGenerator.makeCompactGrid(middleLeftPanel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        JPanel middleRightPanel = new JPanel(new SpringLayout());
        // panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator middleRightLayoutGenerator = new SpringLayoutGenerator();
        middleRightLayoutGenerator.addLabelWidgetPair("<html>Include Measures<br/>with No Cost Data:</html>", includeUnspecifiedCostsCheckBox(), middleRightPanel);
        middleRightLayoutGenerator.makeCompactGrid(middleRightPanel, 1, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        panelBottom.add(middleLeftPanel, BorderLayout.WEST);
        panelBottom.add(middleRightPanel, BorderLayout.EAST);
        
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad
        mainPanel.add(panel);
        mainPanel.add(panelBottom);
        return mainPanel;
    }

    private DoubleTextField discountRate() {
        discountRate = new DoubleTextField("discount rate", 1, 20, 12);
        discountRate.setValue(controlStrategy.getDiscountRate() != null ? controlStrategy.getDiscountRate() : 7.0);
        discountRate.setToolTipText("This value is only used for point sources");
        changeablesList.addChangeable(discountRate);
        return discountRate;
    }

    private JCheckBox useCostEquation() {

        useCostEquationCheck = new JCheckBox(" ", null, controlStrategy.getUseCostEquations() != null ? controlStrategy.getUseCostEquations() : true);
        return useCostEquationCheck;
    }

    private JCheckBox applyCAPMeasureOnHAPPoll() {

        applyCAPMeasureOnHAPPollCheck = new JCheckBox(" ", null, controlStrategy.getApplyCAPMeasuresOnHAPPollutants() != null ? controlStrategy.getApplyCAPMeasuresOnHAPPollutants() : false);
        return applyCAPMeasureOnHAPPollCheck;
    }

    private JCheckBox includeUnspecifiedCostsCheckBox() {

        includeUnspecifiedCostsCheck = new JCheckBox(" ", null, controlStrategy.getIncludeUnspecifiedCosts() != null ? controlStrategy.getIncludeUnspecifiedCosts() : true);
        return includeUnspecifiedCostsCheck;
    }

    private IntTextField costYearTextField() {
        costYear = new IntTextField("cost year", 0, Integer.MAX_VALUE, 12);
        costYear.setValue(controlStrategy.getCostYear() != 0 ? controlStrategy.getCostYear() : CostYearTable.REFERENCE_COST_YEAR);
        return costYear;
    }

    private IntTextField inventoryYearTextField() {
        inventoryYear = new IntTextField("Target year", 0, Integer.MAX_VALUE, 12);
        inventoryYear.setToolTipText("This is the target year for the strategy run, often this is the year of the inventory.");
        if (controlStrategy.getInventoryYear() != 0)
            inventoryYear.setValue(controlStrategy.getInventoryYear());
        return inventoryYear;
    }

    private JLabel lastModifiedDate() {
        return createLeftAlignedLabel(CustomDateFormat.format_MM_DD_YYYY_HH_mm(controlStrategy.getLastModifiedDate()));
    }

    private JLabel creator() {
        return createLeftAlignedLabel(controlStrategy.getCreator().getName());
    }

    private TextArea description() {
        description = new TextArea("description", controlStrategy.getDescription(), 40, 3);
        changeablesList.addChangeable(description);

        return description;
    }

    private TextField name() {
        name = new TextField("name", 40);
        name.setText(controlStrategy.getName());
        name.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(name);

        return name;
    }

    private Project[] getProjects() throws EmfException {
        return session.getProjects();
    }
    
    private EditableComboBox projects() throws EmfException {
        projectsCombo = new EditableComboBox(getProjects());

        if (!(this.session != null && this.session.user() != null && this.session.user().isAdmin())) {
            projectsCombo.setEditable(false);
        }

        projectsCombo.setSelectedItem(controlStrategy.getProject());
        projectsCombo.setPreferredSize(name.getPreferredSize());
        changeablesList.addChangeable(projectsCombo);

        return projectsCombo;
    }

    private EditableComboBox regions() throws EmfException {
        regionsCombo = new EditableComboBox(session.dataCommonsService().getRegions());
        regionsCombo.setSelectedItem(controlStrategy.getRegion());
        regionsCombo.setPreferredSize(comboSize);

        changeablesList.addChangeable(regionsCombo);

        return regionsCombo;
    }

    private void updatePollutantsPanel(StrategyType type) throws EmfException {
        pollutantsPanel.removeAll();
        
        if (type == null || !type.getName().equals(StrategyType.MULTI_POLLUTANT_MAX_EMISSIONS_REDUCTION))
            pollutantsPanel.add(majorPollutants(), BorderLayout.LINE_START);
        else
            pollutantsPanel.add(createMultiPollutants(), BorderLayout.LINE_START);
        
        pollutantsPanel.validate();
    }
    
    private ComboBox majorPollutants() throws EmfException {
        Pollutant[] pollutants = getAllPollutants(this.session);
        majorPollutant = new ComboBox(pollutants);
        majorPollutant.setSelectedItem(controlStrategy.getTargetPollutant());
        majorPollutant.setPreferredSize(comboSize);

        changeablesList.addChangeable(majorPollutant);

        return majorPollutant;
    }
    
    private JPanel createMultiPollutants() {
        JPanel panel = new JPanel(new BorderLayout());
        multiPollField = new TextField("multipollfiels", 12);
        multiPollField.setText(getPollutantsString(controlStrategy.getTargetPollutants()));
        multiPollField.setEditable(false);
        
        Button set = new Button("Set", new AbstractAction(){
            public void actionPerformed(ActionEvent arg0) {
                try {
                    Pollutant[] pollutants = getAllPollutants(session);
                    TargetPollutantListWidget pollSetter = new TargetPollutantListWidget(pollutants, changeablesList, parentConsole);
                    pollSetter.setPollutants(targetPollutants);
                    pollSetter.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
                    PollutantsSelectionDialog selectionDialog = new PollutantsSelectionDialog(pollSetter, parentConsole);
                    selectionDialog.observe(presenter.getSummaryPresenter());
                    selectionDialog.display();
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
            
        });
        
        panel.add(multiPollField, BorderLayout.LINE_START);
        panel.add(set, BorderLayout.LINE_END);
        changeablesList.addChangeable(multiPollField);
        panel.setPreferredSize(comboSize);
        
        return panel;
    }

    private Pollutant[] getAllPollutants(EmfSession session) throws EmfException {
        return session.dataCommonsService().getPollutants();
    }

    private JLabel createLeftAlignedLabel(String name) {
        JLabel label = new JLabel(name);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }

    private JPanel resultsPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Results"));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        user = new JLabel("");
        user.setBackground(Color.white);
        
        startDate = new JLabel("");
        startDate.setBackground(Color.white);

        completionDate = new JLabel("");
        completionDate.setBackground(Color.white);

        costValue = new JLabel("");
        costValue.setBackground(Color.white);

        emissionReductionValue = new JLabel("");
        emissionReductionValue.setBackground(Color.white);

        updateSummaryResultPanel(controlStrategy, controlStrategyResults);

        layoutGenerator.addLabelWidgetPair("Start Date:", startDate, panel);
        layoutGenerator.addLabelWidgetPair("Completion Date:", completionDate, panel);
        layoutGenerator.addLabelWidgetPair("Running User:", user, panel);
        layoutGenerator.addLabelWidgetPair("Total Annualized Cost:", costValue, panel);
        layoutGenerator.addLabelWidgetPair("Target Poll. Reduction (tons):", emissionReductionValue, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    public void save(ControlStrategy controlStrategy) throws EmfException {
        messagePanel.clear();
        controlStrategy.setName(name.getText());
        controlStrategy.setDescription(description.getText());
        updateProject();

        // isDatasetSelected(controlStrategy);
        controlStrategy.setCostYear(new YearValidation("Cost Year").value(costYear.getText(), costYearTable
                .getStartYear(), costYearTable.getEndYear()));
        controlStrategy.setInventoryYear(new YearValidation("Target Year").value(inventoryYear.getText()));
        updateRegion();

        controlStrategy.setDiscountRate(checkDiscountRate());
        StrategyType strategyType = checkStrategyType();
        controlStrategy.setStrategyType(strategyType);
        if (!strategyType.getName().equals(StrategyType.MULTI_POLLUTANT_MAX_EMISSIONS_REDUCTION)) {
            controlStrategy.setTargetPollutant(checkMajorPollutant(!strategyType.getName().equals(StrategyType.projectFutureYearInventory)));
        } else {
            List<ControlStrategyTargetPollutant> list = new ArrayList<ControlStrategyTargetPollutant>();
            for (String targetPollutantName : multiPollField.getText().split("; ")) {
                for (Pollutant pollutant : getAllPollutants(this.session)) {
                    if (pollutant.getName().equals(targetPollutantName))
                        list.add(new ControlStrategyTargetPollutant(pollutant));
                }
            }
            controlStrategy.setTargetPollutants(list.toArray(new ControlStrategyTargetPollutant[0]));
        }
        controlStrategy.setUseCostEquations(useCostEquationCheck.isSelected());
        controlStrategy.setApplyCAPMeasuresOnHAPPollutants(applyCAPMeasureOnHAPPollCheck.isSelected());
        controlStrategy.setIncludeUnspecifiedCosts(includeUnspecifiedCostsCheck.isSelected());
        
        Boolean isFinal = makeFinalCheck.isSelected();
        controlStrategy.setIsFinal(isFinal);
    }

    private void updateMultiTargetPolls() {
        List<ControlStrategyTargetPollutant> list = new ArrayList<ControlStrategyTargetPollutant>();
        ControlStrategyTargetPollutant[] exists = controlStrategy.getTargetPollutants();
        HashMap<Pollutant, ControlStrategyTargetPollutant> map = new HashMap<Pollutant, ControlStrategyTargetPollutant>();
        
        for (ControlStrategyTargetPollutant pol : exists)
            map.put(pol.getPollutant(), pol);
        
        for (Pollutant pol : targetPollutants) {
            if (!map.containsKey(pol)) {
                ControlStrategyTargetPollutant target = new ControlStrategyTargetPollutant();
                target.setPollutant(pol);
                list.add(target);
            } else
                list.add(map.get(pol));
        }
        
        controlStrategy.setTargetPollutants(list.toArray(new ControlStrategyTargetPollutant[0]));
    }

    private double checkDiscountRate() throws EmfException {
        // check to see that it's not empty
        if (discountRate.getText().trim().length() == 0)
            throw new EmfException("Enter the Discount Rate as a percentage (e.g., 9 for 9% percent)");

        double value = verifier.parseDouble(discountRate.getText());

        // make sure the number makes sense...
        if (value < 1 || value > 20) {
            throw new EmfException("Enter the Discount Rate as a percent between 1 and 20 (e.g., 7% is entered as 7)");
        }
        return value;

    }

    private StrategyType checkStrategyType() throws EmfException {
        StrategyType strategyType = (StrategyType) this.strategyTypeCombo.getSelectedItem();
        if (strategyType == null)
            throw new EmfException("Please select a strategy type");
        return strategyType;
    }

    private Pollutant checkMajorPollutant(boolean required) throws EmfException {
        if (majorPollutant == null) return null;
        
        Pollutant pollutant = (Pollutant) majorPollutant.getSelectedItem();
        if (pollutant == null && required) {
            throw new EmfException("Please select a target pollutant");
        }
        return pollutant;
    }

    // private void isDatasetSelected(ControlStrategy controlStrategy) throws EmfException {
    // if (controlStrategy.getControlStrategyInputDatasets().length == 0) {
    // throw new EmfException("Please select a dataset");
    // }
    // }

    private void updateRegion() throws EmfException {
        Object selected = regionsCombo.getSelectedItem();
        if (selected instanceof String) {
            String regionName = ((String) selected).trim();
            if (regionName.length() > 0) {
                Region region = region(regionName);// checking for duplicates
                controlStrategy.setRegion(region);
            }
        } else if (selected instanceof Region) {
            controlStrategy.setRegion((Region) selected);
        }
    }

    private Region region(String regionName) throws EmfException {
        return new Regions(session.dataCommonsService().getRegions()).get(regionName);
    }

    private void updateProject() throws EmfException {
        Object selected = projectsCombo.getSelectedItem();
        if (selected instanceof String) {
            String projectName = ((String) selected).trim();
            if (projectName.length() > 0) {
                Project project = project(projectName);// checking for duplicates
                controlStrategy.setProject(project);
            }
        } else if (selected instanceof Project) {
            controlStrategy.setProject((Project) selected);
        }
    }

    private Project project(String projectName) throws EmfException {
        return new Projects(session.getProjects()).get(projectName);
    }

    public void setRunMessage(ControlStrategy controlStrategy) {
        messagePanel.clear();
        updateStartDate(controlStrategy);
        updateSummaryPanelValuesExceptStartDate("Running", "", null, null);
    }

    public void refresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
//        this.controlStrategy = controlStrategy;
        updateSummaryResultPanel(controlStrategy, controlStrategyResults);
        
        //ControlStrategyResultsSummary summary = new ControlStrategyResultsSummary(controlStrategyResults);
        //String runStatus = summary.getRunStatus();
        String runStatus = controlStrategy.getRunStatus();
        
        if ( runStatus.indexOf("Running") == -1 
                && runStatus.indexOf("Waiting") == -1 ){
            messagePanel.clear();
            presenter.resetButtons(true);
        }
    }

    private void updateSummaryResultPanel(ControlStrategy controlStrategy,
            ControlStrategyResult[] controlStrategyResults) {
        
        String runStatus = controlStrategy.getRunStatus(); //summary.getRunStatus();
        if (controlStrategyResults == null || controlStrategyResults.length == 0) {
            updateStartDate(controlStrategy);
            updateSummaryPanelValuesExceptStartDate(""+runStatus, "", null, null);
            return;
        }
//        ControlStrategyResultsSummary summary = new ControlStrategyResultsSummary(controlStrategyResults);
//        summary.getCompletionTime();
        
        String completionTime; 
        if (runStatus.indexOf("Finished") == -1 )
            completionTime = runStatus;     
        else 
            completionTime = CustomDateFormat.format_MM_DD_YYYY_HH_mm_ss(controlStrategy.getCompletionDate());
        String userName =controlStrategy.getCreator().getName();
//        String userName= summary.getUser().getName()== null ? summary.getUser().getName() : "";
        Date startTime = controlStrategy.getStartDate()== null? null:controlStrategy.getStartDate();
        updateStartDate(startTime);
        
        updateSummaryPanelValuesExceptStartDate(""+completionTime, "" + userName , controlStrategy.getTotalCost(), 
                (!controlStrategy.getStrategyType().getName().equals(StrategyType.MULTI_POLLUTANT_MAX_EMISSIONS_REDUCTION) ? (controlStrategy.getTotalReduction() != null ? controlStrategy.getTotalReduction() + "" : null) : "N/A"));
    }

    private void updateStartDate(Date startTime) {
        String time = startTime == null? "": CustomDateFormat.format_MM_DD_YYYY_HH_mm_ss(startTime); 
        startDate.setText(time);
    }

    private void updateStartDate(ControlStrategy controlStrategy) {
        String startDateString = CustomDateFormat.format_MM_DD_YYYY_HH_mm_ss(controlStrategy.getStartDate());
        startDate.setText((startDateString == null ||startDateString.trim()=="" ? "Not started" : startDateString));
    }

    private void updateSummaryPanelValuesExceptStartDate(String closeDate, String userName, Double cost, String emisReduction) {
        completionDate.setText(closeDate);
        user.setText(userName);
        costValue.setText(cost == null ? "" : "$" + decFormat.format(cost));
        emissionReductionValue.setText(emisReduction == null ? "" : (!emisReduction.equals("N/A") ? decFormat.format(Double.parseDouble(emisReduction)) : "N/A"));
    }

    public void stopRun() {
        // NOTE Auto-generated method stub
        
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

    public void setTargetPollutants(Pollutant[] pollutants) {
        targetPollutants = pollutants;
        multiPollField.setText(getPollutantsString(pollutants));
        StrategyType strategyType = (StrategyType)strategyTypeCombo.getSelectedItem();
        
        if (strategyType.getName().equals(StrategyType.MULTI_POLLUTANT_MAX_EMISSIONS_REDUCTION))
            updateMultiTargetPolls();
        
        presenter.doChangeStrategyType(strategyType); //To notify constraints tab to change accordingly
    }

    private String getPollutantsString(Pollutant[] pollutants) {
        String text = "";
        
        for (Pollutant pol : pollutants) {
            text += pol.getName() + "; ";
        }
        
        return text;
    }
    
    private String getPollutantsString(ControlStrategyTargetPollutant[] polls) {
        String text = "";
        
        if (polls == null || polls.length == 0)
            return text;
        
        targetPollutants = new Pollutant[polls.length];
        int i = 0;
        
        for (ControlStrategyTargetPollutant pol : polls) {
            text += pol.getPollutant().getName() + "; ";
            targetPollutants[i++] = pol.getPollutant();
        }
        
        return text;
    }

    public void fireStrategyTypeChanges(StrategyType strategyType) {
        // NOTE Auto-generated method stub
        
    }
}
