package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class ImportInputPanel extends JPanel {

    private MessagePanel messagePanel;

    private ImportPresenter presenter;

    private DataCommonsService service;

    private DefaultComboBoxModel datasetTypesModel;

    private TextField name, pattern;

    private TextField folder;

    private TextArea filenames;

    private JCheckBox isMultipleDatasets;

    private static String lastFolder = null;

    JComboBox datasetTypesComboBox = null;

    private DatasetType defaultDSType;

    private EmfConsole parent;

    private EmfSession session;

    public ImportInputPanel(EmfSession session, DataCommonsService service, MessagePanel messagePanel, EmfConsole parent, DatasetType dsType)
            throws EmfException {
        this.messagePanel = messagePanel;
        this.service = service;
        this.parent = parent;
        this.defaultDSType = dsType;
        this.session = session;

        initialize();
    }

    private void initialize() throws EmfException {
        int width = 40;
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
        
        datasetTypesComboBox = typesComboBox();
        JPanel typesCombo = new JPanel(new BorderLayout(10, 10));
        //layoutGenerator.addLabelWidgetPair("Dataset Type", datasetTypesComboBox, this);
        typesCombo.add(new JLabel("Dataset Type"),BorderLayout.WEST);
        typesCombo.add(datasetTypesComboBox);
        
        JPanel chooser = new JPanel(new BorderLayout(10, 10));
        folder = new TextField("folder", width);
        chooser.add(new JLabel("Folder             "),BorderLayout.WEST);
        chooser.add(folder);
        chooser.add(browseFileButton(), BorderLayout.EAST);
        
        JPanel apply = new JPanel(new BorderLayout(10,10));
        pattern = new TextField("pattern", width);
        apply.add(new JLabel("Pattern           "),BorderLayout.WEST);
        apply.add(pattern);
        apply.add(applyPatternButton(), BorderLayout.EAST);

        JPanel fileNamesPanel = new JPanel(new BorderLayout(4,10));
        filenames = new TextArea("filenames", "", width, 6);
        JScrollPane fileTextAreaPane = new JScrollPane(filenames, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        fileNamesPanel.add(new JLabel("Filenames        "),BorderLayout.WEST);
        fileNamesPanel.add(fileTextAreaPane);

        name = new TextField("name", width);
        JPanel nameField = new JPanel(new BorderLayout(10, 10));
        nameField.add(new JLabel("Dataset Names"),BorderLayout.WEST);
        nameField.add(name);
        //layoutGenerator.addLabelWidgetPair("Dataset Name", name, this);

        isMultipleDatasets = new JCheckBox("Create Multiple Datasets");
        isMultipleDatasets.addActionListener(multipleDatasetsActionListener());

        //layoutGenerator.addLabelWidgetPair("", isMultipleDatasets, this);

        // Lay out the panel.
        
        mainPanel.add(typesCombo);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(chooser);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(apply);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(fileNamesPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(nameField);
        mainPanel.add(Box.createVerticalStrut(10));
        this.setBorder(BorderFactory.createEmptyBorder(10,10,10,20));
        this.setLayout(new BorderLayout(10,10));
        this.add(mainPanel,BorderLayout.NORTH);
        this.add(isMultipleDatasets);

    }

    private JComboBox typesComboBox() {
        DatasetType[] allDatasetTypes = session.getLightDatasetTypes();
        DatasetType[] allTypesWithMessage = new DatasetType[allDatasetTypes.length + 1];
        copyDatasetTypes(allDatasetTypes, allTypesWithMessage);
        datasetTypesModel = new DefaultComboBoxModel(allTypesWithMessage);
        JComboBox datasetTypesComboBox = new JComboBox(datasetTypesModel);
        datasetTypesComboBox.setName("datasetTypes");

        if (this.defaultDSType != null && !defaultDSType.getName().equalsIgnoreCase("Select one")
                && !defaultDSType.getName().equalsIgnoreCase("All"))
            datasetTypesComboBox.setSelectedItem(defaultDSType);

        datasetTypesComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clear();
            }
        });
        return datasetTypesComboBox;
    }

    private ActionListener multipleDatasetsActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clear();
                makeVisibleDatasetNameField(!isMultipleDatasets.isSelected());
            }
        };
    }

    protected void makeVisibleDatasetNameField(boolean singleDataset) {
        messagePanel.clear();
        name.setEnabled(singleDataset);
        name.setVisible(singleDataset);
    }

    private void copyDatasetTypes(DatasetType[] allDatasetTypes, DatasetType[] allTypesWithMessage) {
        allTypesWithMessage[0] = new DatasetType("Choose a type ...");
        for (int i = 0; i < allDatasetTypes.length; i++) {
            allTypesWithMessage[i + 1] = allDatasetTypes[i];
        }
    }

    private JButton browseFileButton() {
        Button button = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                clear();
                selectFile();
            }
        });

        Icon icon = new ImageResources().open("Import a File");
        button.setIcon(icon);

        return button;
    }

    private JButton applyPatternButton() {
        Button button = new Button("Apply Pattern", new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                clear();
                selectFilesFromPattern();
            }
        });

        return button;
    }

    public void register(ImportPresenter presenter) {
        this.presenter = presenter;
    }

    private void selectFilesFromPattern() {
        try {
            populateFilenamesFiled(presenter.getFilesFromPatten(folder.getText(), pattern.getText()));
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void populateFilenamesFiled(String[] files) {
        String text = "";
        for (int i = 0; i < files.length; i++) {
            text += files[i] + System.getProperty("line.separator");
        }
        filenames.setText(text);
    }

    private void selectFile() {
        EmfFileInfo[] files = getSelectedFiles();

        if (files == null || files.length == 0)
            return;

        if (files.length > 1) {
            String[] fileNames = new String[files.length];
            for (int i = 0; i < fileNames.length; i++) {
                fileNames[i] = files[i].getName();
            }
            populateFilenamesFiled(fileNames);
        } else {
            singleFile(files[0]);
        }
    }

    private EmfFileInfo[] getSelectedFiles() {
        EmfFileInfo initDir = new EmfFileInfo(folder.getText(), true, true);

        // TODO: bugz 3536 add refresh button on file browser, start here - 2010/11/01 - Jason
        EmfFileChooser chooser = new EmfFileChooser(initDir, new EmfFileSystemView(service));
        chooser.setTitle("Select the " + datasetTypesComboBox.getSelectedItem() + " files to import into Datasets");
        chooser.setDirectoryAndFileMode();

        int option = chooser.showDialog(parent, "Select a file");

        EmfFileInfo[] files = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedFiles() : null;
        EmfFileInfo dir = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedDir() : null;

        if (dir != null && !dir.getAbsolutePath().equals(lastFolder)) {
            folder.setText(dir.getAbsolutePath());
            lastFolder = dir.getAbsolutePath();
            clearfilenames();
        }

        return files != null ? files : null;
    }

    private void clearfilenames() {
        filenames.setText("");
        name.setText("");
    }

    private void singleFile(EmfFileInfo file) {
        filenames.setText(file.getName());
        name.setText(file.getName());
    }

    public void setDefaultBaseFolder(String folder) {
        if (lastFolder == null)
            this.folder.setText(folder);
        else
            this.folder.setText(lastFolder);
    }

    public boolean isCreateMutlipleDatasets() {
        return isMultipleDatasets.isSelected();
    }

    public String folder() {
        return folder.getText();
    }

    public String[] files() {
        List<String> names = new ArrayList<String>();
        int lines = filenames.getLineCount();
        try {
            for (int i = 0; i < lines; i++) {
                int start = filenames.getLineStartOffset(i);
                int end = filenames.getLineEndOffset(i);
                names.add(filenames.getText(start, end - start));
            }
            names = removeEmptyFileNams(names);

        } catch (Exception e) {
            messagePanel.setError(e.getMessage());
        }
        return names.toArray(new String[0]);
    }

    private List<String> removeEmptyFileNams(List files) {
        List<String> nonEmptyList = new ArrayList<String>();
        for (int i = 0; i < files.size(); i++) {
            String file = (String) files.get(i);
            if (file.trim().length() != 0) {
                nonEmptyList.add(file.trim());
            }
        }
        return nonEmptyList;
    }

    public DatasetType datasetType() {
        return (DatasetType) datasetTypesModel.getSelectedItem();

    }

    public String datasetName() {
        return name.getText().trim();
    }

    public void setMessage(String message) {
        messagePanel.setMessage(message);
    }

    public void setErrorMessage(String message) {
        messagePanel.setError(message);
    }

    private void clear() {
        messagePanel.clear();
    }

}
