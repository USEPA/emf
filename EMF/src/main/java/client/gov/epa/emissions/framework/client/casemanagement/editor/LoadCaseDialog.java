package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;

public class LoadCaseDialog extends JDialog {

    private SingleLineMessagePanel messagePanel;

    private JTextField path;

    private TextArea msgArea;

    private JComboBox jobs;

    private Button loadButton;
    
    private Button cancelButton;

    private LoadCasePresenter presenter;

    private EmfConsole parentConsole;

    private DataCommonsService service;

    public LoadCaseDialog(String title, Component container, EmfConsole parentConsole, EmfSession session) {
        super(parentConsole);
        super.setTitle(title);
        super.setLocation(ScreenUtils.getCascadedLocation(container, container.getLocation(), 300, 300));
        super.setModal(true);

        this.parentConsole = parentConsole;
        this.service = session.dataCommonsService();
    }

    public void display() {
        this.getContentPane().add(createLayout());
        this.pack();
        this.setVisible(true);
    }

    public void observe(LoadCasePresenter presenter) {
        this.presenter = presenter;
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        panel.add(createFolderPanel());
        panel.add(createButtonsPanel());

        return panel;
    }

    private JPanel createFolderPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // folder
        path = new JTextField(30);
        path.setName("path");
        this.setMostRecentUsedFolder(presenter.getDefaultBaseFolder());

        Button button = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                clearMessagePanel();
                selectFile();
            }
        });
        Icon icon = new ImageResources().open("Open a file");
        button.setIcon(icon);

        try {
            jobs = new JComboBox(presenter.getJobs());
            jobs.setSelectedItem(null);
            jobs.setPreferredSize(new Dimension(334, 30));
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }

        JPanel folderPanel = new JPanel(new BorderLayout(2, 0));
        folderPanel.add(path, BorderLayout.LINE_START);
        folderPanel.add(button, BorderLayout.LINE_END);
        layoutGenerator.addLabelWidgetPair("File:", folderPanel, panel);
        layoutGenerator.addLabelWidgetPair("Case Job:", jobs, panel);

        msgArea = new TextArea("messages", "", 38, 8);
        msgArea.setEditable(false);
        msgArea.setLineWrap(false);
        JScrollPane msgTextArea = new JScrollPane(msgArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        layoutGenerator.addLabelWidgetPair("Messages:", msgTextArea, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        loadButton = new Button("Load", loadCase());
        container.add(loadButton);
        getRootPane().setDefaultButton(loadButton);

        cancelButton = new Button("Done", closeAction());
        container.add(cancelButton);

        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    private Action loadCase() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clearMessagePanel();
                    msgArea.setText("");

                    checkFolderField();
                    CaseJob selected = (CaseJob) jobs.getSelectedItem();

                    if (selected == null) {
                        messagePanel.setError("Please select a valid job.");
                        return;
                    }

                    LoadCaseDialog.this.validate();
                    kickLoadingThread(selected);
                } catch (EmfException e1) {
                    messagePanel.setError(e1.getMessage());
                }
            }
        };
    }

    private Action closeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
    }

    private void kickLoadingThread(final CaseJob job) {
        Thread loadingThread = new Thread(new Runnable() {
            public void run() {
                loadInputs(job);
            }
        });

        loadingThread.start();
    }

    private synchronized void loadInputs(CaseJob selected) {
        try {
            messagePanel.setMessage("Please wait while loading case values...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            loadButton.setEnabled(false);
            cancelButton.setEnabled(false);
            String loadingMsg = presenter.loadCase(path.getText(), selected);
            msgArea.setText(loadingMsg);
            messagePanel.setMessage("Finished loading case.");
        } catch (Exception e) {
            messagePanel.setError(e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
            loadButton.setEnabled(true);
            cancelButton.setEnabled(true);
        }
    }

    private void clearMessagePanel() {
        messagePanel.clear();
    }

    public void setMostRecentUsedFolder(String mostRecentUsedFolder) {
        if (mostRecentUsedFolder != null && path != null)
            path.setText(mostRecentUsedFolder);
    }

    private void selectFile() {
        EmfFileInfo initDir = new EmfFileInfo(path.getText(), true, true);

        EmfFileChooser chooser = new EmfFileChooser(initDir, new EmfFileSystemView(service));
        chooser.setTitle("Select a case log file for loading data into case");
        chooser.setDirectoryAndFileMode();
        int option = chooser.showDialog(parentConsole, "Select a file");

        EmfFileInfo[] files = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedFiles() : null;

        if (files == null || files.length == 0)
            return;

        if (files.length > 1) {
            path.setText("");
            messagePanel.setError("Please select a single log file.");
            return;
        }

        if (files[0].isFile()) {
            path.setText(files[0].getAbsolutePath());
        }
    }

    private void checkFolderField() throws EmfException {
        String specified = path.getText();

        if (specified == null || specified.trim().isEmpty() || specified.trim().length() == 1)
            throw new EmfException("Please specify a valid file.");

        if (specified.charAt(0) != '/' && specified.charAt(1) != ':')
            throw new EmfException("Specified folder is not in a right format (ex. C:\\, /home, etc.).");

        if (specified.charAt(0) != '/' && !Character.isLetter(specified.charAt(0)))
            throw new EmfException("Specified folder is not in a right format (ex. C:\\).");
    }

}
