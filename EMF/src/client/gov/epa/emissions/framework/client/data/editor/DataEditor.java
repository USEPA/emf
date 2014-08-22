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
import gov.epa.emissions.framework.client.meta.versions.EditVersionsPanel;
import gov.epa.emissions.framework.client.meta.versions.EditVersionsView;
import gov.epa.emissions.framework.client.swingworker.GenericSwingWorker;
import gov.epa.emissions.framework.client.util.ComponentUtility;
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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class DataEditor extends DisposableInteralFrame implements DataEditorView {

    private JPanel layout;

    private MessagePanel messagePanel;

    private DataEditorPresenter presenter;

    private EmfDataset dataset;

    private EditorPanel editorPanel;

    private JLabel lockInfo;

    private EmfConsole parent;

    private Version version;

    private User user;

    private RevisionPanel revisionPanel;

    private ChangeAwareButton save, discard;

    private boolean hasReplacedValues;

    private JLabel loadingPanel;
    
    private EditVersionsView parentView;

    public DataEditor(EmfDataset dataset, EmfConsole parent, DesktopManager desktopManager, EditVersionsView parentView) {
        super("Data Editor: " + dataset.getName(), desktopManager);
        setDimension();
        this.dataset = dataset;
        this.parent = parent;
        this.parentView = parentView;

        layout = new JPanel(new BorderLayout());
        layout.add(topPanel(), BorderLayout.PAGE_START);
        loadingPanel = new JLabel("Loading...", SwingConstants.CENTER);
        loadingPanel.setFont(new Font("default", Font.BOLD, 40));
        layout.add(loadingPanel, BorderLayout.CENTER);

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

    public void display(Version version, String table, User user) {
        this.version = version;
        this.user = user;

        updateTitle(version, table);
        super.setName("dataEditor:" + version.getDatasetId() + ":" + version.getId());

//        JPanel container = new JPanel(new BorderLayout());
//        container.add(tablePanel(version, table, tableMetadata), BorderLayout.CENTER);
//        container.add(bottomPanel(notes), BorderLayout.PAGE_END);
//        layout.add(container, BorderLayout.CENTER);

        super.display();
        
        populate(table);
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

    private JPanel tablePanel(Version version, final String table, TableMetadata tableMetadata) {
        editorPanel = new EditorPanel(dataset, version, tableMetadata, messagePanel, this, this);
        editorPanel.setDesktopManager(desktopManager);
        editorPanel.setEmfSession(presenter.getEmfSession());
        displayTable(table, tableMetadata);
        return editorPanel;
    }

    private void displayTable(String table, TableMetadata tableMetadata) {
        try {
            presenter.displayTable(editorPanel, this, messagePanel, tableMetadata);
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
        new GenericSwingWorker<Void>(layout, messagePanel) {

            @Override
            public Void doInBackground() throws EmfException {
                presenter.doSave();
                return null;
            }

            @Override
            public void done() {
                try {
                    get();
                    presenter.clearTable();
                    SwingUtilities.invokeLater(new Runnable() {
                        
                        @Override
                        public void run() {
                            try {
                                presenter.reloadCurrent();
                            } catch (EmfException e) {
                                // NOTE Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    });
                    parentView.refresh();
                    displayMessage("Saved changes.");
                } catch (InterruptedException | ExecutionException /*| EmfException */e) {
                    e.printStackTrace();
                    displayError("Could not save: " + e.getMessage());
                } finally {
                    finalize();
                }
            }

        }.execute();
    }

    private void clearMessages() {
        messagePanel.clear();
        refreshLayout();
    }

    private void doClose() {
        clearMessages();
        if (revisionPanel != null) 
            revisionPanel.enableWhatNWhy();
        ManagedView findReplaceWindow = desktopManager.getWindow("Find and Replace Column Values: "
                + dataset.getName() + " (version: " + version.getVersion() + ")");
        if (findReplaceWindow != null)
            findReplaceWindow.windowClosing(); // NOTE: to close the find and replace window
        new GenericSwingWorker<Void>(layout, messagePanel) {

            @Override
            public Void doInBackground() throws EmfException {
                presenter.doClose();
                return null;
            }

            @Override
            public void done() {
                try {
                    get();
                } catch (InterruptedException e) {
                    displayError("Could not close: " + e.getMessage());
                } catch (ExecutionException e) {
                    displayError("Could not close: " + e.getMessage());
                } finally {
                    finalize();
                }
            }

        }.execute();
    }

    public Revision revision() {
        return revisionPanel.revision();
    }

    public boolean verifyRevisionInput() {
        return revisionPanel.verifyInput();
    }

    private void doDiscard() {
        clearMessages();
        new GenericSwingWorker<Void>(layout, messagePanel) {

            @Override
            public Void doInBackground() throws EmfException {
//                presenter.doDiscard();
                presenter.discard();
                presenter.reloadCurrent();
                return null;
            }

            @Override
            public void done() {
                try {
                    get();
                    resetChanges();
                    disableSaveDiscard();
                    displayMessage("Discarded changes.");
                } catch (InterruptedException e) {
                    displayError("Could not close: " + e.getMessage());
                } catch (ExecutionException e) {
                    displayError("Could not close: " + e.getMessage());
                } finally {
                    finalize();
                }
            }

        }.execute();
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


    @Override
    public void populate(final String table) {
        new GenericSwingWorker<Void>(layout, messagePanel) {
            
            private String lockMessage = "Dataset lock issue";
            private DataAccessToken token;
            private TableMetadata tableMetadata;
            private DatasetNote[] notes;
            
            @Override
            public Void doInBackground() throws EmfException {
                token = presenter.openSession();
                if (!token.isLocked(user)) {// abort
                    throw new EmfException(lockMessage);
                }
                tableMetadata = presenter.getTableMetadata();
                notes = presenter.getDatasetNotes();

//                display(version, table, session.user(), tableMetadata, notes);
//                presenter.applyConstraints(token, null, null);
                return null;
            }

            @Override
            public void done() {
                try {
                    get();
                    JPanel container = new JPanel(new BorderLayout());
                    container.add(tablePanel(version, table, tableMetadata), BorderLayout.CENTER);
                    container.add(bottomPanel(notes), BorderLayout.PAGE_END);
                    layout.remove(loadingPanel);
                    layout.add(container, BorderLayout.CENTER);
                    updateLockPeriod(token.lockStart(), token.lockEnd());

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    messagePanel.setError(e.getMessage());
                } catch (ExecutionException e) {
                    if (e.getMessage().equals(lockMessage))
                        notifyLockFailure(token);
                    else {
                        messagePanel.setError(e.getMessage());
                    }
                } finally {
                    finalize();
                    System.out.println("All done");
                    editorPanel.revalidate();
                }
            }

        }.execute();
    }
}
