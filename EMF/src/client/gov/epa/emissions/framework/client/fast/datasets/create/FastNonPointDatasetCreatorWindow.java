package gov.epa.emissions.framework.client.fast.datasets.create;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionView;
import gov.epa.emissions.framework.client.fast.AbstractFastAction;
import gov.epa.emissions.framework.client.fast.datasets.AbstractFastDatasetWindow;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.fast.FastAnalysis;
import gov.epa.emissions.framework.services.fast.FastDatasetWrapper;
import gov.epa.emissions.framework.services.fast.FastNonPointDataset;
import gov.epa.emissions.framework.services.fast.Grid;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class FastNonPointDatasetCreatorWindow extends AbstractFastDatasetWindow {

    private TextField nameField;

    private TextField smokeDatasetField;

    private ComboBox smokeVersionComboBox;

    private TextField nonPointDatasetField;

    private ComboBox nonPointVersionComboBox;

    private TextField invTableDatasetField;

    private ComboBox invTableVersionComboBox;

    private ComboBox gridCombobox;

    private FastDatasetWrapper wrapper;

    public FastNonPointDatasetCreatorWindow(DesktopManager desktopManager, EmfSession session, EmfConsole parentConsole) {

        super("New Fast Non-Point Dataset", desktopManager, session, parentConsole);

        this.dimensions(750, 380);
        this.setResizable(false);
    }

    protected void doLayout(FastDatasetWrapper wrapper) {

        this.wrapper = wrapper;

        Container contentPane = getContentPane();
        contentPane.removeAll();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(this.getMessagePanel(), BorderLayout.PAGE_START);
        contentPane.add(createMiddlePane());
        contentPane.add(createButtonsPanel(), BorderLayout.PAGE_END);
    }

    public void refresh(FastDatasetWrapper wrapper) {
        this.wrapper = wrapper;
    }

    // private Grid grid;
    // private EmfDataset quasiPointDataset;

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

            JLabel nameLabel = new JLabel("Dataset Name:");
            panel.add(nameLabel, constraints);

            constraints.gridx = 1;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = valueInsets;

            this.nameField = new TextField("Fast Dataset Name", 45);
            this.nameField.setPreferredSize(fieldSize);
            panel.add(this.nameField, constraints);

            this.addChangeable(this.nameField);

            constraints.gridx = 0;
            constraints.gridy += 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = labelInsets;

            JLabel smokeLabel = new JLabel("SMOKE Report Dataset:");
            panel.add(smokeLabel, constraints);

            constraints.gridx = 1;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = valueInsets;

            this.smokeDatasetField = new TextField("SMOKE Report Dataset Name", 45);
            this.smokeDatasetField.setPreferredSize(fieldSize);
            this.smokeDatasetField.setEditable(false);
            this.smokeDatasetField.setBackground(Color.WHITE);
            panel.add(this.smokeDatasetField, constraints);

            this.addChangeable(this.smokeDatasetField);

            constraints.gridx = 2;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = buttonInsets;

            Button smokeButton = new Button("Browse", new AbstractFastAction(this.getMessagePanel(),
                    "Error adding SMOKE dataset") {
                @Override
                protected void doActionPerformed(ActionEvent e) throws EmfException {
                    getSmokeDataset();
                }
            });

            panel.add(smokeButton, constraints);

            constraints.gridx = 0;
            constraints.gridy += 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = labelInsets;

            JLabel smokeVersionLabel = new JLabel("SMOKE Dataset Version:");
            panel.add(smokeVersionLabel, constraints);

            constraints.gridx = 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = valueInsets;

            this.smokeVersionComboBox = new ComboBox();
            this.smokeVersionComboBox.setPreferredSize(comboBoxSize);
            this.smokeVersionComboBox.setEditable(false);
            this.smokeVersionComboBox.setEnabled(false);
            panel.add(this.smokeVersionComboBox, constraints);

            this.addChangeable(this.smokeVersionComboBox);

            constraints.gridx = 0;
            constraints.gridy += 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = labelInsets;

            JLabel baseLabel = new JLabel("Non-Point Dataset:");
            panel.add(baseLabel, constraints);

            constraints.gridx = 1;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = valueInsets;

            this.nonPointDatasetField = new TextField("Base Dataset", 45);
            this.nonPointDatasetField.setPreferredSize(fieldSize);
            this.nonPointDatasetField.setEditable(false);
            this.nonPointDatasetField.setBackground(Color.WHITE);
            panel.add(this.nonPointDatasetField, constraints);

            this.addChangeable(this.nonPointDatasetField);

            constraints.gridx = 2;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = buttonInsets;

            Button nonPointButton = new Button("Browse", new AbstractFastAction(this.getMessagePanel(),
                    "Error adding non-point dataset") {
                @Override
                protected void doActionPerformed(ActionEvent e) throws EmfException {
                    getNonPointDataset();
                }
            });

            panel.add(nonPointButton, constraints);

            constraints.gridx = 0;
            constraints.gridy += 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = labelInsets;

            JLabel nonPointVersionLabel = new JLabel("Non-Point Dataset Version:");
            panel.add(nonPointVersionLabel, constraints);

            constraints.gridx = 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = valueInsets;

            this.nonPointVersionComboBox = new ComboBox();
            this.nonPointVersionComboBox.setPreferredSize(comboBoxSize);
            this.nonPointVersionComboBox.setEditable(false);
            this.nonPointVersionComboBox.setEnabled(false);
            panel.add(this.nonPointVersionComboBox, constraints);

            this.addChangeable(this.nonPointVersionComboBox);

            constraints.gridx = 0;
            constraints.gridy += 1;
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

            this.addChangeable(this.invTableDatasetField);

            constraints.gridx = 2;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = buttonInsets;

            Button invTableButton = new Button("Browse", new AbstractFastAction(this.getMessagePanel(),
                    "Error adding inventory table") {
                @Override
                protected void doActionPerformed(ActionEvent e) throws EmfException {
                    getInventoryTable();
                }
            });

            panel.add(invTableButton, constraints);

            constraints.gridx = 0;
            constraints.gridy += 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = labelInsets;

            JLabel invTableVersionLabel = new JLabel("Inv Table Dataset Version:");
            panel.add(invTableVersionLabel, constraints);

            constraints.gridx = 1;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.insets = valueInsets;

            this.invTableVersionComboBox = new ComboBox();
            this.invTableVersionComboBox.setPreferredSize(comboBoxSize);
            this.invTableVersionComboBox.setEditable(false);
            this.invTableVersionComboBox.setEnabled(false);
            panel.add(this.invTableVersionComboBox, constraints);

            this.addChangeable(this.invTableVersionComboBox);

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

            this.addChangeable(this.gridCombobox);

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

    @Override
    protected boolean showSave() {
        return true;
    }

    private void getSmokeDataset() throws EmfException {

        // TODO Fix this!
        final DatasetType datasetType = this.getPresenter().getDatasetType(DatasetType.SMOKE_REPORT);
        InputDatasetSelectionView view = new InputDatasetSelectionDialog(this.getParentConsole()) {
            public void setTitle(String title) {

                title = "Select SMOKE Report Dataset";
                super.setTitle(title);
            }
        };

        InputDatasetSelectionPresenter inputDatasetPresenter = new InputDatasetSelectionPresenter(view, this
                .getSession(), new DatasetType[] { datasetType, });
        try {

            inputDatasetPresenter.display(datasetType, true);

            if (view.shouldCreate()) {

                EmfDataset[] inputDatasets = inputDatasetPresenter.getDatasets();
                if (inputDatasets != null && inputDatasets.length > 0) {

                    EmfDataset smokeDataset = inputDatasets[0];

                    FastNonPointDataset nonPointDataset = this.wrapper.getNonPointDataset();
                    nonPointDataset.setGriddedSMKDataset(smokeDataset);
                    String name = nonPointDataset.getGriddedSMKDataset().getName();
                    this.smokeDatasetField.setText(name);
                    this.smokeDatasetField.setToolTipText(name);

                    updateVersions(smokeDataset, this.smokeVersionComboBox);
                }
            }
        } catch (Exception exp) {
            this.showError(exp.getMessage());
        }
    }

    private void getNonPointDataset() throws EmfException {

        InputDatasetSelectionView view = new InputDatasetSelectionDialog(this.getParentConsole()) {
            public void setTitle(String title) {

                title = "Select Non-Point Dataset";
                super.setTitle(title);
            }
        };

        List<DatasetType> datasetTypes = new ArrayList<DatasetType>(3);
        datasetTypes.add(this.getPresenter().getDatasetType(DatasetType.orlNonpointInventory));
        datasetTypes.add(this.getPresenter().getDatasetType(DatasetType.orlNonroadInventory));
        datasetTypes.add(this.getPresenter().getDatasetType(DatasetType.orlOnroadInventory));

        InputDatasetSelectionPresenter inputDatasetPresenter = new InputDatasetSelectionPresenter(view, this
                .getSession(), datasetTypes.toArray(new DatasetType[0]));
        try {

            inputDatasetPresenter.display(null, true);

            if (view.shouldCreate()) {

                EmfDataset[] inputDatasets = inputDatasetPresenter.getDatasets();
                if (inputDatasets != null && inputDatasets.length > 0) {

                    EmfDataset baseNonPointDataset = inputDatasets[0];
                    FastNonPointDataset nonPointDataset = this.wrapper.getNonPointDataset();
                    nonPointDataset.setBaseNonPointDataset(baseNonPointDataset);
                    String name = nonPointDataset.getBaseNonPointDataset().getName();
                    this.nonPointDatasetField.setText(name);
                    this.nonPointDatasetField.setToolTipText(name);

                    updateVersions(baseNonPointDataset, this.nonPointVersionComboBox);
                }
            }
        } catch (Exception exp) {
            this.showError(exp.getMessage());
        }
    }

    private void getInventoryTable() throws EmfException {

        InputDatasetSelectionView view = new InputDatasetSelectionDialog(this.getParentConsole()) {
            public void setTitle(String title) {

                title = "Select Inventory Table Dataset";
                super.setTitle(title);
            }
        };

        List<DatasetType> datasetTypes = new ArrayList<DatasetType>(1);
        DatasetType datasetType = this.getPresenter().getDatasetType(DatasetType.invTable);
        datasetTypes.add(datasetType);

        InputDatasetSelectionPresenter inputDatasetPresenter = new InputDatasetSelectionPresenter(view, this
                .getSession(), datasetTypes.toArray(new DatasetType[0]));
        try {

            inputDatasetPresenter.display(datasetType, true);

            if (view.shouldCreate()) {

                EmfDataset[] inputDatasets = inputDatasetPresenter.getDatasets();
                if (inputDatasets != null && inputDatasets.length > 0) {

                    EmfDataset invTableDataset = inputDatasets[0];
                    FastNonPointDataset nonPointDataset = this.wrapper.getNonPointDataset();
                    nonPointDataset.setInvTableDataset(invTableDataset);
                    String name = nonPointDataset.getInvTableDataset().getName();
                    this.invTableDatasetField.setText(name);
                    this.invTableDatasetField.setToolTipText(name);

                    updateVersions(invTableDataset, this.invTableVersionComboBox);
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

    public void save(FastDatasetWrapper localWrapper) throws EmfException {

        this.validateFields();

        FastNonPointDataset memberNonPointDataset = this.wrapper.getNonPointDataset();
        FastNonPointDataset localNonPointDataset = localWrapper.getNonPointDataset();
        localNonPointDataset.setName(this.nameField.getText().trim());
        localNonPointDataset.setBaseNonPointDataset(memberNonPointDataset.getBaseNonPointDataset());
        localNonPointDataset.setInvTableDataset(memberNonPointDataset.getInvTableDataset());
        localNonPointDataset.setGriddedSMKDataset(memberNonPointDataset.getGriddedSMKDataset());
        localNonPointDataset.setGrid((Grid) this.gridCombobox.getSelectedItem());

        Object selectedItem = this.smokeVersionComboBox.getSelectedItem();
        if (selectedItem != null) {

            Version version = (Version) selectedItem;
            localNonPointDataset.setGriddedSMKDatasetVersion(version.getVersion());
        }

        selectedItem = this.nonPointVersionComboBox.getSelectedItem();
        if (selectedItem != null) {

            Version version = (Version) selectedItem;
            localNonPointDataset.setBaseNonPointDatasetVersion(version.getVersion());
        }

        selectedItem = this.invTableVersionComboBox.getSelectedItem();
        if (selectedItem != null) {

            Version version = (Version) selectedItem;
            localNonPointDataset.setInvTableDatasetVersion(version.getVersion());
        }
    }

    private void validateFields() throws EmfException {

        if (this.nameField.getText().trim().isEmpty()) {
            throw new EmfException("Please enter name.");
        }

        if (this.smokeDatasetField.getText().trim().isEmpty()) {
            throw new EmfException("Please select gridded SMOKE dataset.");
        }

        if (this.smokeVersionComboBox.getSelectedItem() == null) {
            throw new EmfException("Please select version for gridded SMOKE dataset.");
        }

        if (this.nonPointDatasetField.getText().trim().isEmpty()) {
            throw new EmfException("Please select base non-point dataset.");
        }

        if (this.nonPointVersionComboBox.getSelectedItem() == null) {
            throw new EmfException("Please select version for base non-point dataset.");
        }

        if (this.invTableDatasetField.getText().trim().isEmpty()) {
            throw new EmfException("Please select inventory table dataset.");
        }

        if (this.invTableVersionComboBox.getSelectedItem() == null) {
            throw new EmfException("Please select version for inventory table dataset.");
        }

        if (this.gridCombobox.getSelectedItem() == null) {
            throw new EmfException("Please select Grid.");
        }
    }

    public void refresh(FastAnalysis analysis) {
        /*
         * no-op
         */
    }
}