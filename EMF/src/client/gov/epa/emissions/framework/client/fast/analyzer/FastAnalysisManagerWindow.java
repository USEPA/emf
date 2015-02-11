package gov.epa.emissions.framework.client.fast.analyzer;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.buttons.CopyButton;
import gov.epa.emissions.commons.gui.buttons.NewButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.fast.AbstractCommand;
import gov.epa.emissions.framework.client.fast.AbstractFastAction;
import gov.epa.emissions.framework.client.fast.AbstractMPSDTManagerTab;
import gov.epa.emissions.framework.client.fast.ExceptionHandlingCommand;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastAnalysis;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class FastAnalysisManagerWindow extends AbstractMPSDTManagerTab
		implements FastAnalysisManagerView, Runnable {

	private FastAnalysisManagerPresenter presenter;

	private SelectableSortFilterWrapper table;

	private static final String ROOT_SELECT_PROMPT = "Please select one or more Fast analyses to ";

	private static final SortCriteria SORT_CRITERIA = new SortCriteria(
			new String[] { "Start" }, new boolean[] { false },
			new boolean[] { true });

	public FastAnalysisManagerWindow(EmfConsole parentConsole,
			EmfSession session, DesktopManager desktopManager) {
		super(session, parentConsole, desktopManager);
	}

	public String getTitle() {
		return "Fast Analyses";
	}

	public void observe(FastAnalysisManagerPresenterImpl presenter) {
		this.presenter = presenter;
	}

	public void display(FastAnalysis[] analyses) {

		doLayout(analyses, this.getSession());
		SwingUtilities.invokeLater(this);
	}

	public void run() {

		try {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			this.presenter.loadAnalyses();
		} catch (Exception e) {
			this.showError("Cannot retrieve all Fast analyses.");
		} finally {
			setCursor(Cursor.getDefaultCursor());
		}
	}

	public void refresh(FastAnalysis[] analyses) {

		try {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			this.clearMessage();
			this.table.refresh(new FastAnalysisTableData(analyses));
			SwingUtilities.invokeLater(this);
		} finally {
			setCursor(Cursor.getDefaultCursor());
		}
	}

	public void doRefresh() throws EmfException {
		this.presenter.doRefresh();
	}

	private void doLayout(FastAnalysis[] analyses, EmfSession session) {

		this.removeAll();

		this.add(createTopPanel(), BorderLayout.NORTH);
		this.add(createTablePanel(analyses, getParentConsole(), session),
				BorderLayout.CENTER);
		this.add(createControlPanel(), BorderLayout.SOUTH);
	}

	private JPanel createTablePanel(FastAnalysis[] analyses,
			EmfConsole parentConsole, EmfSession session) {

		this.table = new SelectableSortFilterWrapper(parentConsole,
				new FastAnalysisTableData(analyses), SORT_CRITERIA);

		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.add(this.table, BorderLayout.CENTER);

		return tablePanel;
	}

	protected JPanel createButtonPanel() {

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		String message = "You have asked to open a lot of windows. Do you want to proceed?";
		ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning",
				this);

		buttonPanel.add(viewButton(confirmDialog));
		buttonPanel.add(editButton(confirmDialog));

		Button newButton = new NewButton(new AbstractFastAction(this
				.getMessagePanel(), "Error creating Fast analysis") {

			@Override
			protected void doActionPerformed(ActionEvent e) throws EmfException {
				createNewAnalysis();
			}
		});
		buttonPanel.add(newButton);

		Button removeButton = new RemoveButton(new AbstractFastAction(this
				.getMessagePanel(), "Error removing Fast analyses") {

			@Override
			protected void doActionPerformed(ActionEvent e) throws EmfException {
				removeAnalyses();
			}
		});
		buttonPanel.add(removeButton);

		Button copyButton = new CopyButton(new AbstractFastAction(this
				.getMessagePanel(), "Error copying Fast analyses") {

			@Override
			protected void doActionPerformed(ActionEvent e) throws EmfException {
				copyAnalyses();
			}
		});
		buttonPanel.add(copyButton);

		Button analyzeButton = new Button("Analyze", new AbstractFastAction(
				this.getMessagePanel(), "Error running Fast analyses") {

			@Override
			protected void doActionPerformed(ActionEvent e) throws EmfException {
				runAnalyses();
			}
		});
		buttonPanel.add(analyzeButton);

		Button exportButton = new Button("Export", new AbstractFastAction(this
				.getMessagePanel(), "Error exporting Fast analyses") {

			@Override
			protected void doActionPerformed(ActionEvent e) throws EmfException {
				exportAnalyses();
			}
		});
        exportButton.setEnabled(false);

		buttonPanel.add(exportButton);

		return buttonPanel;
	}

	private SelectAwareButton editButton(ConfirmDialog confirmDialog) {

		Action editAction = new AbstractFastAction(this.getMessagePanel(),
				"Error editing Fast analyses") {

			@Override
			protected void doActionPerformed(ActionEvent e) {
				editAnalyses();
			}
		};

		return new SelectAwareButton("Edit", editAction, table, confirmDialog);
	}

	private SelectAwareButton viewButton(ConfirmDialog confirmDialog) {

		Action viewAction = new AbstractFastAction(this.getMessagePanel(),
				"Error viewing Fast analyses") {

			@Override
			protected void doActionPerformed(ActionEvent e) {
				viewAnalyses();
			}
		};

		SelectAwareButton viewButton = new SelectAwareButton("View",
				viewAction, this.table, confirmDialog);
		viewButton.setEnabled(false);
		return viewButton;
	}

	private void viewAnalyses() {

		List<FastAnalysis> analyses = getSelected();
		if (analyses.isEmpty()) {
			this.showMessage(ROOT_SELECT_PROMPT + "view.");
		} else {
			for (FastAnalysis analysis : analyses) {

				try {
					presenter.doView(analysis.getId());
				} catch (EmfException e) {
					showError("Error viewing Fast analyses: " + e.getMessage());
				}
			}
		}
	}

	private void editAnalyses() {

		List<FastAnalysis> analyses = getSelected();
		if (analyses.isEmpty()) {
			this.showMessage(ROOT_SELECT_PROMPT + "edit.");
		} else {
			for (FastAnalysis analysis : analyses) {

				try {
					presenter.doEdit(analysis.getId());
				} catch (EmfException e) {
					showError("Error editing Fast analyses: " + e.getMessage());
				}
			}
		}
	}

	private void removeAnalyses() throws EmfException {

		this.clearMessage();
		final List<FastAnalysis> analyses = getSelected();

		if (analyses.isEmpty()) {
			this.showMessage(ROOT_SELECT_PROMPT + "remove.");
		} else {

			String message = null;
			int size = analyses.size();
			if (size == 1) {
				message = "Are you sure you want to remove the selected Fast analysis?";
			} else {
				message = "Are you sure you want to remove the " + size
						+ " selected Fast analyses?";
			}

			int selection = JOptionPane.showConfirmDialog(getParentConsole(),
					message, "Warning", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);

			if (selection == JOptionPane.YES_OPTION) {

				ExceptionHandlingCommand removeCommand = new AbstractCommand(
						this) {
					public void execute() throws EmfException {
						presenter.doRemove(getIds(analyses));
					}
				};

				this.executeCommand(removeCommand);
			}
		}
	}

	private void runAnalyses() throws EmfException {

		this.clearMessage();
		final List<FastAnalysis> analyses = getSelected();

		if (analyses.isEmpty()) {
			this.showMessage(ROOT_SELECT_PROMPT + "analyze.");
		} else {

			String message = null;
			int size = analyses.size();
			if (size == 1) {
				message = "Are you sure you want to run the selected Fast analysis?";
			} else {
				message = "Are you sure you want to run the " + size
						+ " selected Fast analyses?";
			}

			int selection = JOptionPane.showConfirmDialog(getParentConsole(),
					message, "Warning", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);

			if (selection == JOptionPane.YES_OPTION) {

				ExceptionHandlingCommand executeCommand = new AbstractCommand(
						this) {
					public void execute() throws EmfException {
						presenter.doAnalysis(getIds(analyses));
					}
				};

				this.executeCommand(executeCommand);
			}
		}
	}

	private void exportAnalyses() throws EmfException {

		this.clearMessage();
		final List<FastAnalysis> analyses = getSelected();

		if (analyses.isEmpty()) {
			this.showMessage(ROOT_SELECT_PROMPT + "export.");
		} else {

			String message = null;
			int size = analyses.size();
			if (size == 1) {
				message = "Are you sure you want to export the selected Fast analysis?";
			} else {
				message = "Are you sure you want to export the " + size
						+ " selected Fast analyses?";
			}

			int selection = JOptionPane.showConfirmDialog(getParentConsole(),
					message, "Warning", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);

			if (selection == JOptionPane.YES_OPTION) {

				ExceptionHandlingCommand exportCommand = new AbstractCommand(
						this) {
					public void execute() throws EmfException {
						presenter.doExport(getIds(analyses));
					}
				};

				this.executeCommand(exportCommand);
			}
		}
	}

	private void copyAnalyses() throws EmfException {

		this.clearMessage();
		final List<FastAnalysis> analyses = getSelected();

		if (analyses.isEmpty()) {
			this.showMessage(ROOT_SELECT_PROMPT + "copy.");
		} else {

			String message = null;
			int size = analyses.size();
			if (size == 1) {
				message = "Are you sure you want to copy the selected Fast analysis?";
			} else {
				message = "Are you sure you want to copy the " + size
						+ " selected Fast analyses?";
			}

			int selection = JOptionPane.showConfirmDialog(getParentConsole(),
					message, "Warning", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);

			if (selection == JOptionPane.YES_OPTION) {

				ExceptionHandlingCommand saveCommand = new AbstractCommand(this) {
					public void execute() throws EmfException {

						int[] ids = getIds(analyses);
						for (int id : ids) {
							presenter.doSaveCopiedAnalysis(id, getSession()
									.user());
						}
					}
				};

				this.executeCommand(saveCommand);
			}
		}
	}

	private void createNewAnalysis() throws EmfException {
		presenter.doNew();
	}

	private List<FastAnalysis> getSelected() {
		return (List<FastAnalysis>) table.selected();
	}
}
