package gov.epa.emissions.framework.client.cost.controlprogram.viewer;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.BorderlessButton;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.DisabledButton;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfPanel;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.viewer.DataViewPresenter;
import gov.epa.emissions.framework.client.data.viewer.DataViewer;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlProgramType;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ControlProgramSummaryTab extends EmfPanel implements ControlProgramTabView {

    private ControlProgramSummaryTabPresenter presenter;

    private ControlProgram controlProgram;

    private TextField name;

    private TextField startDate;

    private TextField endDate;

    private TextArea description;

    private ComboBox controlProgramTypeCombo;

    private TextField dataset;

    protected ComboBox version;

    private ComboBox dsType;

    private Dimension preferredSize = new Dimension(450, 25);

    public ControlProgramSummaryTab(ControlProgram controlProgram, MessagePanel messagePanel, EmfConsole parentConsole,
            DesktopManager desktopManager) {

        super("summary", parentConsole, desktopManager, messagePanel);
        this.controlProgram = controlProgram;
    }

    public void display() throws EmfException {
        setLayout();
    }

    private void setLayout() throws EmfException {

        super.setLayout(new BorderLayout());
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getBorderedPanel(createmMainSection(), ""), BorderLayout.CENTER);
        super.add(panel, BorderLayout.CENTER);
    }

    private JPanel createmMainSection() throws EmfException {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel panelTop = new JPanel(new SpringLayout());
        // panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Name:", getNameComponent(), panelTop);
        layoutGenerator
                .addLabelWidgetPair("Description:", new ScrollableComponent(getDescriptionComponent()), panelTop);

        layoutGenerator.addLabelWidgetPair("Start Date:", getStartComponent(), panelTop);
        layoutGenerator.addLabelWidgetPair("End Date:", getEndComponent(), panelTop);
        layoutGenerator.addLabelWidgetPair("Last Modified Date:", getLastModifiedDateComponent(), panelTop);
        layoutGenerator.addLabelWidgetPair("Creator:", getCreatorComponent(), panelTop);
        layoutGenerator.makeCompactGrid(panelTop, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad
        panel.add(panelTop);

        JPanel panelBottom = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator1 = new SpringLayoutGenerator();

        layoutGenerator1.addLabelWidgetPair("Type of Control Program:", getTypeOfAnalysisComponent(), panelBottom);

        dsType = new ComboBox(presenter.getDatasetTypes());
        if (controlProgram.getDataset() != null) {
            dsType.setSelectedItem(controlProgram.getDataset().getDatasetType());
        }

        dsType.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                dataset.setText("");
                fillVersions(null, new Version[] {});
            }
        });

        dsType.setPreferredSize(preferredSize);
        layoutGenerator.addLabelWidgetPair("Dataset Type:", dsType, panelBottom);

        layoutGenerator.addLabelWidgetPair("Dataset:", datasetPanel(), panelBottom);
        Version[] versions = getVersions(controlProgram.getDataset());
        version = new ComboBox(versions);
        fillVersions(controlProgram.getDataset(), versions);

        if (controlProgram.getDatasetVersion() != null) {
            for (Version v : versions) {
                if (v.getVersion() == controlProgram.getDatasetVersion()) {
                    version.setSelectedItem(v);
                }
            }
        }

        version.setPreferredSize(preferredSize);
        layoutGenerator.addLabelWidgetPair("Version:", version, panelBottom);

        // Lay out the panel.
        layoutGenerator1.makeCompactGrid(panelBottom, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        panel.add(panelBottom);
        return panel;
    }

    private JComponent datasetPanel() {

        dataset = new TextField("dataset", 35);
        dataset.setEditable(false);
        EmfDataset inputDataset = controlProgram.getDataset();
        if (inputDataset != null) {
            dataset.setText(controlProgram.getDataset().getName());
        }

        JButton selectButton = new DisabledButton("Select");
        selectButton.setMargin(new Insets(1, 2, 1, 2));
        Button viewButton = new BorderlessButton("View", viewDatasetAction());
        Button viewDataButton = new BorderlessButton("View Data", viewDataAction());
        JPanel invPanel = new JPanel(new BorderLayout(5, 0));
        JPanel subPanel = new JPanel(new BorderLayout(5, 0));

        invPanel.add(dataset, BorderLayout.LINE_START);
        subPanel.add(selectButton, BorderLayout.LINE_START);
        subPanel.add(viewDataButton, BorderLayout.LINE_END);
        invPanel.add(subPanel);
        invPanel.add(viewButton, BorderLayout.LINE_END);
        return invPanel;
    }

    private JComponent getTypeOfAnalysisComponent() throws EmfException {

        ControlProgramType[] types = this.getSession().controlProgramService().getControlProgramTypes();
        controlProgramTypeCombo = new ComboBox("Choose a control program type", types);
        controlProgramTypeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                ControlProgramType controlProgramType = (ControlProgramType) controlProgramTypeCombo.getSelectedItem();
                presenter.doChangeControlProgramType(controlProgramType);
            }
        });
        if (controlProgram.getControlProgramType() != null)
            controlProgramTypeCombo.setSelectedItem(controlProgram.getControlProgramType());

        return controlProgramTypeCombo;
    }

    private JPanel getBorderedPanel(JPanel component, String border) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new Border(border));
        panel.add(component);

        return panel;
    }

    private JComponent getLastModifiedDateComponent() {
        return createLeftAlignedLabel(controlProgram.getLastModifiedDate() != null ? CustomDateFormat
                .format_MM_DD_YYYY_HH_mm(controlProgram.getLastModifiedDate()) : "");
    }

    private JComponent getCreatorComponent() {
        return createLeftAlignedLabel(controlProgram.getCreator() != null ? controlProgram.getCreator().getName() : "");
    }

    private JComponent getDescriptionComponent() {

        description = new TextArea("description", controlProgram.getDescription() != null ? controlProgram
                .getDescription() : "", 40, 3);
        description.setEditable(false);

        return description;
    }

    private JComponent getNameComponent() {

        name = new TextField("name", 40);
        name.setEditable(false);
        name.setText(controlProgram.getLastModifiedDate() != null ? controlProgram.getName() : "");
        name.setMaximumSize(new Dimension(300, 15));

        return name;
    }

    private JComponent getStartComponent() {

        startDate = new TextField("start", 40);
        this.startDate.setEditable(false);
        startDate.setText(controlProgram.getStartDate() != null ? CustomDateFormat.format_MM_DD_YYYY(controlProgram
                .getStartDate()) : "");
        startDate.setMaximumSize(new Dimension(300, 15));

        return startDate;
    }

    private JComponent getEndComponent() {

        endDate = new TextField("end", 40);
        this.endDate.setEditable(false);
        endDate.setText(controlProgram.getEndDate() != null ? CustomDateFormat.format_MM_DD_YYYY(controlProgram
                .getEndDate()) : "");
        endDate.setToolTipText("Optional, the date efter which the control program no longer applies.");
        endDate.setMaximumSize(new Dimension(300, 15));

        return endDate;
    }

    private JLabel createLeftAlignedLabel(String name) {
        JLabel label = new JLabel(name);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }

    public void notifyControlProgramTypeChange(ControlProgramType controlProgramType) {
        // NOTE Auto-generated method stub

    }

    public void observe(ControlProgramSummaryTabPresenter presenter) {
        this.presenter = presenter;
    }

    private Version[] getVersions(EmfDataset dataset) {
        Version[] versions = new Version[] {};
        try {
            versions = presenter.getVersions(dataset);
        } catch (EmfException e) {
            this.showError(e.getMessage());
        }
        return versions;
    }

    protected void fillVersions(EmfDataset dataset, Version[] versions) {
        version.setEnabled(true);
        version.removeAllItems();
        version.setModel(new DefaultComboBoxModel(versions));
        version.revalidate();
        if (versions.length > 0)
            version.setSelectedIndex(getDefaultVersionIndex(versions, dataset));
    }

    private int getDefaultVersionIndex(Version[] versions, EmfDataset dataset) {
        int defaultversion = dataset.getDefaultVersion();

        for (int i = 0; i < versions.length; i++)
            if (defaultversion == versions[i].getVersion())
                return i;

        return 0;
    }

    private Action viewDatasetAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    viewAction();
                } catch (EmfException e) {
                    showError("Error viewing dataset: " + e.getMessage());
                }
            }
        };
    }

    private Action viewDataAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearMessage();
                viewDataSetData();
            }
        };
    }

    private void viewDataSetData() {
        EmfDataset dataset = controlProgram.getDataset();
        if (dataset == null) {
            this.showMessage("Dataset is not available.");
            return;
        }

        Version[] versions = getVersions(controlProgram.getDataset());
        Version version = null;

        if (controlProgram.getDatasetVersion() != null) {
            for (Version v : versions) {
                if (v.getVersion() == controlProgram.getDatasetVersion())
                    version = v;
            }
        }

        showDatasetDataViewer(dataset, version);
    }

    private void showDatasetDataViewer(EmfDataset dataset, Version version) {
        try {
            DataViewer dataViewerView = new DataViewer(dataset, this.getParentConsole(), this.getDesktopManager());
            DataViewPresenter dataViewPresenter = new DataViewPresenter(dataset, version, getTableName(dataset),
                    dataViewerView, this.getSession());
            dataViewPresenter.display();
        } catch (EmfException e) {
            // displayError(e.getMessage());
        }
    }

    protected String getTableName(Dataset dataset) {
        InternalSource[] internalSources = dataset.getInternalSources();
        String tableName = "";
        if (internalSources.length > 0)
            tableName = internalSources[0].getTable();
        return tableName;
    }

    protected void viewAction() throws EmfException {
        this.clearMessage();

        if (controlProgram.getDataset() == null) {
            this.showMessage("Dataset is not available.");
            return;
        }

        PropertiesViewPresenter datasetViewPresenter = new PropertiesViewPresenter(presenter.getDataset(controlProgram
                .getDataset().getId()), this.getSession());
        DatasetPropertiesViewer view = new DatasetPropertiesViewer(this.getSession(), this.getParentConsole(), this
                .getDesktopManager());
        datasetViewPresenter.doDisplay(view);
    }
}
