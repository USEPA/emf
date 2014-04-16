package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ChangeAwareButton;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.notes.NewNoteDialog;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetNote;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.editor.Revision;
import gov.epa.emissions.framework.ui.Dimensions;
import gov.epa.emissions.framework.ui.InfoDialog;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DataEditor extends DisposableInteralFrame implements DataEditorView {

    private JPanel layout;

    private MessagePanel messagePanel;

    private DataEditorPresenter presenter;

    private EmfDataset dataset;

    private EditorPanel pageContainer;

    private JLabel lockInfo;

    private EmfConsole parent;

    private Version version;

    private User user;

    private RevisionPanel revisionPanel;

    private ChangeAwareButton save, discard;

    private boolean hasReplacedValues;

    public DataEditor(EmfDataset dataset, EmfConsole parent, DesktopManager desktopManager) {
        super("Data Editor: " + dataset.getName(), desktopManager);
        setDimension();
        this.dataset = dataset;
        this.parent = parent;

        layout = new JPanel(new BorderLayout());
        layout.add(topPanel(), BorderLayout.PAGE_START);

        this.getContentPane().add(layout);
    }

    private void setDimension() {
        Dimension dim = new Dimensions().getSize(0.7, 0.7);
        int height = (int) dim.getHeight();
        if (dim.getWidth() < 850) {
            dim.setSize(850, height);
        }
        setSize(dim);
    }

    private JPanel topPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        lockInfo = new JLabel();
        panel.add(lockInfo, BorderLayout.LINE_END);

        return panel;
    }

    public void observe(DataEditorPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(Version version, String table, User user, TableMetadata tableMetadata, DatasetNote[] notes) {
        this.version = version;
        this.user = user;

        updateTitle(version, table);
        super.setName("dataEditor:" + version.getDatasetId() + ":" + version.getId());

        JPanel container = new JPanel(new BorderLayout());
        container.add(tablePanel(version, table, tableMetadata), BorderLayout.CENTER);
        container.add(bottomPanel(notes), BorderLayout.PAGE_END);
        layout.add(container, BorderLayout.CENTER);

        super.display();
    }

    private void updateTitle(Version version, String table) {
        String label = super.getTitle();
        label += ", Version: " + version.getName();
        label += ", Table: " + table + "]";
        super.setTitle(label);
    }

    public void updateLockPeriod(Date start, Date end) {
        lockInfo.setText("Lock expires at " + format(end) + "  ");
    }

    private JPanel tablePanel(Version version, String table, TableMetadata tableMetadata) {
        pageContainer = new EditorPanel(dataset, version, tableMetadata, messagePanel, this);
        pageContainer.setDesktopManager(desktopManager);
        pageContainer.setEmfSession(presenter.getEmfSession());
        displayTable(table);

        return pageContainer;
    }

    private void displayTable(String table) {
        try {
            presenter.displayTable(pageContainer);
        } catch (EmfException e) {
            displayError("Could not display table: " + table + "." + e.getMessage());
        }
    }

    private JPanel bottomPanel(DatasetNote[] notes) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(revisionPanel(notes));
        panel.add(controlPanel());

        return panel;
    }

    private JPanel revisionPanel(DatasetNote[] notes) {
        revisionPanel = new RevisionPanel(user, dataset, version, notes, parent);
        revisionPanel.disableWhatNWhy();
        return revisionPanel;
    }

    private JPanel controlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(leftControlPanel(), BorderLayout.LINE_START);
        panel.add(rightControlPanel(), BorderLayout.LINE_END);

        return panel;
    }

    private JPanel leftControlPanel() {
        JPanel panel = new JPanel();

        Button addNote = new AddButton("Add Note", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                messagePanel.clear();
                NewNoteDialog view = new NewNoteDialog(parent);
                try {
                    presenter.doAddNote(view);
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        });
        panel.add(addNote);

        return panel;
    }

    private JPanel rightControlPanel() {
        JPanel panel = new JPanel();

        panel.add(discardButton());
        panel.add(saveButton());
        panel.add(closeButton());

        return panel;
    }

    private Button closeButton() {
        Button close = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doClose();
            }
        });
        close.setToolTipText("Close without Saving your changes");
        return close;
    }

    private Button saveButton() {
        save = new ChangeAwareButton("Save", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doSave();
            }
        });

        save.setToolTipText("Save your changes");
        save.setEnabled(false);
        return save;
    }

    private Button discardButton() {
        // TODO: prompts for Discard and Close (if changes exist)
        discard = new ChangeAwareButton("Discard", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doDiscard();
            }
        });
        discard.setToolTipText("Discard your changes");
        discard.setEnabled(false);
        return discard;
    }

    private void displayError(String message) {
        messagePanel.setError(message);
        refreshLayout();
    }

    private void displayMessage(String message) {
        messagePanel.setMessage(message);
        refreshLayout();
    }

    private void doSave() {
        clearMessages();
        try {
            presenter.doSave();
            displayMessage("Saved changes.");
        } catch (EmfException e) {
            displayError("Could not save: " + e.getMessage());
            return;
        }
    }

    private void clearMessages() {
        messagePanel.clear();
        refreshLayout();
    }

    private void doClose() {
        clearMessages();
        revisionPanel.enableWhatNWhy();
        try {
            ManagedView findReplaceWindow = desktopManager.getWindow("Find and Replace Column Values: "
                    + dataset.getName() + " (version: " + version.getVersion() + ")");
            if (findReplaceWindow != null)
                findReplaceWindow.windowClosing(); // NOTE: to close the find and replace window
            presenter.doClose();
        } catch (EmfException e) {
            displayError("Could not close: " + e.getMessage());
        }
    }

    public Revision revision() {
        return revisionPanel.revision();
    }

    public boolean verifyRevisionInput() {
        return revisionPanel.verifyInput();
    }

    private void doDiscard() {
        clearMessages();
        try {
            presenter.doDiscard();
            displayMessage("Discarded changes.");
        } catch (EmfException e) {
            displayError("Could not discard: " + e.getMessage());
        }

    }

    public void windowClosing() {
        doClose();
    }

    public void notifyLockFailure(DataAccessToken token) {
        Version version = token.getVersion();
        String message = "Cannot edit Version: " + version.getName() + "(" + version.getVersion() + ") of Dataset: "
                + dataset.getName() + System.getProperty("line.separator") + " as it was locked by User: "
                + version.getLockOwner() + "(at " + format(version.getLockDate()) + ")";
        InfoDialog dialog = new InfoDialog(this, "Message", message);
        dialog.confirm();
    }

    private String format(Date lockDate) {
        return CustomDateFormat.format_YYYY_MM_DD_HH_MM(lockDate);
    }

    public void notifySaveFailure(String message) {
        InfoDialog dialog = new InfoDialog(this, "Message", message);
        dialog.confirm();
    }

    public boolean confirmDiscardChanges() {
        return super.shouldDiscardChanges();
    }

    public void signalChanges() {
        revisionPanel.enableWhatNWhy();
        enableSaveDiscard();
        super.signalChanges();
    }

    public void signalSaved() {
        disableSaveDiscard();
        super.signalSaved();
    }

    private void enableSaveDiscard() {
        save.signalChanges();
        discard.signalChanges();
    }

    public void disableSaveDiscard() {
        save.signalSaved();
        discard.signalSaved();
    }

    public void append2WhatField(String text) {
        revisionPanel.appendWhatField(text);
    }

    public boolean hasReplacedValues() {
        return hasReplacedValues;
    }

    public void setHasReplacedValues(boolean hasReplacedValues) {
        this.hasReplacedValues = hasReplacedValues;
        presenter.setSaveChanged(true);
    }

}
