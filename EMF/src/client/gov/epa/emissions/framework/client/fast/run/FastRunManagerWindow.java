package gov.epa.emissions.framework.client.fast.run;

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
import gov.epa.emissions.framework.services.fast.FastRun;
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
public class FastRunManagerWindow extends AbstractMPSDTManagerTab implements FastRunManagerView, Runnable {

    private FastRunManagerPresenter presenter;

    private SelectableSortFilterWrapper table;

    private static final String ROOT_SELECT_PROMPT = "Please select one or more Fast runs to ";

    private static final SortCriteria SORT_CRITERIA = new SortCriteria(new String[] { "Start" },
            new boolean[] { false }, new boolean[] { true });

    public FastRunManagerWindow(EmfConsole parentConsole, EmfSession session, DesktopManager desktopManager) {
        super(session, parentConsole, desktopManager);
    }

    public String getTitle() {
        return "Fast Runs";
    }

    public void display(FastRun[] runs) {

        doLayout(runs, this.getSession());
        SwingUtilities.invokeLater(this);
    }

    public void save(FastRun[] objects) {
        //
    }

    public void observe(FastRunManagerPresenter presenter) {
        this.presenter = presenter;
    }

    public void run() {

        try {
            this.presenter.loadRuns();
        } catch (Exception e) {
            this.showError("Cannot retrieve all Fast runs.");
        }
    }

    public void refresh(FastRun[] runs) {

        try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            this.clearMessage();
            this.table.refresh(new FastRunTableData(runs));
            // this.refreshLayout();

            SwingUtilities.invokeLater(this);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    public void doRefresh() throws EmfException {
        this.presenter.doRefresh();
    }

    private void doLayout(FastRun[] runs, EmfSession session) {

        this.removeAll();
        this.setLayout(new BorderLayout());

        this.add(createTopPanel(), BorderLayout.NORTH);
        this.add(tablePanel(runs, getParentConsole(), session), BorderLayout.CENTER);
        this.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel tablePanel(FastRun[] runs, EmfConsole parentConsole, EmfSession session) {

        this.table = new SelectableSortFilterWrapper(parentConsole, new FastRunTableData(runs), SORT_CRITERIA);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(this.table, BorderLayout.CENTER);

        return tablePanel;
    }

    protected JPanel createButtonPanel() {

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        String message = "You have asked to open a lot of windows. Do you want to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        buttonPanel.add(viewButton(confirmDialog));
        buttonPanel.add(editButton(confirmDialog));

        Button newButton = new NewButton(new AbstractFastAction(this.getMessagePanel(), "Error creating Fast run") {

            @Override
            protected void doActionPerformed(ActionEvent e) throws EmfException {
                createNewRun();
            }
        });
        buttonPanel.add(newButton);

        Button removeButton = new RemoveButton(new AbstractFastAction(this.getMessagePanel(),
                "Error removing Fast run(s)") {

            @Override
            protected void doActionPerformed(ActionEvent e) throws EmfException {
                removeSelectedRuns();
            }
        });
        buttonPanel.add(removeButton);

        Button copyButton = new CopyButton(new AbstractFastAction(this.getMessagePanel(), "Error copying Fast run(s)") {

            @Override
            protected void doActionPerformed(ActionEvent e) throws EmfException {
                copySelectedRuns();
            }
        });
        buttonPanel.add(copyButton);

        Button executeButton = new Button("Execute", new AbstractFastAction(this.getMessagePanel(),
                "Error executing Fast run(s)") {

            @Override
            protected void doActionPerformed(ActionEvent e) throws EmfException {
                executeRuns();
            }
        });
        buttonPanel.add(executeButton);

        Button exportButton = new Button("Export", new AbstractFastAction(this.getMessagePanel(),
                "Error exporting Fast run(s)") {

            @Override
            protected void doActionPerformed(ActionEvent e) throws EmfException {
                exportRuns();
            }
        });
        exportButton.setEnabled(false);
        buttonPanel.add(exportButton);

        return buttonPanel;
    }

    private SelectAwareButton editButton(ConfirmDialog confirmDialog) {

        Action editAction = new AbstractFastAction(this.getMessagePanel(), "Error editing Fast runs") {

            @Override
            protected void doActionPerformed(ActionEvent e) {
                editRuns();
            }
        };

        return new SelectAwareButton("Edit", editAction, table, confirmDialog);
    }

    private SelectAwareButton viewButton(ConfirmDialog confirmDialog) {

        Action viewAction = new AbstractFastAction(this.getMessagePanel(), "Error viewing Fast runs") {

            @Override
            protected void doActionPerformed(ActionEvent e) {
                viewRuns();
            }
        };

        SelectAwareButton viewButton = new SelectAwareButton("View", viewAction, this.table, confirmDialog);
        viewButton.setEnabled(false);
        return viewButton;
    }

    private void viewRuns() {

        List<FastRun> runs = getSelected();
        if (runs.isEmpty()) {
            this.showMessage(ROOT_SELECT_PROMPT + "view.");
        } else {
            for (FastRun run : runs) {

                try {
                    presenter.doView(run);
                } catch (EmfException e) {
                    showError("Error viewing Fast runs: " + e.getMessage());
                }
            }
        }
    }

    private void editRuns() {

        List<FastRun> runs = getSelected();
        if (runs.isEmpty()) {
            this.showMessage(ROOT_SELECT_PROMPT + "edit.");
        } else {
            for (FastRun run : runs) {

                try {
                    presenter.doEdit(run);
                } catch (EmfException e) {
                    showError("Error editing Fast runs: " + e.getMessage());
                }
            }
        }
    }

    protected void removeSelectedRuns() throws EmfException {

        this.clearMessage();
        final List<FastRun> runs = getSelected();

        if (runs.isEmpty()) {
            this.showMessage(ROOT_SELECT_PROMPT + "remove.");
        } else {

            String message = null;
            int size = runs.size();
            if (size == 1) {
                message = "Are you sure you want to remove the selected Fast run?";
            } else {
                message = "Are you sure you want to remove the " + size + " selected Fast runs?";
            }

            int selection = JOptionPane.showConfirmDialog(getParentConsole(), message, "Warning",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (selection == JOptionPane.YES_OPTION) {

                ExceptionHandlingCommand removeCommand = new AbstractCommand(this) {
                    public void execute() throws EmfException {
                        presenter.doRemove(runs);
                    }
                };

                this.executeCommand(removeCommand);
            }
        }
    }

    private void copySelectedRuns() throws EmfException {

        this.clearMessage();
        final List<FastRun> runs = getSelected();

        if (runs.isEmpty()) {
            this.showMessage(ROOT_SELECT_PROMPT + "copy.");
        } else {

            String message = null;
            int size = runs.size();
            if (size == 1) {
                message = "Are you sure you want to copy the selected Fast run?";
            } else {
                message = "Are you sure you want to copy the " + size + " selected Fast runs?";
            }

            int selection = JOptionPane.showConfirmDialog(getParentConsole(), message, "Warning",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (selection == JOptionPane.YES_OPTION) {

                ExceptionHandlingCommand copyCommand = new AbstractCommand(this) {
                    public void execute() throws EmfException {

                        for (FastRun fastRun : runs) {
                            presenter.doSaveCopiedRun(fastRun, getSession().user());
                        }
                    }
                };

                this.executeCommand(copyCommand);
            }
        }
    }

    private void executeRuns() throws EmfException {

        this.clearMessage();
        final List<FastRun> runs = getSelected();

        if (runs.isEmpty()) {
            this.showMessage(ROOT_SELECT_PROMPT + "run.");
        } else {

            String message = null;
            int size = runs.size();
            if (size == 1) {
                message = "Are you sure you want to execute the selected Fast run?";
            } else {
                message = "Are you sure you want to execute the " + size + " selected Fast runs?";
            }

            int selection = JOptionPane.showConfirmDialog(getParentConsole(), message, "Warning",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (selection == JOptionPane.YES_OPTION) {

                ExceptionHandlingCommand executeCommand = new AbstractCommand(this) {
                    public void execute() throws EmfException {
                        presenter.doExecuteRuns(runs, getSession().user());
                    }
                };

                this.executeCommand(executeCommand);
                showMessage("Executing Fast run(s). Monitor the status window for progress, and refresh this window after completion to see results");
            }
        }
    }

    private void exportRuns() throws EmfException {

        this.clearMessage();
        final List<FastRun> runs = getSelected();

        if (runs.isEmpty()) {
            this.showMessage(ROOT_SELECT_PROMPT + "export.");
        } else {

            String message = null;
            int size = runs.size();
            if (size == 1) {
                message = "Are you sure you want to export the selected Fast run?";
            } else {
                message = "Are you sure you want to export the " + size + " selected Fast runs?";
            }

            int selection = JOptionPane.showConfirmDialog(getParentConsole(), message, "Warning",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (selection == JOptionPane.YES_OPTION) {

                ExceptionHandlingCommand executeCommand = new AbstractCommand(this) {
                    public void execute() throws EmfException {
                        presenter.doExportRuns(runs, getSession().user());
                    }
                };

                this.executeCommand(executeCommand);
                showMessage("Exporting Fast run(s). Monitor the status window for progress, and refresh this window after completion to see results");
            }
        }
    }

    private List<FastRun> getSelected() {
        return (List<FastRun>) table.selected();
    }

    private void createNewRun() throws EmfException {
        presenter.doNew();
    }
}
