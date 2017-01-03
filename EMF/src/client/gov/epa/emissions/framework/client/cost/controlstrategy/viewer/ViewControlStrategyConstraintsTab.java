package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.EmfPanel;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.TargetPollutantTableData;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.TargetPollutantsPanel;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyConstraint;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyTargetPollutant;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlmeasure.EfficiencyRecordValidation;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;
import javax.swing.border.EtchedBorder;

public class ViewControlStrategyConstraintsTab extends EmfPanel implements ViewControlStrategyConstraintsTabView {

    private TextField emisReduction;

    private TextField contrlEff;

    private TextField costPerTon;

    private TextField annCost;

    private TextField domainWideEmisReduction;

    private TextField domainWidePctReduction;

    private TextField domainWidePctReductionIncrement;

    private TextField domainWidePctReductionStart;

    private TextField domainWidePctReductionEnd;

    private TextField replacementControlMinEfficiencyDiff;

    private TextField controlProgramMeasureMinPctRedDiff;

    private JRadioButton alwaysApplyReplacement;
    private JRadioButton matchDevicePollutant;
    private JRadioButton doNotApplyReplacement;

    private ViewControlStrategyConstraintsTabPresenter presenter;
    
    private TargetPollutantTableData pollutantsTableData;
    
    private TargetPollutantsPanel pollutantsPanel;

    private ControlStrategy controlStrategy;

    private JPanel leastCostPanel;

    private JPanel leastCostCurvePanel;

    private JPanel controlProgramPanel;

    private JPanel leastCostPanelContainer;

    private DecimalFormat decFormat;
    
    private EmfConsole parentConsole;

    private SelectableSortFilterWrapper table;

    public ViewControlStrategyConstraintsTab(ControlStrategy controlStrategy, MessagePanel messagePanel,
            EmfConsole parentConsole, DesktopManager desktopManager) {

        super("csConstraints", parentConsole, desktopManager, messagePanel);

        this.controlStrategy = controlStrategy;
        this.decFormat = new DecimalFormat("###0.0#");
        this.parentConsole = parentConsole;
    }

    private void setupLayout() {

        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JPanel optionsPanel = null;
        if (controlStrategy.getStrategyType() == null ||
            controlStrategy.getStrategyType().getName().equals(StrategyType.maxEmissionsReduction) ||
            controlStrategy.getStrategyType().getName().equals(StrategyType.leastCost) ||
            controlStrategy.getStrategyType().getName().equals(StrategyType.leastCostCurve) ||
            controlStrategy.getStrategyType().getName().equals(StrategyType.MULTI_POLLUTANT_MAX_EMISSIONS_REDUCTION)) {
            optionsPanel = getBorderedPanel(createReplaceOptionsPanel(), "Replacement Control Measure Options");
        }
        
        if (controlStrategy.getStrategyType() != null && controlStrategy.getStrategyType().getName().equals(StrategyType.MULTI_POLLUTANT_MAX_EMISSIONS_REDUCTION)) {
            try {
                add(optionsPanel, BorderLayout.NORTH);
                add(getBorderedPanel(createMultiPollutantsPanel(controlStrategy.getTargetPollutants(), 
                        presenter.getAllPollutants()), "Multi-Pollutant Max Emis Reducation"), BorderLayout.CENTER);
            } catch (EmfException e) {
                e.printStackTrace();
            }
            
            return;
        }
        
        JPanel container = new JPanel(new BorderLayout());
        if (optionsPanel != null) {
            container.add(optionsPanel, BorderLayout.NORTH);
        }
        container.add(getBorderedPanel(createAllStrategiesPanel(), "All Strategy Types"), BorderLayout.CENTER);
        add(container, BorderLayout.NORTH);

        leastCostPanelContainer = new JPanel(new BorderLayout());

        leastCostPanel = getBorderedPanel(createLeastCostPanel(), "Least Cost");
        leastCostCurvePanel = getBorderedPanel(createLeastCostCurvePanel(), "Least Cost Curve");
        controlProgramPanel = getBorderedPanel(createControlProgramPanel(), "Project Future Year Inventory");

        if (controlStrategy.getStrategyType() != null) {

            if (controlStrategy.getStrategyType().getName().equals(StrategyType.leastCost)) {
                leastCostPanelContainer.add(leastCostPanel, BorderLayout.NORTH);
            } else if (controlStrategy.getStrategyType().getName().equals(StrategyType.leastCostCurve)) {
                leastCostPanelContainer.add(leastCostCurvePanel, BorderLayout.NORTH);
            } else if (controlStrategy.getStrategyType().getName().equals(StrategyType.projectFutureYearInventory)) {
                leastCostPanelContainer.add(controlProgramPanel, BorderLayout.NORTH);
            }
        }

        this.add(leastCostPanelContainer, BorderLayout.CENTER);

        notifyStrategyTypeChange(controlStrategy.getStrategyType());
    }

    private JPanel getBorderedPanel(JPanel component, String border) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new Border(border));
        panel.add(component);

        return panel;
    }
    
    private SortCriteria sortCriteria() {
        String[] columnNames = {"Order" };
        return new SortCriteria(columnNames, new boolean[] {true}, new boolean[] { true });
    }

    private JPanel createMultiPollutantsPanel(ControlStrategyTargetPollutant[] targets, Pollutant[] all) {
        pollutantsTableData = new TargetPollutantTableData(targets, all);
        table = new SelectableSortFilterWrapper(parentConsole, pollutantsTableData, sortCriteria());
//        pollutantsPanel = new TargetPollutantsPanel("Multi-Pollutants", pollutantsTableData, null, parentConsole);
        table.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        table.setMinimumSize(new Dimension(80, 100));
        return table;
    }

    private JPanel createReplaceOptionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5,10,10,10));

        doNotApplyReplacement = new JRadioButton("Never apply replacement controls");
        doNotApplyReplacement.setEnabled(false);
        matchDevicePollutant = new JRadioButton("Apply replacement controls when device pollutant doesn't match");
        matchDevicePollutant.setEnabled(false);
        alwaysApplyReplacement = new JRadioButton("Always apply replacement controls");
        alwaysApplyReplacement.setEnabled(false);
        
        if (controlStrategy.getApplyReplacementControls() == null ||
            controlStrategy.getApplyReplacementControls() == 1) {
            alwaysApplyReplacement.setSelected(true);
        } else if (controlStrategy.getApplyReplacementControls() == 2) {
            matchDevicePollutant.setSelected(true);
        } else {
            doNotApplyReplacement.setSelected(true);
        }
        
        panel.add(new JLabel("Handling for sources that have an existing device code but no reduction percentage"));
        panel.add(doNotApplyReplacement);
        panel.add(matchDevicePollutant);
        panel.add(alwaysApplyReplacement);

        return panel;
    }

    private JPanel createAllStrategiesPanel() {
        ControlStrategyConstraint constraint = presenter.getConstraint();
        JPanel panel = new JPanel(new SpringLayout());
        // panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); //100,80));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Constraints for Target Pollutant:", new JLabel(), panel);

        this.emisReduction = new TextField("emission reduction", 10);
        this.emisReduction.setText(constraint != null ? (constraint.getMaxEmisReduction() != null ? constraint
                .getMaxEmisReduction()
                + "" : "") : "");
        this.emisReduction
                .setToolTipText("Enter the sources target pollutant minimum emission reduction (in tons).  The control measure must be able to control greater than or equal to this tonnage.");
        this.emisReduction.setEditable(false);

        layoutGenerator.addLabelWidgetPair("Minimum Emissions Reduction (tons)", this.emisReduction, panel);

        this.contrlEff = new TextField("control efficiency", 10);
        this.contrlEff.setText(constraint != null ? (constraint.getMaxControlEfficiency() != null ? constraint
                .getMaxControlEfficiency()
                + "" : "") : "");
        this.contrlEff
                .setToolTipText("Enter the sources target pollutant minimum control efficiency (%).  The control measure must be have a control efficiency greater than or equal to this perecentage.");
        this.contrlEff.setEditable(false);

        layoutGenerator.addLabelWidgetPair("Minimum Control Efficiency (%)", this.contrlEff, panel);

        this.costPerTon = new TextField("cost per ton", 10);
        this.costPerTon.setText(constraint != null ? (constraint.getMinCostPerTon() != null ? constraint
                .getMinCostPerTon()
                + "" : "") : "");
        this.costPerTon
                .setToolTipText("Enter the sources target pollutant maximum cost per ton.  The control measure must be have a cost per ton less than or equal to this cost per ton.");
        this.costPerTon.setEditable(false);

        layoutGenerator.addLabelWidgetPair("Maximum Cost per Ton ($/ton) [in Strategy Cost Year dollars]",
                this.costPerTon, panel);

        this.annCost = new TextField("annual cost", 10);
        this.annCost.setText(constraint != null ? (constraint.getMinAnnCost() != null ? constraint.getMinAnnCost() + ""
                : "") : "");
        this.annCost
                .setToolTipText("Enter the sources target pollutant maximum annualized cost.  The controlled source must have a annualized cost less than or equal to this cost.");
        this.annCost.setEditable(false);

        layoutGenerator.addLabelWidgetPair("Maximum Annualized Cost ($/yr) [in Strategy Cost Year dollars]",
                this.annCost, panel);

        this.replacementControlMinEfficiencyDiff = new TextField("replacementControlMinEfficiencyDiff", 10);
        this.replacementControlMinEfficiencyDiff.setText(constraint != null ? (constraint
                .getReplacementControlMinEfficiencyDiff() != null ? constraint.getReplacementControlMinEfficiencyDiff()
                + "" : "10.0") : "10.0");
        this.replacementControlMinEfficiencyDiff
                .setToolTipText("Enter the minimum control percent reduction difference to use for replacement controls.");
        this.replacementControlMinEfficiencyDiff.setEditable(false);

        layoutGenerator.addLabelWidgetPair("Minimum Percent Reduction Difference for Replacement Control (%)",
                this.replacementControlMinEfficiencyDiff, panel);

        layoutGenerator.makeCompactGrid(panel, 6, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10); // xPad, yPad

        return panel;
    }

    private JPanel createControlProgramPanel() {

        ControlStrategyConstraint constraint = presenter.getConstraint();

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        mainPanel.add(new JLabel(
                "Specify EITHER an emission reduction (tons) or percent reduction (%) for the Target Pollutant:"),
                BorderLayout.WEST);

        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        this.controlProgramMeasureMinPctRedDiff = new TextField("domain wide percent reduction", 10);
        this.controlProgramMeasureMinPctRedDiff.setText(constraint != null ? (constraint
                .getControlProgramMeasureMinPctRedDiff() != null ? constraint.getControlProgramMeasureMinPctRedDiff()
                + "" : "10") : "10");
        this.controlProgramMeasureMinPctRedDiff
                .setToolTipText("Enter the minimum control percent reduction difference for predicting controls.");
        this.controlProgramMeasureMinPctRedDiff.setEditable(false);

        layoutGenerator.addLabelWidgetPair("Minimum Percent Reduction Difference for Predicting Controls (%)",
                this.controlProgramMeasureMinPctRedDiff, panel);

        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10); // xPad, yPad

        mainPanel.add(panel, BorderLayout.SOUTH);
        return mainPanel;
    }

    private JPanel createLeastCostPanel() {

        ControlStrategyConstraint constraint = presenter.getConstraint();

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        mainPanel.add(new JLabel(
                "Specify EITHER an emission reduction (tons) or percent reduction (%) for the Target Pollutant:"),
                BorderLayout.WEST);

        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        this.domainWideEmisReduction = new TextField("domain wide emission reduction", 10);
        this.domainWideEmisReduction
                .setText(constraint != null ? (constraint.getDomainWideEmisReduction() != null ? decFormat
                        .format(constraint.getDomainWideEmisReduction())
                        + "" : "") : "");
        this.domainWideEmisReduction
                .setToolTipText("Enter the target domain wide emission reduction (in tons for the target pollutant).");
        this.domainWideEmisReduction.setEditable(false);

        layoutGenerator
                .addLabelWidgetPair("Domain Wide Emission Reduction (tons)", this.domainWideEmisReduction, panel);

        this.domainWidePctReduction = new TextField("domain wide percent reduction", 10);
        this.domainWidePctReduction
                .setText(constraint != null ? (constraint.getDomainWidePctReduction() != null ? decFormat
                        .format(constraint.getDomainWidePctReduction())
                        + "" : "") : "");
        this.domainWidePctReduction
                .setToolTipText("Enter the target domain wide precent reduction (% for the target pollutant).");
        this.domainWidePctReduction.setEditable(false);

        layoutGenerator.addLabelWidgetPair("Domain Wide Percent Reduction (%)", this.domainWidePctReduction, panel);

        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10); // xPad, yPad

        mainPanel.add(panel, BorderLayout.SOUTH);
        return mainPanel;
    }

    private JPanel createLeastCostCurvePanel() {

        ControlStrategyConstraint constraint = presenter.getConstraint();
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        // layoutGenerator.addLabelWidgetPair("Constraints for Target Pollutant:", new JLabel(), panel);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel(
                "Specify an increment, starting and ending percent reduction (%) for the Target Pollutant:"),
                BorderLayout.WEST);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        JPanel panel = new JPanel(new SpringLayout());
        this.domainWidePctReductionIncrement = new TextField("domain wide percent reduction increment", 10);
        this.domainWidePctReductionIncrement.setText(constraint != null ? (constraint
                .getDomainWidePctReductionIncrement() != null ? constraint.getDomainWidePctReductionIncrement() + ""
                : "25") : "25");
        this.domainWidePctReductionIncrement.setToolTipText("Enter the domain wide percent reduction increment.");
        this.domainWidePctReductionIncrement.setEditable(false);

        layoutGenerator.addLabelWidgetPair(
                "   Domain-wide Percent Reduction Increment (%)                                ",
                this.domainWidePctReductionIncrement, panel);

        this.domainWidePctReductionStart = new TextField("domain wide percent reduction start", 10);
        this.domainWidePctReductionStart
                .setText(constraint != null ? (constraint.getDomainWidePctReductionStart() != null ? constraint
                        .getDomainWidePctReductionStart()
                        + "" : "0") : "0");
        this.domainWidePctReductionStart.setToolTipText("Enter the domain wide percent reduction start precentage.");
        this.domainWidePctReductionStart.setEditable(false);

        layoutGenerator.addLabelWidgetPair("   Domain-wide Percent Reduction Start (%)",
                this.domainWidePctReductionStart, panel);

        this.domainWidePctReductionEnd = new TextField("domain wide percent reduction end", 10);
        this.domainWidePctReductionEnd
                .setText(constraint != null ? (constraint.getDomainWidePctReductionEnd() != null ? constraint
                        .getDomainWidePctReductionEnd()
                        + "" : "100") : "100");
        this.domainWidePctReductionEnd.setToolTipText("Enter the domain wide percent reduction end precentage.");
        this.domainWidePctReductionEnd.setEditable(false);

        layoutGenerator.addLabelWidgetPair("   Domain-wide Percent Reduction End (%)", this.domainWidePctReductionEnd,
                panel);

        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10); // xPad, yPad

        mainPanel.add(panel, BorderLayout.CENTER);

        return mainPanel;
    }

    public void run(ControlStrategy controlStrategy) throws EmfException {
        ControlStrategyConstraint constraint = null;
        constraint = new ControlStrategyConstraint();
        constraint.setControlStrategyId(controlStrategy.getId());
        EfficiencyRecordValidation erValidation = new EfficiencyRecordValidation();
        // make sure constraints are in the correct numerical format... validation will happen in the run method.
        if (this.contrlEff.getText().trim().length() > 0)
            constraint.setMaxControlEfficiency(erValidation.parseDouble("maximum control efficieny", this.contrlEff
                    .getText()));
        if (this.emisReduction.getText().trim().length() > 0)
            constraint.setMaxEmisReduction(erValidation.parseDouble("maximum emission reduction", this.emisReduction
                    .getText()));
        if (this.costPerTon.getText().trim().length() > 0)
            constraint.setMinCostPerTon(erValidation.parseDouble("minimum cost per ton", this.costPerTon.getText()));
        if (this.annCost.getText().trim().length() > 0)
            constraint.setMinAnnCost(erValidation.parseDouble("minimum annualized cost", this.annCost.getText()));
        if (this.domainWideEmisReduction.getText().trim().length() > 0)
            constraint.setDomainWideEmisReduction(erValidation.parseDouble("domain wide emission reduction",
                    this.domainWideEmisReduction.getText()));
        if (this.domainWidePctReduction.getText().trim().length() > 0)
            constraint.setDomainWidePctReduction(erValidation.parseDouble("domain wide percent reduction",
                    this.domainWidePctReduction.getText()));
        if (this.domainWidePctReductionIncrement.getText().trim().length() > 0)
            constraint.setDomainWidePctReductionIncrement(erValidation.parseDouble(
                    "domain wide percent reduction increment", this.domainWidePctReductionIncrement.getText()));
        if (this.domainWidePctReductionStart.getText().trim().length() > 0)
            constraint.setDomainWidePctReductionStart(erValidation.parseDouble("domain wide percent reduction start",
                    this.domainWidePctReductionStart.getText()));
        if (this.domainWidePctReductionEnd.getText().trim().length() > 0)
            constraint.setDomainWidePctReductionEnd(erValidation.parseDouble("domain wide percent reduction end",
                    this.domainWidePctReductionEnd.getText()));
        if (this.replacementControlMinEfficiencyDiff.getText().trim().length() > 0)
            constraint.setReplacementControlMinEfficiencyDiff(erValidation.parseDouble(
                    "replacement control minimum control efficiency difference",
                    this.replacementControlMinEfficiencyDiff.getText()));
        if (this.controlProgramMeasureMinPctRedDiff.getText().trim().length() > 0)
            constraint.setControlProgramMeasureMinPctRedDiff(erValidation.parseDouble(
                    "control program minimum percent reduction difference", this.controlProgramMeasureMinPctRedDiff
                            .getText()));
        if (controlStrategy.getStrategyType().getName().equalsIgnoreCase(StrategyType.leastCost)) {
            // make sure that either Emis OR Pct Reduction was specified for the Least Cost. This is needed for the run.
            if (constraint.getReplacementControlMinEfficiencyDiff() == null
                    || constraint.getReplacementControlMinEfficiencyDiff() <= 0.0D)
                throw new EmfException(
                        "Constraints Tab: The replacement control minimum control efficiencny difference is missing.");
            if (constraint.getDomainWideEmisReduction() == null && constraint.getDomainWidePctReduction() == null)
                throw new EmfException(
                        "Constraints Tab: Please specify either an emission reduction or percent reduction.");
            if (constraint.getDomainWideEmisReduction() != null && constraint.getDomainWidePctReduction() != null)
                throw new EmfException("Constraints Tab: Specify only an emission reduction or a percent reduction.");
        }
        if (controlStrategy.getStrategyType().getName().equalsIgnoreCase(StrategyType.leastCostCurve)) {
            // make sure that the Pct Reduction Increment was specified for the Least Cost Curve. This is needed for the
            // run.
            if (constraint.getReplacementControlMinEfficiencyDiff() == null
                    || constraint.getReplacementControlMinEfficiencyDiff() <= 0.0D)
                throw new EmfException(
                        "Constraints Tab: The replacement control minimum control efficiencny difference is missing.");
            if (constraint.getDomainWidePctReductionIncrement() == null
                    || constraint.getDomainWidePctReductionIncrement() <= 0.0D)
                throw new EmfException("Constraints Tab: The percent reduction increment is missing.");
            if (constraint.getDomainWidePctReductionStart() == null)
                throw new EmfException("Constraints Tab: The percent reduction starting percentage is missing.");
            if (constraint.getDomainWidePctReductionEnd() == null)
                throw new EmfException("Constraints Tab: The percent reduction ending percentage is missing.");
        }
        if (controlStrategy.getStrategyType().getName().equalsIgnoreCase(StrategyType.maxEmissionsReduction)) {
            if (constraint.getReplacementControlMinEfficiencyDiff() == null
                    || constraint.getReplacementControlMinEfficiencyDiff() <= 0.0D)
                throw new EmfException(
                        "Constraints Tab: The replacement control minimum control efficiencny difference is missing.");
        }
        if (controlStrategy.getStrategyType().getName().equalsIgnoreCase(StrategyType.projectFutureYearInventory)) {
            if (constraint.getControlProgramMeasureMinPctRedDiff() == null
                    || constraint.getControlProgramMeasureMinPctRedDiff() <= 0.0D)
                throw new EmfException(
                        "Constraints Tab: The measure minimum percent reduction difference for predicting controls is missing.");
        }
    }

    public void refresh(ControlStrategy strategy, ControlStrategyResult[] controlStrategyResults) {
        ControlStrategyConstraint constraint = strategy.getConstraint();
        if (constraint != null) {
            this.domainWideEmisReduction.setText(constraint.getDomainWideEmisReduction() != null ? decFormat
                    .format(constraint.getDomainWideEmisReduction())
                    + "" : "");
            this.domainWidePctReduction.setText(constraint.getDomainWidePctReduction() != null ? decFormat
                    .format(constraint.getDomainWidePctReduction())
                    + "" : "");
        }
    }

    public void display(ControlStrategy strategy) {
        setupLayout();
    }

    public void observe(ViewControlStrategyConstraintsTabPresenter presenter) {
        this.presenter = presenter;
    }

    public void notifyStrategyTypeChange(StrategyType strategyType) {
        leastCostPanelContainer.removeAll();
        if (strategyType != null) {
            if (strategyType.getName().equals(StrategyType.leastCost)) {
                leastCostPanelContainer.add(leastCostPanel, BorderLayout.NORTH);
            } else if (strategyType.getName().equals(StrategyType.leastCostCurve)) {
                leastCostPanelContainer.add(leastCostCurvePanel, BorderLayout.NORTH);
            } else if (strategyType.getName().equals(StrategyType.projectFutureYearInventory)) {
                leastCostPanelContainer.add(controlProgramPanel, BorderLayout.NORTH);
            }
        }
    }

    public void notifyStrategyRun(ControlStrategy controlStrategy) {
        // NOTE Auto-generated method stub
    }
}
