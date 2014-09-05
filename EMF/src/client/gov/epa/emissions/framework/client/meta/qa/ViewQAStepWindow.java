package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.EmptyStrings;
import gov.epa.emissions.commons.gui.FormattedDateField;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlstrategy.AnalysisEngineTableApp;
import gov.epa.emissions.framework.client.data.QAPrograms;
import gov.epa.emissions.framework.client.preference.DefaultUserPreferences;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.qa.QAProperties;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.NumberFormattedTextField;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class ViewQAStepWindow extends DisposableInteralFrame implements QAStepView {
    
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(CustomDateFormat.PATTERN_yyyyMMddHHmm);

    private EditableComboBox program;

    private TextArea programArguments;

    private NumberFormattedTextField order;

    private TextArea description;

    private SingleLineMessagePanel messagePanel;

    private ViewQAStepPresenter presenter;

    private QAStep step;

    private TextField who;

    private TextArea comments;
    
    private TextField tableName;

    private ComboBox status;

    private FormattedDateField date;

    private CheckBox required;

    private User user;

    private TextField config;

    private QAPrograms qaPrograms;

    private JTextField exportFolder;
    
    private JTextField exportName;
    
    private JCheckBox overide;

    private JLabel creationStatusLabel;
    
    private JLabel creationDateLabel;
    
    private JCheckBox currentTable;

    private EmfConsole parentConsole;

    private EmfSession session;
    
    private EmfDataset origDataset;

    private QAStepResult qaStepResult;

    private JCheckBox download;

    private BrowseButton exportFolderButton;

    public ViewQAStepWindow(EmfConsole parentConsole, EmfSession session, DesktopManager desktopManager) {
        super("View QA Step", new Dimension(680, 580), desktopManager);
        this.parentConsole = parentConsole;
        this.session = session;
    }

    public void display(QAStep step, QAStepResult qaStepResult, QAProgram[] programs, EmfDataset dataset, User user,
            String versionName, boolean asTemplate) {
        this.step = step;
        this.qaStepResult = qaStepResult;
        this.user = user;
        this.qaPrograms = new QAPrograms(null, programs);
        this.origDataset = dataset;
        super.setLabel(super.getTitle() + ": " + step.getName() + " - " + dataset.getName() + " (v" + step.getVersion()
                + ")");

        JPanel layout = createLayout(step, qaStepResult, versionName, asTemplate);
        super.getContentPane().add(layout);
        super.display();
    }

    public void windowClosing() {
        doClose();
    }

    public void observe(ViewQAStepPresenter presenter) {
        this.presenter = presenter;
    }

    private JPanel createLayout(QAStep step, QAStepResult qaStepResult, 
            String versionName, boolean asTemplate) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);

        panel.add(inputPanel(step, qaStepResult, versionName, asTemplate));
        panel.add(buttonsPanel());

        return panel;
    }

    private JPanel inputPanel(QAStep step, QAStepResult qaStepResult, String versionName, boolean asTemplate) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(upperPanel(step, versionName, asTemplate));
        panel.add(lowerPanel(step, qaStepResult));

        return panel;
    }

    private JPanel lowerPanel(QAStep step, QAStepResult qaStepResult) {

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.add(lowerTopLeftPanel(step));
        topPanel.add(lowerTopRightPanel(qaStepResult));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(topPanel);
        panel.add(lowerBottomPanel(step));
        return panel;
    }

    private JPanel lowerTopRightPanel(QAStepResult stepResult) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        if (stepResult == null)
            stepResult = new QAStepResult();

        String table = stepResult.getTable();
        table = (table == null) ? "" : table;
        tableName = new TextField("tableName", table, 20);
        tableName.setEditable(false);
        layoutGenerator.addLabelWidgetPair("Ouput Name:", tableName, panel);

        creationStatusLabel = new JLabel();
        String tableCreationStatus = stepResult.getTableCreationStatus();
        creationStatusLabel.setText((tableCreationStatus != null) ? tableCreationStatus : "");
        layoutGenerator.addLabelWidgetPair("Run Status:", creationStatusLabel, panel);

        creationDateLabel = new JLabel();
        Date tableCreationDate = stepResult.getTableCreationDate();
        String creationDate = (tableCreationDate != null) ? CustomDateFormat.format_MM_DD_YYYY_HH_mm(tableCreationDate)
                : "";
        creationDateLabel.setText(creationDate);
        layoutGenerator.addLabelWidgetPair("Run Date:", creationDateLabel, panel);

        currentTable = new JCheckBox();
        currentTable.setEnabled(false);
        currentTable.setSelected(stepResult.isCurrentTable());
        layoutGenerator.addLabelWidgetPair("Current Output?", currentTable, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad
        return panel;
    }

    private JPanel lowerBottomPanel(QAStep step) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        config = new TextField("config", step.getConfiguration(), 40);
        //addChangeable(config);
        config.setToolTipText("Enter the name of the Dataset that is the configuration "
                + "file (e.g., a REPCONFIG file)");
        layoutGenerator.addLabelWidgetPair("Configuration:", config, panel);

        comments = new TextArea("Comments", step.getComments(), 40, 2);
        //addChangeable(comments);
        ScrollableComponent scrollableComment = ScrollableComponent.createWithVerticalScrollBar(comments);
        layoutGenerator.addLabelWidgetPair("Comments:", scrollableComment, panel);

        layoutGenerator.addLabelWidgetPair("", downloadResultsChkboxPanel(step), panel);
        layoutGenerator.addLabelWidgetPair("Folder:", exportFolderPanel(step), panel);
        layoutGenerator.addLabelWidgetPair("Export Name:", exportNamePanel(step), panel);
        layoutGenerator.addLabelWidgetPair("", overideChkboxPanel(step), panel);
        
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;

    }

    private JPanel exportNamePanel(QAStep step) {
        exportName = new JTextField(40);
        exportName.setToolTipText("The name of the file to which the step results will be exported");
        exportName.setName("exportName");
        exportName.setText("");
        JPanel namePanel = new JPanel(new BorderLayout(2, 10));
        namePanel.add(exportName, BorderLayout.LINE_START);
        return namePanel;
    }
    
    private JPanel downloadResultsChkboxPanel(QAStep step) {
        download = new JCheckBox("Download result file to local machine?");
        download.setName("download");
        download.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {

                Object source = e.getItemSelectable();

                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    exportFolder.setEnabled(true);
                    exportFolderButton.setEnabled(true);
                } else {
                    exportFolder.setEnabled(false);
                    exportFolderButton.setEnabled(false);
                }
            }            
        });
        JPanel downloadPanel = new JPanel(new BorderLayout(2, 10));
        downloadPanel.add(download, BorderLayout.LINE_START);
        return downloadPanel;
    }

    private JPanel overideChkboxPanel(QAStep step) {
        overide = new JCheckBox("Overwrite files if they exist?");
        overide.setToolTipText("If the box checked, the files with the same names will be overiden if they already exist in the folder.");
        overide.setName("overid");
        JPanel overidePanel = new JPanel(new BorderLayout(2, 10));
        overidePanel.add(overide, BorderLayout.LINE_START);
        return overidePanel;
    }

    private JPanel exportFolderPanel(QAStep step) {
        exportFolder = new JTextField(40);
        exportFolder.setName("folder");
        String outputFolder = step.getOutputFolder();
        exportFolder.setText(outputFolder != null ? outputFolder : "");
        exportFolderButton = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                selectFolder();
            }
        });
        JPanel folderPanel = new JPanel(new BorderLayout(10, 10));
        folderPanel.add(exportFolder);
        folderPanel.add(exportFolderButton, BorderLayout.EAST);
        return folderPanel;
    }
    
    private void selectFolder() {
        EmfFileInfo initDir = new EmfFileInfo(exportFolder.getText(), true, true);
        
        EmfFileChooser chooser = new EmfFileChooser(initDir, new EmfFileSystemView(session.dataCommonsService()));
        chooser.setTitle("Select a folder to contain the exported QA step results");
        int option = chooser.showDialog(parentConsole, "Select a folder");

        EmfFileInfo file = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedDir() : null;
        if (file == null)
            return;

        if (file.isDirectory()) {
            exportFolder.setText(file.getAbsolutePath());
        }
    }

    private JPanel lowerTopLeftPanel(QAStep step) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        status = status(step);
        //addChangeable(status);
        layoutGenerator.addLabelWidgetPair("QA Status:", status, panel);

        who = new TextField("who", step.getWho(), 10);
        //addChangeable(who);
        layoutGenerator.addLabelWidgetPair("User:", who, panel);

        date = new FormattedDateField("Date", step.getDate(), DATE_FORMATTER, messagePanel);
        //addChangeable(date);
        layoutGenerator.addLabelWidgetPair("QA Date:", date, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                40, 10);// xPad, yPad

        return panel;

    }

    private ComboBox status(QAStep step) {
        ComboBox status = new ComboBox(statusValue(step), QAProperties.status());
        status.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                date.setValue(new Date());
                who.setText(user.getName());
            }
        });

        return status;
    }

    private String statusValue(QAStep step) {
        return step.getStatus() != null ? step.getStatus() : QAProperties.initialStatus();
    }

    private JPanel upperPanel(QAStep step, String versionName, boolean asTemplate) {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Name:", new Label(step.getName()), panel);

        program = new EditableComboBox(qaPrograms.names());
        program.setPreferredSize(new Dimension(250, 20));
        program.setEditable(false);
        program.setPrototypeDisplayValue(EmptyStrings.create(20));
        QAProgram qaProgram = step.getProgram();
        if (qaProgram != null)
            program.setSelectedItem(qaProgram.getName());
        else
            program.setSelectedItem(null);
        //addChangeable(program);
        
        JPanel prgpanel = new JPanel();
        prgpanel.add(new Label(versionName + " (" + step.getVersion() + ")"));
        prgpanel.add(new JLabel(EmptyStrings.create(20)));
        prgpanel.add(new JLabel("Program:  "));
        prgpanel.add(program);
        layoutGenerator.addLabelWidgetPair("Version:", prgpanel, panel);

        programArguments = new TextArea("", step.getProgramArguments(), 40, 3);
        //addChangeable(programArguments);
        ScrollableComponent scrollableDetails = ScrollableComponent.createWithVerticalScrollBar(programArguments);
        
        //Added new dimensioning to make the window wider
        scrollableDetails.setPreferredSize(new Dimension(525,100));
        
        layoutGenerator.addLabelWidgetPair("Arguments:", scrollableDetails, panel);

        required = new CheckBox("", step.isRequired());
        if (step.isRequired())
            required.setEnabled(false);
        CheckBox sameAstemplate = new CheckBox("", asTemplate);
        sameAstemplate.setEnabled(false);
        
        order = new NumberFormattedTextField(5, orderAction());
        order.setText(step.getOrder() + "");
        order.addKeyListener(keyListener());
        //addChangeable(order);
        
        JPanel checkBoxPanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layout = new SpringLayoutGenerator();
        JPanel reqirepanel = new JPanel();
        reqirepanel.add(new JLabel(EmptyStrings.create(20)));
        reqirepanel.add(new JLabel("Required?"));
        reqirepanel.add(required);
        reqirepanel.add(new JLabel(EmptyStrings.create(20)));
        reqirepanel.add(new JLabel("Arguments same as template?"));
        reqirepanel.add(sameAstemplate);
        layout.addWidgetPair(order, reqirepanel, checkBoxPanel);
        layout.makeCompactGrid(checkBoxPanel, 1, 2, 0, 0, 0, 0);
        layoutGenerator.addLabelWidgetPair("Order:", checkBoxPanel, panel);

        description = new TextArea("", step.getDescription(), 40, 3);
        //addChangeable(description);
        ScrollableComponent scrollableDesc = ScrollableComponent.createWithVerticalScrollBar(description);
        
        //Added new dimensioning to make the window wider
        scrollableDesc.setPreferredSize(new Dimension(525,100));
        
        layoutGenerator.addLabelWidgetPair("Description:", scrollableDesc, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private KeyListener keyListener() {
        return new KeyListener() {
            public void keyTyped(KeyEvent e) {
                keyActions();
            }

            public void keyReleased(KeyEvent e) {
                keyActions();
            }

            public void keyPressed(KeyEvent e) {
                keyActions();
            }
        };
    }

    private void keyActions() {
        try {
            messagePanel.clear();
            Float.parseFloat(order.getText());
        } catch (NumberFormatException ex) {
            messagePanel.setError("Order should be a floating point number");
        }
    }

    private AbstractAction orderAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Float.parseFloat(order.getText());
                } catch (NumberFormatException ex) {
                    messagePanel.setError("Order should be a floating point number");
                }
            }
        };
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button cancel = closeButton();
        panel.add(cancel);

        Button viewResults = viewResultsButton();
        panel.add(viewResults);

        Button export = exportButton();
        panel.add(export);
        return panel;
    }

    private Button exportButton() {
        Button export = new Button("Export", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clear();
                
                doExport();
            }
        });
        return export;
    }

//    protected void doExport() {
//        try {
//            checkExportFolder();
//            QAStepResult result = presenter.getStepResult(step);
//            resetRunStatus(result);
//            messagePanel.setMessage("Started Export. Please monitor the Status window "
//                    + "to track your export request.");
//            presenter.doExport(step, result, exportFolder.getText(), exportName.getText(), overide.isSelected());
//        } catch (EmfException e) {
//            messagePanel.setError(e.getMessage());
//        }
//    }
    
    protected void doExport() {
        try {
            //only make user specify folder when exporting to EMF server, not downloading
            if (!download.isSelected())
                checkExportFolder();
            if ( !checkExportName()) 
                return;
//            if (!presenter.ignoreShapeFileFunctionality() 
//                    && presenter.isShapeFileCapable(qaStepResult)) {
                QAStepExportWizard dialog = new QAStepExportWizard(parentConsole);
                QAStepExportWizardPresenter presenter2 = new QAStepExportWizardPresenter(session);
                presenter2.display(dialog, qaStepResult);
                if (!presenter2.isCanceled()) { //make sure they didn't cancel the export operation...
                    if (dialog.shouldCreateShapeFile()){
                        if (download.isSelected()) {
                            messagePanel.setMessage("Started Exporting Shape File to download. Please monitor the Status window "
                                    + "to track your export request.");
                            this.presenter.downloadToShapeFile(step, qaStepResult, exportName.getText(), dialog.getProjectionShapeFile(), dialog.getRowFilter(), dialog.getPivotConfiguration(), overide.isSelected());
                        } else {
                            messagePanel.setMessage("Started Exporting Shape File. Please monitor the Status window "
                                    + "to track your export request.");
                            this.presenter.exportToShapeFile(step, qaStepResult, exportFolder.getText(), exportName.getText(), overide.isSelected(), dialog.getProjectionShapeFile(), dialog.getRowFilter(), dialog.getPivotConfiguration());
                        }
                    }
                    if(dialog.shouldCreateCSV()) {
                        if (download.isSelected()) {
                            messagePanel.setMessage("Started Export to download. Please monitor the Status window "
                                    + "to track your export request.");
                            this.presenter.download(step, qaStepResult, exportName.getText(), overide.isSelected(), dialog.getRowFilter());
                        } else {
                            messagePanel.setMessage("Started Export. Please monitor the Status window "
                                    + "to track your export request.");
                            this.presenter.export(step, qaStepResult, exportFolder.getText(), exportName.getText(), overide.isSelected(), dialog.getRowFilter()); // pass in fileName
                        }
                    }
                }
//            } else {
//                if (download.isSelected()) {
//                    messagePanel.setMessage("Started Export to download. Please monitor the Status window "
//                            + "to track your export request.");
//                    this.presenter.download(step, qaStepResult, exportName.getText(), overide.isSelected());
//                } else {
//                    messagePanel.setMessage("Started Export. Please monitor the Status window "
//                            + "to track your export request.");
//                    this.presenter.export(step, qaStepResult, exportFolder.getText(), exportName.getText(), overide.isSelected(), dialog.get); // pass in fileName
//                }
//            }
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private boolean checkExportName() {
        
        if (exportName.getText().trim().isEmpty())
        {
            // show a dialog to remind user that the name will be default
            int n = JOptionPane.showConfirmDialog(
                    this,
                    "You did not specify the Export Name. It will be generated automatically. Would you like to continue?",
                    "Export Name not Specified",
                    JOptionPane.YES_NO_OPTION);
            if ( n == JOptionPane.YES_OPTION) 
                return true;
            return false;
        }

        return true;
    }

    private void checkExportFolder() throws EmfException{
        if (exportFolder.getText().trim().isEmpty())
            throw new EmfException (" Please specify the export folder. ");
    }

    private Button viewResultsButton() {
        return new Button("View Results", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clear();
                try {
                    viewResults();
                } catch (EmfException exc) {
                    messagePanel.setError(exc.getMessage());
                }
            }
        });
    }
    
    private void viewResults() throws EmfException { // TODO: 2011-02
        QAStepResult stepResult = presenter.getStepResult(step);
        if (stepResult == null)
            throw new EmfException("Please run the QA step before trying to view.");
        resetRunStatus(stepResult);
        
        final String exportDir = exportFolder.getText();

//        if (exportDir == null || exportDir.trim().isEmpty())
//            throw new EmfException("Please specify an export directory.");

        Thread viewResultsThread = new Thread(new Runnable() {
            public void run() {
                try {
                    QAStepResult stepResult = presenter.getStepResult(step);
                    clear();
                    
                    DefaultUserPreferences userPref = new DefaultUserPreferences();
                    String sLimit = userPref.property("View_QA_results_limit");
                    long rlimit;
                    if ( sLimit == null ){
                        JOptionPane.showMessageDialog(parentConsole, 
                                "View_QA_results_limit is not specified in EMFPrefs.txt, default value is 50000.", "Warning", JOptionPane.WARNING_MESSAGE);
                        rlimit = 50000;
                    }
                    else   
                        try {
                            rlimit = Integer.parseInt(sLimit.trim());
                        } catch (NumberFormatException e) {
                            //just default if they entered a non number string
                            rlimit = 50000;
                        }
                    
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    long records = presenter.getTableRecordCount(stepResult);
                    long viewCount =  records;
                    
                    if ( viewCount > rlimit) {
                        messagePanel.setMessage("Total records: " + records + ", limit: "+rlimit );
                        ViewQAResultDialg dialog = new ViewQAResultDialg(step.getName(), parentConsole);
                        dialog.run();          
                        if ( dialog.shouldViewNone() )
                            return; 
                        else if ( !dialog.shouldViewall()){ 
                            viewCount = dialog.getLines();
                        } 
                        if ( viewCount > records ) viewCount = records;
                        if (viewCount > 100000) {
                            String title = "Warning";
                            String message = "Are you sure you want to view more than 100,000 records?  It could take several minutes to load the data.";
                            int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE);

                            if (selection == JOptionPane.NO_OPTION) {
                                return;
                            }
                        }
                    }
                    presenter.viewResults(step, stepResult, viewCount);
                } catch (EmfException e) {
                    try  {
                        //if ( presenter.checkBizzareCharInColumn(step, "plant")) {
                        if ( e.getMessage().contains("Invalid XML character")) {
                            messagePanel.setError("There are bizarre characters in the dataset." + 
                                    ((origDataset.getDatasetType().getName().equals(DatasetType.FLAT_FILE_2010_POINT) || 
                                      origDataset.getDatasetType().getName().equals(DatasetType.orlPointInventory)) 
                                      ? ", please run a QA step Detect Bizarre Characters." : ".")); 
                        } else {
                            messagePanel.setError(e.getMessage());
                        }
                    } catch (Exception e2) {
                        messagePanel.setError(e2.getMessage());
                    }
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });

        viewResultsThread.start();
    }
    
    private Button closeButton() {
        Button cancel = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clear();
                doClose();
            }
        });
        return cancel;
    }

    protected void doClose() {
        if (super.shouldDiscardChanges())
            presenter.doClose();
    }

    public void setMostRecentUsedFolder(String mostRecentUsedFolder) {
        if (mostRecentUsedFolder != null)
            exportFolder.setText(mostRecentUsedFolder);
    }

    private void clear() {
        messagePanel.clear();
    }

    public void displayResultsTable(String qaStepName, String exportedFileName) {
        AnalysisEngineTableApp app = new AnalysisEngineTableApp("View QA Step Results: " + qaStepName, new Dimension(500, 500), desktopManager, parentConsole);
        app.display(new String[] { exportedFileName });
    }
    
    private void resetRunStatus(QAStepResult result) {
        who.setText(step.getWho());
        date.setText(DATE_FORMATTER.format(step.getDate()));
        status.setSelectedItem(step.getStatus());
        tableName.setText(result == null ? "" : result.getTable());
        creationStatusLabel.setText(result == null ? "" : result.getTableCreationStatus());
        creationDateLabel.setText(CustomDateFormat.format_MM_DD_YYYY_HH_mm(result == null ? null : result.getTableCreationDate()));
        currentTable.setSelected(result == null ? false : result.isCurrentTable());
        super.revalidate();
    }

}
