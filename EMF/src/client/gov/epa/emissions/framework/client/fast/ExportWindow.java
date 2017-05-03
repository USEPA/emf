package gov.epa.emissions.framework.client.fast;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.fast.FastOutputExportWrapper;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class ExportWindow extends DisposableInteralFrame implements ExportView {

    private List<FastOutputExportWrapper> outputs;

    private SingleLineMessagePanel messagePanel;

    private JTextField folderTextField;

    private ExportPresenter presenter;

    // private JCheckBox overwriteCheckbox;

    private JButton exportButton;

    private DataCommonsService service;

    private EmfConsole parentConsole;

    public ExportWindow(List<FastOutputExportWrapper> outputs, DesktopManager desktopManager, EmfConsole parentConsole,
            EmfSession session) {

        super(generateLabel(outputs, "Export: "), desktopManager);

        this.setName("exportWindow:" + hashCode());

        this.parentConsole = parentConsole;
        this.outputs = outputs;
        this.service = session.dataCommonsService();

        this.getContentPane().add(createLayout());
        this.pack();
    }

    private static String generateLabel(List<FastOutputExportWrapper> outputs, String title) {

        StringBuilder sb = new StringBuilder(title);

        for (FastOutputExportWrapper output : outputs) {

            sb.append(output.getName());
            sb.append(", ");
        }

        int sbLength = sb.length();
        if (sbLength > title.length()) {
            sb.delete(sbLength - 2, sbLength);
        }

        return sb.toString();
    }

    public void observe(ExportPresenter presenter) {
        this.presenter = presenter;
    }

    private JPanel createLayout() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        panel.add(createExportPanel());
        panel.add(createButtonsPanel());
        return panel;
    }

    private JPanel createExportPanel() {

        int width = 40;

        JPanel panel = new JPanel(new GridBagLayout());

        Insets labelInsets = new Insets(5, 10, 4, 5);
        Insets valueInsets = new Insets(5, 0, 5, 10);
        Insets buttonInsets = new Insets(5, 0, 5, 10);

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = labelInsets;

        JLabel nameLabel = new JLabel("Output(s):");
        panel.add(nameLabel, constraints);

        constraints.gridx = 1;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = valueInsets;

        TextArea outputsTextArea = new TextArea("datasets", generateLabel(outputs, ""), width, 6);
        outputsTextArea.setEditable(false);
        JScrollPane outputsSP = new JScrollPane(outputsTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        panel.add(outputsSP, constraints);

        constraints.gridx = 0;
        constraints.gridwidth = 1;
        constraints.weightx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = labelInsets;

        JLabel abbreviationLabel = new JLabel("Folder:");
        panel.add(abbreviationLabel, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = valueInsets;

        this.folderTextField = new TextField("folder", width);
        panel.add(this.folderTextField, constraints);

        constraints.gridx = 2;
        constraints.weightx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = buttonInsets;

        JButton browseButton = browseFileButton();
        panel.add(browseButton, constraints);

        // constraints.gridx = 0;
        // constraints.gridy = 2;
        // constraints.fill = GridBagConstraints.HORIZONTAL;
        // constraints.anchor = GridBagConstraints.WEST;
        // constraints.insets = labelInsets;
        //
        // overwriteCheckbox = new JCheckBox("Overwrite files if they exist?", false);
        // overwriteCheckbox.setEnabled(true);
        // overwriteCheckbox.setName("overwrite");
        //
        // panel.add(this.overwriteCheckbox, constraints);

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.weighty = 1;

        JLabel emptyLabel = new JLabel();
        panel.add(emptyLabel, constraints);

        return panel;
    }

    private JButton browseFileButton() {

        Button button = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                selectFolder();
            }
        });

        Icon icon = new ImageResources().open("Export a Dataset");
        button.setIcon(icon);

        return button;
    }

    private JPanel createButtonsPanel() {

        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        exportButton = new ExportButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                clearMessagePanel();
                doExport();
            }
        });
        container.add(exportButton);
        getRootPane().setDefaultButton(exportButton);

        JButton done = new Button("Done", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.notifyDone();
            }
        });
        container.add(done);

        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    private void refresh() {
        super.validate();
    }

    private void doExport() {
        try {
            validateFolder(folderTextField.getText());

            // if (!overwriteCheckbox.isSelected()) {
            // presenter.doExport(this.outputs, folderTextField.getText());
            // } else {
            presenter.doExportWithOverwrite(this.outputs, folderTextField.getText());
            // }

            messagePanel.setMessage("Started export. Please monitor the Status window "
                    + "to track your Export request.");

            exportButton.setEnabled(false);
        } catch (EmfException e) {
            e.printStackTrace();
            messagePanel.setError(e.getMessage());
        }
    }

    private void clearMessagePanel() {
        messagePanel.clear();
        refresh();
    }

    public void setMostRecentUsedFolder(String mostRecentUsedFolder) {
        if (mostRecentUsedFolder != null)
            folderTextField.setText(mostRecentUsedFolder);
    }

    private void selectFolder() {
        EmfFileInfo initDir = new EmfFileInfo(folderTextField.getText(), true, true);

        EmfFileChooser chooser = new EmfFileChooser(parentConsole.getSession(), initDir, new EmfFileSystemView(service));
        chooser.setTitle("Select a folder to contain the exported Datasets");
        int option = chooser.showDialog(parentConsole, "Select a folder");

        EmfFileInfo file = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedDir() : null;
        if (file == null)
            return;

        if (file.isDirectory()) {
            folderTextField.setText(file.getAbsolutePath());
            presenter.setLastFolder(file.getAbsolutePath());
        }
    }

    protected void validateFolder(String folder) throws EmfException {
        if (folder == null || folder.trim().isEmpty())
            throw new EmfException("Please specify a valid export folder.");

        if (folder.contains("/home/") || folder.endsWith("/home")) {
            throw new EmfException("Export data into user's home directory is not allowed.");
        }
    }
}
