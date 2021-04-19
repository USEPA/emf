package gov.epa.emissions.framework.client.fast.datasets;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.buttons.CopyButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionView;
import gov.epa.emissions.framework.client.fast.AbstractCommand;
import gov.epa.emissions.framework.client.fast.AbstractFastAction;
import gov.epa.emissions.framework.client.fast.AbstractMPSDTManagerTab;
import gov.epa.emissions.framework.client.fast.ExceptionHandlingCommand;
import gov.epa.emissions.framework.client.fast.datasets.create.FastNonPointDatasetCreatorPresenterImpl;
import gov.epa.emissions.framework.client.fast.datasets.create.FastNonPointDatasetCreatorWindow;
import gov.epa.emissions.framework.client.fast.datasets.view.FastNonPointDatasetViewPresenterImpl;
import gov.epa.emissions.framework.client.fast.datasets.view.FastNonPointDatasetViewWindow;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.fast.FastDataset;
import gov.epa.emissions.framework.services.fast.FastDatasetWrapper;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class FastDatasetManagerWindow extends AbstractMPSDTManagerTab implements
		FastDatasetManagerView, Runnable {

	private FastDatasetManagerPresenter presenter;

	private SelectableSortFilterWrapper table;

	private static final String ROOT_SELECT_PROMPT = "Please select one or more Fast datasets to ";

	private static final SortCriteria SORT_CRITERIA = new SortCriteria(
			new String[] { "Start" }, new boolean[] { false },
			new boolean[] { true });

	private FastDatasetTableData tableData;

	public FastDatasetManagerWindow(EmfConsole parentConsole,
			EmfSession session, DesktopManager desktopManager) {
		super(session, parentConsole, desktopManager);
	}

	public String getTitle() {
		return "Datasets";
	}

	public void observe(FastDatasetManagerPresenterImpl presenter) {
		this.presenter = presenter;
	}

	public void display(List<FastDatasetWrapper> datasetWrappers) {

		doLayout(datasetWrappers, this.getSession());
		SwingUtilities.invokeLater(this);
	}

	public void run() {

		try {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			this.clearMessage();

			this.presenter.loadDatasets();
		} catch (Exception e) {
			this.showError("Cannot retrieve all Fast datasets.");
		} finally {
			setCursor(Cursor.getDefaultCursor());
		}
	}

	public void refresh(List<FastDatasetWrapper> datasetWrappers) {

		try {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			this.clearMessage();

			this.table.refresh(new FastDatasetTableData(datasetWrappers));
			SwingUtilities.invokeLater(this);
		} finally {
			setCursor(Cursor.getDefaultCursor());
		}
	}

	public void doRefresh() throws EmfException {
		this.presenter.doRefresh();
	}

	private void doLayout(List<FastDatasetWrapper> datasetWrappers,
			EmfSession session) {

		this.removeAll();

		this.add(createTopPanel(), BorderLayout.NORTH);
		this.add(createTablePanel(datasetWrappers, getParentConsole()),
				BorderLayout.CENTER);
		this.add(createControlPanel(), BorderLayout.SOUTH);
	}

	private JPanel createTablePanel(List<FastDatasetWrapper> datasetWrappers,
			EmfConsole parentConsole) {

		this.tableData = new FastDatasetTableData(datasetWrappers);
		this.table = new SelectableSortFilterWrapper(parentConsole, tableData,
				SORT_CRITERIA);

		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.add(this.table, BorderLayout.CENTER);

		return tablePanel;
	}

	protected JPanel createButtonPanel() {

		JPanel crudPanel = new JPanel();
		crudPanel.setLayout(new FlowLayout());

		String message = "You have asked to open a lot of windows. Do you want to proceed?";
		ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning",
				this);

		crudPanel.add(viewButton(confirmDialog));
		crudPanel.add(editButton(confirmDialog));

		Button newPointButton = new Button("Add Point", new AbstractFastAction(
				this.getMessagePanel(), "Error adding Fast point dataset") {

			@Override
			protected void doActionPerformed(ActionEvent e) throws EmfException {
				createNewPointDataset();
			}
		});
		crudPanel.add(newPointButton);

		Button newNonPointButton = new Button("Add Non-Point",
				new AbstractFastAction(this.getMessagePanel(),
						"Error adding Fast non-point dataset") {

					@Override
					protected void doActionPerformed(ActionEvent e)
							throws EmfException {
						createNewNonPointDataset();
					}
				});
		crudPanel.add(newNonPointButton);

		Button removeButton = new RemoveButton(new AbstractFastAction(this
				.getMessagePanel(), "Error removing Fast datasets") {

			@Override
			protected void doActionPerformed(ActionEvent e) throws EmfException {
				removeSelectedDatasets();
			}
		});
		crudPanel.add(removeButton);

		Button copyButton = new CopyButton(new AbstractFastAction(this
				.getMessagePanel(), "Error copying Fast datasets") {

			@Override
			protected void doActionPerformed(ActionEvent e) throws EmfException {
				copySelectedDatasets();
			}
		});
		copyButton.setEnabled(false);
		crudPanel.add(copyButton);

		Button controlButton = new Button("Control", new AbstractFastAction(
				this.getMessagePanel(), "Error copying Fast datasets") {

			@Override
			protected void doActionPerformed(ActionEvent e) throws EmfException {
				copySelectedDatasets();
			}

			public void actionPerformed(ActionEvent e) {
				clearMessage();
                controlInventories();
			}
		});
		controlButton.setEnabled(false);
		crudPanel.add(controlButton);

		return crudPanel;
	}

	SelectAwareButton editButton(ConfirmDialog confirmDialog) {

		Action editAction = new AbstractFastAction(this.getMessagePanel(),
				"Error editing Fast datasets") {

			@Override
			protected void doActionPerformed(ActionEvent e){
				editInventories();
			}
		};

		SelectAwareButton selectAwareButton = new SelectAwareButton("Edit",
				editAction, table, confirmDialog);
		selectAwareButton.setEnabled(false);
		return selectAwareButton;
	}

	SelectAwareButton viewButton(ConfirmDialog confirmDialog) {

		Action viewAction = new AbstractFastAction(this.getMessagePanel(),
				"Error viewing Fast datasets") {

			@Override
			protected void doActionPerformed(ActionEvent e){
				viewDatasets();
			}
		};

		SelectAwareButton button = new SelectAwareButton("View", viewAction,
				this.table, confirmDialog);
		button.setEnabled(false);

		return button;
	}

	private void viewDatasets() {

		List<FastDatasetWrapper> wrappers = getSelected();
		if (wrappers.isEmpty()) {
			this.showMessage(ROOT_SELECT_PROMPT + "view.");
		} else {
			for (FastDatasetWrapper datasetWrapper : wrappers) {

				try {

					if (datasetWrapper.isNonPoint()) {
						FastDatasetView view = new FastNonPointDatasetViewWindow(
								this.getDesktopManager(), this.getSession(),
								this.getParentConsole());
						FastDatasetPresenter presenter = new FastNonPointDatasetViewPresenterImpl(
								this.getSession(), view, datasetWrapper);
						presenter.doDisplay();
					} else {
						presenter.doView(datasetWrapper.getId());
					}
				} catch (EmfException e) {
					showError("Error viewing Fast dataset: " + e.getMessage());
				}
			}
		}
	}

	private void createNewPointDataset() throws EmfException {

		InputDatasetSelectionView view = new InputDatasetSelectionDialog(this
				.getParentConsole());
		DatasetType datasetType = this.presenter
				.getDatasetType(DatasetType.orlPointInventory);
		InputDatasetSelectionPresenter inputDatasetPresenter = new InputDatasetSelectionPresenter(
				view, this.getSession(), new DatasetType[] { datasetType, });
		try {

			final List<FastDatasetWrapper> datasetWrappers = new ArrayList<FastDatasetWrapper>();
			inputDatasetPresenter.display(datasetType, false);

			if (view.shouldCreate()) {

				EmfDataset[] inputDatasets = inputDatasetPresenter
						.getDatasets();
				for (int i = 0; i < inputDatasets.length; i++) {

					EmfDataset emfDataset = inputDatasets[i];
					if (emfDataset.getDatasetType().getName().equals(
							DatasetType.orlPointInventory)) {

						FastDataset fastDataset = new FastDataset();
						fastDataset.setDataset(emfDataset);
						fastDataset.setAddedDate(new Date());

						FastDatasetWrapper fastDatasetWrapper = new FastDatasetWrapper(
								fastDataset);
						datasetWrappers.add(fastDatasetWrapper);
					}
				}

				ExceptionHandlingCommand saveCommand = new AbstractCommand(this) {
					public void execute() throws EmfException {

						for (FastDatasetWrapper fastDatasetWrapper : datasetWrappers) {
							presenter.doSaveDataset(fastDatasetWrapper);
						}
					}
				};

				this.executeCommand(saveCommand);
				this.doRefresh();
			}
		} catch (Exception exp) {
			this.showError(exp.getMessage());
		}
	}

	private void createNewNonPointDataset() throws EmfException {

		FastDatasetView view = new FastNonPointDatasetCreatorWindow(this
				.getDesktopManager(), this.getSession(), this
				.getParentConsole());
		FastDatasetPresenter presenter = new FastNonPointDatasetCreatorPresenterImpl(
				this.getSession(), view);
		presenter.doDisplay();
	}

	private void editInventories() {

		List<FastDatasetWrapper> datasets = getSelected();
		if (datasets.isEmpty()) {
			this.showMessage(ROOT_SELECT_PROMPT + "edit.");
		} else {
			for (FastDatasetWrapper dataset : datasets) {

				try {
					presenter.doEdit(dataset.getId());
				} catch (EmfException e) {
					showError("Error editting Fast dataset: " + e.getMessage());
				}
			}
		}
	}

	protected void removeSelectedDatasets() throws EmfException {

		final List<FastDatasetWrapper> datasetWrappers = getSelected();

		if (datasetWrappers.isEmpty()) {
			this.showMessage(ROOT_SELECT_PROMPT + "remove.");
		} else {

			String message = null;
			int size = datasetWrappers.size();
			if (size == 1) {
				message = "Are you sure you want to remove the selected dataset?";
			} else {
				message = "Are you sure you want to remove the " + size
						+ " selected datasets?";
			}

			int selection = JOptionPane.showConfirmDialog(getParentConsole(),
					message, "Warning", JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);

			if (selection == JOptionPane.YES_OPTION) {

				ExceptionHandlingCommand removeCommand = new AbstractCommand(
						this) {
					public void execute() throws EmfException {

						for (FastDatasetWrapper fastDatasetWrapper : datasetWrappers) {
							presenter.doRemove(fastDatasetWrapper);
						}
					}
				};

				this.executeCommand(removeCommand);
				this.doRefresh();
			}
		}
	}

	void copySelectedDatasets() throws EmfException {

		final List<FastDatasetWrapper> datasetWrappers = getSelected();

		if (datasetWrappers.isEmpty()) {
			this.showMessage(ROOT_SELECT_PROMPT + "copy.");
		} else {

			String message = null;
			int size = datasetWrappers.size();
			if (size == 1) {
				message = "Are you sure you want to copy the selected Fast dataset?";
			} else {
				message = "Are you sure you want to copy the " + size
						+ " selected Fast datasets?";
			}

			int selection = JOptionPane.showConfirmDialog(getParentConsole(),
					message, "Warning", JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);

			if (selection == JOptionPane.YES_OPTION) {

				ExceptionHandlingCommand saveCommand = new AbstractCommand(this) {
					public void execute() throws EmfException {

						for (FastDatasetWrapper fastDatasetWrapper : datasetWrappers) {
							presenter.doSaveDataset(fastDatasetWrapper);
						}
					}
				};

				this.executeCommand(saveCommand);
				this.doRefresh();
			}
		}
	}

	private void controlInventories() {

		List<FastDatasetWrapper> datasets = getSelected();
		if (datasets.isEmpty()) {
			this.showMessage(ROOT_SELECT_PROMPT + "run a control on.");
		} else {
			for (FastDatasetWrapper dataset : datasets) {

				try {
					presenter.doControl(dataset.getId());
				} catch (EmfException e) {
					showError("Error running contols on Fast datasets: "
							+ e.getMessage());
				}
			}
		}
	}

	private List<FastDatasetWrapper> getSelected() {
		return (List<FastDatasetWrapper>) table.selected();
	}
}
