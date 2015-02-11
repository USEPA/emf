package gov.epa.emissions.framework.client.fast.run.tabs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfInternalFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.fast.AbstractFastAction;
import gov.epa.emissions.framework.client.fast.ChangeableImpl;
import gov.epa.emissions.framework.client.fast.run.FastRunPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.fast.FastDataset;
import gov.epa.emissions.framework.services.fast.FastRun;
import gov.epa.emissions.framework.services.fast.FastRunInventory;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class FastRunInventoriesTab extends AbstractFastRunTab {

    private SelectableSortFilterWrapper table;

    private FastRunInventoryTableData tableData;

    private ChangeableImpl changeable = new ChangeableImpl();

//    private static final String WARNING_MESSAGE = "You have asked to open several windows. Do you want to proceed?";

    public FastRunInventoriesTab(FastRun run, EmfSession session, MessagePanel messagePanel,
            EmfInternalFrame parentInternalFrame, DesktopManager desktopManager, EmfConsole parentConsole,
            FastRunPresenter presenter) {

        super(run, session, messagePanel, parentInternalFrame, desktopManager, parentConsole, presenter);
        this.setName("Inventories");
    }

    public void display() {

        this.setLayout(new BorderLayout());
        this.add(this.createTablePanel(this.getRun(), this.getParentConsole(), this.getSession()), BorderLayout.CENTER);
        this.add(this.createCrudPanel(), BorderLayout.SOUTH);
        super.display();
    }

    protected void addChangables() {

        ManageChangeables changeablesList = this.getChangeablesList();
        changeablesList.addChangeable(this.changeable);
    }

    protected void populateFields() {
        /*
         * no-op
         */
    }

    public void save(FastRun run) {

        this.clearMessage();

        try {
            validateFields();
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        run.setInventories(this.getRun().getInventories());
    }

    private void validateFields() throws EmfException {

        this.clearMessage();

        FastRunInventory[] inventories = this.getRun().getInventories();
        if (inventories == null || inventories.length == 0) {
            throw new EmfException(this.getName() + " tab: At least one Fast run inventory must be specified");
        }
    }

    @Override
    void refreshData() {

        // EmfSession session = this.getSession();
        // FastDataset[] fastDatasets = session.fastService().getFastDatasets();
        // FastNonPointDataset[] fastNonPointDatasets = session.fastService().getFastNonPointDatasets();
        // FastDatasetMap.getInstance().initMap(fastDatasets, fastNonPointDatasets);

        this.tableData = new FastRunInventoryTableData(this.getRun().getInventories());
        this.table.refresh(this.tableData);
    }

    private JPanel createCrudPanel() {

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.insets = new Insets(5, 5, 5, 0);

        crudPanel.add(this.createAddButton(), constraints);

        constraints.gridx += 1;

        // crudPanel.add(this.createEditButton(), constraints);
        //
        // constraints.gridx = 2;

        crudPanel.add(this.createRemoveButton(), constraints);

        JLabel emptyLabel = new JLabel();
        constraints.gridx += 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        crudPanel.add(emptyLabel, constraints);

        return crudPanel;
    }

    protected Button createAddButton() {

        Button addButton = new AddButton(
                new AbstractFastAction(this.getMessagePanel(), "Error adding Fast inventories") {

                    @Override
                    protected void doActionPerformed(ActionEvent e) {
                        addInventories();
                    }
                });

        return addButton;
    }

    // protected Button createEditButton() {
    //
    // Action editAction = new AbstractFastAction(this.getMessagePanel(), "Error editing Fast inventories") {
    //
    // @Override
    // protected void doActionPerformed(ActionEvent e) throws EmfException {
    // editSelectedInventories();
    // }
    // };
    //
    // return new SelectAwareButton("Edit", editAction, table, new ConfirmDialog(WARNING_MESSAGE, "Warning", this));
    // }

    protected Button createRemoveButton() {

        Button removeButton = new RemoveButton(new AbstractFastAction(this.getMessagePanel(),
                "Error removing Fast inventories") {

            @Override
            protected void doActionPerformed(ActionEvent e) {
                removeSelectedInventories();
            }
        });

        return removeButton;
    }

    private void addInventories() {

        FastDatasetSelectionView datasetSelectionDialog = new FastDatasetSelectionDialog(this.getParentConsole());
        FastDatasetSelectionPresenter datasetSelectionPresenter = new FastDatasetSelectionPresenter(
                datasetSelectionDialog, this.getSession());
        try {

            datasetSelectionPresenter.display(false);

            if (datasetSelectionDialog.shouldCreate()) {

                FastRun run = this.getRun();
                FastRunInventory[] currentInventories = run.getInventories();
                List<FastRunInventory> inputInventories = new ArrayList<FastRunInventory>();
                for (FastRunInventory fastRunInventory : currentInventories) {
                    inputInventories.add(fastRunInventory);
                }

                List<FastDataset> inputDatasets = datasetSelectionPresenter.getDatasets();
                for (FastDataset fastDataset : inputDatasets) {

                    EmfDataset dataset = fastDataset.getDataset();
                    inputInventories.add(new FastRunInventory(dataset, dataset.getDefaultVersion()));
                }

                run.setInventories(inputInventories.toArray(new FastRunInventory[0]));

                this.changeable.notifyChanges();
                this.refresh(run);
            }
        } catch (Exception exp) {
            this.showError(exp.getMessage());
        }
    }

    private List<FastRunInventory> getSelected() {
        return (List<FastRunInventory>) this.table.selected();
    }

    private void removeSelectedInventories() {

        this.clearMessage();
        List<FastRunInventory> selected = this.getSelected();

        if (selected.size() == 0) {
            this.showError("Please select one or more Fast run inventories to remove");
        } else {

            String title = "Warning";
            String message = "Are you sure you want to remove the selected Fast run inventories?";
            int selection = JOptionPane.showConfirmDialog(this.getParentConsole(), message, title,
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (selection == JOptionPane.YES_OPTION) {

                FastRun run = this.getRun();
                run.removeInventories(selected);
                this.getPresenter().fireTracking();
                this.changeable.notifyChanges();
                this.refresh(run);
            }
        }
    }

    private JPanel createTablePanel(FastRun run, EmfConsole parentConsole, EmfSession session) {

        JPanel tablePanel = new JPanel(new BorderLayout());

        this.tableData = new FastRunInventoryTableData(run.getInventories());
        this.table = new SelectableSortFilterWrapper(parentConsole, this.tableData, sortCriteria());
        tablePanel.add(this.table, BorderLayout.CENTER);

        return tablePanel;
    }

    private SortCriteria sortCriteria() {
        return new SortCriteria(new String[] { "Name" }, new boolean[] { false }, new boolean[] { true });
    }

    public void viewOnly() {
        //
    }
}
