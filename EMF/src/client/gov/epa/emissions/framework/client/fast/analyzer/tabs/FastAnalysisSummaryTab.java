package gov.epa.emissions.framework.client.fast.analyzer.tabs;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfInternalFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.fast.analyzer.FastAnalysisPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastAnalysis;
import gov.epa.emissions.framework.services.fast.FastRun;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

@SuppressWarnings("serial")
public class FastAnalysisSummaryTab extends AbstractFastAnalysisTab {

    private TextField nameField;

    private TextField abbreviationField;

    private TextArea descriptionField;

    private JLabel creatorValueLabel;

    private JLabel startTimeValueLabel;

    private JLabel endTimeValueLabel;

    private JLabel lastModifiedTimeValueLabel;

    private JLabel lastModifiedByValueLabel;

    public FastAnalysisSummaryTab(FastAnalysis analysis, EmfSession session, MessagePanel messagePanel,
            EmfInternalFrame parentInternalFrame, DesktopManager desktopManager, EmfConsole parentConsole,
            FastAnalysisPresenter presenter) {

        super(analysis, session, messagePanel, parentInternalFrame, desktopManager, parentConsole, presenter);
        this.setName("Summary");
    }

    public void display() {

        this.setLayout(new BorderLayout());
        this.add(createOverviewSection(), BorderLayout.CENTER);
        super.display();
    }

    protected void addChangables() {

        ManageChangeables changeablesList = this.getChangeablesList();
        changeablesList.addChangeable(this.nameField);
        changeablesList.addChangeable(this.abbreviationField);
        changeablesList.addChangeable(this.descriptionField);
    }

    protected void populateFields() {

        FastAnalysis analysis = this.getAnalysis();
        this.nameField.setText(getNonNullText(analysis.getName()));
        this.abbreviationField.setText(getNonNullText(analysis.getAbbreviation()));
        this.descriptionField.setText(getNonNullText(analysis.getDescription()));
        this.creatorValueLabel.setText(getNonNullText(analysis.getCreator().getUsername()));
        this.startTimeValueLabel.setText(this.formatDate(analysis.getStartDate()));
        this.endTimeValueLabel.setText(this.formatDate(analysis.getCompletionDate()));
        this.lastModifiedTimeValueLabel.setText(this.formatDate(analysis.getLastModifiedDate()));
        this.lastModifiedByValueLabel.setText(analysis.getCreator().getUsername());
    }

    private String formatDate(Date date) {
        return CustomDateFormat.format_MM_DD_YYYY(date);
    }

    private String getNonNullText(String value) {
        return (value != null) ? value : "";
    }

    private JPanel createOverviewSection() {

        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addWidgetPair(createLeftOverview(), createRightOverview(), panel);
        widgetLayout(1, 2, 5, 5, 5, 5, layoutGenerator, panel);

        return panel;
    }

    private JPanel createLeftOverview() {

        JPanel panel = new JPanel(new GridBagLayout());

        Insets labelInsets = new Insets(5, -30, 4, 5);
        Insets valueInsets = new Insets(0, 0, 0, 0);

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = labelInsets;

        JLabel nameLabel = new JLabel("Name:");
        panel.add(nameLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = valueInsets;

        this.nameField = new TextField("Fast Run Name", 27);
        panel.add(this.nameField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = labelInsets;

        JLabel abbreviationLabel = new JLabel("Abbreviation:");
        panel.add(abbreviationLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = valueInsets;

        this.abbreviationField = new TextField("Fast Run Abbreviation", 27);
        panel.add(this.abbreviationField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = labelInsets;

        JLabel descriptionLabel = new JLabel("Description:");
        panel.add(descriptionLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = valueInsets;

        this.descriptionField = new TextArea("Fast Run Description", "");
        ScrollableComponent scrollPane = new ScrollableComponent(this.descriptionField);
        scrollPane.setPreferredSize(new Dimension(300, 86));

        panel.add(scrollPane, constraints);

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.weighty = 1;

        JLabel emptyLabel = new JLabel();
        panel.add(emptyLabel, constraints);

        return panel;
    }

    private JPanel createRightOverview() {

        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        Insets insets = new Insets(0, 10, 8, 0);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = insets;

        JLabel label = new JLabel("Created by:");
        Font labelFont = label.getFont();
        panel.add(label, constraints);

        Font valueFont = new Font(labelFont.getName(), Font.PLAIN, labelFont.getSize());

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = insets;

        this.creatorValueLabel = new JLabel();
        this.creatorValueLabel.setFont(valueFont);
        panel.add(this.creatorValueLabel, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = insets;

        label = new JLabel("Start time:");
        panel.add(label, constraints);

        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = insets;

        this.startTimeValueLabel = new JLabel();
        this.startTimeValueLabel.setFont(valueFont);
        panel.add(this.startTimeValueLabel, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = insets;

        label = new JLabel("End time:");
        panel.add(label, constraints);

        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = insets;

        this.endTimeValueLabel = new JLabel();
        this.endTimeValueLabel.setFont(valueFont);
        panel.add(this.endTimeValueLabel, constraints);

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = insets;

        label = new JLabel("Last modified time:");
        panel.add(label, constraints);

        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = insets;

        this.lastModifiedTimeValueLabel = new JLabel();
        this.lastModifiedTimeValueLabel.setFont(valueFont);
        panel.add(this.lastModifiedTimeValueLabel, constraints);

        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = insets;

        label = new JLabel("Last modified by:");
        panel.add(label, constraints);

        constraints.gridx = 1;
        constraints.gridy = 4;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = insets;

        this.lastModifiedByValueLabel = new JLabel();
        this.lastModifiedByValueLabel.setFont(valueFont);
        panel.add(this.lastModifiedByValueLabel, constraints);

        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.weighty = 1;

        JLabel emptyLabel = new JLabel();
        panel.add(emptyLabel, constraints);

        return panel;
    }

    private void widgetLayout(int rows, int cols, int initX, int initY, int xPad, int yPad,
            SpringLayoutGenerator layoutGenerator, JPanel panel) {
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, rows, cols, // rows, cols
                initX, initY, // initialX, initialY
                xPad, yPad);// xPad, yPad
    }

    @Override
    void refreshData() {
        this.populateFields();
    }

    public void save(FastAnalysis analysis) {

        this.clearMessage();

        try {
            validateFields();
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        analysis.setName(this.nameField.getText());
        analysis.setAbbreviation(this.abbreviationField.getText());
        analysis.setDescription(this.descriptionField.getText());
        analysis.setLastModifiedDate(new Date());
        analysis.setCreator(this.getSession().user());
    }

    public void save(FastRun run) throws EmfException {

        this.clearMessage();

        validateFields();

        run.setName(this.nameField.getText());
        run.setAbbreviation(this.abbreviationField.getText());
        run.setDescription(this.descriptionField.getText());
        run.setLastModifiedDate(new Date());
        run.setCreator(this.getSession().user());
    }

    private void validateFields() throws EmfException {

        this.clearMessage();

        if (this.nameField.getText().trim().length() == 0) {
            throw new EmfException(this.getName() + " tab: A name must be specified");
        }

        if (this.abbreviationField.getText().trim().length() == 0) {
            throw new EmfException(this.getName() + " tab: An abbreviation must be specified");
        }
    }

    public void refresh(FastAnalysis analysis) {

        this.setAnalysis(analysis);

        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            this.populateFields();
            this.refreshLayout();
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    public void modify() {
        populateLastModifiedFields();
    }

    private void populateLastModifiedFields() {

        this.lastModifiedTimeValueLabel.setText(CustomDateFormat.format_YYYY_MM_DD_HH_MM(new Date()));
        this.lastModifiedByValueLabel.setText(this.getSession().user().getUsername());
    }

    public void viewOnly() {

        this.nameField.setEditable(false);
        this.descriptionField.setEditable(false);
    }
}
