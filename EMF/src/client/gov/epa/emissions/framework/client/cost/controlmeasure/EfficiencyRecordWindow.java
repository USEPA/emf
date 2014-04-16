package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.Utils;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlmeasure.YearValidation;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.NumberFieldVerifier;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public abstract class EfficiencyRecordWindow extends DisposableInteralFrame {

    protected ControlMeasure measure;

    protected EmfSession session;

    protected MessagePanel messagePanel;

    protected TextField efficiency;

    protected TextField minEmis;

    protected TextField maxEmis;

    protected TextField costYear;

    protected TextField costperTon;

    protected TextField measureAbbreviation;

    protected TextField existingdevCode;

    protected TextField locale;

    protected TextField ruleEffectiveness;

    protected TextField rulePenetration;

    protected TextField caprecFactor;

    protected TextField discountRate;

    protected TextArea detail;

    protected TextField effectiveDate;

    protected ComboBox pollutant;

    protected ComboBox equationType;

    private NumberFieldVerifier verifier;

    protected EfficiencyRecord record;

    protected Pollutant[] allPollutants;

    private String[] equationTypes = { "cpton" };

    protected TextField lastModifiedTime;

    protected TextField lastModifiedBy;
    
    protected TextField capAnnRatio;
    
    protected TextField incrementalCPT;

    protected CostYearTable costYearTable;
    
    protected JLabel refYrCostPerTon;

    static int counter = 0;
    
    protected SaveButton saveRecord;
    
    protected Button cancel;

    public EfficiencyRecordWindow(String title, ManageChangeables changeablesList, DesktopManager desktopManager,
            EmfSession session, CostYearTable costYearTable) {
        super(title, new Dimension(675, 500), desktopManager);
        super.setMinimumSize(new Dimension(675,460));
        this.session = session;
        this.verifier = new NumberFieldVerifier("");
        this.costYearTable = costYearTable;
        this.costYearTable.setTargetYear(CostYearTable.REFERENCE_COST_YEAR);
    }

    public abstract void save();

    public void display(ControlMeasure measure, EfficiencyRecord record) {
        String name = measure.getName();
        if (name == null) {
            name = "New Control Measure";
        }

        this.record = record;

        super.setLabel(super.getTitle() + " " + (counter++) + " for " + name);
        JPanel layout = createLayout();
        super.getContentPane().add(layout);        
        super.display();

        resetChanges();

    }

    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        panel.add(recordPanel());

 //       panel.add(detailPanel());
        panel.add(buttonsPanel());

        return panel;
    }


    private JPanel recordPanel() {
  
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
//        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        container.add(LeftRecordPanel());
        container.add(RightRecordPanel());
        panel.add(container, BorderLayout.NORTH);//LINE_START);

        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        JPanel detailContainer = new JPanel(new SpringLayout());
        detail = new TextArea("Details", "", 50, 5);
        ScrollableComponent detailPane = new ScrollableComponent(detail);
//      detailPane.setPreferredSize(new Dimension(540, 50));

        this.addChangeable(detail);
        layoutGenerator.addLabelWidgetPair("Details:", detailPane, detailContainer);
        layoutGenerator.makeCompactGrid(detailContainer, 1, 2, 30, 5, 10, 10);
        
////        JPanel emptyPane=new JPanel();
////        JPanel dPane=new JPanel(new BorderLayout());
//        //dPane.add(detailContainer, BorderLayout.NORTH);
//        dPane.add(emptyPane, BorderLayout.SOUTH);
        panel.add(detailContainer, BorderLayout.CENTER);
        return panel;
    }

    private Component LeftRecordPanel() {
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        JPanel panel = new JPanel(new SpringLayout());

        try {
            allPollutants = session.dataCommonsService().getPollutants();
            pollutant = new ComboBox("Select One", allPollutants);
            pollutant.setPreferredSize(new Dimension(113, 30));
            pollutant.setSelectedItem(this.record.getPollutant());
        } catch (EmfException e) {
            messagePanel.setError("Could not retrieve Pollutants");
        }
        this.addChangeable(pollutant);
        layoutGenerator.addLabelWidgetPair("Pollutant:*", pollutant, panel);

        locale = new TextField("Locale", 10);
        this.addChangeable(locale);
        layoutGenerator.addLabelWidgetPair("Locale:", locale, panel);

        effectiveDate = new TextField("Effective Date", 10);
        this.addChangeable(effectiveDate);
        layoutGenerator.addLabelWidgetPair("Effective Date:", effectiveDate, panel);

        measureAbbreviation = new TextField("Existing Measure Abbreviation", 10);
        this.addChangeable(measureAbbreviation);
        layoutGenerator.addLabelWidgetPair("Existing Measure Abbreviation:", measureAbbreviation, panel);

        existingdevCode = new TextField("Existing NEI Device Code", 10);
        this.addChangeable(existingdevCode);
        layoutGenerator.addLabelWidgetPair("Existing NEI Device Code:", existingdevCode, panel);

        costYear = new TextField("Cost Year", 10);
        costYear.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                try {
                    refreshRefYrCostPerTon();
                } catch (EmfException e1) {
                    refYrCostPerTon.setText("");
                    messagePanel.setError("Could not update reference year cost per ton: " + e1.getMessage());
                }
            }
        });
        this.addChangeable(costYear);
        layoutGenerator.addLabelWidgetPair("Cost Year:", costYear, panel);

        costperTon = new TextField("Cost Per Ton Reduced", 10);
        costperTon.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                try {
                    refreshRefYrCostPerTon();
                } catch (EmfException e1) {
                    refYrCostPerTon.setText("");
                    messagePanel.setError("Could not update reference year cost per ton: " + e1.getMessage());
                }
            }
        });

        this.addChangeable(costperTon);
        layoutGenerator.addLabelWidgetPair("Cost Per Ton Reduced:", costperTon, panel);

        
        capAnnRatio = new TextField("Cap Ann Ratio", 10);
        this.addChangeable(capAnnRatio);
        layoutGenerator.addLabelWidgetPair("Capital to Annual Ratio:", capAnnRatio, panel);
                      
        
        refYrCostPerTon = new JLabel("");
        layoutGenerator.addLabelWidgetPair("Ref Yr Cost Per Ton Reduced:", refYrCostPerTon, panel);

        efficiency = new TextField("", 10);
        efficiency.setName("Control Efficiency");
        efficiency.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                checkPercentageValue(efficiency);
            }
        });
        this.addChangeable(efficiency);
        efficiency.setToolTipText("Enter the Control Efficiency as a percentage (e.g., 90%, or -10% for a disbenefit)");
        layoutGenerator.addLabelWidgetPair("Control Efficiency (% Red):*", efficiency, panel);

        widgetLayout(10, 2, 5, 5, 10, 10, layoutGenerator, panel);

        return panel;
    }

    private void refreshRefYrCostPerTon() throws EmfException {
        messagePanel.clear();
        if (costperTon.getText().trim().length() == 0) {
            refYrCostPerTon.setText("");
            return;
        }
        int costYearValue = verifier.parseInteger(costYear);
        float costPerTonValue = verifier.parseFloat(costperTon);
        DecimalFormat currency = new DecimalFormat("#0.00"); 
        refYrCostPerTon.setText(currency.format(costPerTonValue * costYearTable.factor(costYearValue)));
    }

    //see if percentage is between 0 and 1, display message that it should be XXX% format, not a decimal fomat
    private void checkPercentageValue(TextField field) {
        messagePanel.clear();
        if (field.getText().trim().length() == 0)
            return;
        float value = 0;
        try {
            value = verifier.parseFloat(field);
        } catch (EmfException e) {
            //suppress error message, were just looking at if % is btw 0 and 1
            //let the save process, display an error for not being a number
            return;
        }
        if (value > 0 && value <= 1) messagePanel.setError("Enter the " + field.getName() + " as a percentage (e.g., 90%).");

    }

    private Component RightRecordPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        minEmis = new TextField("Minimum Emissions", 10);
        this.addChangeable(minEmis);
        layoutGenerator.addLabelWidgetPair("Minimum Emissions:", minEmis, panel);

        maxEmis = new TextField("Maximum Emissions", 10);
        this.addChangeable(maxEmis);
        layoutGenerator.addLabelWidgetPair("Maximum Emissions:", maxEmis, panel);

        ruleEffectiveness = new TextField("Rule Effectiveness", 10);
        ruleEffectiveness.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                checkPercentageValue(ruleEffectiveness);
            }
        });
        this.addChangeable(ruleEffectiveness);
        layoutGenerator.addLabelWidgetPair("Rule Effectiveness (%):", ruleEffectiveness, panel);

        rulePenetration = new TextField("Rule Penetration", 10);
        rulePenetration.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                checkPercentageValue(rulePenetration);
            }
        });
        this.addChangeable(rulePenetration);
        layoutGenerator.addLabelWidgetPair("Rule Penetration (%):", rulePenetration, panel);

        equationType = new ComboBox(equationTypes);
        equationType.setSelectedItem(record.getEquationType());
        equationType.setPreferredSize(new Dimension(113, 20));
        this.addChangeable(equationType);
        layoutGenerator.addLabelWidgetPair("Equation Type:", equationType, panel);

        caprecFactor = new TextField("Capital Recovery Factor", 10);
        this.addChangeable(caprecFactor);
        layoutGenerator.addLabelWidgetPair("Capital Recovery Factor:", caprecFactor, panel);

        discountRate = new TextField("Discount Rate", 10);
        this.addChangeable(discountRate);
        layoutGenerator.addLabelWidgetPair("Discount Rate (%):", discountRate, panel);
  //add invremental CPT      
        incrementalCPT = new TextField("Incremental CPT", 10);
        this.addChangeable(incrementalCPT);
        layoutGenerator.addLabelWidgetPair("<html>Incremental CPT (Based on<br/>specified cost year dollars):</html>", incrementalCPT, panel);


        lastModifiedBy = new TextField("Last Modified By", 10);
        lastModifiedBy.setEnabled(false);
        lastModifiedBy.setOpaque(false);
        lastModifiedBy.setDisabledTextColor(Color.BLACK);
        lastModifiedBy.setBorder(BorderFactory.createEmptyBorder());
        layoutGenerator.addLabelWidgetPair("Last Modified By:", lastModifiedBy, panel);

        lastModifiedTime = new TextField("Last Modified Time", 10);
        lastModifiedTime.setEnabled(false);
        lastModifiedTime.setOpaque(false);
        lastModifiedTime.setDisabledTextColor(Color.BLACK);
        lastModifiedTime.setBorder(BorderFactory.createEmptyBorder());
        layoutGenerator.addLabelWidgetPair("Last Modified Time:", lastModifiedTime, panel);

        widgetLayout(10, 2, 5, 5, 10, 10, layoutGenerator, panel);

        return panel;
    }

    protected String formatEffectiveDate() {
        Date effectiveDate = record.getEffectiveDate();
        return effectiveDate == null ? "" : CustomDateFormat.format_MM_DD_YYYY(effectiveDate);
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        saveRecord = new SaveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        getRootPane().setDefaultButton(saveRecord);
        panel.add(saveRecord);

        cancel = new CancelButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                closeWindow();
            }
        });
        panel.add(cancel);

        return panel;
    }

    protected void doSave() throws EmfException {
        savePollutant();
        record.setEquationType(equationType.getSelectedItem() + "");
        saveEfficiency(efficiency);
        saveMinMaxEmis();
        saveCostYear();
        saveCostPerTon();
        saveRefYrCostPerTon();
        saveLocale();
        saveRuleEffectiveness();
        saveRulePenetration();
        saveCapRecFactor();
        saveCapAnnRatio();
        saveIncrementalCPT();
        saveDiscountRate();
        record.setDetail(detail.getText().trim());
        saveEffectiveDate();
        record.setExistingMeasureAbbr(measureAbbreviation.getText().trim());
        record.setLastModifiedBy(session.user().getName());
        record.setLastModifiedTime(new Date());
        saveExistingDevCode();
    }

    private void saveIncrementalCPT() throws EmfException {
        if (incrementalCPT.getText().trim().length() > 0) {
            double value = verifier.parseDouble(incrementalCPT);         
            record.setIncrementalCostPerTon(value);
        } else {
            record.setIncrementalCostPerTon(null);
        }
    }

    private void saveCapAnnRatio() throws EmfException {
        if (capAnnRatio.getText().trim().length() > 0) {
            double value = verifier.parseDouble(capAnnRatio);         
            record.setCapitalAnnualizedRatio(value);
        } else {
            record.setCapitalAnnualizedRatio(null);
        }
    }

    private void savePollutant() throws EmfException {
        if (pollutant.getSelectedItem() == null)
            throw new EmfException("Please Select a Pollutant");
        record.setPollutant((Pollutant) pollutant.getSelectedItem());
    }

    private void saveExistingDevCode() throws EmfException {
        if (existingdevCode.getText().trim().length() == 0)
            return;
        int value = verifier.parseInteger(existingdevCode);
        record.setExistingDevCode(value);
    }

    private void saveEffectiveDate() throws EmfException {

        String dateStr = this.effectiveDate.getText().trim();
        String fieldName = "Effective Date";
        //only validate if a date was specified
        if (!dateStr.isEmpty()) {
            Utils.validateDate(dateStr, fieldName);
        }
        try {
            record.setEffectiveDate(CustomDateFormat.parse_MMddyyyy(dateStr));
        } catch (ParseException e) {
            throw new EmfException("Error while parsing date '" + dateStr + "' for '" + fieldName + "'.");
        }
    }

    private void saveDiscountRate() throws EmfException {
        if (discountRate.getText().trim().length() > 0) {
            double value = verifier.parseDouble(discountRate);
            if (value < 1 || value > 20)
                throw new EmfException("Enter the Discount Rate as a percent between 1 and 20 (e.g., 7% is entered as 7)");
            record.setDiscountRate(value);
        }else
            record.setDiscountRate(null);
    }

    private void saveCapRecFactor() throws EmfException {
        
        if (caprecFactor.getText().trim().length() > 0) {
            double value=verifier.parseDouble(caprecFactor);
            record.setCapRecFactor(value);   
        }else  
            record.setCapRecFactor(null);
    }

    private void saveRulePenetration() throws EmfException {
        float value = verifier.parseFloat(rulePenetration);
        if (value <= 1 || value > 100)
            throw new EmfException(
                    "Enter the Rule Penetration as a percent between 1 and 100 (e.g., 75% is entered as 75).");
        record.setRulePenetration(value);
    }

    private void saveRuleEffectiveness() throws EmfException {
        float value = verifier.parseFloat(ruleEffectiveness);
        if (value <= 1 || value > 100)
            throw new EmfException(
                    "Enter the Rule Effectiveness as a percent between 1 and 100.(e.g., 75% is entered as 75)");
        record.setRuleEffectiveness(value);
    }

    private void saveLocale() throws EmfException {
        String localeText = locale.getText().trim();
        if (localeText.length() == 0) {
            record.setLocale("");
            return;
        }
        verifier.parseInteger(locale);
        if (localeText.length() == 2 || localeText.length() == 5 || localeText.length() == 6)
            record.setLocale(localeText);
        else
            throw new EmfException("Locale must be a two, five, or six digit integer.");
    }

    private void saveCostPerTon() throws EmfException {
        if (costperTon.getText().trim().length() > 0) {
            double value = verifier.parseDouble(costperTon);
            if (costYear.getText().trim().length() == 0) 
                throw new EmfException("A cost year is required when a cost per ton is specified");
            record.setCostPerTon(value);
        } else {
            record.setCostPerTon(null);
        }
    }

    private void saveRefYrCostPerTon() {
        if (costperTon.getText().trim().length() > 0) {
            record.setRefYrCostPerTon(new Double(refYrCostPerTon.getText()));
        } else {
            record.setRefYrCostPerTon(null);
        }
    }

    private void saveCostYear() throws EmfException {
        if (costYear.getText().trim().length() > 0) {
            YearValidation validation = new YearValidation("Cost Year");
            record.setCostYear(validation.value(costYear.getText(), costYearTable.getStartYear(), costYearTable.getEndYear()));
        } else {
            record.setCostYear(null);
        }
    }

    private void saveEfficiency(TextField efficiency) throws EmfException {
        String efficiencyStr = efficiency.getText().trim();
        Double value = null;
        
        //Only validate if something was specified 
        if (!efficiencyStr.isEmpty())
            value = verifier.parseDouble(efficiency);
        
        record.setEfficiency(value);
    }

    private void saveMinMaxEmis() throws EmfException {
        double minEmisValue = 0;
        double maxEmisValue = Double.NaN;
        if (minEmis.getText().trim().length() > 0) {
            minEmisValue = verifier.parseDouble(minEmis);
            record.setMinEmis(minEmisValue);
        } else {
            record.setMinEmis(null);
        }
        if (maxEmis.getText().trim().length() > 0) {
            maxEmisValue = verifier.parseDouble(maxEmis);
            record.setMaxEmis(maxEmisValue);
        } else {
            record.setMaxEmis(null);
        }
        if (minEmisValue != Double.NaN 
                && maxEmisValue != Double.NaN
                && minEmisValue >= maxEmisValue)
            throw new EmfException("The minimum emission must be be less than maximum emission.");
    }

    private void widgetLayout(int rows, int cols, int initX, int initY, int xPad, int yPad,
            SpringLayoutGenerator layoutGenerator, JPanel panel) {
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, rows, cols, // rows, cols
                initX, initY, // initialX, initialY
                xPad, yPad);// xPad, yPad
    }
    
    private void closeWindow() {
        if (shouldDiscardChanges())
            disposeView();
    }
    
    public void viewOnly() {

        this.efficiency.setEditable(false);
        this.minEmis.setEditable(false);
        this.maxEmis.setEditable(false);
        this.costYear.setEditable(false);
        this.costperTon.setEditable(false);
        this.measureAbbreviation.setEditable(false);
        this.existingdevCode.setEditable(false);
        this.locale.setEditable(false);
        this.ruleEffectiveness.setEditable(false);
        this.rulePenetration.setEditable(false);
        this.caprecFactor.setEditable(false);
        this.discountRate.setEditable(false);
        this.detail.setEditable(false);
        this.effectiveDate.setEditable(false);
        this.capAnnRatio.setEditable(false);
        this.incrementalCPT.setEditable(false);
        this.detail.setEditable(false);
        
        this.saveRecord.setVisible(false);
        this.cancel.setText("Close");

        this.disableComboBoxChanges();
    }
    
    private void disableComboBoxChanges() {

        this.pollutant.addItemListener(new ComboBoxResetListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                pollutant.setSelectedItem(record.getPollutant());
            }
        }));

        this.equationType.addItemListener(new ComboBoxResetListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                equationType.setSelectedItem(record.getEquationType());
            }
        }));
    }
}