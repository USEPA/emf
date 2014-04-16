package gov.epa.emissions.framework.client.fast.run.tabs;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.Changeable;
import gov.epa.emissions.commons.gui.Changeables;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.EmfInternalFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionView;
import gov.epa.emissions.framework.client.fast.AbstractFastAction;
import gov.epa.emissions.framework.client.fast.run.FastRunPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.fast.FastRun;
import gov.epa.emissions.framework.services.fast.Grid;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

@SuppressWarnings("serial")
public class FastRunConfigurationTab extends AbstractFastRunTab {

    private TextField invTableDatasetField;

    private ComboBox invTableDatasetVersionComboBox;

    private TextField speciesMappingDatasetField;

    private ComboBox speciesMappingDatasetVersionComboBox;

    private PollutantList pollutantList;

    private TextField transferCoeffDatasetField;

    private ComboBox transferCoeffDatasetDatasetVersionComboBox;

    private ComboBox gridCombobox;

    private TextField domainPopulationDatasetField;

    private ComboBox domainPopulationDatasetVersionComboBox;

    private ComboBox cancerRiskDatasetVersionComboBox;

    private TextField cancerRiskDatasetField;

    public FastRunConfigurationTab(FastRun run, EmfSession session, MessagePanel messagePanel,
            EmfInternalFrame parentInternalFrame, DesktopManager desktopManager, EmfConsole parentConsole,
            FastRunPresenter presenter) {

        super(run, session, messagePanel, parentInternalFrame, desktopManager, parentConsole, presenter);
        this.setName("Configuration");
    }

    public void display() {

        this.setLayout(new BorderLayout());
        this.add(this.createMiddlePane(), BorderLayout.CENTER);
        super.display();
    }

    protected void addChangables() {

        ManageChangeables changeablesList = this.getChangeablesList();
        changeablesList.addChangeable(this.invTableDatasetField);
        changeablesList.addChangeable(this.invTableDatasetVersionComboBox);
        changeablesList.addChangeable(this.speciesMappingDatasetField);
        changeablesList.addChangeable(this.speciesMappingDatasetVersionComboBox);
        changeablesList.addChangeable(this.transferCoeffDatasetField);
        changeablesList.addChangeable(this.transferCoeffDatasetDatasetVersionComboBox);
        changeablesList.addChangeable(this.gridCombobox);
        changeablesList.addChangeable(this.domainPopulationDatasetField);
        changeablesList.addChangeable(this.domainPopulationDatasetVersionComboBox);
        changeablesList.addChangeable(this.cancerRiskDatasetField);
        changeablesList.addChangeable(this.cancerRiskDatasetVersionComboBox);
        changeablesList.addChangeable(this.pollutantList);
    }

    protected void populateFields() {

        FastRun run = this.getRun();

        EmfDataset dataset = run.getInvTableDataset();
        if (dataset != null) {

            this.invTableDatasetField.setText(dataset.getName());

            this.updateVersions(dataset, this.invTableDatasetVersionComboBox);
            this.invTableDatasetVersionComboBox.setSelectedIndex(getVersionIndex(dataset, run
                    .getInvTableDatasetVersion()));
        }

        dataset = run.getSpeciesMapppingDataset();
        if (dataset != null) {

            this.speciesMappingDatasetField.setText(dataset.getName());

            this.updateVersions(dataset, this.speciesMappingDatasetVersionComboBox);
            this.speciesMappingDatasetVersionComboBox.setSelectedIndex(getVersionIndex(dataset, run
                    .getSpeciesMapppingDatasetVersion()));
        }

        dataset = run.getTransferCoefficientsDataset();
        if (dataset != null) {

            this.transferCoeffDatasetField.setText(dataset.getName());

            this.updateVersions(dataset, this.transferCoeffDatasetDatasetVersionComboBox);
            this.transferCoeffDatasetDatasetVersionComboBox.setSelectedIndex(getVersionIndex(dataset, run
                    .getTransferCoefficientsDatasetVersion()));
        }

        dataset = run.getDomainPopulationDataset();
        if (dataset != null) {

            this.domainPopulationDatasetField.setText(dataset.getName());

            this.updateVersions(dataset, this.domainPopulationDatasetVersionComboBox);
            this.domainPopulationDatasetVersionComboBox.setSelectedIndex(getVersionIndex(dataset, run
                    .getDomainPopulationDatasetVersion()));
        }

        dataset = run.getCancerRiskDataset();
        if (dataset != null) {

            this.cancerRiskDatasetField.setText(dataset.getName());

            this.updateVersions(dataset, this.cancerRiskDatasetVersionComboBox);
            this.cancerRiskDatasetVersionComboBox.setSelectedIndex(getVersionIndex(dataset, run
                    .getCancerRiskDatasetVersion()));
        }

    }

    private JPanel createMiddlePane() {

        JPanel panel = new JPanel(new GridBagLayout());

        try {

            Grid[] grids = this.getSession().fastService().getGrids();

            Insets labelInsets = new Insets(8, 10, 8, 5);
            Insets valueInsets = new Insets(4, 0, 4, 0);
            Insets buttonInsets = new Insets(0, 10, 0, 10);
            Dimension fieldSize = new Dimension(0, 10);
            Dimension comboBoxSize = new Dimension(100, 24);

            GridBagConstraints constraints = new GridBagConstraints();

            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = labelInsets;

            JLabel invTableLabel = new JLabel("Inventory Table Dataset:");
            panel.add(invTableLabel, constraints);

            constraints.gridx = 1;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = valueInsets;

            this.invTableDatasetField = new TextField("Inventory Table Dataset", 45);
            this.invTableDatasetField.setPreferredSize(fieldSize);
            this.invTableDatasetField.setEditable(false);
            this.invTableDatasetField.setBackground(Color.WHITE);
            panel.add(this.invTableDatasetField, constraints);

            constraints.gridx = 2;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = buttonInsets;

            Button invTableButton = new Button("Browse", new AbstractFastAction(this.getMessagePanel(),
                    "Error adding inventory table dataset") {
                @Override
                protected void doActionPerformed(ActionEvent e) throws EmfException {

                    DatasetCommand command = new DatasetCommand() {

                        public void execute() {
                            FastRun run = getRun();
                            run.setInvTableDataset(this.getDataset());
                        }
                    };

                    getDataset(getPresenter().getDatasetType(DatasetType.invTable), invTableDatasetField,
                            invTableDatasetVersionComboBox, command);
                }
            });

            panel.add(invTableButton, constraints);

            constraints.gridx = 0;
            constraints.gridy += 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = labelInsets;

            JLabel invTableVersionLabel = new JLabel("Inventory Table Dataset Version:");
            panel.add(invTableVersionLabel, constraints);

            constraints.gridx = 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = valueInsets;

            this.invTableDatasetVersionComboBox = new ComboBox();
            this.invTableDatasetVersionComboBox.setPreferredSize(comboBoxSize);
            this.invTableDatasetVersionComboBox.setEditable(false);
            this.invTableDatasetVersionComboBox.setEnabled(false);
            panel.add(this.invTableDatasetVersionComboBox, constraints);

            constraints.gridx = 0;
            constraints.gridy += 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = labelInsets;

            JLabel speciesMappingLabel = new JLabel("Species Mapping Dataset:");
            panel.add(speciesMappingLabel, constraints);

            constraints.gridx = 1;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = valueInsets;

            this.speciesMappingDatasetField = new TextField("Species Mapping Dataset", 45);
            this.speciesMappingDatasetField.setPreferredSize(fieldSize);
            this.speciesMappingDatasetField.setEditable(false);
            this.speciesMappingDatasetField.setBackground(Color.WHITE);
            panel.add(this.speciesMappingDatasetField, constraints);

            constraints.gridx = 2;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = buttonInsets;

            Button speciesMappingButton = new Button("Browse", new AbstractFastAction(this.getMessagePanel(),
                    "Error adding species mapping dataset") {
                @Override
                protected void doActionPerformed(ActionEvent e) throws EmfException {

                    DatasetCommand command = new DatasetCommand() {
                        public void execute() {
                            FastRun run = getRun();
                            run.setSpeciesMapppingDataset(this.getDataset());
                        }
                    };

                    getDataset(getPresenter().getDatasetType(DatasetType.FAST_SPECIES_MAPPING),
                            speciesMappingDatasetField, speciesMappingDatasetVersionComboBox, command);

                }
            });

            panel.add(speciesMappingButton, constraints);

            constraints.gridx = 0;
            constraints.gridy += 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = labelInsets;

            JLabel speciesMappingVersionLabel = new JLabel("Species Mapping Dataset Version:");
            panel.add(speciesMappingVersionLabel, constraints);

            constraints.gridx = 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = valueInsets;

            this.speciesMappingDatasetVersionComboBox = new ComboBox();
            this.speciesMappingDatasetVersionComboBox.setPreferredSize(comboBoxSize);
            this.speciesMappingDatasetVersionComboBox.setEditable(false);
            this.speciesMappingDatasetVersionComboBox.setEnabled(false);
            panel.add(this.speciesMappingDatasetVersionComboBox, constraints);

            constraints.gridx = 0;
            constraints.gridy += 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.NORTHWEST;
            constraints.insets = labelInsets;

            JLabel pollutantsLabel = new JLabel("Pollutant(s):");
            panel.add(pollutantsLabel, constraints);

            constraints.gridx = 1;
            constraints.weightx = 0;
            constraints.weighty = 1;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = valueInsets;

            this.pollutantList = new PollutantList();
            this.pollutantList.setVisibleRowCount(5);
            this.pollutantList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

            JScrollPane scrollPane = new JScrollPane(this.pollutantList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            panel.add(scrollPane, constraints);

            constraints.gridx = 0;
            constraints.gridy += 1;
            constraints.weightx = 0;
            constraints.weighty = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = labelInsets;

            JLabel transferCoeffLabel = new JLabel("Transfer Coefficient Dataset:");
            panel.add(transferCoeffLabel, constraints);

            constraints.gridx = 1;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = valueInsets;

            this.transferCoeffDatasetField = new TextField("Transfer Coefficient Dataset", 45);
            this.transferCoeffDatasetField.setPreferredSize(fieldSize);
            this.transferCoeffDatasetField.setEditable(false);
            this.transferCoeffDatasetField.setBackground(Color.WHITE);
            panel.add(this.transferCoeffDatasetField, constraints);

            constraints.gridx = 2;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = buttonInsets;

            Button transferCoeffButton = new Button("Browse", new AbstractFastAction(this.getMessagePanel(),
                    "Error adding transfer coefficient dataset") {
                @Override
                protected void doActionPerformed(ActionEvent e) throws EmfException {

                    DatasetCommand command = new DatasetCommand() {
                        public void execute() {
                            FastRun run = getRun();
                            run.setTransferCoefficientsDataset(this.getDataset());
                        }
                    };

                    getDataset(getPresenter().getDatasetType(DatasetType.FAST_TRANSFER_COEFFICIENTS),
                            transferCoeffDatasetField, transferCoeffDatasetDatasetVersionComboBox, command);
                }
            });

            panel.add(transferCoeffButton, constraints);

            constraints.gridx = 0;
            constraints.gridy += 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = labelInsets;

            JLabel transferCoeffDatasetVersionLabel = new JLabel("Transfer Coefficient Dataset Version:");
            panel.add(transferCoeffDatasetVersionLabel, constraints);

            constraints.gridx = 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = valueInsets;

            this.transferCoeffDatasetDatasetVersionComboBox = new ComboBox();
            this.transferCoeffDatasetDatasetVersionComboBox.setPreferredSize(comboBoxSize);
            this.transferCoeffDatasetDatasetVersionComboBox.setEditable(false);
            this.transferCoeffDatasetDatasetVersionComboBox.setEnabled(false);
            panel.add(this.transferCoeffDatasetDatasetVersionComboBox, constraints);

            constraints.gridx = 0;
            constraints.gridy += 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = labelInsets;

            JLabel domainPopulationLabel = new JLabel("Domain Population Dataset:");
            panel.add(domainPopulationLabel, constraints);

            constraints.gridx = 1;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = valueInsets;

            this.domainPopulationDatasetField = new TextField("Domain Population Dataset", 45);
            this.domainPopulationDatasetField.setPreferredSize(fieldSize);
            this.domainPopulationDatasetField.setEditable(false);
            this.domainPopulationDatasetField.setBackground(Color.WHITE);
            panel.add(this.domainPopulationDatasetField, constraints);

            constraints.gridx = 2;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = buttonInsets;

            Button domainPopulationDatasetButton = new Button("Browse", new AbstractFastAction(this.getMessagePanel(),
                    "Error adding domain population dataset") {
                @Override
                protected void doActionPerformed(ActionEvent e) throws EmfException {

                    DatasetCommand command = new DatasetCommand() {
                        public void execute() {
                            FastRun run = getRun();
                            run.setDomainPopulationDataset(this.getDataset());
                        }
                    };

                    getDataset(getPresenter().getDatasetType(DatasetType.FAST_DOMAIN_POPULATION),
                            domainPopulationDatasetField, domainPopulationDatasetVersionComboBox, command);
                }
            });

            panel.add(domainPopulationDatasetButton, constraints);

            constraints.gridx = 0;
            constraints.gridy += 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = labelInsets;

            JLabel domainPopulationDatasetVersionLabel = new JLabel("Domain Population Dataset Version:");
            panel.add(domainPopulationDatasetVersionLabel, constraints);

            constraints.gridx = 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = valueInsets;

            this.domainPopulationDatasetVersionComboBox = new ComboBox();
            this.domainPopulationDatasetVersionComboBox.setPreferredSize(comboBoxSize);
            this.domainPopulationDatasetVersionComboBox.setEditable(false);
            this.domainPopulationDatasetVersionComboBox.setEnabled(false);
            panel.add(this.domainPopulationDatasetVersionComboBox, constraints);

            constraints.gridx = 0;
            constraints.gridy += 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = labelInsets;

            JLabel cancerRiskLabel = new JLabel("Cancer Risk Dataset:");
            panel.add(cancerRiskLabel, constraints);

            constraints.gridx = 1;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = valueInsets;

            this.cancerRiskDatasetField = new TextField("Cancer Risk Dataset", 45);
            this.cancerRiskDatasetField.setPreferredSize(fieldSize);
            this.cancerRiskDatasetField.setEditable(false);
            this.cancerRiskDatasetField.setBackground(Color.WHITE);
            panel.add(this.cancerRiskDatasetField, constraints);

            constraints.gridx = 2;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = buttonInsets;

            Button cancerRiskDatasetButton = new Button("Browse", new AbstractFastAction(this.getMessagePanel(),
                    "Error adding cancer risk dataset") {
                @Override
                protected void doActionPerformed(ActionEvent e) throws EmfException {

                    DatasetCommand command = new DatasetCommand() {
                        public void execute() {
                            FastRun run = getRun();
                            run.setCancerRiskDataset(this.getDataset());
                        }
                    };

                    getDataset(getPresenter().getDatasetType(DatasetType.FAST_CANCER_RISK), cancerRiskDatasetField,
                            cancerRiskDatasetVersionComboBox, command);
                }
            });

            panel.add(cancerRiskDatasetButton, constraints);

            constraints.gridx = 0;
            constraints.gridy += 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = labelInsets;

            JLabel cancerRiskDatasetVersionLabel = new JLabel("Cancer Risk Dataset Version:");
            panel.add(cancerRiskDatasetVersionLabel, constraints);

            constraints.gridx = 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = valueInsets;

            this.cancerRiskDatasetVersionComboBox = new ComboBox();
            this.cancerRiskDatasetVersionComboBox.setPreferredSize(comboBoxSize);
            this.cancerRiskDatasetVersionComboBox.setEditable(false);
            this.cancerRiskDatasetVersionComboBox.setEnabled(false);
            panel.add(this.cancerRiskDatasetVersionComboBox, constraints);

            constraints.gridx = 0;
            constraints.gridy += 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = labelInsets;

            JLabel gridLabel = new JLabel("Grid:");
            panel.add(gridLabel, constraints);

            constraints.gridx = 1;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = valueInsets;

            this.gridCombobox = new ComboBox(grids);
            this.gridCombobox.setPreferredSize(fieldSize);
            panel.add(this.gridCombobox, constraints);

            constraints.gridx = 0;
            constraints.gridy += 1;
            constraints.gridwidth = 3;
            constraints.weightx = 1;
            constraints.weighty = 1;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.NORTH;

            JLabel emptyLabel = new JLabel();
            emptyLabel.setOpaque(false);

            panel.add(emptyLabel, constraints);

        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        return panel;
    }

    private void getDataset(DatasetType datasetType, TextField datasetField, ComboBox datasetComboBox,
            DatasetCommand command) {

        InputDatasetSelectionView view = new InputDatasetSelectionDialog(this.getParentConsole());

        List<DatasetType> datasetTypes = new ArrayList<DatasetType>(1);
        datasetTypes.add(datasetType);

        InputDatasetSelectionPresenter inputDatasetPresenter = new InputDatasetSelectionPresenter(view, this
                .getSession(), datasetTypes.toArray(new DatasetType[0]));
        try {

            inputDatasetPresenter.display(datasetType, true);

            if (view.shouldCreate()) {

                EmfDataset[] inputDatasets = inputDatasetPresenter.getDatasets();
                if (inputDatasets != null && inputDatasets.length > 0) {

                    EmfDataset dataset = inputDatasets[0];
                    datasetField.setText(dataset.getName());
                    datasetField.setToolTipText(dataset.getName());

                    if (dataset != null) {

                        command.setDataset(dataset);
                        command.execute();

                        updateVersions(dataset, datasetComboBox);
                    }
                }
            }
        } catch (Exception exp) {
            this.showError(exp.getMessage());
        }
    }

    private void updateVersions(EmfDataset dataset, ComboBox comboBox) {

        try {

            Version[] versions = this.getPresenter().getVersions(dataset);
            comboBox.removeAllItems();
            comboBox.setModel(new DefaultComboBoxModel(versions));
            comboBox.revalidate();
            if (versions.length > 0) {

                comboBox.setSelectedItem(getDefaultVersionIndex(versions, dataset));
                comboBox.setEnabled(true);
            } else {
                comboBox.setEnabled(false);
            }
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
    }

    private int getDefaultVersionIndex(Version[] versions, EmfDataset dataset) {

        int retVal = 0;
        int defaultversion = dataset.getDefaultVersion();
        for (int i = 0; i < versions.length; i++) {

            if (defaultversion == versions[i].getVersion()) {

                retVal = i;
                break;
            }
        }

        return retVal;
    }

    private int getVersionIndex(EmfDataset dataset, int versionNumber) {

        Version[] versions = new Version[0];
        try {
            versions = this.getPresenter().getVersions(dataset);
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        int retVal = 0;
        for (int i = 0; i < versions.length; i++) {

            if (versionNumber == versions[i].getVersion()) {

                retVal = i;
                break;
            }
        }

        return retVal;
    }

//    private void widgetLayout(int rows, int cols, int initX, int initY, int xPad, int yPad,
//            SpringLayoutGenerator layoutGenerator, JPanel panel) {
//        // Lay out the panel.
//        layoutGenerator.makeCompactGrid(panel, rows, cols, // rows, cols
//                initX, initY, // initialX, initialY
//                xPad, yPad);// xPad, yPad
//    }

    @Override
    void refreshData() {
        this.populateFields();
    }

    public void save(FastRun run) {

        this.clearMessage();
        try {
            validateFields();
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        run.setInvTableDataset(this.getRun().getInvTableDataset());
        run.setSpeciesMapppingDataset(this.getRun().getSpeciesMapppingDataset());
        run.setTransferCoefficientsDataset(this.getRun().getTransferCoefficientsDataset());
        run.setDomainPopulationDataset(this.getRun().getDomainPopulationDataset());
        run.setCancerRiskDataset(this.getRun().getCancerRiskDataset());

        run.setGrid((Grid) this.gridCombobox.getSelectedItem());

        Object selectedItem = this.invTableDatasetVersionComboBox.getSelectedItem();
        if (selectedItem != null) {

            Version version = (Version) selectedItem;
            run.setInvTableDatasetVersion(version.getVersion());
        }

        selectedItem = this.speciesMappingDatasetVersionComboBox.getSelectedItem();
        if (selectedItem != null) {

            Version version = (Version) selectedItem;
            run.setSpeciesMapppingDatasetVersion(version.getVersion());
        }

        selectedItem = this.transferCoeffDatasetDatasetVersionComboBox.getSelectedItem();
        if (selectedItem != null) {

            Version version = (Version) selectedItem;
            run.setTransferCoefficientsDatasetVersion(version.getVersion());
        }

        selectedItem = this.domainPopulationDatasetVersionComboBox.getSelectedItem();
        if (selectedItem != null) {

            Version version = (Version) selectedItem;
            run.setDomainPopulationDatasetVersion(version.getVersion());
        }

        selectedItem = this.cancerRiskDatasetVersionComboBox.getSelectedItem();
        if (selectedItem != null) {

            Version version = (Version) selectedItem;
            run.setCancerRiskDatasetVersion(version.getVersion());
        }
    }

    private void validateFields() throws EmfException {

        this.clearMessage();

        if (this.invTableDatasetField.getText().trim().length() == 0) {
            throw new EmfException(this.getName() + " tab: An inventory table dataset must be specified");
        }

        if (this.speciesMappingDatasetField.getText().trim().length() == 0) {
            throw new EmfException(this.getName() + " tab: A species mapping dataset must be specified");
        }

        if (this.transferCoeffDatasetField.getText().trim().length() == 0) {
            throw new EmfException(this.getName() + " tab: A transfer coefficient dataset must be specified");
        }

        if (this.domainPopulationDatasetField.getText().trim().length() == 0) {
            throw new EmfException(this.getName() + " tab: A domain population dataset must be specified");
        }

        if (this.cancerRiskDatasetField.getText().trim().length() == 0) {
            throw new EmfException(this.getName() + " tab: A cancer risk dataset must be specified");
        }
    }

    public void refresh(FastRun run) {

        this.setRun(run);

        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            this.populateFields();
            this.refreshLayout();
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    public void viewOnly() {

        // this.nameField.setEditable(false);
        // this.abbreviationField.setEditable(false);
        // this.descriptionField.setEditable(false);
    }

    class PollutantList extends JList implements Changeable {

        private boolean changed;

        private Changeables changeables;

        public void clear() {
            this.changed = false;
        }

        public boolean hasChanges() {
            return this.changed;
        }

        public void observe(Changeables changeables) {
            this.changeables = changeables;
            addListDataListener();
        }

        private void addListDataListener() {
            ListModel model = this.getModel();
            model.addListDataListener(new ListDataListener() {
                public void contentsChanged(ListDataEvent e) {
                    notifyChanges();
                }

                public void intervalAdded(ListDataEvent e) {
                    notifyChanges();
                }

                public void intervalRemoved(ListDataEvent e) {
                    notifyChanges();
                }
            });
        }

        void notifyChanges() {

            this.changed = true;
            if (this.changeables != null) {
                this.changeables.onChanges();
            }
        }
    }
}
