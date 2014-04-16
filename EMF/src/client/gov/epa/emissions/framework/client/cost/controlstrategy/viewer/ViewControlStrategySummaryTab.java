package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfPanel;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlstrategy.TargetPollutantListWidget;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.PollutantsSelectionDialog;
import gov.epa.emissions.framework.client.data.Projects;
import gov.epa.emissions.framework.client.data.region.Regions;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyTargetPollutant;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlmeasure.YearValidation;
import gov.epa.emissions.framework.services.cost.data.ControlStrategyResultsSummary;
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
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ViewControlStrategySummaryTab extends EmfPanel implements ViewControlStrategySummaryTabView {

    private ControlStrategy controlStrategy;

    private TextField name;
    
    private TextField multiPollField;

    private TextArea description;

    private DoubleTextField discountRate;

    private ComboBox projectsCombo;

    private Dimension comboSize = new Dimension(200, 20);

    private IntTextField costYear;

    private ComboBox regionsCombo;

    private IntTextField inventoryYear;

    private ComboBox majorPollutant;

    private JLabel startDate;

    private JLabel completionDate;

    private JLabel user;

    private JLabel costValue;

    private JLabel emissionReductionValue;
    
    private JPanel pollutantsPanel;

    private ComboBox strategyTypeCombo;
    
    protected JCheckBox useCostEquationCheck;
    
    protected JCheckBox isFinalCheck;

    private JCheckBox includeUnspecifiedCostsCheck;

    private ControlStrategyResult[] controlStrategyResults;

    private DecimalFormat decFormat;

    private CostYearTable costYearTable;

    private NumberFieldVerifier verifier;

    private ViewControlStrategyPresenter presenter;

    private JCheckBox applyCAPMeasureOnHAPPollCheck;

    public ViewControlStrategySummaryTab(ControlStrategy controlStrategy,
            ControlStrategyResult[] controlStrategyResults, CostYearTable costYearTable, MessagePanel messagePanel,
            EmfConsole parentConsole, DesktopManager desktopManager, ViewControlStrategyPresenter presenter)
            throws EmfException {

        super("csSummary", parentConsole, desktopManager, messagePanel);

        this.controlStrategy = controlStrategy;
        this.controlStrategyResults = controlStrategyResults;
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
        JPanel panelBottom = new JPanel(new GridLayout(1, 2, 5, 5));

        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Name:", name(), panelTop);
        layoutGenerator.addLabelWidgetPair("Description:", new ScrollableComponent(description()), panelTop);

        JPanel middleLeftPanel = new JPanel(new SpringLayout());

        SpringLayoutGenerator middleLeftLayoutGenerator = new SpringLayoutGenerator();
        middleLeftLayoutGenerator.addLabelWidgetPair("Creator:", creator(), middleLeftPanel);
        middleLeftLayoutGenerator.addLabelWidgetPair("Type of Analysis:", typeOfAnalysis(), middleLeftPanel);
        middleLeftLayoutGenerator.addLabelWidgetPair("Is Final:", makeFinal(), middleLeftPanel);
        
        middleLeftLayoutGenerator.makeCompactGrid(middleLeftPanel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        JPanel middleRightPanel = new JPanel(new SpringLayout());

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
                30, 10);// xPad, yPad

        layoutGenerator.addLabelWidgetPair("Project:", projects(), panelTop);
        panelBottom.add(middleLeftPanel);
        panelBottom.add(middleRightPanel);

        layoutGenerator.makeCompactGrid(panelTop, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad
        panel.add(panelTop);
        panel.add(panelBottom);

        return panel;
    }

    private JComponent typeOfAnalysis() throws EmfException {

        StrategyType[] types = this.getSession().controlStrategyService().getStrategyTypes();
        this.strategyTypeCombo = new ComboBox(types);
        this.strategyTypeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                StrategyType strategyType = (StrategyType) strategyTypeCombo.getSelectedItem();
                presenter.doChangeStrategyType(strategyType);
            }
        });

        this.strategyTypeCombo.setEditable(false);
        this.strategyTypeCombo.setSelectedItem(controlStrategy.getStrategyType());

        return this.strategyTypeCombo;
    }

    private JComponent createLowerSection() throws EmfException {

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getBorderedPanel(createLowerLeftSection(), "Parameters"), BorderLayout.WEST);
        panel.add(resultsPanel(), BorderLayout.CENTER);

        return panel;
    }

    private JComponent getBorderedPanel(JComponent component, String border) {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new Border(border));
        panel.add(component);

        return panel;
    }

    private JComponent createLowerLeftSection() throws EmfException {

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JPanel panel = new JPanel(new SpringLayout());
        JPanel panelBottom = new JPanel(new BorderLayout());

        updatePollutantsPanel(controlStrategy.getStrategyType());
        
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Cost Year:", costYearTextField(), panel);
        layoutGenerator.addLabelWidgetPair("Target Year:", inventoryYearTextField(), panel);
        layoutGenerator.addLabelWidgetPair("Region:", regions(), panel);
        layoutGenerator.addLabelWidgetPair("Target Pollutant:", pollutantsPanel, panel);
        layoutGenerator.addLabelWidgetPair("Discount Rate (%):", discountRate(), panel);

        JPanel middleLeftPanel = new JPanel(new SpringLayout());

        SpringLayoutGenerator middleLeftLayoutGenerator = new SpringLayoutGenerator();
        middleLeftLayoutGenerator.addLabelWidgetPair("Use Cost Equations:", useCostEquation(), middleLeftPanel);
        middleLeftLayoutGenerator.addLabelWidgetPair("<html>Apply CAP measures<br/>on HAP Pollutants:</html>:", applyCAPMeasureOnHAPPoll(), middleLeftPanel);

        middleLeftLayoutGenerator.makeCompactGrid(middleLeftPanel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        JPanel middleRightPanel = new JPanel(new SpringLayout());

        SpringLayoutGenerator middleRightLayoutGenerator = new SpringLayoutGenerator();
        middleRightLayoutGenerator.addLabelWidgetPair("<html>Include Measures<br/>with No Cost Data:</html>",
                includeUnspecifiedCostsCheckBox(), middleRightPanel);
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

    private void updatePollutantsPanel(StrategyType type) throws EmfException {
        if (type == null || !type.getName().equals(StrategyType.MULTI_POLLUTANT_MAX_EMISSIONS_REDUCTION))
            pollutantsPanel.add(majorPollutants(), BorderLayout.LINE_START);
        else
            pollutantsPanel.add(createMultiPollutants(), BorderLayout.LINE_START);
    }
    
    private JComponent discountRate() {

        this.discountRate = new DoubleTextField("discount rate", 1, 20, 16);
        this.discountRate.setValue(this.controlStrategy.getDiscountRate() != null ? controlStrategy.getDiscountRate()
                : 7.0);
        this.discountRate.setToolTipText("This value is only used for point sources");
        this.discountRate.setEditable(false);

        return discountRate;
    }

    private JCheckBox applyCAPMeasureOnHAPPoll() {

        applyCAPMeasureOnHAPPollCheck = new JCheckBox(" ", null, controlStrategy.getApplyCAPMeasuresOnHAPPollutants() != null ? controlStrategy.getApplyCAPMeasuresOnHAPPollutants() : false);
        this.applyCAPMeasureOnHAPPollCheck.setEnabled(false);
        return applyCAPMeasureOnHAPPollCheck;
    }

    private JComponent useCostEquation() {

        this.useCostEquationCheck = new JCheckBox(" ", null,
                controlStrategy.getUseCostEquations() != null ? controlStrategy.getUseCostEquations() : true);
        this.useCostEquationCheck.setEnabled(false);

        return this.useCostEquationCheck;
    }
    
    private JCheckBox makeFinal() {

        isFinalCheck = new JCheckBox("", null, controlStrategy.getIsFinal()); // need to check with database to see if it is final
        return isFinalCheck;
    }     

    private JComponent includeUnspecifiedCostsCheckBox() {

        this.includeUnspecifiedCostsCheck = new JCheckBox(" ", null,
                controlStrategy.getIncludeUnspecifiedCosts() != null ? controlStrategy.getIncludeUnspecifiedCosts()
                        : true);
        this.includeUnspecifiedCostsCheck.setEnabled(false);

        return this.includeUnspecifiedCostsCheck;
    }

    private JComponent costYearTextField() {

        this.costYear = new IntTextField("cost year", 0, Integer.MAX_VALUE, 16);
        this.costYear.setValue(controlStrategy.getCostYear() != 0 ? controlStrategy.getCostYear()
                : CostYearTable.REFERENCE_COST_YEAR);
        this.costYear.setEditable(false);

        return this.costYear;
    }

    private JComponent inventoryYearTextField() {

        this.inventoryYear = new IntTextField("Target year", 0, Integer.MAX_VALUE, 16);
        this.inventoryYear
                .setToolTipText("This is the target year for the strategy run, often this is the year of the inventory.");

        if (this.controlStrategy.getInventoryYear() != 0) {
            this.inventoryYear.setValue(controlStrategy.getInventoryYear());
        }

        this.inventoryYear.setEditable(false);

        return this.inventoryYear;
    }

    private JComponent lastModifiedDate() {
        return createLeftAlignedLabel(CustomDateFormat.format_MM_DD_YYYY_HH_mm(controlStrategy.getLastModifiedDate()));
    }

    private JComponent creator() {
        return createLeftAlignedLabel(controlStrategy.getCreator().getName());
    }

    private JComponent description() {

        this.description = new TextArea("description", controlStrategy.getDescription(), 40, 3);
        this.description.setEditable(false);

        return this.description;
    }

    private JComponent name() {

        this.name = new TextField("name", 40);
        this.name.setEditable(false);
        this.name.setText(controlStrategy.getName());
        this.name.setMaximumSize(new Dimension(300, 15));

        return this.name;
    }

    private Project[] getProjects() throws EmfException {
        return this.getSession().getProjects();
    }

    private JComponent projects() throws EmfException {

        this.projectsCombo = new ComboBox(getProjects());
        this.projectsCombo.setSelectedItem(controlStrategy.getProject());
        this.projectsCombo.setPreferredSize(name.getPreferredSize());

        return this.projectsCombo;
    }

    private JComponent regions() throws EmfException {

        this.regionsCombo = new ComboBox(this.getSession().dataCommonsService().getRegions());
        this.regionsCombo.setSelectedItem(controlStrategy.getRegion());
        this.regionsCombo.setPreferredSize(comboSize);

        return this.regionsCombo;
    }

    private JComponent majorPollutants() throws EmfException {

        Pollutant[] pollutants = getAllPollutants(this.getSession());
        this.majorPollutant = new ComboBox(pollutants);
        this.majorPollutant.setSelectedItem(controlStrategy.getTargetPollutant());
        this.majorPollutant.setPreferredSize(comboSize);

        return this.majorPollutant;
    }
    
    private JPanel createMultiPollutants() {
        JPanel panel = new JPanel(new BorderLayout());
        multiPollField = new TextField("multipollfiels", 16);
        multiPollField.setText(getPollutantsString(controlStrategy.getTargetPollutants()));
        multiPollField.setEditable(false);
        panel.add(multiPollField, BorderLayout.LINE_START);
        panel.setPreferredSize(comboSize);
        
        return panel;
    }

    private Pollutant[] getAllPollutants(EmfSession session) throws EmfException {
        return session.dataCommonsService().getPollutants();
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

        clearMessage();

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
        controlStrategy.setTargetPollutant(checkMajorPollutant(!strategyType.getName().equals(
                StrategyType.projectFutureYearInventory)));
        controlStrategy.setUseCostEquations(useCostEquationCheck.isSelected());
        controlStrategy.setIncludeUnspecifiedCosts(includeUnspecifiedCostsCheck.isSelected());
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
        return new Regions(this.getSession().dataCommonsService().getRegions()).get(regionName);
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
        return new Projects(this.getSession().getProjects()).get(projectName);
    }

    public void setRunMessage(ControlStrategy controlStrategy) {

        clearMessage();

        updateStartDate(controlStrategy);
        updateSummaryPanelValuesExceptStartDate("Running", "", null, null);
    }

    public void refresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        // this.controlStrategy = controlStrategy;
        updateSummaryResultPanel(controlStrategy, controlStrategyResults);

        // ControlStrategyResultsSummary summary = new ControlStrategyResultsSummary(controlStrategyResults);
        // String runStatus = summary.getRunStatus();
        String runStatus = controlStrategy.getRunStatus();

        if (runStatus.indexOf("Running") == -1 && runStatus.indexOf("Waiting") == -1) {
            clearMessage();
        }
    }

    private void updateSummaryResultPanel(ControlStrategy controlStrategy,
            ControlStrategyResult[] controlStrategyResults) {

        String runStatus = controlStrategy.getRunStatus(); // summary.getRunStatus();
        if (controlStrategyResults == null || controlStrategyResults.length == 0) {
            updateStartDate(controlStrategy);
            updateSummaryPanelValuesExceptStartDate("" + runStatus, "", null, null);
            return;
        }
        ControlStrategyResultsSummary summary = new ControlStrategyResultsSummary(controlStrategyResults);
        summary.getCompletionTime();

        String completionTime;
        if (runStatus.indexOf("Finished") == -1)
            completionTime = runStatus;
        else
            completionTime = CustomDateFormat.format_MM_DD_YYYY_HH_mm_ss(controlStrategy.getCompletionDate());
        String userName = controlStrategy.getCreator().getName();
        // String userName= summary.getUser().getName()== null ? summary.getUser().getName() : "";
        Date startTime = controlStrategy.getStartDate() == null ? null : controlStrategy.getStartDate();
        updateStartDate(startTime);

        updateSummaryPanelValuesExceptStartDate("" + completionTime, "" + userName, controlStrategy.getTotalCost(), 
                (!controlStrategy.getStrategyType().getName().equals(StrategyType.MULTI_POLLUTANT_MAX_EMISSIONS_REDUCTION) ? (controlStrategy.getTotalReduction() != null ? controlStrategy.getTotalReduction() + "" : null) : "N/A"));
    }

    private void updateStartDate(Date startTime) {
        String time = startTime == null ? "" : CustomDateFormat.format_MM_DD_YYYY_HH_mm_ss(startTime);
        startDate.setText(time);
    }

    private void updateStartDate(ControlStrategy controlStrategy) {
        String startDateString = CustomDateFormat.format_MM_DD_YYYY_HH_mm_ss(controlStrategy.getStartDate());
        startDate.setText((startDateString == null || startDateString.trim() == "" ? "Not started" : startDateString));
    }

//    private void updateSummaryPanelValuesExceptStartDate(String closeDate, String userName, Double cost,
//            Double reduction) {
//        completionDate.setText(closeDate);
//        user.setText(userName);
//        costValue.setText(cost == null ? "" : "$" + decFormat.format(cost));
//        emissionReductionValue.setText(reduction == null ? "" : decFormat.format(reduction));
//    }
//    
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
    
    private String getPollutantsString(ControlStrategyTargetPollutant[] polls) {
        String text = "";
        
        if (polls == null || polls.length == 0)
            return text;
        
        for (ControlStrategyTargetPollutant pol : polls) {
            text += pol.getPollutant().getName() + "; ";
        }
        
        return text;
    }
}
