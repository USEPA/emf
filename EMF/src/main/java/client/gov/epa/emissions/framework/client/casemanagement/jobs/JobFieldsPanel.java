package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.BrowseButton;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.AddRemoveWidget;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class JobFieldsPanel extends JPanel implements JobFieldsPanelView {

    private JobFieldsPanelPresenter presenter;

    private ManageChangeables changeablesList;

    private CaseJob job;

    private boolean edit;

    private TextField name;

    private TextArea purpose;

    private TextField jobOrder;

    private TextField version;

    private TextField args;

    private TextField path;

    private EmfConsole parent;

    private EmfSession session;

    private EditableComboBox host;

    private TextField qoption;

    private TextField jobGroup;

    private MessagePanel messagePanel;

    private ComboBox status;

    private ComboBox sector;
    
    private ComboBox region;

    private Dimension comboSize = new Dimension(190, 25);

    private JLabel queID;

    private JLabel start;

    private JLabel complete;

    private TextArea runNote;

    private TextArea lastMsg;

    private JLabel userLabel;

    private Case caseObj;

    private TextField oldJobOrder;

    private Button browseButton;

    private CheckBox localBox;

    private AddRemoveWidget dependentJobsList;

    private static String lastPath = "";

    public JobFieldsPanel(boolean edit, MessagePanel messagePanel, ManageChangeables changeablesList,
            EmfConsole parent, EmfSession session) {
        this.edit = edit;
        this.changeablesList = changeablesList;
        this.parent = parent;
        this.session = session;
        this.messagePanel = messagePanel;
    }

    public void display(Case caseObj, CaseJob job, JComponent container) {
        this.job = job;
        this.caseObj = caseObj;
        localBox = new CheckBox("Local");

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        container.add(nameNPurposPanel());

        try {
            container.add(setupPanel());
        } catch (EmfException e) {
            setError(e.getMessage());
        }

        if (job.getId() > 0) {            // Not new job
            container.add(resultPanel());
            populateFields();
        }

        if (edit)
            setEditTracking();

        if (!edit)
            setViewOnly();
    }

    private JPanel nameNPurposPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        // name
        name = new TextField("name", 45);
        layoutGenerator.addLabelWidgetPair(" Name:", name, panel);

        // description
        purpose = new TextArea("purposes", job.getPurpose());
        ScrollableComponent scrolpane = new ScrollableComponent(purpose);
        scrolpane.setPreferredSize(new Dimension(500, 80));
        layoutGenerator.addLabelWidgetPair(" Purpose:", scrolpane, panel);

        String execPath = job.getPath();
        String caseInputPath = this.caseObj.getInputFileDir();
        if ((execPath == null || execPath.trim().isEmpty()) && caseInputPath != null && !caseInputPath.isEmpty())
            execPath = caseInputPath + getFileSeparator(caseInputPath);

        path = new TextField("path", execPath, 39);
        path.setPreferredSize(new Dimension(300, 15));
        layoutGenerator.addLabelWidgetPair(" Executable:", getFolderChooserPanel(path, "Select the Executable File"),
                panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 10);// xPad, yPad

        return panel;
    }

    private JPanel setupPanel() throws EmfException {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 2, 5, 5));
        panel.add(leftSetupPanel());
        panel.add(rightSetupPanel());

        if (edit)
            panel.setBorder(BorderFactory.createTitledBorder("Setup"));

        return panel;
    }

    private JPanel leftSetupPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        int charsWide = 16;

        version = new TextField("version", job.getVersion() + "", charsWide);
        layoutGenerator.addLabelWidgetPair("Version:", version, panel);

        String argsText = job.getArgs();
        args = new TextField("args", argsText, charsWide);
        args.setToolTipText(argsText);
        layoutGenerator.addLabelWidgetPair("Arguments:", args, panel);

        jobOrder = new TextField("jobOrder", job.getJobNo() + "", charsWide);
        // AME: Used what was job number for Job order, since we don't use the order or
        // number for dependencies, and jobNo was a float, while job order was int
        layoutGenerator.addLabelWidgetPair("Job Order:", jobOrder, panel);
        jobOrder.setToolTipText("The order in which this job should be displayed in the table.");

        // temporarily leave this there
        oldJobOrder = new TextField("oldJobOrder", job.getOrder() + "", charsWide);
        jobGroup = new TextField("jobGroup", job.getJobGroup(), charsWide);
        layoutGenerator.addLabelWidgetPair("Job Group:", jobGroup, panel);

        qoption = new TextField("qoption", job.getQueOptions(), charsWide);
        layoutGenerator.addLabelWidgetPair("Queue Options:", qoption, panel);
        qoption.setToolTipText(qoption.getText());

        JPanel pIdLocal = new JPanel(new BorderLayout());
        JLabel parentCase = new JLabel(job.getParentCaseId() + "");
        pIdLocal.add(parentCase, BorderLayout.LINE_START);
        pIdLocal.add(localBox, BorderLayout.LINE_END);
        pIdLocal.setPreferredSize(new Dimension(150, 20));
        layoutGenerator.addLabelWidgetPair("Parent case ID: ", pIdLocal, panel);

        String user = job.getUser() == null ? session.user().getName() : job.getUser().getName();
        userLabel = new JLabel(user);
        layoutGenerator.addLabelWidgetPair("User:", userLabel, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 7, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 10);// xPad, yPad

        return panel;
    }

    private Component rightSetupPanel() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("<html>Depends on:<br><br><br></html>", jobDependencyPanel(), panel);

        region = new ComboBox(presenter.getGeoRegions());
        region.setPreferredSize(comboSize);
        region.setSelectedItem(job.getRegion() == null ? region.getItemAt(0) : job.getRegion());
        addPopupMenuListener(region, "grids");
        layoutGenerator.addLabelWidgetPair("Region:", region, panel);
        
        sector = new ComboBox(presenter.getSectors());
        sector.setPreferredSize(comboSize);
        sector.setSelectedItem(job.getSector() == null ? sector.getItemAt(0) : job.getSector());
        addPopupMenuListener(sector, "sectors");
        layoutGenerator.addLabelWidgetPair("Sector:", sector, panel);

        host = new EditableComboBox(presenter.getHosts());
        host.setSelectedItem(job.getHost());
        host.setPreferredSize(comboSize);
        addPopupMenuListener(host, "hosts");
        layoutGenerator.addLabelWidgetPair("Host:", host, panel);

        status = new ComboBox(presenter.getRunStatuses());
        status.setToolTipText("CAUTION: use 'Cancel' button in Jobs tab instead of " +
        		"setting an active status (Exporting, Waiting, Submitted, and Running).");
        status.setPreferredSize(comboSize);
        status.setSelectedItem(job.getRunstatus());
        addPopupMenuListener(status, "status");
        addActionListener(status);
        layoutGenerator.addLabelWidgetPair("Run Status:", status, panel);
        // layoutGenerator.addLabelWidgetPair("Local?",localBox,panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 10);// xPad, yPad

        return panel;
    }

    private void addActionListener(final ComboBox status) {
        status.addActionListener(new AbstractAction(){
            public void actionPerformed(ActionEvent arg0) {
                JobRunStatus st =  job.getRunstatus();
                JobRunStatus sl =  (JobRunStatus)status.getSelectedItem();
                
                if (isActive(st) && !st.equals(sl)) {
                    JOptionPane.showMessageDialog(JobFieldsPanel.this, 
                            "Please use the 'Cancel' button on the Jobs tab instead of changing the status.",
                            "Cannot Change Run Status", JOptionPane.ERROR_MESSAGE);
                    status.setSelectedItem(st);
                }
                
                if (!isActive(st) && isActive(sl)) {
                    JOptionPane.showMessageDialog(JobFieldsPanel.this, 
                            "Please use the 'Run' button on the Jobs tab instead of changing the status.",
                            "Cannot Change Run Status", JOptionPane.ERROR_MESSAGE);
                    status.setSelectedItem(st);
                }
            }
        });
    }

    private boolean isActive(JobRunStatus runstatus) {
        if (runstatus == null)
            return false;
        
        String st = runstatus.getName();
        
        if (st != null && (st.equalsIgnoreCase("Exporting")
                || st.equalsIgnoreCase("Waiting")
                || st.equalsIgnoreCase("Running")
                || st.equalsIgnoreCase("Submitted")))
            return true;
            
        return false;
    }

    private void addPopupMenuListener(final JComboBox box, final String toget) {
        box.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuCanceled(PopupMenuEvent event) {
                // NOTE Auto-generated method stub
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent event) {
                // NOTE Auto-generated method stub
            }

            public void popupMenuWillBecomeVisible(PopupMenuEvent event) {
                try {
                    Object selected = box.getSelectedItem();
                    box.setModel(new DefaultComboBoxModel(getAllObjects(toget)));
                    box.setSelectedItem(selected);
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        });
    }

    protected Object[] getAllObjects(String toget) throws EmfException {
        if (toget.equals("hosts"))
            return presenter.getHosts();
        
        if (toget.equals("grids"))
            return presenter.getGeoRegions();

        if (toget.equals("sectors"))
            return presenter.getSectors();

        if (toget.equals("status"))
            return presenter.getRunStatuses();

        return null;
    }

    private JPanel getFolderChooserPanel(final JTextField dir, final String title) {
        browseButton = new BrowseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                clearMessage();
                selectFolder(dir, title);
            }
        });
        browseButton.setMargin(new Insets(3, 5, 3, 5));
        JPanel folderPanel = new JPanel(new BorderLayout(2, 0));
        folderPanel.add(dir, BorderLayout.LINE_START);
        folderPanel.add(browseButton, BorderLayout.LINE_END);

        return folderPanel;
    }

    protected void selectFolder(JTextField dir, String title) {
        EmfFileInfo initDir = new EmfFileInfo(dir.getText(), true, false);
        if ((initDir.getAbsolutePath() == null) || (initDir.getAbsolutePath().length() == 0)) {
            initDir = new EmfFileInfo(lastPath, true, false);
        }

        EmfFileChooser chooser = new EmfFileChooser(initDir, new EmfFileSystemView(session.dataCommonsService()));
        chooser.setTitle(title);
        chooser.setDirectoryAndFileMode();
        int option = chooser.showDialog(parent, "Select a file that contains the executable");

        EmfFileInfo[] files = (option == EmfFileChooser.APPROVE_OPTION) ? chooser.getSelectedFiles() : null;
        if (files == null || files.length == 0)
            return;

        if (files.length > 1) {
            setError("Please select a single file for the executable.");
        }

        dir.setText(files[0].getAbsolutePath());
    }

    private JPanel resultPanel() {
        JPanel panel = new JPanel();
        // panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setLayout(new GridLayout(1, 2, 5, 5));

        JPanel leftpanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator leftlayout = new SpringLayoutGenerator();

        queID = new JLabel();
        leftlayout.addLabelWidgetPair("Queue ID:", queID, leftpanel);

        start = new JLabel();
        leftlayout.addLabelWidgetPair("Date Started:", start, leftpanel);

        complete = new JLabel();
        leftlayout.addLabelWidgetPair("Date Completed:", complete, leftpanel);

        // Lay out the panel.
        leftlayout.makeCompactGrid(leftpanel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 10);// xPad, yPad

        JPanel rightpanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator rightlayout = new SpringLayoutGenerator();

        runNote = new TextArea("runnote", job.getRunNotes());
        ScrollableComponent scrolpane1 = new ScrollableComponent(runNote);
        scrolpane1.setPreferredSize(new Dimension(180, 80));
        rightlayout.addLabelWidgetPair("Job Notes:", scrolpane1, rightpanel);

        lastMsg = new TextArea("lastmessage", job.getRunNotes());
        lastMsg.setEditable(false);
        ScrollableComponent scrolpane2 = new ScrollableComponent(lastMsg);
        scrolpane2.setPreferredSize(new Dimension(180, 80));
        rightlayout.addLabelWidgetPair("Last Message:", scrolpane2, rightpanel);

        // Lay out the panel.
        rightlayout.makeCompactGrid(rightpanel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 10);// xPad, yPad

        panel.add(leftpanel);
        panel.add(rightpanel);
        // panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.gray), "Job Run
        // Results",
        // 1, 1, Font.getFont(Font.SANS_SERIF), Color.blue));
        panel.setBorder(BorderFactory.createTitledBorder("Run Results"));

        return panel;
    }

    private void populateFields() {
        name.setText(job.getName());
        purpose.setText(job.getPurpose());
        String jobPath = job.getPath();
        Executable exec = job.getExecutable();

        if (jobPath != null && !path.isEmpty())
            path.setText(jobPath + getFileSeparator(jobPath) + ((exec == null) ? "" : exec.getName()));

        args.setText(job.getArgs());
        jobOrder.setText(job.getJobNo() + "");
        jobGroup.setText(job.getJobGroup());
        oldJobOrder.setText(job.getOrder() + "");
        this.qoption.setText(job.getQueOptions());
        this.version.setText(job.getVersion() + "");
        this.localBox.setSelected(job.isLocal());

        User user = job.getUser();
        Date startDate = job.getRunStartDate();
        Date completeDate = job.getRunCompletionDate();

        this.userLabel.setText(user == null ? "" : user.getName());
        this.queID.setText(job.getIdInQueue() == null ? "" : job.getIdInQueue());
        this.start.setText(startDate == null ? "" : CustomDateFormat.format_MM_DD_YYYY_HH_mm(startDate));
        this.complete.setText(completeDate == null ? "" : CustomDateFormat.format_MM_DD_YYYY_HH_mm(completeDate));
        this.runNote.setText(job.getRunNotes());
        this.lastMsg.setText(job.getRunLog());
    }

    public CaseJob setFields() throws EmfException {
        job.setCaseId(this.caseObj.getId());
        job.setName(name.getText().trim());
        job.setPurpose(purpose.getText().trim());

        // NOTE: order is being stored as job number and order should be a float number
        job.setJobNo(Float.parseFloat(jobOrder.getText().trim()));
        job.setOrder(Integer.parseInt(oldJobOrder.getText().trim()));
        job.setArgs(args.getText().trim());
        setPathNExecutable();
        setHost();
        setRegion();
        updateSector();
        job.setRunstatus((JobRunStatus) status.getSelectedItem());
        job.setVersion(Integer.parseInt(version.getText().trim()));
        job.setQueOptions(qoption.getText().trim());
        job.setLocal(localBox.isSelected());
        job.setJobGroup(jobGroup.getText().trim());
        Object[] dependentJobs = dependentJobsList.getObjects();
        job.setDependentJobs(presenter.dependentJobs(dependentJobs));

        if (edit && job.getId() > 0) {
            job.setRunLog(lastMsg.getText());
            job.setRunNotes(runNote.getText());
            job.setUser(session.user());
        }

        return job;
    }

    private void setRegion() {
     GeoRegion selected = (GeoRegion)region.getSelectedItem();
     
     if (selected.getName().trim().isEmpty())
         selected = null;
         
     job.setRegion(selected);
    }

    private void setPathNExecutable() {
        String absolute = (path.getText() == null) ? null : path.getText().trim();

        if (absolute == null || absolute.isEmpty())
            return;

        char separator = getFileSeparator(absolute);
        int index = absolute.lastIndexOf(separator);
        if (index >= 0)
            job.setPath(absolute.substring(0, index));

        lastPath = job.getPath();

        if (++index < absolute.length()) {
            Executable exe = new Executable(absolute.substring(index));
            job.setExecutable(getExecutable(exe));
        }
    }

    private char getFileSeparator(String path) {
        if (path == null || path.isEmpty()) {
            // this assumes that the server and client are running on the same platform
            return File.separatorChar;
        }

        if (path.contains("/"))
            return '/';

        if (path.contains("\\"))
            return '\\';

        return '/';
    }

    private Executable getExecutable(Executable exe) {
        CaseService service = session.caseService();
        try {
            return service.addExecutable(exe);
        } catch (EmfException e) {
            setError("Could not add the new executable " + exe.getName());
            return null;
        }
    }

    private void updateSector() {
        Sector selected = (Sector) sector.getSelectedItem();

        if (selected.getName().equalsIgnoreCase("All sectors")) {
            job.setSector(null);
            return;
        }

        job.setSector(selected);
    }

    private void setHost() throws EmfException {
        job.setHost(presenter.getHost(host.getSelectedItem()));
    }

    public void observe(JobFieldsPanelPresenter presenter) {
        this.presenter = presenter;
    }

    public void validateFields() throws EmfException {
        String temp = name.getText().trim();

        if (temp.trim().length() == 0)
            throw new EmfException("Please enter a name for the job.");

        String absolute = path.getText();
        // File execFile = new File(absolute);

        if (absolute == null || absolute.trim().equals(""))
            throw new EmfException("Please select an executable file.");

        if (!absolute.contains("\\") && !absolute.contains("/"))
            throw new EmfException("Please specify an absolute path for executable file.");

        try {
            Float.parseFloat(jobOrder.getText().trim());
        } catch (NumberFormatException e) {
            throw new EmfException("Please enter a floating point number into the Job Number field.");
        }

        Object selected = host.getSelectedItem();

        if (selected == null || selected.toString().trim().equals(""))
            throw new EmfException("Please enter a valid host name.");

        validateJobGroup(jobGroup.getText().trim());

        try {
            Integer.parseInt(version.getText().trim());
        } catch (NumberFormatException e) {
            throw new EmfException("Please enter an integer that is the version of the executable.");
        }
    }

    private void validateJobGroup(String abbrev) throws EmfException {
        String underscore = "_";
        for (int i = 0; i < abbrev.length(); i++) {
            if (!(Character.isLetterOrDigit(abbrev.charAt(i)) || underscore.equalsIgnoreCase(abbrev.charAt(i) + "")))
                throw new EmfException("Job group must contain only letters, digits, and underscores. ");
        }
    }

    public CaseJob getJob() throws EmfException {
        presenter.doValidateFields();
        return this.job;
    }

    private JPanel jobDependencyPanel() throws EmfException {
        CaseJob[] jobs = null;
        CaseJob[] dependentJobs = null;

        if (edit)
            jobs = presenter.getAllValidJobs(job.getId());
        else
            jobs = new CaseJob[0];

        dependentJobs = presenter.getDependentJobs(job.getId());

        Arrays.sort(jobs, new Comparator<CaseJob>() {
            public final int compare(CaseJob a, CaseJob b) {
                return a.toString().compareToIgnoreCase(b.toString());
            }
        });
        Arrays.sort(dependentJobs);

        dependentJobsList = new AddRemoveWidget(jobs, changeablesList, parent, true, true);
        dependentJobsList.setObjects(dependentJobs);
        dependentJobsList.setPreferredSize(new Dimension(190, 120));
        return dependentJobsList;
    }

    private void clearMessage() {
        messagePanel.clear();
    }

    private void setError(String error) {
        messagePanel.setError(error);
    }

    private void setEditTracking() {
        changeablesList.addChangeable(name);
        changeablesList.addChangeable(purpose);
        changeablesList.addChangeable(path);
        changeablesList.addChangeable(version);
        changeablesList.addChangeable(args);
        changeablesList.addChangeable(jobOrder);
        changeablesList.addChangeable(jobGroup);
        changeablesList.addChangeable(qoption);
        changeablesList.addChangeable(sector);
        changeablesList.addChangeable(region);
        changeablesList.addChangeable(host);
        changeablesList.addChangeable(status);
        changeablesList.addChangeable(runNote);
        changeablesList.addChangeable(localBox);
    }

    public void setViewOnly() {
        browseButton.setVisible(false);
        name.setEditable(false);
        purpose.setEditable(false);
        path.setEditable(false);
        version.setEditable(false);
        args.setEditable(false);
        dependentJobsList.viewOnly();
        localBox.setEnabled(false);

        jobOrder.setEditable(false);
        host.setEditable(false);
        qoption.setEditable(false);
        jobGroup.setEditable(false);

        runNote.setEditable(false);
    }
}
