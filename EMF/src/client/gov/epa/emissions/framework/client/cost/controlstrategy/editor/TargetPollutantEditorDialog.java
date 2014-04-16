package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyTargetPollutant;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlmeasure.EfficiencyRecordValidation;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.emissions.framework.ui.TrackableSortFilterSelectModel;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

public class TargetPollutantEditorDialog extends JDialog implements TargetPollutantEditorView {

    private TrackableSortFilterSelectModel selectModel;

    private EmfConsole parent;

    private TargetPollutantEditorPresenter presenter;
    
    private ManageChangeables changeables;

    private JLabel pollutant;

    private TextField emisReduction;

    private TextField contrlEff;

    private TextField costPerTon;

    private TextField annCost;

    private TextField replacementControlMinEfficiencyDiff;

    private ControlStrategyTargetPollutant controlStrategyTargetPollutant;

    private TextArea filter;

    private ComboBox dataset;

    private ComboBox version;

    private EditControlStrategyPresenter editControlStrategyPresenter;

    private SingleLineMessagePanel messagePanel;

    private int order;

    private JLabel orderLabel;

    public TargetPollutantEditorDialog(EmfConsole parent, ManageChangeables changeables, ControlStrategyTargetPollutant controlStrategyTargetPollutant, int order) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        
        this.parent = parent;
        this.changeables = changeables;
        this.controlStrategyTargetPollutant = controlStrategyTargetPollutant;
        this.order = order;
    }

    public void display() {

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        messagePanel = new SingleLineMessagePanel();

        contentPane.add(messagePanel, BorderLayout.NORTH);
        try {
            contentPane.add(mainPanel(), BorderLayout.CENTER);
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        contentPane.add(buttonPanel(), BorderLayout.SOUTH);

        setTitle("Edit Control Strategy Target Pollutant");
        this.pack();
        this.setSize(800,450);
        this.setLocation(ScreenUtils.getPointToCenter(parent));
        this.setVisible(true);
    }

    private Component mainPanel() throws EmfException {
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        JPanel panel = new JPanel(new SpringLayout());

        
//        source.getPollutant().getName(), 
//        source.getMaxEmisReduction(),
//        source.getMaxControlEfficiency(),
//        source.getMinCostPerTon(),
//        source.getMinAnnCost(),
//        source.getReplacementControlMinEfficiencyDiff(),
//        source.getInvFilter(),
//        source.getCountyDataset(),
//        source.getCountyDatasetVersion()
        
        
        orderLabel = new JLabel(order + "");
//      this.addChangeable(locale);
        layoutGenerator.addLabelWidgetPair("Order:", orderLabel, panel);

        
        pollutant = new JLabel(controlStrategyTargetPollutant.getPollutant().getName());
//        this.addChangeable(locale);
        layoutGenerator.addLabelWidgetPair("Pollutant:", pollutant, panel);

        emisReduction = new TextField("emission reduction", 10);
        emisReduction.setText(controlStrategyTargetPollutant != null ? (controlStrategyTargetPollutant.getMaxEmisReduction() != null ? controlStrategyTargetPollutant.getMaxEmisReduction() + "" : "") : "");
        emisReduction.setToolTipText("Enter the sources target pollutant minimum emission reduction (in tons).  The control measure must be able to control greater than or equal to this tonnage.");
        changeables.addChangeable(emisReduction);
        layoutGenerator.addLabelWidgetPair("Minimum Emissions Reduction (tons)", emisReduction, panel);

        contrlEff = new TextField("control efficiency", 10);
        contrlEff.setText(controlStrategyTargetPollutant != null ? (controlStrategyTargetPollutant.getMaxControlEfficiency() != null ? controlStrategyTargetPollutant.getMaxControlEfficiency() + "" : "") : "");
        contrlEff.setToolTipText("Enter the sources target pollutant minimum control efficiency (%).  The control measure must be have a control efficiency greater than or equal to this perecentage.");
        changeables.addChangeable(contrlEff);
        layoutGenerator.addLabelWidgetPair("Minimum Control Efficiency (%)", contrlEff, panel);

        costPerTon = new TextField("cost per ton", 10);
        costPerTon.setText(controlStrategyTargetPollutant != null ? (controlStrategyTargetPollutant.getMinCostPerTon() != null ? controlStrategyTargetPollutant.getMinCostPerTon() + "" : "") : "");
        costPerTon.setToolTipText("Enter the sources target pollutant maximum cost per ton.  The control measure must be have a cost per ton less than or equal to this cost per ton.");
        changeables.addChangeable(costPerTon);
        layoutGenerator.addLabelWidgetPair("Maximum " + CostYearTable.REFERENCE_COST_YEAR + " Cost per Ton ($/ton)", costPerTon, panel);

        annCost = new TextField("annual cost", 10);
        annCost.setText(controlStrategyTargetPollutant != null ? (controlStrategyTargetPollutant.getMinAnnCost() != null ? controlStrategyTargetPollutant.getMinAnnCost() + "" : "") : "");
        annCost.setToolTipText("Enter the sources target pollutant maximum annualized cost.  The controlled source must have a annualized cost less than or equal to this cost.");
        changeables.addChangeable(annCost);
        layoutGenerator.addLabelWidgetPair("Maximum " + CostYearTable.REFERENCE_COST_YEAR + " Annualized Cost ($/yr)", annCost, panel);
        
        replacementControlMinEfficiencyDiff = new TextField("replacementControlMinEfficiencyDiff", 10);
        replacementControlMinEfficiencyDiff.setText(controlStrategyTargetPollutant != null ? (controlStrategyTargetPollutant.getReplacementControlMinEfficiencyDiff() != null ? controlStrategyTargetPollutant.getReplacementControlMinEfficiencyDiff() + "" : "10.0") : "10.0");
        replacementControlMinEfficiencyDiff.setToolTipText("Enter the minimum control percent reduction difference to use for replacement controls.");
        changeables.addChangeable(replacementControlMinEfficiencyDiff);
        layoutGenerator.addLabelWidgetPair("Minimum Percent Reduction Difference for Replacement Control (%)", replacementControlMinEfficiencyDiff, panel);

        String value = controlStrategyTargetPollutant.getInvFilter();
        if (value == null)
            value = "";
        
        filter = new TextArea("filter", value, 32, 2);
        filter.setToolTipText("Enter a filter that could be entered as a SQL where clause (e.g., ANN_EMIS>5000 and SCC like '30300%')");
        JScrollPane scrollPane = new JScrollPane(filter);
        changeables.addChangeable(filter);
        
        layoutGenerator.addLabelWidgetPair("<html>Inventory Filter:<br/>(e.g., ANN_EMIS&gt;5000 and SCC like '30300%')</html>", scrollPane, panel);

        EmfDataset[] datasets = editControlStrategyPresenter.getDatasets( editControlStrategyPresenter.getDatasetType("List of Counties (CSV)") );
//        String width = EmptyStrings.create(80);
//        Dimension size=new Dimension(500, 13);
        
        dataset = new ComboBox("Not selected", datasets);
        if (controlStrategyTargetPollutant.getCountyDataset() != null) dataset.setSelectedItem(controlStrategyTargetPollutant.getCountyDataset());

        dataset.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    EmfDataset countyDataset = (EmfDataset)dataset.getSelectedItem();
                    Integer versionNumber = (countyDataset != null ? countyDataset.getDefaultVersion() : null);

                    fillVersions(countyDataset, versionNumber);
                } catch (EmfException e1) {
                    // NOTE Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });

        version =new ComboBox(new Version[0]);           
//        version.setPrototypeDisplayValue(width);
        try {
            EmfDataset countyDataset = controlStrategyTargetPollutant.getCountyDataset();
            Integer versionNumber = (countyDataset != null ? controlStrategyTargetPollutant.getCountyDatasetVersion() : null);
            fillVersions(countyDataset, versionNumber);
        } catch (EmfException e1) {
            // NOTE Auto-generated catch block
            e1.printStackTrace();
        }
//        if (controlStrategyTargetPollutant.getCountyDataset() != null) version.setSelectedItem(controlStrategyTargetPollutant.getCountyDatasetVersion());
        
        layoutGenerator.addLabelWidgetPair("County Dataset:", dataset, panel);
        layoutGenerator.addLabelWidgetPair("County Dataset Version:", version, panel);

        layoutGenerator.makeCompactGrid(panel, 10, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10); // xPad, yPad

        return panel;
    }

    private void fillVersions(EmfDataset dataset, Integer versionNumber) throws EmfException{
        version.setEnabled(true);

        if (dataset != null && dataset.getName().equals("None")) dataset = null;
        Version[] versions = editControlStrategyPresenter.getVersions(dataset);
        version.removeAllItems();
        version.setModel(new DefaultComboBoxModel(versions));
        version.revalidate();
        if (versions.length > 0) 
            version.setSelectedIndex(getVersionIndex(versions, dataset, versionNumber));

    }
    
    private int getVersionIndex(Version[] versions, EmfDataset dataset, Integer version) {
//        int defaultversion = dataset.getDefaultVersion();
        
        if (version != null) {
            for (int i = 0; i < versions.length; i++)
                if (version == versions[i].getVersion())
                    return i;
        }

        return 0;
    }
 
    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new OKButton(okAction()));
        panel.add(new CancelButton(cancelAction()));
        return panel;
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }

        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    edit();
                    setVisible(false);
                    dispose();
                } catch (EmfException e1) {
                    // NOTE Auto-generated catch block
                    messagePanel.setError(e1.getMessage());
                    e1.printStackTrace();
                }
            }
        };
    }

    private void edit() throws EmfException {

        
        
        //validate the data, set to null if applicable
        EfficiencyRecordValidation erValidation = new EfficiencyRecordValidation();
        //make sure constraints are in the correct numerical format... validation will happen in the run method.
        if (contrlEff.getText().trim().length() > 0) controlStrategyTargetPollutant.setMaxControlEfficiency(erValidation.parseDouble("maximum control efficieny", contrlEff.getText())); else controlStrategyTargetPollutant.setMaxControlEfficiency(null);
        if (emisReduction.getText().trim().length() > 0) controlStrategyTargetPollutant.setMaxEmisReduction(erValidation.parseDouble("maximum emission reduction", emisReduction.getText())); else controlStrategyTargetPollutant.setMaxEmisReduction(null);
        if (costPerTon.getText().trim().length() > 0) controlStrategyTargetPollutant.setMinCostPerTon(erValidation.parseDouble("minimum cost per ton", costPerTon.getText())); else controlStrategyTargetPollutant.setMinCostPerTon(null);
        if (annCost.getText().trim().length() > 0) controlStrategyTargetPollutant.setMinAnnCost(erValidation.parseDouble("minimum annualized cost", annCost.getText())); else controlStrategyTargetPollutant.setMinAnnCost(null);
        if (replacementControlMinEfficiencyDiff.getText().trim().length() > 0) controlStrategyTargetPollutant.setReplacementControlMinEfficiencyDiff(erValidation.parseDouble("replacement control minimum control efficiency difference", replacementControlMinEfficiencyDiff.getText()));

        controlStrategyTargetPollutant.setInvFilter(filter.getText().trim());

        if (dataset.getSelectedItem() == null) {
            controlStrategyTargetPollutant.setCountyDataset(null);
            controlStrategyTargetPollutant.setCountyDatasetVersion(null);
        } else {
            controlStrategyTargetPollutant.setCountyDataset((EmfDataset)dataset.getSelectedItem());
            controlStrategyTargetPollutant.setCountyDatasetVersion(((Version)version.getSelectedItem()).getVersion());
        }
        
        presenter.doEdit(controlStrategyTargetPollutant);

    }

    public void observe(TargetPollutantEditorPresenter presenter, EditControlStrategyPresenter editControlStrategyPresenter) {
        this.presenter = presenter;
        this.editControlStrategyPresenter = editControlStrategyPresenter;
    }

}
