package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.db.version.Version;
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
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.commons.gui.buttons.RunButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlstrategy.AnalysisEngineTableApp;
import gov.epa.emissions.framework.client.data.QAPrograms;
import gov.epa.emissions.framework.client.meta.qa.compareDatasetFieldsProgram.CompareDatasetFieldsQAProgamWindow;
import gov.epa.emissions.framework.client.meta.qa.comparedatasetsprogram.CompareDatasetsQAProgamWindow;
import gov.epa.emissions.framework.client.meta.qa.flatFile2010Pnt.EnhanceFlatFile2010PointSettingWindows;
import gov.epa.emissions.framework.client.meta.versions.VersionsSet;
import gov.epa.emissions.framework.client.preference.DefaultUserPreferences;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.data.DatasetVersion;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.editor.DataEditorService;
import gov.epa.emissions.framework.services.qa.QAProperties;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.NumberFormattedTextField;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class EditQAStepWindow extends DisposableInteralFrame implements EditQAStepView {

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(CustomDateFormat.PATTERN_yyyyMMddHHmm);

    private EditableComboBox program;

    private TextArea programArguments;

    private NumberFormattedTextField order;

    private TextArea description;

    private SingleLineMessagePanel messagePanel;

    private EditQAStepPresenter presenter;

    private QAStep step;

    private TextField who;

    private TextArea comments;

    private ComboBox status;

    private FormattedDateField date;

    private CheckBox required;

    private User user;

    private TextField config;
    
    private TextField name; 
    
    private ComboBox versionsSelection;

    private QAPrograms qaPrograms;

    private JTextField exportFolder;
    
    private JTextField exportName;
    
    private JCheckBox overide;

    private QAStepResult qaStepResult;

    private Button saveButton;

    private Button runButton;

    private JCheckBox currentTable;

    private EmfSession session;

    private EmfConsole parentConsole;

    private TextField tableName;

    private JLabel creationStatusLabel;

    private JLabel creationDateLabel;
    
    private EmfDataset[] flatFile2010PointInv = null;
    private EmfDataset[] supportingSmokeFlatFileInv = null;
    private EmfDataset[] supportingFlatFileInv2 = null;

    private EmfDataset[] inventories = null;
    private EmfDataset[] invBase = null;
    private EmfDataset[] invCompare=null;

    private EmfDataset[] invTables = null;

    private EmfDataset origDataset;

    private String summaryType = "";
    private String emissionType = "";
    
    public static final String smkRptTag = "-smkrpt";

    public static final String coStCyTag = "-costcy";

    public static final String pollListTag = "-polllist";

    public static final String specieListTag = "-specielist";

    public static final String exclPollTag = "-exclpoll";

    public static final String sortPollTag = "-sortpoll";

    public static final String gsrefTag = "-gsref";

    public static final String gsproTag = "-gspro";

    public static final String detailedResultTag = "-detailed_result";

    public static final String BASE_TAG = "-base"; 
    
    public static final String FF10P_TAG = "-ff10p"; 
    
    public static final String SSFF_TAG = "-ssff"; 
    
    public static final String MANYNEIID_TAG = "-manyneiid"; 
    
    public static final String MANYFRS_TAG = "-manyfrs"; 

    //Sample:
/*
dataset name1|1
dataset name2|5
dataset name3|3
*/
    
    public static final String COMPARE_TAG = "-compare";
    //Sample:
/*
dataset name1|1
dataset name2|5
dataset name3|3
*/
    
    public static final String GROUP_BY_EXPRESSIONS_TAG = "-groupby";
    //Sample:
/*
scc
fips
plantid
pointid
stackid
segment
poll
*/
    public static final String JOIN_EXPRESSIONS_TAG = "-join_expressions";
    //Sample:
    /*
    scc
    fips
    plantid
    pointid
    stackid
    segment
    poll
    */
    
    public static final String AGGREGATE_EXPRESSIONS_TAG = "-aggregate";
    //Sample:
/*
ann_emis
avd_emis
*/
    
    public static final String COMPARISON_EXPRESSIONS_TAG = "-comparison_expressions";
    //Sample:
/*
ann_emis
avd_emis
*/

    public static final String MATCHING_EXPRESSIONS_TAG = "-matching";
/*
scc|scc
fips|fips
plantid|plantid
pointid|pointid
stackid|stackid
segment|segment
poll|poll
*/
    public static final String JOIN_TYPE_TAG = "-join";
/*
outer
inner    
*/
    public static final String WHERE_FILTER_TAG = "-where";
/*
substring(fips,1,2)='37'
*/
    public static final String BASE_SUFFIX_TAG = "-base_field_suffix";
    public static final String COMPARE_SUFFIX_TAG = "-compare_field_suffix";
    
    private String lineFeeder = System.getProperty("line.separator");
    
    private ActionListener versionSelectionActionListener = null;

    private JCheckBox download;

    private BrowseButton exportFolderButton; 

    public EditQAStepWindow(DesktopManager desktopManager, EmfConsole parentConsole) {
        super("Edit QA Step", new Dimension(680, 600), desktopManager);
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManager;
        // inventoryList = new ArrayList<EmfDataset>();
        // invTableList = new ArrayList<EmfDataset>();
//        versionSelectionActionListener = new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                onVersionSelectionChange();
//            }
//        };
    }

    public void display(QAStep step, QAStepResult qaStepResult, QAProgram[] programs, EmfDataset dataset,
            String versionName, boolean asTemplate, EmfSession session) {
        this.session = session;
        this.step = step;
        this.qaStepResult = qaStepResult;
        this.user = session.user();
        this.qaPrograms = new QAPrograms(session, programs);
        this.origDataset = dataset;
        super.setLabel(super.getTitle() + ": " + step.getName() + " - " + dataset.getName() + " (v" + step.getVersion()
                + ")");

        JPanel layout = createLayout(qaStepResult, versionName, asTemplate);
        super.getContentPane().add(layout);
        super.display();
    }

    public void windowClosing() {
        doClose();
    }

    public void observe(EditQAStepPresenter presenter) {
        this.presenter = presenter;
    }

    private JPanel createLayout(QAStepResult qaStepResult, String versionName, 
            boolean asTemplate) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);

        panel.add(inputPanel(qaStepResult, versionName, asTemplate));
        panel.add(buttonsPanel());

        return panel;
    }

    private JPanel inputPanel(QAStepResult qaStepResult, String versionName, boolean astemplate) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(upperPanel(versionName, astemplate));
        panel.add(lowerPanel(qaStepResult));

        return panel;
    }

    private JPanel lowerPanel(QAStepResult qaStepResult) {

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.add(lowerTopLeftPanel());
        topPanel.add(lowerTopRightPanel(qaStepResult));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(topPanel);
        panel.add(lowerBottomPanel());
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
        tableName.setToolTipText("The name of the output of the step (e.g. name of a table in the database");
        layoutGenerator.addLabelWidgetPair("Output Name:", tableName, panel);

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
        currentTable
                .setToolTipText("True when the source data and QA step have not been modified since the step was run");
        layoutGenerator.addLabelWidgetPair("Current Output?", currentTable, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad
        return panel;
    }
    
    

    private JPanel lowerBottomPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        config = new TextField("config", step.getConfiguration(), 40);
        addChangeable(config);
        config.setToolTipText("The name of the Dataset that is the configuration "
                + "file for the step (e.g., a REPCONFIG file)");
        layoutGenerator.addLabelWidgetPair("Configuration:", config, panel);

        comments = new TextArea("Comments", step.getComments(), 40, 2);
        addChangeable(comments);
        ScrollableComponent scrollableComment = ScrollableComponent.createWithVerticalScrollBar(comments);
        layoutGenerator.addLabelWidgetPair("Comments:", scrollableComment, panel);
        comments.setToolTipText("Enter any notes of interest that you found when performing the step");

        layoutGenerator.addLabelWidgetPair("", downloadResultsChkboxPanel(step), panel);
        layoutGenerator.addLabelWidgetPair("Export Folder:", exportFolderPanel(step), panel);
        layoutGenerator.addLabelWidgetPair("Export Name:", exportNamePanel(step), panel);
        layoutGenerator.addLabelWidgetPair("", overideChkboxPanel(step), panel);
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;

    }

    private JPanel exportFolderPanel(QAStep step) {
        exportFolder = new JTextField(40);
        exportFolder.setToolTipText("The folder (directory) to which the step results will be exported");
        exportFolder.setName("folder");
        String outputFolder = step.getOutputFolder();
        exportFolder.setText(outputFolder != null ? outputFolder : "");
        exportFolderButton = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                selectFolder();
            }
        });
        JPanel folderPanel = new JPanel(new BorderLayout(2, 10));
        folderPanel.add(exportFolder, BorderLayout.LINE_START);
        folderPanel.add(exportFolderButton, BorderLayout.LINE_END);
        return folderPanel;
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

    private JPanel lowerTopLeftPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        status = status(step);
        addChangeable(status);
        layoutGenerator.addLabelWidgetPair("QA Status:", status, panel);
        status.setToolTipText("Choosing a new status will automatically set the user and date");

        who = new TextField("who", step.getWho(), 10);
        addChangeable(who);
        layoutGenerator.addLabelWidgetPair("QA User:", who, panel);

        date = new FormattedDateField("Date", step.getDate(), DATE_FORMATTER, messagePanel);
        addChangeable(date);
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
    
    private Version[] dsVersions(QAStep step) throws EmfException {
        DataEditorService service = session.dataEditorService();
        return service.getVersions(step.getDatasetId());
    }
    
    private void setVersions(QAStep step) {
        Version[] dsVersions = null;
        try {
            dsVersions = this.dsVersions(step);
        } catch (EmfException e1) {
            // NOTE Auto-generated catch block
            e1.printStackTrace();
        }
        VersionsSet versionsSet = new VersionsSet(dsVersions);
        versionsSelection.setModel(new ComboBox("Select one", versionsSet.nameAndNumbers()).getModel());
        if (versionsSelection.getModel().getSize()==2)
        {
            versionsSelection.setSelectedIndex(1);
        }
        String vStr = "(" + step.getVersion() + ")";
        for (int i=0; i<versionsSelection.getItemCount(); i++) {
            String itemString = (String) versionsSelection.getItemAt(i);
            if ( itemString.contains(vStr)) {
                versionsSelection.setSelectedIndex(i);
                break;
            }
        }
    }
    
    private void onVersionSelectionChange() {
        String nameAndVersion = (String) versionsSelection.getSelectedItem();
        int pos1 = nameAndVersion.lastIndexOf("(");
        int pos2 = nameAndVersion.lastIndexOf(")");
        String vStr = nameAndVersion.substring(pos1+1, pos2);
        String oldName = super.getTitle();
        String newName = "Edit QA Step: " + step.getName() + " - " + this.origDataset.getName() + " (v" + Integer.parseInt(vStr.trim()) + ")";
        if (oldName.endsWith(" *"))
            newName += " *";
        super.setLabel(newName);
    }

    private JPanel upperPanel(String versionName, boolean astemplate) {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        
        name = new TextField("name", step.getName(), 40);
        addChangeable(name);
        layoutGenerator.addLabelWidgetPair("Name:", name, panel);

        program = new EditableComboBox(qaPrograms.names());
        program.setPreferredSize(new Dimension(250, 20));
        program.setPrototypeDisplayValue(EmptyStrings.create(20));
        QAProgram qaProgram = step.getProgram();
        if (qaProgram != null)
            program.setSelectedItem(qaProgram.getName());
        else
            program.setSelectedItem(null);
        addChangeable(program);
        program.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                currentTable.setSelected(false);
            }
        });

        Version[] dsVersions = null;
        try {
            dsVersions = this.dsVersions(step);
        } catch (EmfException e1) {
            // NOTE Auto-generated catch block
            e1.printStackTrace();
        }
        VersionsSet versionsSet = new VersionsSet(dsVersions);
        versionsSelection = new ComboBox("Select one", versionsSet.nameAndNumbers());
        if (versionsSelection.getModel().getSize()==2)
        {
            versionsSelection.setSelectedIndex(1);
        }
        String vStr = "(" + step.getVersion() + ")";
        for (int i=0; i<versionsSelection.getItemCount(); i++) {
            String itemString = (String) versionsSelection.getItemAt(i);
            if ( itemString.contains(vStr)) {
                versionsSelection.setSelectedIndex(i);
                break;
            }
        }
        versionsSelection.addActionListener( this.versionSelectionActionListener);
        addChangeable(versionsSelection);
        JPanel prgpanel = new JPanel();
//        prgpanel.add(new Label(versionName + " (" + step.getVersion() + ")"));
//        prgpanel.add(new JLabel(EmptyStrings.create(37)));
//        prgpanel.add(new JLabel("Program:  "));
        prgpanel.add(versionsSelection);
        prgpanel.add(program);

        layoutGenerator.addLabelWidgetPair("Version:", prgpanel, panel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        programArguments = new TextArea("", step.getProgramArguments(), 41, 3);
        addChangeable(programArguments);
        ScrollableComponent scrollableDetails = ScrollableComponent.createWithVerticalScrollBar(programArguments);
        buttonPanel.add(scrollableDetails);
        JPanel setButtonPanel = new JPanel(new BorderLayout());
        setButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 0));
        Button button1 = setButton();
        setButtonPanel.add(button1, BorderLayout.NORTH);
        buttonPanel.add(setButtonPanel);
        layoutGenerator.addLabelWidgetPair("Arguments:", buttonPanel, panel);
        programArguments.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                currentTable.setSelected(false);
            }
        });

        required = new CheckBox("", step.isRequired());
        if (step.isRequired())
            required.setEnabled(false);
        
        CheckBox sameAstemplate = new CheckBox("", astemplate);
        sameAstemplate.setEnabled(false);

        order = new NumberFormattedTextField(3, orderAction());
        order.setText(step.getOrder() + "");
        order.addKeyListener(keyListener());
        addChangeable(order);

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

        description = new TextArea("", step.getDescription(), 41, 3);
        addChangeable(description);
        ScrollableComponent scrollableDesc = ScrollableComponent.createWithVerticalScrollBar(description);
        layoutGenerator.addLabelWidgetPair("Description:", scrollableDesc, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

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
        saveButton = saveButton();
        getRootPane().setDefaultButton(saveButton);
        panel.add(saveButton);

        Button cancel = closeButton();
        panel.add(cancel);

        panel.add(Box.createHorizontalStrut(50));
        runButton = runButton();
        panel.add(runButton);

        Button viewResults = viewResultsButton();
        panel.add(viewResults);

        Button export = exportButton();
        panel.add(export);

        Button refresh = refreshButton();
        panel.add(refresh);

        return panel;
    }

    private Button viewResultsButton() {
        Button view = new Button("View Results", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clear();
                try {
                    viewResults();
                } catch ( Exception ex) {
                    messagePanel.setError( ex.getMessage());
                }
            }
        });
        view.setToolTipText(
"View the result as a local file on the client computer. Optionally create Google Earth .kmz file");
        view.setMnemonic('V');
        return view;
    }

    private Button runButton() {
        Button run = new RunButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    boolean result = checkDatasets();
                    //checkExportFolder();
                    if (result) {
                        clear();
                        runQAStep();
                    }
                } catch (Exception ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        });

        return run;
    }

    private boolean checkDatasets() throws EmfException {
        String programSwitches = "";
        boolean check = true;
        programSwitches = programArguments.getText();
        int invTableIndex = programSwitches.indexOf(QAStep.invTableTag);
        int invIndex = programSwitches.indexOf(QAStep.invTag);
        int emiIndex = programSwitches.indexOf(QAStep.emissionTypeTag);
        int sumTypeIndex = programSwitches.indexOf(QAStep.summaryTypeTag);

        if (QAStep.avgDaySummaryProgram.equalsIgnoreCase(program.getSelectedItem().toString())
                || QAStep.fireDataSummaryProgram.equals(program.getSelectedItem().toString())
                || QAStep.MultiInvSumProgram.equalsIgnoreCase(program.getSelectedItem().toString()))
        {
            if (!(programSwitches.trim().equals("")) && invIndex != -1 
                    && invTableIndex !=-1 && sumTypeIndex !=-1 ) {
                check = true;
                //getInventories(programSwitches, 0, invTableIndex);
                //getSummaryType(programSwitches, sumTypeIndex);
            }
            // if any of them doesn't exist
            //if (!(inventories.length > 0) || summaryType.trim().equalsIgnoreCase(""))
                //check = false;
            else
                check = false;

            if (!check)
                throw new EmfException(" Inventories, summary type are needed ");

        }else if (QAStep.compareVOCSpeciationWithHAPInventoryProgram.equalsIgnoreCase(program.getSelectedItem().toString())) {

            int capIndex = programSwitches.indexOf("-cap");
            int hapIndex = programSwitches.indexOf("-hap");
            int gstsiIndex = programSwitches.indexOf("-gstsi");
            int gscnvIndex = programSwitches.indexOf("-gscnv");
            int gspwIndex = programSwitches.indexOf("-gspw");
            int gsrefIndex = programSwitches.indexOf("-gsref");
            //int sumTypeIndex = programSwitches.indexOf(summaryTypeTag);
            
            String capInventory = null;
            String hapInventory = null;
            String speciationToolSpecieInfoDataset = null;
            String pollToPollConversionDataset = null;
            String[] speciationProfileWeightDatasets = null; 
            String[] speciationCrossReferenceDatasets = null;
            String[] datasets;
            String summaryType = "";
            if (capIndex != -1) {
                datasets = getDatasetNames(programSwitches, capIndex, programSwitches.indexOf("\n-", capIndex) != -1 ? programSwitches.indexOf("\n-", capIndex) : programSwitches.length()).toArray(new String[0]);
                if (datasets != null && datasets.length > 0) capInventory = datasets[0];
            }
            if (hapIndex != -1) {
                datasets = getDatasetNames(programSwitches, hapIndex, programSwitches.indexOf("\n-", hapIndex) != -1 ? programSwitches.indexOf("\n-", hapIndex) : programSwitches.length()).toArray(new String[0]);
                if (datasets != null && datasets.length > 0) hapInventory = datasets[0];
            }
            if (gstsiIndex != -1) {
                datasets = getDatasetNames(programSwitches, gstsiIndex, programSwitches.indexOf("\n-", gstsiIndex) != -1 ? programSwitches.indexOf("\n-", gstsiIndex) : programSwitches.length()).toArray(new String[0]);
                if (datasets != null && datasets.length > 0) speciationToolSpecieInfoDataset = datasets[0];
            }
            if (gscnvIndex != -1) {
                datasets = getDatasetNames(programSwitches, gscnvIndex, programSwitches.indexOf("\n-", gscnvIndex) != -1 ? programSwitches.indexOf("\n-", gscnvIndex) : programSwitches.length()).toArray(new String[0]);
                if (datasets != null && datasets.length > 0) pollToPollConversionDataset = datasets[0];
            }
            if (gspwIndex != -1) {
                datasets = getDatasetNames(programSwitches, gspwIndex, programSwitches.indexOf("\n-", gspwIndex) != -1 ? programSwitches.indexOf("\n-", gspwIndex) : programSwitches.length()).toArray(new String[0]);
                if (datasets != null && datasets.length > 0) speciationProfileWeightDatasets = datasets;
            }
            if (gsrefIndex != -1) {
                datasets = getDatasetNames(programSwitches, gsrefIndex, programSwitches.indexOf("\n-", gsrefIndex) != -1 ? programSwitches.indexOf("\n-", gsrefIndex) : programSwitches.length()).toArray(new String[0]);
                if (datasets != null && datasets.length > 0) speciationCrossReferenceDatasets = datasets;
            }
            summaryType = getSummaryType(programSwitches, sumTypeIndex, programSwitches.indexOf("\n-", sumTypeIndex) != -1 ? programSwitches.indexOf("\n-", sumTypeIndex) : programSwitches.length());

            
            String errors = "";

            if (capInventory == null) 
                errors = "Missing CAP inventory. ";
            if (hapInventory == null) 
                errors += "Missing HAP inventory. ";
            if (speciationToolSpecieInfoDataset == null) 
                errors += "Missing Speciation Tool Gas Profiles Dataset. ";
            if (pollToPollConversionDataset == null) 
                errors += "Missing Pollutant-To-Pollutant Conversion Dataset. ";
            if (speciationProfileWeightDatasets == null || speciationProfileWeightDatasets.length == 0) 
                errors += "Missing Speciation Profile Weight Dataset(s). ";
            if (speciationCrossReferenceDatasets == null || speciationCrossReferenceDatasets.length == 0) 
                errors += "Missing Speciation Cross Reference Dataset(s). ";
            if (summaryType == null || summaryType.trim().length() == 0) 
                errors += "Missing summary type value. ";
            
            if (errors.length() > 0) 
                throw new EmfException(errors);
        
        }else if (QAStep.MultiInvRepProgram.equalsIgnoreCase(program.getSelectedItem().toString())) {
            if (!(programSwitches.trim().equals("")) && invIndex != -1 
                    && emiIndex !=-1 && invTableIndex !=-1 && sumTypeIndex !=-1) 
                 check =true; 
             else 
                 check = false; 
               
             if (!check)
                 throw new EmfException(" Inventories, summary type are needed "); 
        
        } else if (QAStep.avgDayToAnnualProgram.equals(program.getSelectedItem())) {
            // if argument is empty, return false
            if (!(programSwitches.trim().equals("")) && invIndex != -1) {
                getInventories(programSwitches, 0, programSwitches.length());
                if (!(inventories.length > 0))
                    check = false;
            } else
                check = false;
            if (!check)
                throw new EmfException (" Inventories are needed ");
        } else if (QAStep.sqlProgram.equals(program.getSelectedItem())) {
//            if (!programSwitches.trim().toUpperCase().startsWith("SELECT ")) {
//                check = false;
//                throw new EmfException(" SQL is not start with SELECT ");
//            }
        }else if (QAStep.MultiInvDifRepProgram.equalsIgnoreCase(program.getSelectedItem().toString())
                || QAStep.CompareControlStrategies.equalsIgnoreCase(program.getSelectedItem().toString())){
            check = checkMultiInvDiff(programSwitches) ;
            if (!check)
                throw new EmfException (" Both base and compare inventories are needed ");
        } else if (QAStep.createMoEmisByCountyFromAnnEmisProgram.equalsIgnoreCase(program.getSelectedItem().toString())) {
            //
        }
        return check;
    }
    
    private boolean checkMultiInvDiff(String programSwitches) throws EmfException{
        int baseIndex = programSwitches.indexOf(QAStep.invBaseTag);
        int compareIndex = programSwitches.indexOf(QAStep.invCompareTag);
        int invTableIndex = programSwitches.indexOf(QAStep.invTableTag);
        int sumTypeIndex = programSwitches.indexOf(QAStep.summaryTypeTag);
        if (!(programSwitches.trim().equals("")) 
                && baseIndex != -1 && compareIndex != -1
                && sumTypeIndex != -1) {
                getBaseInventories(programSwitches, 0, compareIndex);
                getCompareInventories(programSwitches, compareIndex, invTableIndex);
                getInventoryTable(programSwitches, invTableIndex, sumTypeIndex);
                getSummaryType(programSwitches, sumTypeIndex);
                return true; 
        }
        return false; 
    }
    
    private void checkExportFolder() throws EmfException{
        if (exportFolder.getText().trim().isEmpty())
            throw new EmfException (" Please specify the export folder. ");
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

    private void getInventories(String programSwitches, int beginIndex, int endIndex) throws EmfException {
        List<EmfDataset> inventoryList= new ArrayList<EmfDataset>();
        inventoryList = getDatasets(programSwitches, beginIndex, endIndex);
        inventories = inventoryList.toArray(new EmfDataset[inventoryList.size()]);

    }
    
    private List<EmfDataset> getDatasets(String programSwitches, int beginIndex, int endIndex) throws EmfException{
        List<EmfDataset> inventoryList = new ArrayList<EmfDataset>();
        String nextDataset = "";
        String inventoriesString = "";
        // get the part of the arguments starting with -inventories or -inventories_base(compare)
        
        inventoriesString = programSwitches.substring(beginIndex, endIndex);
        StringTokenizer tokenizer2 = new StringTokenizer(inventoriesString, "\n");
        tokenizer2.nextToken(); // skip the flag
        EmfDataset ds = null;

        while (tokenizer2.hasMoreTokens()) {
            try {
                nextDataset = tokenizer2.nextToken().trim();
                //System.out.println("----"+ nextDataset);
                if (!nextDataset.isEmpty())
                    ds = presenter.getDataset(nextDataset);
                if ( ds !=null) 
                    inventoryList.add(presenter.getDataset(nextDataset));
                else
                    throw new EmfException("Could not get dataset --" + nextDataset);
            } catch (EmfException ex) {
                 throw new EmfException(ex.getMessage());
            }
        }
        return inventoryList;
    }

    private List<String> getDatasetNames(String programSwitches, int beginIndex, int endIndex){
        List<String> inventoryList = new ArrayList<String>();
        String nextDataset = "";
        String datasetsString = "";
        // get the part of the arguments starting with -inventories or -inventories_base(compare)
        
        datasetsString = programSwitches.substring(beginIndex, endIndex);
        StringTokenizer tokenizer2 = new StringTokenizer(datasetsString, "\n");
        tokenizer2.nextToken(); // skip the flag

        while (tokenizer2.hasMoreTokens()) {
            nextDataset = tokenizer2.nextToken().trim();
            //System.out.println("----"+ nextDataset);
            if (!nextDataset.isEmpty())
                inventoryList.add(nextDataset);
        }
        return inventoryList;
    }

    private void getInventoryTable(String programSwitches, int beginIndex, int endIndex) throws EmfException {
        List<EmfDataset> invTableList= new ArrayList<EmfDataset>();
        invTableList = getDatasets(programSwitches, beginIndex, endIndex);
        invTables = invTableList.toArray(new EmfDataset[invTableList.size()]);
    }
    
    private void getBaseInventories(String programSwitches, int beginIndex, int endIndex) throws EmfException {
        List<EmfDataset> baseInvList= new ArrayList<EmfDataset>();
        baseInvList = getDatasets(programSwitches, beginIndex, endIndex);
        invBase = baseInvList.toArray(new EmfDataset[baseInvList.size()]);
    }
    
    private void getCompareInventories(String programSwitches, int beginIndex, int endIndex) throws EmfException {
        List<EmfDataset> compareInvList= new ArrayList<EmfDataset>();
        compareInvList = getDatasets(programSwitches, beginIndex, endIndex);
        invCompare = compareInvList.toArray(new EmfDataset[compareInvList.size()]);
    }
    
    private void getEmissionType(String programSwitches, int emiIndex, int sumTypeIndex) {
        String emissionTypeToken = "";
        // return if no summary type 
        emissionTypeToken = programSwitches.substring(emiIndex, sumTypeIndex);
        StringTokenizer tokenizer3 = new StringTokenizer(emissionTypeToken);
        tokenizer3.nextToken(); // skip the -emisionType flag

        if (tokenizer3.hasMoreTokens())
            emissionType = tokenizer3.nextToken().trim();
    }
    
    private void getSummaryType(String programSwitches, int sumTypeIndex) {
        String summaryTypeToken = "";
        // return if no summary type 
        if (sumTypeIndex == -1) {
            summaryType = "";
            return;
        }
        summaryTypeToken = programSwitches.substring(sumTypeIndex);
        StringTokenizer tokenizer3 = new StringTokenizer(summaryTypeToken);
        tokenizer3.nextToken(); // skip the -summaryType flag

        if (tokenizer3.hasMoreTokens())
            summaryType = tokenizer3.nextToken().trim();
    }

    private String getSummaryType(String programSwitches, int beginIndex, int endIndex) {
        String summaryType = "";
        String summaryTypeString = "";
        // get the part of the arguments starting with -inventories or -inventories_base(compare)
        
        if (beginIndex != -1) {
            summaryTypeString = programSwitches.substring(beginIndex, endIndex);
            StringTokenizer tokenizer2 = new StringTokenizer(summaryTypeString, "\n");
            tokenizer2.nextToken(); // skip the flag

            while (tokenizer2.hasMoreTokens()) {
                summaryType = tokenizer2.nextToken().trim();
            }
        }
        return summaryType;
    }

    private Button exportButton() {
        Button export = new ExportButton("Export", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clear();
                doExport();
            }
        });
        export.setToolTipText("Export Results to CSV or Shapefile");
        return export;
    }

    private Button refreshButton() {
        Button refresh = new Button("Refresh", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clear();
                    resetRunStatus(presenter.getStepResult(step));
                } catch (Exception ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        });
        refresh.setMnemonic('R');

        return refresh;
    }

    private Button setButton() {
        Button export = new Button("Set", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clear();
                saveButton.setEnabled(true);
                runButton.setEnabled(true);
                if (QAStep.avgDaySummaryProgram.equalsIgnoreCase(program.getSelectedItem().toString())
                        || QAStep.fireDataSummaryProgram.equalsIgnoreCase(program.getSelectedItem().toString())
                        || QAStep.MultiInvSumProgram.equalsIgnoreCase(program.getSelectedItem().toString())){
                    showAvgDaySummaryWindow();
                }else if (QAStep.MultiInvRepProgram.equalsIgnoreCase(program.getSelectedItem().toString())) {
                    showColumnSummaryWindow();
                } else if (QAStep.compareVOCSpeciationWithHAPInventoryProgram.equalsIgnoreCase(program.getSelectedItem().toString())) {
                    showCompareCAPHAPInventoriesWindow();
                } else if (QAStep.avgDayToAnnualProgram.equalsIgnoreCase(program.getSelectedItem().toString())) {
                    showAvgDayToAnnualWindow();
                } else if (QAStep.MultiInvDifRepProgram.equalsIgnoreCase(program.getSelectedItem().toString())){
                    showMultiInvDiffWindow();
                } else if (QAStep.COMPARE_DATASETS_PROGRAM.equalsIgnoreCase(program.getSelectedItem().toString())){
                    try {
                        showCompareDatasetsWindow();
                    } catch (EmfException e1) {
                        // NOTE Auto-generated catch block
                        e1.printStackTrace();
                    }
                } else if (QAStep.COMPARE_DATASET_FIELDS_PROGRAM.equalsIgnoreCase(program.getSelectedItem().toString())){
                    try {
                        showCompareDatasetFieldsWindow();
                    } catch (EmfException e1) {
                        // NOTE Auto-generated catch block
                        e1.printStackTrace();
                    }
                } else if (QAStep.MERGE_IN_SUPPORTING_DATA.equalsIgnoreCase(program.getSelectedItem().toString())){
                    try {
                        showEnhanceFlatFile2010PointSettingWindows();
                    } catch (EmfException e1) {
                        // NOTE Auto-generated catch block
                        e1.printStackTrace();
                    }
                } else if (QAStep.CompareControlStrategies.equalsIgnoreCase(program.getSelectedItem().toString())){
                    showMultiInvDiffWindow();
                } else if (QAStep.createMoEmisByCountyFromAnnEmisProgram.equalsIgnoreCase(program.getSelectedItem().toString())){
                    showCreateMoEmisByCountyFromAnnEmisWindow();
                } else if (QAStep.compareAnnStateSummaryProgram.equalsIgnoreCase(program.getSelectedItem().toString())){
                    showCompareAnnualStateSummariesWindow();
                } else if (QAStep.smokeOutputAnnStateSummaryCrosstabProgram.equalsIgnoreCase(program.getSelectedItem().toString())){
                    showAnnualStateSummariesCrosstabWindow();
                } else if (QAStep.ecControlScenarioProgram.equalsIgnoreCase(program.getSelectedItem().toString())) {
                    showECControlScenarioWindow();
                }else{
                    doSetWindow();
                }
            }
        });
        return export;
    }



    private void showMultiInvDiffWindow() {
        // When there is no data in window, set button causes new window to pop up,
        // with the warning message to also show up. When data in window is invalid, a new window still
        // pops up, but with a different warning message.
        // Also change the window name to EditQASetArgumentsWindow
        invBase = null;
        invCompare = null;
        invTables = null;
        summaryType = "";

        String programSwitches = "";
        programSwitches = programArguments.getText();
        String programVal = program.getSelectedItem().toString();
        
        int baseIndex = programSwitches.indexOf(QAStep.invBaseTag);
        int compareIndex = programSwitches.indexOf(QAStep.invCompareTag);
        int invTableIndex = programSwitches.indexOf(QAStep.invTableTag);
        int sumTypeIndex = programSwitches.indexOf(QAStep.summaryTypeTag);
        if (!(programSwitches.trim().equals("")) 
                && baseIndex != -1 && compareIndex != -1
                && invTableIndex != -1 && sumTypeIndex != -1  ) {
            try {
                getBaseInventories(programSwitches, 0, compareIndex);
            } catch (EmfException e) {
                System.out.println(e.getMessage());
                messagePanel.setError(e.getMessage());
            }
            try{
                getCompareInventories(programSwitches, compareIndex, invTableIndex);
                getInventoryTable(programSwitches, invTableIndex, sumTypeIndex);
                getSummaryType(programSwitches, sumTypeIndex);
            } catch (EmfException e) {
                //e.printStackTrace();
                messagePanel.setError(e.getMessage());
                
//            }finally {
//                EditMultiInvDiffWindow view = new EditMultiInvDiffWindow(desktopManager, programVal, session, invBase, invCompare, invTables,
//                        summaryType);
//                EditQAEmissionsPresenter presenter = new EditQAEmissionsPresenter(view, this);
//                presenter.display(origDataset, step);
            }
        }
        if (QAStep.CompareControlStrategies.equalsIgnoreCase(program.getSelectedItem().toString())){
            EditControlDetailedDiffWindow view = new EditControlDetailedDiffWindow(desktopManager, programVal, session, invBase, invCompare, summaryType);
            EditQAEmissionsPresenter presenter = new EditQAEmissionsPresenter(view, this, session);
            presenter.display(origDataset, step);
        }else{
            EditMultiInvDiffWindow view = new EditMultiInvDiffWindow(desktopManager, programVal, session, invBase, invCompare, invTables,
                    summaryType);
            EditQAEmissionsPresenter presenter = new EditQAEmissionsPresenter(view, this, session);
            presenter.display(origDataset, step);
        }
    }
    
    protected void showEnhanceFlatFile2010PointSettingWindows() throws EmfException{
        // When there is no data in window, set button causes new window to pop up,
        // with the warning message to also show up. When data in window is invalid, a new window still
        // pops up, but with a different warning message.
        // Also change the window name to EditQASetArgumentsWindow
        
        //flatFile2010PointInv = null;
        //supportingSmokeFlatFileInv = null;
        
        flatFile2010PointInv = null;
        supportingSmokeFlatFileInv = null;
        supportingFlatFileInv2 = null;

        String programSwitches = "";
        programSwitches = programArguments.getText();
        String programVal = program.getSelectedItem().toString();
        String[] arguments;

        String[] ff10pTokens = new String[] {};
        String[] ssffTokens = new String[] {};
        String[] facTokens = new String[] {};
        
        List<DatasetVersion> ff10pDatasetList = new ArrayList<DatasetVersion>();
        List<DatasetVersion> ssffDatasetList = new ArrayList<DatasetVersion>();
        List<DatasetVersion> facDatasetList = new ArrayList<DatasetVersion>();

        int indexFF10P = programSwitches.indexOf(QAStep.FF10P_TAG);
        int indexSSFF = programSwitches.indexOf(QAStep.SSFF_TAG);
        int indexFAC = programSwitches.indexOf(QAStep.FAC_TAG);
        int indexMultiNEI = programSwitches.indexOf(QAStep.MANYNEIID_TAG);
        int indexMultiFRS = programSwitches.indexOf(QAStep.MANYFRS_TAG);
        int indexWhereFilter = programSwitches.indexOf(QAStep.WHERE_FILTER_TAG);

        if (indexFF10P != -1) {
            arguments = parseSwitchArguments(programSwitches, indexFF10P, programSwitches.indexOf("\n-", indexFF10P) != -1 ? programSwitches.indexOf("\n-", indexFF10P) : programSwitches.length());
            if (arguments != null && arguments.length > 0) ff10pTokens = arguments;
            for (String datasetVersion : ff10pTokens) {
                String[] datasetVersionToken = new String[] {};
                if (!datasetVersion.equals("$DATASET")) { 
                    datasetVersionToken = datasetVersion.split("\\|");
                } else {
                    EmfDataset qaStepDataset = presenter.getDataset(step.getDatasetId());
                    datasetVersionToken = new String[] { qaStepDataset.getName(), qaStepDataset.getDefaultVersion() + "" };
                }

                EmfDataset dataset = presenter.getDataset(datasetVersionToken[0]);
                //make sure dataset exists
                if (dataset == null)
                    break;
                Version version = presenter.version(dataset.getId(), Integer.parseInt(datasetVersionToken[1]));
                //make sure version exists
                if (version == null)
                    break;
                ff10pDatasetList.add(new DatasetVersion(dataset, version));
            }
        }
        if (indexSSFF != -1) {
            arguments = parseSwitchArguments(programSwitches, indexSSFF, programSwitches.indexOf("\n-", indexSSFF) != -1 ? programSwitches.indexOf("\n-", indexSSFF) : programSwitches.length());
            if (arguments != null && arguments.length > 0) ssffTokens = arguments;
            for (String datasetVersion : ssffTokens) {
                String[] datasetVersionToken = new String[] {};
                if (!datasetVersion.equals("$DATASET")) { 
                    datasetVersionToken = datasetVersion.split("\\|");
                } else {
                    EmfDataset qaStepDataset = presenter.getDataset(step.getDatasetId());
                    datasetVersionToken = new String[] { qaStepDataset.getName(), qaStepDataset.getDefaultVersion() + "" };
                }
                EmfDataset dataset = presenter.getDataset(datasetVersionToken[0]);
                //make sure dataset exists
                if (dataset == null)
                    break;

                Version version = presenter.version(dataset.getId(), Integer.parseInt(datasetVersionToken[1]));
                //make sure version exists
                if (version == null)
                    break;

                ssffDatasetList.add(new DatasetVersion(dataset, version));
            }
        }
        if (indexFAC != -1) {
            arguments = parseSwitchArguments(programSwitches, indexFAC, programSwitches.indexOf("\n-", indexFAC) != -1 ? programSwitches.indexOf("\n-", indexFAC) : programSwitches.length());
            if (arguments != null && arguments.length > 0) facTokens = arguments;
            for (String datasetVersion : facTokens) {
                String[] datasetVersionToken = new String[] {};
                if (!datasetVersion.equals("$DATASET")) { 
                    datasetVersionToken = datasetVersion.split("\\|");
                } else {
                    EmfDataset qaStepDataset = presenter.getDataset(step.getDatasetId());
                    datasetVersionToken = new String[] { qaStepDataset.getName(), qaStepDataset.getDefaultVersion() + "" };
                }
                EmfDataset dataset = presenter.getDataset(datasetVersionToken[0]);
                //make sure dataset exists
                if (dataset == null)
                    break;

                Version version = presenter.version(dataset.getId(), Integer.parseInt(datasetVersionToken[1]));
                //make sure version exists
                if (version == null)
                    break;

                facDatasetList.add(new DatasetVersion(dataset, version));
            }
        }
        
        boolean boolMultiNEI = false;
        boolean boolMultiFRS = false;
        String multiNEI = (indexMultiNEI != -1 ? programSwitches.substring(indexMultiNEI + MANYNEIID_TAG.length() + 1, programSwitches.indexOf("\n-", indexMultiNEI) != -1 ? programSwitches.indexOf("\n-", indexMultiNEI) : programSwitches.length()) : "");
        if ("TRUE".equals( multiNEI.trim().toUpperCase())) {
            boolMultiNEI = true;
        }
        String multiFRS = (indexMultiFRS != -1 ? programSwitches.substring(indexMultiFRS + MANYFRS_TAG.length() + 1, programSwitches.indexOf("\n-", indexMultiFRS) != -1 ? programSwitches.indexOf("\n-", indexMultiFRS) : programSwitches.length()) : "");
        if ("TRUE".equals( multiFRS.trim().toUpperCase())) {
            boolMultiFRS = true;
        }
        
        EnhanceFlatFile2010PointSettingWindows view = new EnhanceFlatFile2010PointSettingWindows(desktopManager, programVal, session,
                ff10pDatasetList.toArray(new DatasetVersion[0]), 
                ssffDatasetList.toArray(new DatasetVersion[0]),
                facDatasetList.toArray(new DatasetVersion[0]),
                boolMultiNEI, 
                boolMultiFRS, 
                (indexWhereFilter != -1 && (indexWhereFilter + WHERE_FILTER_TAG.length() + 1) < (programSwitches.indexOf("\n-", indexWhereFilter) != -1 ? programSwitches.indexOf("\n-", indexWhereFilter) : programSwitches.length()) ? programSwitches.substring(indexWhereFilter + WHERE_FILTER_TAG.length() + 1, programSwitches.indexOf("\n-", indexWhereFilter) != -1 ? programSwitches.indexOf("\n-", indexWhereFilter) : programSwitches.length()) : "")
        );
        EditQAEmissionsPresenter presenter = new EditQAEmissionsPresenter(view, this, session);
        presenter.display(origDataset, step);        
    }    

    private void showCompareDatasetsWindow() throws EmfException {
        
/*sample program arguments        

-base
ptipm_cap2005v2_nc_sc_va|0
-compare
$DATASET
-groupby
scc
substring(fips,1,2)
-aggregate
ann_emis
avd_emis
-matching
substring(fips,1,2)=substring(region_cd,1,2)
scc=scc_code
ann_emis=emis_ann
avd_emis=emis_avd
*/
        
        // When there is no data in window, set button causes new window to pop up,
        // with the warning message to also show up. When data in window is invalid, a new window still
        // pops up, but with a different warning message.
        // Also change the window name to EditQASetArgumentsWindow
        invBase = null;
        invCompare = null;
        invTables = null;
        summaryType = "";

        String programSwitches = "";
        programSwitches = programArguments.getText();
        String programVal = program.getSelectedItem().toString();
        String[] arguments;

        String[] baseTokens = new String[] {};
        String[] compareTokens = new String[] {};
        
        List<DatasetVersion> baseDatasetList = new ArrayList<DatasetVersion>();
        List<DatasetVersion> compareDatasetList = new ArrayList<DatasetVersion>();

        int indexBase = programSwitches.indexOf(BASE_TAG);
        int indexCompare = programSwitches.indexOf(COMPARE_TAG);
        int indexGroupBy = programSwitches.indexOf(GROUP_BY_EXPRESSIONS_TAG);
        int indexAggregate = programSwitches.indexOf(AGGREGATE_EXPRESSIONS_TAG);
        int indexMatching = programSwitches.indexOf(MATCHING_EXPRESSIONS_TAG);
        int indexJoin     = programSwitches.indexOf(JOIN_TYPE_TAG);
        int indexWhereFilter = programSwitches.indexOf(WHERE_FILTER_TAG);
        int indexBSuffix = programSwitches.indexOf(BASE_SUFFIX_TAG);
        int indexCSuffix = programSwitches.indexOf(COMPARE_SUFFIX_TAG);

        if (indexBase != -1) {
            arguments = parseSwitchArguments(programSwitches, indexBase, programSwitches.indexOf("\n-", indexBase) != -1 ? programSwitches.indexOf("\n-", indexBase) : programSwitches.length());
            if (arguments != null && arguments.length > 0) baseTokens = arguments;
            for (String datasetVersion : baseTokens) {
                String[] datasetVersionToken = new String[] {};
                if (!datasetVersion.equals("$DATASET")) { 
                    datasetVersionToken = datasetVersion.split("\\|");
                } else {
                    EmfDataset qaStepDataset = presenter.getDataset(step.getDatasetId());
                    datasetVersionToken = new String[] { qaStepDataset.getName(), qaStepDataset.getDefaultVersion() + "" };
                }

                EmfDataset dataset = presenter.getDataset(datasetVersionToken[0]);
                //make sure dataset exists
                if (dataset == null)
                    break;
//                    throw new EmfException("Dataset, " + datasetVersionToken[0] + ", doesn't exist.");
//                datasetVersion.setDataset(dataset);
                Version version = presenter.version(dataset.getId(), Integer.parseInt(datasetVersionToken[1]));
                //make sure version exists
                if (version == null)
                    break;
//                    throw new EmfException("Version, " + datasetVersionToken[0] + " - " + datasetVersion.getDatasetVersion() + ", doesn't exists.");
//                datasetVersion.setVersion(version);

                baseDatasetList.add(new DatasetVersion(dataset, version));
            }
        }
        if (indexCompare != -1) {
            arguments = parseSwitchArguments(programSwitches, indexCompare, programSwitches.indexOf("\n-", indexCompare) != -1 ? programSwitches.indexOf("\n-", indexCompare) : programSwitches.length());
            if (arguments != null && arguments.length > 0) compareTokens = arguments;
            for (String datasetVersion : compareTokens) {
                String[] datasetVersionToken = new String[] {};
                if (!datasetVersion.equals("$DATASET")) { 
                    datasetVersionToken = datasetVersion.split("\\|");
                } else {
                    EmfDataset qaStepDataset = presenter.getDataset(step.getDatasetId());
                    datasetVersionToken = new String[] { qaStepDataset.getName(), qaStepDataset.getDefaultVersion() + "" };
                }
                EmfDataset dataset = presenter.getDataset(datasetVersionToken[0]);
                //make sure dataset exists
                if (dataset == null)
                    break;
//                    throw new EmfException("Dataset, " + datasetVersionToken[0] + ", doesn't exist.");
//                datasetVersion.setDataset(dataset);
                Version version = presenter.version(dataset.getId(), Integer.parseInt(datasetVersionToken[1]));
                //make sure version exists
                if (version == null)
                    break;
//                    throw new EmfException("Version, " + datasetVersionToken[0] + " - " + datasetVersion.getDatasetVersion() + ", doesn't exists.");
//                datasetVersion.setVersion(version);

                compareDatasetList.add(new DatasetVersion(dataset, version));
            }
        }

        CompareDatasetsQAProgamWindow view = new CompareDatasetsQAProgamWindow(desktopManager, programVal, session,
                baseDatasetList.toArray(new DatasetVersion[0]), compareDatasetList.toArray(new DatasetVersion[0]),

//                programSwitches.substring(indexGroupBy + GROUP_BY_EXPRESSIONS_TAG.length() + 1, programSwitches.indexOf("\n-", indexGroupBy) != -1 ? programSwitches.indexOf("\n-", indexGroupBy) : programSwitches.length()), 
//                programSwitches.substring(indexAggregate + AGGREGATE_EXPRESSIONS_TAG.length() + 1, programSwitches.indexOf("\n-", indexAggregate) != -1 ? programSwitches.indexOf("\n-", indexAggregate) : programSwitches.length()), 
//                programSwitches.substring(indexMatching + MATCHING_EXPRESSIONS_TAG.length() + 1, programSwitches.indexOf("\n-", indexMatching) != -1 ? programSwitches.indexOf("\n-", indexMatching) : programSwitches.length())

                (indexGroupBy != -1 ? programSwitches.substring(indexGroupBy + GROUP_BY_EXPRESSIONS_TAG.length() + 1, programSwitches.indexOf("\n-", indexGroupBy) != -1 ? programSwitches.indexOf("\n-", indexGroupBy) : programSwitches.length()) : ""), 
                (indexAggregate != -1 ? programSwitches.substring(indexAggregate + AGGREGATE_EXPRESSIONS_TAG.length() + 1, programSwitches.indexOf("\n-", indexAggregate) != -1 ? programSwitches.indexOf("\n-", indexAggregate) : programSwitches.length()) : ""), 
                (indexMatching != -1  && (indexMatching + MATCHING_EXPRESSIONS_TAG.length() + 1) < (programSwitches.indexOf("\n-", indexMatching) != -1 ? programSwitches.indexOf("\n-", indexMatching) : programSwitches.length()) ? programSwitches.substring(indexMatching + MATCHING_EXPRESSIONS_TAG.length() + 1, programSwitches.indexOf("\n-", indexMatching) != -1 ? programSwitches.indexOf("\n-", indexMatching) : programSwitches.length()) : ""),
                (indexJoin != -1 && (indexJoin + JOIN_TYPE_TAG.length() + 1) < (programSwitches.indexOf("\n-", indexJoin) != -1 ? programSwitches.indexOf("\n-", indexJoin) : programSwitches.length()) ? programSwitches.substring(indexJoin + JOIN_TYPE_TAG.length() + 1, programSwitches.indexOf("\n-", indexJoin) != -1 ? programSwitches.indexOf("\n-", indexJoin) : programSwitches.length()) : ""),
                (indexWhereFilter != -1 && (indexWhereFilter + WHERE_FILTER_TAG.length() + 1) < (programSwitches.indexOf("\n-", indexWhereFilter) != -1 ? programSwitches.indexOf("\n-", indexWhereFilter) : programSwitches.length()) ? programSwitches.substring(indexWhereFilter + WHERE_FILTER_TAG.length() + 1, programSwitches.indexOf("\n-", indexWhereFilter) != -1 ? programSwitches.indexOf("\n-", indexWhereFilter) : programSwitches.length()) : ""),
                (indexBSuffix != -1 && (indexBSuffix + BASE_SUFFIX_TAG.length() + 1) < (programSwitches.indexOf("\n-", indexBSuffix) != -1 ? programSwitches.indexOf("\n-", indexBSuffix) : programSwitches.length()) ? programSwitches.substring(indexBSuffix + BASE_SUFFIX_TAG.length() + 1, programSwitches.indexOf("\n-", indexBSuffix) != -1 ? programSwitches.indexOf("\n-", indexBSuffix) : programSwitches.length()) : ""),
                (indexCSuffix != -1 && (indexCSuffix + COMPARE_SUFFIX_TAG.length() + 1) < (programSwitches.indexOf("\n-", indexCSuffix) != -1 ? programSwitches.indexOf("\n-", indexCSuffix) : programSwitches.length()) ? programSwitches.substring(indexCSuffix + COMPARE_SUFFIX_TAG.length() + 1, programSwitches.indexOf("\n-", indexCSuffix) != -1 ? programSwitches.indexOf("\n-", indexCSuffix) : programSwitches.length()) : "")
        );
        EditQAEmissionsPresenter presenter = new EditQAEmissionsPresenter(view, this, session);
        presenter.display(origDataset, step);
    }

    private void showCompareDatasetFieldsWindow() throws EmfException {
        
        /*sample program arguments        

        -base
        ptipm_cap2005v2_nc_sc_va|0
        -compare
        $DATASET
        -join
        scc
        substring(fips,1,2)
        -comparison
        ann_emis
        avd_emis
        -matching
        substring(fips,1,2)=substring(region_cd,1,2)
        scc=scc_code
        ann_emis=emis_ann
        avd_emis=emis_avd
        */
                
                // When there is no data in window, set button causes new window to pop up,
                // with the warning message to also show up. When data in window is invalid, a new window still
                // pops up, but with a different warning message.
                // Also change the window name to EditQASetArgumentsWindow
                invBase = null;
                invCompare = null;
                invTables = null;
                summaryType = "";

                String programSwitches = "";
                programSwitches = programArguments.getText();
                String programVal = program.getSelectedItem().toString();
                String[] arguments;

                String[] baseTokens = new String[] {};
                String[] compareTokens = new String[] {};
                
                List<DatasetVersion> baseDatasetList = new ArrayList<DatasetVersion>();
                List<DatasetVersion> compareDatasetList = new ArrayList<DatasetVersion>();

                int indexBase = programSwitches.indexOf(BASE_TAG);
                int indexCompare = programSwitches.indexOf(COMPARE_TAG);
                int indexJoin = programSwitches.indexOf(JOIN_EXPRESSIONS_TAG + "\n");
                int indexComparison = programSwitches.indexOf(COMPARISON_EXPRESSIONS_TAG);
                int indexMatching = programSwitches.indexOf(MATCHING_EXPRESSIONS_TAG);
                int indexJoinType     = programSwitches.indexOf(JOIN_TYPE_TAG + "\n");
                int indexWhereFilter = programSwitches.indexOf(WHERE_FILTER_TAG);
                int indexBSuffix = programSwitches.indexOf(BASE_SUFFIX_TAG);
                int indexCSuffix = programSwitches.indexOf(COMPARE_SUFFIX_TAG);

                if (indexBase != -1) {
                    arguments = parseSwitchArguments(programSwitches, indexBase, programSwitches.indexOf("\n-", indexBase) != -1 ? programSwitches.indexOf("\n-", indexBase) : programSwitches.length());
                    if (arguments != null && arguments.length > 0) baseTokens = arguments;
                    for (String datasetVersion : baseTokens) {
                        String[] datasetVersionToken = new String[] {};
                        if (!datasetVersion.equals("$DATASET")) { 
                            datasetVersionToken = datasetVersion.split("\\|");
                        } else {
                            EmfDataset qaStepDataset = presenter.getDataset(step.getDatasetId());
                            datasetVersionToken = new String[] { qaStepDataset.getName(), qaStepDataset.getDefaultVersion() + "" };
                        }

                        EmfDataset dataset = presenter.getDataset(datasetVersionToken[0]);
                        //make sure dataset exists
                        if (dataset == null)
                            break;
//                            throw new EmfException("Dataset, " + datasetVersionToken[0] + ", doesn't exist.");
//                        datasetVersion.setDataset(dataset);
                        Version version = presenter.version(dataset.getId(), Integer.parseInt(datasetVersionToken[1]));
                        //make sure version exists
                        if (version == null)
                            break;
//                            throw new EmfException("Version, " + datasetVersionToken[0] + " - " + datasetVersion.getDatasetVersion() + ", doesn't exists.");
//                        datasetVersion.setVersion(version);

                        baseDatasetList.add(new DatasetVersion(dataset, version));
                    }
                }
                if (indexCompare != -1) {
                    arguments = parseSwitchArguments(programSwitches, indexCompare, programSwitches.indexOf("\n-", indexCompare) != -1 ? programSwitches.indexOf("\n-", indexCompare) : programSwitches.length());
                    if (arguments != null && arguments.length > 0) compareTokens = arguments;
                    for (String datasetVersion : compareTokens) {
                        String[] datasetVersionToken = new String[] {};
                        if (!datasetVersion.equals("$DATASET")) { 
                            datasetVersionToken = datasetVersion.split("\\|");
                        } else {
                            EmfDataset qaStepDataset = presenter.getDataset(step.getDatasetId());
                            datasetVersionToken = new String[] { qaStepDataset.getName(), qaStepDataset.getDefaultVersion() + "" };
                        }
                        EmfDataset dataset = presenter.getDataset(datasetVersionToken[0]);
                        //make sure dataset exists
                        if (dataset == null)
                            break;
//                            throw new EmfException("Dataset, " + datasetVersionToken[0] + ", doesn't exist.");
//                        datasetVersion.setDataset(dataset);
                        Version version = presenter.version(dataset.getId(), Integer.parseInt(datasetVersionToken[1]));
                        //make sure version exists
                        if (version == null)
                            break;
//                            throw new EmfException("Version, " + datasetVersionToken[0] + " - " + datasetVersion.getDatasetVersion() + ", doesn't exists.");
//                        datasetVersion.setVersion(version);

                        compareDatasetList.add(new DatasetVersion(dataset, version));
                    }
                }

                CompareDatasetFieldsQAProgamWindow view = new CompareDatasetFieldsQAProgamWindow(desktopManager, programVal, session,
                        baseDatasetList.toArray(new DatasetVersion[0]), compareDatasetList.toArray(new DatasetVersion[0]),

//                        programSwitches.substring(indexGroupBy + GROUP_BY_EXPRESSIONS_TAG.length() + 1, programSwitches.indexOf("\n-", indexGroupBy) != -1 ? programSwitches.indexOf("\n-", indexGroupBy) : programSwitches.length()), 
//                        programSwitches.substring(indexAggregate + AGGREGATE_EXPRESSIONS_TAG.length() + 1, programSwitches.indexOf("\n-", indexAggregate) != -1 ? programSwitches.indexOf("\n-", indexAggregate) : programSwitches.length()), 
//                        programSwitches.substring(indexMatching + MATCHING_EXPRESSIONS_TAG.length() + 1, programSwitches.indexOf("\n-", indexMatching) != -1 ? programSwitches.indexOf("\n-", indexMatching) : programSwitches.length())

                        (indexJoin != -1 ? programSwitches.substring(indexJoin + JOIN_EXPRESSIONS_TAG.length() + 1, programSwitches.indexOf("\n-", indexJoin) != -1 ? programSwitches.indexOf("\n-", indexJoin) : programSwitches.length()) : ""), 
                        (indexComparison != -1 ? programSwitches.substring(indexComparison + COMPARISON_EXPRESSIONS_TAG.length() + 1, programSwitches.indexOf("\n-", indexComparison) != -1 ? programSwitches.indexOf("\n-", indexComparison) : programSwitches.length()) : ""), 
                        (indexMatching != -1  && (indexMatching + MATCHING_EXPRESSIONS_TAG.length() + 1) < (programSwitches.indexOf("\n-", indexMatching) != -1 ? programSwitches.indexOf("\n-", indexMatching) : programSwitches.length()) ? programSwitches.substring(indexMatching + MATCHING_EXPRESSIONS_TAG.length() + 1, programSwitches.indexOf("\n-", indexMatching) != -1 ? programSwitches.indexOf("\n-", indexMatching) : programSwitches.length()) : ""),
                        (indexJoinType != -1 && (indexJoinType + JOIN_TYPE_TAG.length() + 1) < (programSwitches.indexOf("\n-", indexJoinType) != -1 ? programSwitches.indexOf("\n-", indexJoinType) : programSwitches.length()) ? programSwitches.substring(indexJoinType + JOIN_TYPE_TAG.length() + 1, programSwitches.indexOf("\n-", indexJoinType) != -1 ? programSwitches.indexOf("\n-", indexJoinType) : programSwitches.length()) : ""),
                        (indexWhereFilter != -1 && (indexWhereFilter + WHERE_FILTER_TAG.length() + 1) < (programSwitches.indexOf("\n-", indexWhereFilter) != -1 ? programSwitches.indexOf("\n-", indexWhereFilter) : programSwitches.length()) ? programSwitches.substring(indexWhereFilter + WHERE_FILTER_TAG.length() + 1, programSwitches.indexOf("\n-", indexWhereFilter) != -1 ? programSwitches.indexOf("\n-", indexWhereFilter) : programSwitches.length()) : ""),
                        (indexBSuffix != -1 && (indexBSuffix + BASE_SUFFIX_TAG.length() + 1) < (programSwitches.indexOf("\n-", indexBSuffix) != -1 ? programSwitches.indexOf("\n-", indexBSuffix) : programSwitches.length()) ? programSwitches.substring(indexBSuffix + BASE_SUFFIX_TAG.length() + 1, programSwitches.indexOf("\n-", indexBSuffix) != -1 ? programSwitches.indexOf("\n-", indexBSuffix) : programSwitches.length()) : ""),
                        (indexCSuffix != -1 && (indexCSuffix + COMPARE_SUFFIX_TAG.length() + 1) < (programSwitches.indexOf("\n-", indexCSuffix) != -1 ? programSwitches.indexOf("\n-", indexCSuffix) : programSwitches.length()) ? programSwitches.substring(indexCSuffix + COMPARE_SUFFIX_TAG.length() + 1, programSwitches.indexOf("\n-", indexCSuffix) != -1 ? programSwitches.indexOf("\n-", indexCSuffix) : programSwitches.length()) : "")
                );
               
                EditQAEmissionsPresenter presenter = new EditQAEmissionsPresenter(view, this, session);
                presenter.display(origDataset, step);
    }

    private void showAvgDaySummaryWindow() {
        // When there is no data in window, set button causes new window to pop up,
        // with the warning message to also show up. When data in window is invalid, a new window still
        // pops up, but with a different warning message.
        // Also change the window name to EditQASetArgumentsWindow
        inventories = null;
        invTables = null;
        summaryType = "";

        String programSwitches = "";
        String programVal = program.getSelectedItem().toString();
        
        programSwitches = programArguments.getText();
        int invTableIndex = programSwitches.indexOf(QAStep.invTableTag);
        int invIndex = programSwitches.indexOf(QAStep.invTag);
        int sumTypeIndex = programSwitches.indexOf(QAStep.summaryTypeTag);
        if (!(programSwitches.trim().equals("")) 
                && invIndex != -1 && invTableIndex != -1 
                && sumTypeIndex != -1) {
            try {
                getInventories(programSwitches, 0, invTableIndex);
                getInventoryTable(programSwitches, invTableIndex, sumTypeIndex);
                getSummaryType(programSwitches, sumTypeIndex);
            } catch (EmfException e) {
                messagePanel.setError(e.getMessage());
            }finally{
                EditQAEmissionsWindow view = new EditQAEmissionsWindow(desktopManager, programVal, session, inventories, invTables,
                        summaryType);
                EditQAEmissionsPresenter presenter = new EditQAEmissionsPresenter(view, this, session);
                presenter.display(origDataset, step); 
            }
        }
        EditQAEmissionsWindow view = new EditQAEmissionsWindow(desktopManager, programVal, session, inventories, invTables,
                summaryType);
        EditQAEmissionsPresenter presenter = new EditQAEmissionsPresenter(view, this, session);
        presenter.display(origDataset, step);

    }
    
    private void showColumnSummaryWindow() {
        // When there is no data in window, set button causes new window to pop up,
        // with the warning message to also show up. When data in window is invalid, a new window still
        // pops up, but with a different warning message.
        // Also change the window name to EditQASetArgumentsWindow
        inventories = null;
        invTables = null;
        summaryType = "";
        emissionType =""; 

        String programSwitches = "";
        String programVal = program.getSelectedItem().toString();
        
        programSwitches = programArguments.getText();
        int invTableIndex = programSwitches.indexOf(QAStep.invTableTag);
        int invIndex = programSwitches.indexOf(QAStep.invTag);
        int emiIndex = programSwitches.indexOf(QAStep.emissionTypeTag);
        int sumTypeIndex = programSwitches.indexOf(QAStep.summaryTypeTag);
        if (!(programSwitches.trim().equals("")) 
                && invIndex != -1 && invTableIndex != -1 
                && emiIndex !=-1 && sumTypeIndex != -1) {
            try {
                getInventories(programSwitches, 0, invTableIndex);
                getInventoryTable(programSwitches, invTableIndex, emiIndex);
                getEmissionType(programSwitches, emiIndex, sumTypeIndex);
                //System.out.println("annual vs average " + emissionType);
                getSummaryType(programSwitches, sumTypeIndex);
            } catch (EmfException e) {
                messagePanel.setError(e.getMessage());
            }finally{
                EditQAEmissionsColumnBasedWindow view = new EditQAEmissionsColumnBasedWindow(desktopManager, programVal, session, inventories, invTables,
                        summaryType, emissionType);
                EditQAEmissionsPresenter presenter = new EditQAEmissionsPresenter(view, this, session);
                presenter.display(origDataset, step); 
            }
        }
        EditQAEmissionsColumnBasedWindow view = new EditQAEmissionsColumnBasedWindow(desktopManager, programVal, session, inventories, invTables,
                summaryType, emissionType);
        EditQAEmissionsPresenter presenter = new EditQAEmissionsPresenter(view, this, session);
        presenter.display(origDataset, step);

    }


    private void showAvgDayToAnnualWindow() {
        // When there is no data in window, set button causes new window to pop up,
        // with the warning message to also show up. When data in window is invalid, a new window still
        // pops up, but with a different warning message.
        String programSwitches = "";
        String programVal = program.getSelectedItem().toString();
        programSwitches = programArguments.getText();
        int invIndex = programSwitches.indexOf(QAStep.invTag);
        if (!(programSwitches.trim().equals("")) && invIndex != -1) {
            try {
                getInventories(programSwitches, 0, programSwitches.length());
            } catch (EmfException e) {
                messagePanel.setError(e.getMessage());
            }finally{
                EditQANonsummaryEmissionsWindow view = new EditQANonsummaryEmissionsWindow(desktopManager, programVal, session, inventories);
                EditQANonsummaryEmissionsPresenter presenter = new EditQANonsummaryEmissionsPresenter(view, this);
                presenter.display(origDataset, step); 
            }
        }
        EditQANonsummaryEmissionsWindow view = new EditQANonsummaryEmissionsWindow(desktopManager, programVal, session, inventories);
        EditQANonsummaryEmissionsPresenter presenter = new EditQANonsummaryEmissionsPresenter(view, this);
        presenter.display(origDataset, step);

    }
    
    private void showCompareCAPHAPInventoriesWindow() {
        // When there is no data in window, set button causes new window to pop up,
        // with the warning message to also show up. When data in window is invalid, a new window still
        // pops up, but with a different warning message.
        // Also change the window name to EditQASetArgumentsWindow
        invBase = null;
        invCompare = null;
        invTables = null;
        summaryType = "";

        String programVal = program.getSelectedItem().toString();
        String programSwitches = programArguments.getText();        
        try {
            int capIndex = programSwitches.indexOf("-cap");
            int hapIndex = programSwitches.indexOf("-hap");
            int gstsiIndex = programSwitches.indexOf("-gstsi");
            int gscnvIndex = programSwitches.indexOf("-gscnv");
            int gspwIndex = programSwitches.indexOf("-gspw");
            int gsrefIndex = programSwitches.indexOf("-gsref");
            int filterIndex = programSwitches.indexOf("-filter");
            int sumTypeIndex = programSwitches.indexOf(QAStep.summaryTypeTag);
            EmfDataset capInventory = null;
            EmfDataset hapInventory = null;
            EmfDataset speciationToolSpecieInfoDataset = null;
            EmfDataset pollToPollConversionDataset = null;
            EmfDataset [] speciationProfileWeightDatasets = null; 
            EmfDataset [] speciationCrossReferenceDatasets = null;
            EmfDataset[] datasets;
            String summaryType = "";
            String filter = "";

            if (capIndex != -1) {
                datasets = getDatasets(programSwitches, capIndex, programSwitches.indexOf("\n-", capIndex) != -1 ? programSwitches.indexOf("\n-", capIndex) : programSwitches.length()).toArray(new EmfDataset[0]);
                if (datasets != null && datasets.length > 0) capInventory = datasets[0];
            }
            if (hapIndex != -1) {
                datasets = getDatasets(programSwitches, hapIndex, programSwitches.indexOf("\n-", hapIndex) != -1 ? programSwitches.indexOf("\n-", hapIndex) : programSwitches.length()).toArray(new EmfDataset[0]);
                if (datasets != null && datasets.length > 0) hapInventory = datasets[0];
            }
            if (gstsiIndex != -1) {
                datasets = getDatasets(programSwitches, gstsiIndex, programSwitches.indexOf("\n-", gstsiIndex) != -1 ? programSwitches.indexOf("\n-", gstsiIndex) : programSwitches.length()).toArray(new EmfDataset[0]);
                if (datasets != null && datasets.length > 0) speciationToolSpecieInfoDataset = datasets[0];
            }
            if (gscnvIndex != -1) {
                datasets = getDatasets(programSwitches, gscnvIndex, programSwitches.indexOf("\n-", gscnvIndex) != -1 ? programSwitches.indexOf("\n-", gscnvIndex) : programSwitches.length()).toArray(new EmfDataset[0]);
                if (datasets != null && datasets.length > 0) pollToPollConversionDataset = datasets[0];
            }
            if (gspwIndex != -1) {
                datasets = getDatasets(programSwitches, gspwIndex, programSwitches.indexOf("\n-", gspwIndex) != -1 ? programSwitches.indexOf("\n-", gspwIndex) : programSwitches.length()).toArray(new EmfDataset[0]);
                if (datasets != null && datasets.length > 0) speciationProfileWeightDatasets = datasets;
            }
            if (gsrefIndex != -1) {
                datasets = getDatasets(programSwitches, gsrefIndex, programSwitches.indexOf("\n-", gsrefIndex) != -1 ? programSwitches.indexOf("\n-", gsrefIndex) : programSwitches.length()).toArray(new EmfDataset[0]);
                if (datasets != null && datasets.length > 0) speciationCrossReferenceDatasets = datasets;
            }
            filter = getSummaryType(programSwitches, filterIndex, programSwitches.indexOf("\n-", filterIndex) != -1 ? programSwitches.indexOf("\n-", filterIndex) : programSwitches.length());
            summaryType = getSummaryType(programSwitches, sumTypeIndex, programSwitches.indexOf("\n-", sumTypeIndex) != -1 ? programSwitches.indexOf("\n-", sumTypeIndex) : programSwitches.length());
            QACompareVOCSpeciationWithHAPInventoryWindow view = new QACompareVOCSpeciationWithHAPInventoryWindow(desktopManager, 
                programVal, 
                session, 
                capInventory, 
                hapInventory, 
                speciationToolSpecieInfoDataset, 
                pollToPollConversionDataset,
                speciationProfileWeightDatasets,
                speciationCrossReferenceDatasets,
                filter,
                summaryType);
            EditQAEmissionsPresenter presenter = new EditQAEmissionsPresenter(view, this, session);
            presenter.display(origDataset, step);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void showCreateMoEmisByCountyFromAnnEmisWindow() {
        // When there is no data in window, set button causes new window to pop up,
        // with the warning message to also show up. When data in window is invalid, a new window still
        // pops up, but with a different warning message.
        // Also change the window name to EditQASetArgumentsWindow
        invBase = null;
        invCompare = null;
        invTables = null;
        summaryType = "";

        String programVal = program.getSelectedItem().toString();
        String programSwitches = programArguments.getText();        
        try {
            int smkRptIndex = programSwitches.indexOf("-smkrpt");
            int temporalIndex = programSwitches.indexOf("-temporal");
            int yearIndex = programSwitches.indexOf("-year");
            EmfDataset temporal = null;
            EmfDataset [] smkRptDatasets = null; 
            EmfDataset[] datasets;
            String[] tokens = null;
            Integer year = null;

            if (temporalIndex != -1) {
                datasets = getDatasets(programSwitches, temporalIndex, programSwitches.indexOf("\n-", temporalIndex) != -1 ? programSwitches.indexOf("\n-", temporalIndex) : programSwitches.length()).toArray(new EmfDataset[0]);
                if (datasets != null && datasets.length > 0) temporal = datasets[0];
            }
            if (smkRptIndex != -1) {
                datasets = getDatasets(programSwitches, smkRptIndex, programSwitches.indexOf("\n-", smkRptIndex) != -1 ? programSwitches.indexOf("\n-", smkRptIndex) : programSwitches.length()).toArray(new EmfDataset[0]);
                if (datasets != null && datasets.length > 0) smkRptDatasets = datasets;
            }
            if (yearIndex != -1) {
                tokens = getDatasetNames(programSwitches, yearIndex, programSwitches.indexOf("\n-", yearIndex) != -1 ? programSwitches.indexOf("\n-", yearIndex) : programSwitches.length()).toArray(new String[0]);
                try {
                    if (tokens != null && tokens.length > 0) year = Integer.parseInt(tokens[0]);
                } catch (NumberFormatException ex) {
                    throw new EmfException("The year is not valid format, it must be a number. ");
                }
            }
            QACreateMoEmisByCountyFromAnnEmisWindow view = new QACreateMoEmisByCountyFromAnnEmisWindow(desktopManager, 
                programVal, 
                session, 
                temporal, 
                smkRptDatasets,
                year);
            EditQAEmissionsPresenter presenter = new EditQAEmissionsPresenter(view, this, session);
            presenter.display(origDataset, step);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void showCompareAnnualStateSummariesWindow() {
        // When there is no data in window, set button causes new window to pop up,
        // with the warning message to also show up. When data in window is invalid, a new window still
        // pops up, but with a different warning message.
        // Also change the window name to EditQASetArgumentsWindow
        invBase = null;
        invCompare = null;
        invTables = null;
        summaryType = "";

        String programVal = program.getSelectedItem().toString();
        String programSwitches = programArguments.getText();        
        try {
            int smkRptIndex = programSwitches.indexOf("-smkrpt");
            int invIndex = programSwitches.indexOf("-inv");
            int invTableIndex = programSwitches.indexOf("-invtable");
            int toleranceIndex = programSwitches.indexOf("-tolerance");
            int coStCyIndex = programSwitches.indexOf("-costcy");
            EmfDataset[] inventories = null;
            EmfDataset[] smkRptDatasets = null; 
            EmfDataset invTableDataset = null; 
            EmfDataset toleranceDataset = null; 
            EmfDataset coStCyDataset = null; 
            EmfDataset[] datasets;

            if (invIndex != -1) {
                datasets = getDatasets(programSwitches, invIndex, programSwitches.indexOf("\n-", invIndex) != -1 ? programSwitches.indexOf("\n-", invIndex) : programSwitches.length()).toArray(new EmfDataset[0]);
                if (datasets != null && datasets.length > 0) inventories = datasets;
            }
            if (smkRptIndex != -1) {
                datasets = getDatasets(programSwitches, smkRptIndex, programSwitches.indexOf("\n-", smkRptIndex) != -1 ? programSwitches.indexOf("\n-", smkRptIndex) : programSwitches.length()).toArray(new EmfDataset[0]);
                if (datasets != null && datasets.length > 0) smkRptDatasets = datasets;
            }
            if (invTableIndex != -1) {
                datasets = getDatasets(programSwitches, invTableIndex, programSwitches.indexOf("\n-", invTableIndex) != -1 ? programSwitches.indexOf("\n-", invTableIndex) : programSwitches.length()).toArray(new EmfDataset[0]);
                if (datasets != null && datasets.length > 0) invTableDataset = datasets[0];
            }
            if (toleranceIndex != -1) {
                datasets = getDatasets(programSwitches, toleranceIndex, programSwitches.indexOf("\n-", toleranceIndex) != -1 ? programSwitches.indexOf("\n-", toleranceIndex) : programSwitches.length()).toArray(new EmfDataset[0]);
                if (datasets != null && datasets.length > 0) toleranceDataset = datasets[0];
            }
            if (coStCyIndex != -1) {
                datasets = getDatasets(programSwitches, coStCyIndex, programSwitches.indexOf("\n-", coStCyIndex) != -1 ? programSwitches.indexOf("\n-", coStCyIndex) : programSwitches.length()).toArray(new EmfDataset[0]);
                if (datasets != null && datasets.length > 0) coStCyDataset = datasets[0];
            }
            QACompareAnnualStateSummariesWindow view = new QACompareAnnualStateSummariesWindow(desktopManager, 
                programVal, 
                session, 
                inventories, 
                smkRptDatasets,
                invTableDataset,
                toleranceDataset,
                coStCyDataset);
            EditQAEmissionsPresenter presenter = new EditQAEmissionsPresenter(view, this, session);
            presenter.display(origDataset, step);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void showECControlScenarioWindow() {
        String programVal = program.getSelectedItem().toString();
        String programSwitches = programArguments.getText();        
        try {
            int invIndex = programSwitches.indexOf(QAStep.invTag);
            int detailedResultIndex = programSwitches.indexOf(detailedResultTag);
            int gsproIndex = programSwitches.indexOf(gsproTag);
            int gsrefIndex = programSwitches.indexOf(gsrefTag);
            EmfDataset detailedResultDataset = null; 
            EmfDataset invDataset = null; 
            EmfDataset[] gsproDatasets = null; 
            EmfDataset[] gsrefDatasets = null; 
            EmfDataset[] datasets;

            if (invIndex != -1) {
                datasets = getDatasets(programSwitches, invIndex, programSwitches.indexOf("\n-", invIndex) != -1 ? programSwitches.indexOf("\n-", invIndex) : programSwitches.length()).toArray(new EmfDataset[0]);
                if (datasets != null && datasets.length > 0) invDataset = datasets[0];
            }
            if (detailedResultIndex != -1) {
                //check for the $TABLE[1] tag in the detailed_result tag
                String programSwitches2 = programSwitches.replace("$DATASET", origDataset.getName());
                datasets = getDatasets(programSwitches2, detailedResultIndex, programSwitches2.indexOf("\n-", detailedResultIndex) != -1 ? programSwitches2.indexOf("\n-", detailedResultIndex) : programSwitches2.length()).toArray(new EmfDataset[0]);
                if (datasets != null && datasets.length > 0) detailedResultDataset = datasets[0];
            }
            if (gsproIndex != -1) {
                datasets = getDatasets(programSwitches, gsproIndex, programSwitches.indexOf("\n-", gsproIndex) != -1 ? programSwitches.indexOf("\n-", gsproIndex) : programSwitches.length()).toArray(new EmfDataset[0]);
                if (datasets != null && datasets.length > 0) gsproDatasets = datasets;
            }
            if (gsrefIndex != -1) {
                datasets = getDatasets(programSwitches, gsrefIndex, programSwitches.indexOf("\n-", gsrefIndex) != -1 ? programSwitches.indexOf("\n-", gsrefIndex) : programSwitches.length()).toArray(new EmfDataset[0]);
                if (datasets != null && datasets.length > 0) gsrefDatasets = datasets;
            }

            
            if (invDataset == null && detailedResultDataset != null) {
                KeyVal[] keyVals = keyValFound(detailedResultDataset, "STRATEGY_INVENTORY_NAME");
                if (keyVals.length > 0) {
                    invDataset = presenter.getDataset(keyVals[0].getValue());
                }
            }

            
            QAECControlScenarioWindow view = new QAECControlScenarioWindow(desktopManager, 
                programVal, 
                session, 
                invDataset,
                detailedResultDataset,
                gsrefDatasets,
                gsproDatasets);
            EditQAECControlScenarioPresenter presenter = new EditQAECControlScenarioPresenter(view, this, session);
            presenter.display(origDataset, step);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private  KeyVal[] keyValFound(EmfDataset dataset, String keyword) {
        KeyVal[] keys = dataset.getKeyVals();
        List<KeyVal> list = new ArrayList<KeyVal>();
        
        for (KeyVal key : keys)
            if (key.getName().equalsIgnoreCase(keyword)) 
                list.add(key);
        
        return list.toArray(new KeyVal[0]);
    }

    private void showAnnualStateSummariesCrosstabWindow() {

        String programVal = program.getSelectedItem().toString();
        String programSwitches = programArguments.getText();        
        try {
            int smkRptIndex = programSwitches.indexOf(smkRptTag);
            int coStCyIndex = programSwitches.indexOf(coStCyTag);
            int pollListIndex = programSwitches.indexOf(pollListTag);
            int specieListIndex = programSwitches.indexOf(specieListTag);
            int exclPollIndex = programSwitches.indexOf(exclPollTag);
            int sortPollIndex = programSwitches.indexOf(sortPollTag);
            EmfDataset[] smkRptDatasets = null; 
            EmfDataset coStCyDataset = null; 
            String[] pollList = null; 
            String[] specieList = null; 
            String[] exclPollList = null; 
            String[] sortPollList = null; 
            EmfDataset[] datasets;
            String[] names;

            if (smkRptIndex != -1) {
                datasets = getDatasets(programSwitches, smkRptIndex, programSwitches.indexOf("\n-", smkRptIndex) != -1 ? programSwitches.indexOf("\n-", smkRptIndex) : programSwitches.length()).toArray(new EmfDataset[0]);
                if (datasets != null && datasets.length > 0) smkRptDatasets = datasets;
            }
            if (coStCyIndex != -1) {
                datasets = getDatasets(programSwitches, coStCyIndex, programSwitches.indexOf("\n-", coStCyIndex) != -1 ? programSwitches.indexOf("\n-", coStCyIndex) : programSwitches.length()).toArray(new EmfDataset[0]);
                if (datasets != null && datasets.length > 0) coStCyDataset = datasets[0];
            }
            if (pollListIndex != -1) {
                names = getDatasetNames(programSwitches, pollListIndex, programSwitches.indexOf("\n-", pollListIndex) != -1 ? programSwitches.indexOf("\n-", pollListIndex) : programSwitches.length()).toArray(new String[0]);
                if (names != null && names.length > 0) pollList = names;
            }
            if (specieListIndex != -1) {
                names = getDatasetNames(programSwitches, specieListIndex, programSwitches.indexOf("\n-", specieListIndex) != -1 ? programSwitches.indexOf("\n-", specieListIndex) : programSwitches.length()).toArray(new String[0]);
                if (names != null && names.length > 0) specieList = names;
            }
            if (exclPollIndex != -1) {
                names = getDatasetNames(programSwitches, exclPollIndex, programSwitches.indexOf("\n-", exclPollIndex) != -1 ? programSwitches.indexOf("\n-", exclPollIndex) : programSwitches.length()).toArray(new String[0]);
                if (names != null && names.length > 0) exclPollList = names;
            }
            if (sortPollIndex != -1) {
                names = getDatasetNames(programSwitches, sortPollIndex, programSwitches.indexOf("\n-", sortPollIndex) != -1 ? programSwitches.indexOf("\n-", sortPollIndex) : programSwitches.length()).toArray(new String[0]);
                if (names != null && names.length > 0) sortPollList = names;
            }
            QAAnnualStateSummariesCrosstabWindow view = new QAAnnualStateSummariesCrosstabWindow(desktopManager, 
                programVal, 
                session, 
                smkRptDatasets,
                coStCyDataset,
                pollList,
                specieList,
                exclPollList,
                sortPollList);
            EditQAAnnualStateSummariesCrosstabEmissionsPresenter presenter = new EditQAAnnualStateSummariesCrosstabEmissionsPresenter(view, this, session);
            presenter.display(origDataset, step);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void doSetWindow() {
        String argumentsText = programArguments.getText();
        EditQAArgumentsWindow view = new EditQAArgumentsWindow(desktopManager, argumentsText);
        EditQAArgumentsPresenter presenter = new EditQAArgumentsPresenter(view, this);
        presenter.display();
    }

    public void updateArgumentsTextArea(String text) {
        programArguments.clear();
        programArguments.setText(text);
    }

    public void updateInventories(Object[] retreivedInventories, Object[] retrievedInvTable,
            String summaryType) {
        clear();
        String datasetNames = "";
        datasetNames += getInvString(QAStep.invTag, retreivedInventories);
        datasetNames += getInvString(QAStep.invTableTag, retrievedInvTable);
        
        datasetNames += QAStep.summaryTypeTag + lineFeeder;
        if (summaryType.length() > 0)
            datasetNames += summaryType + lineFeeder ;
        updateArgumentsTextArea(datasetNames);

    }
    
    public void updateInventories(Object[] retreivedInventories, Object[] retrievedInvTable,
            String summaryType, String emissionType) {
        clear();
        String datasetNames = "";
        datasetNames += getInvString(QAStep.invTag, retreivedInventories);
        datasetNames += getInvString(QAStep.invTableTag, retrievedInvTable);
        
        datasetNames += QAStep.emissionTypeTag + lineFeeder;
        if (emissionType.length() > 0)
            datasetNames += emissionType + lineFeeder ;
        
        datasetNames += QAStep.summaryTypeTag + lineFeeder;
        if (summaryType.length() > 0)
            datasetNames += summaryType + lineFeeder ;
        updateArgumentsTextArea(datasetNames);

    }
    
    public void updateInventories(Object[] invBase, Object[] invCompare, Object[] invTables, String summaryType) {
        clear();
        String datasetNames = "";
        datasetNames += getInvString(QAStep.invBaseTag, invBase);
        datasetNames += getInvString(QAStep.invCompareTag, invCompare);
        datasetNames += getInvString(QAStep.invTableTag, invTables);
        
        datasetNames += QAStep.summaryTypeTag + lineFeeder;
        if (summaryType.length() > 0)
            datasetNames += summaryType + lineFeeder;

        updateArgumentsTextArea(datasetNames);
    }
    
    public void updateDatasets(Object capInventory, 
            Object hapInventory, 
            Object speciationToolSpecieInfoDataset,
            Object pollToPollConversionDataset, 
            Object[] speciationProfileWeightDatasets,
            Object[] speciationCrossReferenceDatasets, 
            String filter,
            String summaryType) {
        clear();
        String datasetNames = "";
        if (capInventory != null) datasetNames += "-cap" + lineFeeder + ((EmfDataset) capInventory).getName() + lineFeeder;
        if (hapInventory != null) datasetNames += "-hap" + lineFeeder + ((EmfDataset) hapInventory).getName() + lineFeeder;
        if (speciationToolSpecieInfoDataset != null) datasetNames += "-gstsi" + lineFeeder + ((EmfDataset) speciationToolSpecieInfoDataset).getName() + lineFeeder;
        if (pollToPollConversionDataset != null) datasetNames += "-gscnv" + lineFeeder + ((EmfDataset) pollToPollConversionDataset).getName() + lineFeeder;
        if (speciationProfileWeightDatasets != null) datasetNames += getInvString("-gspw", speciationProfileWeightDatasets);
        if (speciationCrossReferenceDatasets != null) datasetNames += getInvString("-gsref", speciationCrossReferenceDatasets);

        datasetNames += QAStep.summaryTypeTag + lineFeeder;
        if (summaryType != null && summaryType.length() > 0)
            datasetNames += summaryType + lineFeeder;
        datasetNames += QAStep.filterTag + lineFeeder;
        if (filter != null && filter.length() > 0)
            datasetNames += filter + lineFeeder;

        updateArgumentsTextArea(datasetNames);
    }

    private String getInvString(String tag, Object[] inventories){
        String invString =tag + lineFeeder;
        for (int i = 0; i < inventories.length; i++) {
            invString += ((EmfDataset) inventories[i]).getName() + lineFeeder;
        }   
        return invString;
    }

    private String getTagString(String tag, Object[] tags){
        String invString =tag + lineFeeder;
        for (int i = 0; i < tags.length; i++) {
            invString += tags[i] + lineFeeder;
        }   
        return invString;
    }

    public void updateInventories(Object[] retreivedInventories) {

        clear();
        String datasetNames = "";
        datasetNames += getInvString(QAStep.invTag, retreivedInventories);
        updateArgumentsTextArea(datasetNames);
    }

    private void runQAStep() throws EmfException {
        messagePanel
                .setMessage("Started Run. Please monitor the Status window and click Refresh button to track your run request.");
        status.setSelectedItem("In Progress");
        date.setValue(new Date());
        who.setText(presenter.userName());
        saveButton.setEnabled(false);// to prevent user from clicking save after started running a qa step
        runButton.setEnabled(false); // only allow user to run one time at a open window session

        presenter.run();
        resetChanges();
    }

    private void resetRunStatus(QAStepResult result) throws EmfException {
        saveButton.setEnabled(true);
        runButton.setEnabled(true);
        saveQA();
        
        if (result != null){
            who.setText(step.getWho());
            date.setText(DATE_FORMATTER.format(step.getDate()));
            status.setSelectedItem(step.getStatus());
            tableName.setText(result.getTable());
            tableName.setToolTipText(result.getTable());
            creationStatusLabel.setText(result.getTableCreationStatus());
            creationDateLabel.setText(CustomDateFormat.format_MM_DD_YYYY_HH_mm(result.getTableCreationDate()));
            currentTable.setSelected(result.isCurrentTable());
            qaStepResult = result;
        }
        resetChanges();
        clear();
        super.revalidate();
    }

    protected void doExport() {
        try {


            QAStepResult stepResult = presenter.getStepResult(step);
            if (stepResult == null)
                throw new EmfException("Please run the QA step before trying to view.");
            clear();
            
            //only make user specify folder when exporting to EMF server, not downloading
            if (!download.isSelected())
                checkExportFolder();
            if ( !checkExportName()) 
                return;
//            if (!presenter.ignoreShapeFileFunctionality() 
//                    && presenter.isShapeFileCapable(qaStepResult)) {
                QAStepExportWizard dialog = new QAStepExportWizard(parentConsole);
                QAStepExportWizardPresenter presenter2 = new QAStepExportWizardPresenter(session);
                presenter2.display(dialog, stepResult);
                if (!presenter2.isCanceled()) { //make sure they didn't cancel the export operation...
                    if (dialog.shouldCreateShapeFile()){
                        if (download.isSelected()) {
                            messagePanel.setMessage("Started Exporting Shape File to download. Please monitor the Status window "
                                    + "to track your export request.");
                            this.presenter.downloadToShapeFile(step, stepResult, exportName.getText(), dialog.getProjectionShapeFile(), dialog.getRowFilter(), dialog.getPivotConfiguration(), overide.isSelected());
                        } else {
                            messagePanel.setMessage("Started Exporting Shape File. Please monitor the Status window "
                                    + "to track your export request.");
                            this.presenter.exportToShapeFile(step, stepResult, exportFolder.getText(), exportName.getText(), overide.isSelected(), dialog.getProjectionShapeFile(), dialog.getRowFilter(), dialog.getPivotConfiguration());
                        }
                    }
                    if(dialog.shouldCreateCSV()) {
                        if (download.isSelected()) {
                            messagePanel.setMessage("Started Export to download. Please monitor the Status window "
                                    + "to track your export request.");
                            this.presenter.download(step, stepResult, exportName.getText(), overide.isSelected(), dialog.getRowFilter());
                        } else {
                            messagePanel.setMessage("Started Export. Please monitor the Status window "
                                    + "to track your export request.");
                            this.presenter.export(step, stepResult, exportFolder.getText(), exportName.getText(), overide.isSelected(), dialog.getRowFilter()); // pass in fileName
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
//                    this.presenter.export(step, qaStepResult, exportFolder.getText(), exportName.getText(), overide.isSelected()); // pass in fileName
//                }
//            }
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void viewResults() {
        final String exportDir = exportFolder.getText();

        // if (exportDir == null || exportDir.trim().isEmpty())
        // throw new EmfException("Please specify the exported result directory.");
        Thread viewResultsThread = new Thread(new Runnable() {
            public void run() {
                try {
                    
                    QAStepResult stepResult = presenter.getStepResult(step);
                    if (stepResult == null)
                        throw new EmfException("Please run the QA step before trying to view.");
                    clear();
                    
                    DefaultUserPreferences userPref = new DefaultUserPreferences();
                    String sLimit = userPref.property("View_QA_results_limit");
                    
                    long rlimit;                    
                    if ( sLimit == null ){
                        JOptionPane.showMessageDialog(parentConsole, 
                                "View_QA_results_limit is not specified in EMFPrefs.txt, default value is 50000.", "Warning", JOptionPane.WARNING_MESSAGE);
                        rlimit = 50000;
                    } else
                        try {
                            rlimit = Integer.parseInt(sLimit.trim());
                        } catch (NumberFormatException e) {
                            //just default if they entered a non number string
                            rlimit = 50000;
                        }
                    
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    long records = presenter.getTableRecordCount(stepResult);
                    long viewCount =  records;
                    if ( records > rlimit ){
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
                            int selection = JOptionPane.showConfirmDialog(parentConsole, message, title,
                                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                            if (selection == JOptionPane.NO_OPTION) {
                                return;
                            }
                        }
                    }
                    presenter.viewResults(stepResult, viewCount, exportDir.trim());
                } catch (EmfException e) {
                    e.printStackTrace();
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
                    
                    //javax.xm
                }
            }
        });

        viewResultsThread.start();
    }

    private Button saveButton() {
        Button save = new SaveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clear();
                    saveQA();
                } catch (EmfException e1) {
                    messagePanel.setError(e1.getMessage());
                }
            }
        });
        return save;
    }
    
    private void saveQA() throws EmfException{
        if (hasChanges()){
            presenter.save();
            messagePanel.setMessage("QA step has been saved successfully. ");
            resetChanges();
        }
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
        if (super.shouldDiscardChanges()) {
            this.versionsSelection.removeActionListener(this.versionSelectionActionListener);
            presenter.close();
        }
    }

    public QAStep save() throws EmfException {

        if (order.getText().equals("")) {
            throw new EmfException("Order should be a floating point number");
        }
        step.setName(name.getText().trim());
        String nameAndVersion = (String) versionsSelection.getSelectedItem();
        if (nameAndVersion == null)
            throw new EmfException("Missing version");
        int pos1 = nameAndVersion.lastIndexOf("(");
        int pos2 = nameAndVersion.lastIndexOf(")");
        String vStr = nameAndVersion.substring(pos1+1, pos2);
        if ( vStr != null && !vStr.trim().isEmpty() ) {
            step.setVersion( Integer.parseInt(vStr.trim()));
        }
        //step.setDatasetId(this.origDataset.getId());
        step.setProgram(qaPrograms.get(program.getSelectedItem()));
        step.setProgramArguments(programArguments.getText());
        step.setOrder(Float.parseFloat(order.getText()));
        step.setDescription(description.getText().trim());
        step.setRequired(required.isSelected());

        step.setStatus((String) status.getSelectedItem());
        step.setComments(comments.getText());
        step.setWho(who.getText());
        step.setDate(date.value());
        step.setConfiguration(config.getText());
        if (exportFolder.getText() != null)
            step.setOutputFolder(exportFolder.getText().trim());
        return step;
    }

    public void setMostRecentUsedFolder(String mostRecentUsedFolder) {
        if (mostRecentUsedFolder != null)
            exportFolder.setText(mostRecentUsedFolder);
    }

    private void clear() {
        messagePanel.clear();
    }

    // Modified length in Dimension to make View window ful size
    public void displayResultsTable(String qaStepName, String exportedFileName) {
        AnalysisEngineTableApp app = new AnalysisEngineTableApp("View QA Step Results: " + qaStepName,
                new Dimension(800, 500), desktopManager, parentConsole);
        app.display(new String[] { exportedFileName });
    }

    public void updateArguments(Object temporalProfile, Object[] smkRpts, Object year) {
        clear();
        String arguments = "";
        if (temporalProfile != null) arguments += "-temporal" + lineFeeder + ((EmfDataset) temporalProfile).getName() + lineFeeder;
        if (smkRpts != null) arguments += getInvString("-smkrpt", smkRpts);
        if (year != null) arguments += "-year" + lineFeeder + year + lineFeeder;

        updateArgumentsTextArea(arguments);
    }

    public void updateCompareAnnualStateSummariesArguments(Object[] inventories, Object[] smkRpts, Object invTable, Object tolerance, Object coStCy) {
        clear();
        String arguments = "";
        if (inventories != null) arguments += getInvString("-inv", inventories);
        if (smkRpts != null) arguments += getInvString("-smkrpt", smkRpts);
        if (invTable != null) arguments += getInvString("-invtable", new Object[] {invTable});
        if (tolerance != null) arguments += getInvString("-tolerance", new Object[] {tolerance});
        if (coStCy != null) arguments += getInvString("-costcy", new Object[] {coStCy}) + lineFeeder;

        updateArgumentsTextArea(arguments);
    }

    public void updateCompareAnnualStateSummariesArguments(Object[] smkRpts, Object coStCy, Object[] polls,
            Object[] species, Object[] exclPollutants) {
        clear();
        String arguments = "";
        if (smkRpts != null) arguments += getInvString(smkRptTag, smkRpts);
        if (coStCy != null) arguments += getInvString(coStCyTag, new Object[] {coStCy});
        if (polls != null) arguments += getTagString(pollListTag, polls);
        if (species != null) arguments += getTagString(specieListTag, species);
        if (exclPollutants != null) arguments += getTagString(exclPollTag, exclPollutants) + lineFeeder;
//        if (pollutantsSort != null) arguments += getInvString(sortPollTag, pollutantsSort) + lineFeeder;

        updateArgumentsTextArea(arguments);
    }

    public void updateECControlScenarioArguments(Object inventory, Object detailedResult, 
            Object[] gsrefs, Object[] gspros) {
        clear();
        String arguments = "";
        if (inventory != null) arguments += getInvString(QAStep.invTag, new Object[] {inventory});
        if (detailedResult != null) arguments += getInvString(detailedResultTag, new Object[] {detailedResult});
        if (gsrefs != null) arguments += getTagString(gsrefTag, gsrefs);
        if (gspros != null) arguments += getTagString(gsproTag, gspros);

        updateArgumentsTextArea(arguments);
    }

    private String[] parseSwitchArguments(String programSwitches, int beginIndex, int endIndex) {
        List<String> inventoryList = new ArrayList<String>();
        String value = "";
        String valuesString = "";
        
        valuesString = programSwitches.substring(beginIndex, endIndex);
        StringTokenizer tokenizer2 = new StringTokenizer(valuesString, "\n");
        tokenizer2.nextToken(); // skip the flag

        while (tokenizer2.hasMoreTokens()) {
            value = tokenizer2.nextToken().trim();
            if (!value.isEmpty())
                inventoryList.add(value);
        }
        return inventoryList.toArray(new String[0]);
    }

    public void updateProgramArguments(String programArguments) {
        updateArgumentsTextArea(programArguments);
    }

    public void actionPerformed(ActionEvent arg0) {
        // NOTE Auto-generated method stub
        
    }
}