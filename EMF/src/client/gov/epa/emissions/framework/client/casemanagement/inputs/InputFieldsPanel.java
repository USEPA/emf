package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class InputFieldsPanel extends JPanel implements InputFieldsPanelView {

    private EditableComboBox inputName;

    private EditableComboBox program;
    
    private ComboBox region;

    private ComboBox sector;

    private EditableComboBox envtVar;

    private TextField datasetTxt;
    
    private EmfDataset inputDataset;

    protected ComboBox version;

    private ComboBox dsType;

    private JLabel qaStatus;

    private EditableComboBox subDir;

    private CheckBox required;

    private CheckBox localBox;

    private Button selectButton;

    protected MessagePanel messagePanel;

    protected ManageChangeables changeablesList;

    protected InputFieldsPanelPresenter presenter;

    protected CaseInput input;

    private ComboBox jobs;

    protected EmfSession session;

    protected EmfConsole parentConsole;

    private int modelToRunId;
    
    protected DesktopManager desktopManager;

    private Dimension preferredSize = new Dimension(450, 20);

    public InputFieldsPanel(MessagePanel messagePanel, ManageChangeables changeablesList, EmfConsole parentConsole,
            DesktopManager desktopManager) {
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManager;
    }

    public void display(CaseInput input, JComponent container, int modelToRunId, EmfSession session)
            throws EmfException {
        this.input = input;
        this.session = session;
        this.modelToRunId = modelToRunId;
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        inputName = new EditableComboBox(presenter.getInputNames(modelToRunId));
        inputName.setSelectedItem(input.getInputName());
        addPopupMenuListener(inputName, "inputnames");
        changeablesList.addChangeable(inputName);
        // inputName.setPrototypeDisplayValue(width);
        inputName.setPreferredSize(preferredSize);
        layoutGenerator.addLabelWidgetPair("Input Name:", inputName, panel);

        program = new EditableComboBox(presenter.getPrograms(modelToRunId));
        program.setSelectedItem(input.getProgram());
        addPopupMenuListener(program, "programs");
        changeablesList.addChangeable(program);
        program.setPreferredSize(preferredSize);
        layoutGenerator.addLabelWidgetPair("Program:", program, panel);

        envtVar = new EditableComboBox(presenter.getEnvtVars(modelToRunId));
        envtVar.setSelectedItem(input.getEnvtVars());
        addPopupMenuListener(envtVar, "envtvars");
        changeablesList.addChangeable(envtVar);
        envtVar.setPreferredSize(preferredSize);
        layoutGenerator.addLabelWidgetPair("Envt. Variable:", envtVar, panel);
        
        region = new ComboBox(presenter.getGeoRegions());
        region.setPreferredSize(preferredSize);
        region.setSelectedItem(input.getRegion() == null ? region.getItemAt(0) : input.getRegion());
        addPopupMenuListener(region, "grids");
        changeablesList.addChangeable(region);
        layoutGenerator.addLabelWidgetPair("Region:", region, panel);
        

        sector = new ComboBox(presenter.getSectors());
        if (input.getSector() == null) {
            sector.setSelectedIndex(0); // set to all sectors
        } else {
            sector.setSelectedItem(input.getSector());
        }
        addPopupMenuListener(sector, "sectors");
        changeablesList.addChangeable(sector);
        sector.setPreferredSize(preferredSize);
        layoutGenerator.addLabelWidgetPair("Sector:", sector, panel);

        final DatasetType inputDSType = input.getDatasetType();
        dsType = new ComboBox(presenter.getDSTypes());
        addPopupMenuListener(dsType, "dstypes");
        dsType.setSelectedItem(inputDSType);
        dsType.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                DatasetType selectedType = (DatasetType) dsType.getSelectedItem();
                
                if (subDir != null && selectedType != null && selectedType.isExternal()) {
                    subDir.setSelectedItem(null);
                    subDir.setEnabled(false);
                } else if (subDir != null)
                    subDir.setEnabled(true);
                
                datasetTxt.setText("");
                fillVersions(null);
            }
        });
        changeablesList.addChangeable(dsType);
        dsType.setPreferredSize(preferredSize);
        layoutGenerator.addLabelWidgetPair("Dataset Type:", dsType, panel);

        layoutGenerator.addLabelWidgetPair("Dataset:", datasetPanel(), panel);

        version = new ComboBox(new Version[] { input.getVersion() });
        fillVersions(input.getDataset());

        if (input.getVersion() != null)
            version.setSelectedItem(input.getVersion());

        changeablesList.addChangeable(version);
        version.setPreferredSize(preferredSize);
        layoutGenerator.addLabelWidgetPair("Version:", version, panel);

        setJob();
        changeablesList.addChangeable(jobs);
        jobs.setPreferredSize(preferredSize);
        layoutGenerator.addLabelWidgetPair("Job:", jobs, panel);

        qaStatus = new JLabel("");
        layoutGenerator.addLabelWidgetPair("QA Status:", qaStatus, panel);

        subDir = new EditableComboBox(presenter.getSubdirs(modelToRunId));
        
        if (inputDSType != null && inputDSType.isExternal()) {
            subDir.setSelectedItem(null);
            subDir.setEnabled(false);
        } else {
            subDir.setSelectedItem(input.getSubdirObj());
        }
        
        addPopupMenuListener(subDir, "subdirs");
        changeablesList.addChangeable(subDir);
        subDir.setPreferredSize(preferredSize);
        layoutGenerator.addLabelWidgetPair("Subdirectory:", subDir, panel);

        required = new CheckBox("");
        required.setSelected(input.isRequired());
        changeablesList.addChangeable(required);
        layoutGenerator.addLabelWidgetPair("Required?", required, panel);

        localBox = new CheckBox("");
        localBox.setSelected(input.isLocal());
        changeablesList.addChangeable(localBox);
        layoutGenerator.addLabelWidgetPair("Local?", localBox, panel);

        layoutGenerator.addLabelWidgetPair("Parent case ID:", new JLabel("" + this.input.getParentCaseId()), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 14, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 8);// xPad, yPad

        container.add(panel);
    }

    private JPanel datasetPanel() {

        datasetTxt = new TextField("dataset", 38);
        datasetTxt.setEditable(false);
        inputDataset = input.getDataset();
        if (inputDataset != null)
            datasetTxt.setText(input.getDataset().getName());

        changeablesList.addChangeable(datasetTxt);
        datasetTxt.setToolTipText("Press select button to choose from a dataset list.");
        selectButton = new AddButton("Select", selectAction());
        selectButton.setMargin(new Insets(1, 2, 1, 2));

        JPanel invPanel = new JPanel(new BorderLayout(5, 0));

        invPanel.add(datasetTxt, BorderLayout.LINE_START);
        invPanel.add(selectButton);
        return invPanel;
    }

    private Action selectAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    clearMsgPanel();
                    selectInputDataset();
                } catch (Exception e1) {
                    messagePanel.setError(e1.getMessage());
                }
            }
        };
    }

    private void selectInputDataset() throws Exception {
        DatasetType type = (DatasetType) dsType.getSelectedItem();

        if (type == null)
            throw new EmfException("Please select a valid dataset type.");

        DatasetType[] datasetTypes = new DatasetType[] { type };
        InputDatasetSelectionDialog view = new InputDatasetSelectionDialog(parentConsole);
        InputDatasetSelectionPresenter presenter = new InputDatasetSelectionPresenter(view, session, datasetTypes);
        if (datasetTypes.length == 1)
            presenter.display(datasetTypes[0], true);
        else
            presenter.display(null, true);

        setDatasets(presenter.getDatasets());
    }

    private void setDatasets(EmfDataset[] datasets) {
        if (datasets == null || datasets.length == 0) {
//            dataset.setText("");
//            updateDataset(null);
//            fillVersions(null);
            return;
        }
        if (datasets != null || datasets.length > 0) {
            datasetTxt.setText(datasets[0].getName());
            inputDataset=datasets[0];
            //updateDataset(datasets[0]);
            fillVersions(datasets[0]);
        }
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
                    clearMsgPanel();
                    Object selected = box.getSelectedItem();
                    box.setModel(new DefaultComboBoxModel(getAllObjects(toget)));
                    box.setSelectedItem(selected);
                } catch (Exception e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        });
    }

    protected Object[] getAllObjects(String toget) throws EmfException {
        if (toget.equals("dstypes"))
            return presenter.getDSTypes();

        else if (toget.equals("inputnames"))
            return presenter.getInputNames(modelToRunId);

        else if (toget.equals("programs"))
            return presenter.getPrograms(modelToRunId);

        else if (toget.equals("envtvars"))
            return presenter.getEnvtVars(modelToRunId);
        
        else if (toget.equals("grids"))
            return presenter.getGeoRegions();

        else if (toget.equals("sectors"))
            return presenter.getSectors();

        else if (toget.equals("subdirs"))
            return presenter.getSubdirs(modelToRunId);

        else
            throw new EmfException("Unknown object type: " + toget);

    }

    private void setJob() throws EmfException {
        CaseJob[] jobArray = presenter.getCaseJobs();
        jobs = new ComboBox(jobArray);
        jobs.setSelectedIndex(presenter.getJobIndex(input.getCaseJobID(), jobArray));
    }

    protected void fillVersions(EmfDataset dataset) {
        version.setEnabled(true);
        
        try {
            Version[] versions = presenter.getVersions(dataset);
            version.removeAllItems();
            version.setModel(new DefaultComboBoxModel(versions));
            version.revalidate();
            if (versions.length > 0)
                version.setSelectedIndex(getDefaultVersionIndex(versions, dataset));
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private int getDefaultVersionIndex(Version[] versions, EmfDataset dataset) {
        int defaultversion = dataset.getDefaultVersion();

        for (int i = 0; i < versions.length; i++)
            if (defaultversion == versions[i].getVersion())
                return i;

        return 0;
    }

    public CaseInput setFields() throws EmfException {
        updateInputName();
        updateProgram();
        updateEnvtVar();
        updateRegion();
        updateSector();
        input.setDatasetType((DatasetType) dsType.getSelectedItem());
        updateDataset(inputDataset);
        updateVersion();
        updateSubdir();
        input.setRequired(required.isSelected());
        input.setLocal(localBox.isSelected());
        input.setLastModifiedDate(new Date());
        updateJob();
        return input;
    }

    private void updateJob() {
        Object job = jobs.getSelectedItem();

        if (job == null)
            return;

        if (((CaseJob) job).getName().equalsIgnoreCase(InputFieldsPanelPresenter.ALL_FOR_SECTOR)) {
            input.setCaseJobID(0);
            return;
        }

        input.setCaseJobID(((CaseJob) job).getId());
    }

    private void updateInputName() throws EmfException {
        Object selected = inputName.getSelectedItem();
        input.setInputName(presenter.getInputName(selected, modelToRunId));
    }

    private void updateProgram() throws EmfException {
        Object selected = program.getSelectedItem();
        if (selected == null) {
            input.setProgram(null);
            return;
        }

        input.setProgram(presenter.getCaseProgram(selected, modelToRunId));
    }

    private void updateSubdir() throws EmfException {
        Object selected = subDir.getSelectedItem();
        DatasetType inputDSType = (DatasetType) dsType.getSelectedItem();
        
        if (selected == null || (inputDSType != null && inputDSType.isExternal())) {
            input.setSubdirObj(null);
            return;
        }

        input.setSubdirObj(presenter.getSubDir(selected, modelToRunId));
    }

    private void updateEnvtVar() throws EmfException {
        Object selected = envtVar.getSelectedItem();
        if (selected == null) {
            input.setEnvtVars(null);
            return;
        }

        input.setEnvtVars(presenter.getInputEnvtVar(selected, modelToRunId));
    }
    
    private void updateRegion() {
        GeoRegion selected = (GeoRegion) region.getSelectedItem();

        if (selected.getName().equalsIgnoreCase("")) {
            input.setRegion(null);
            return;
        }

        input.setRegion(selected);
    }

    private void updateSector() {
        Sector selected = (Sector) sector.getSelectedItem();

        if (selected.getName().equalsIgnoreCase("All sectors")) {
            input.setSector(null);
            return;
        }

        input.setSector(selected);
    }

    protected void updateDataset(EmfDataset dataset) {
        input.setDataset(dataset);
    }

    protected void updateVersion() throws EmfException {
        EmfDataset ds = input.getDataset();
        Version ver = (Version) version.getSelectedItem();

        if (ds == null || ds.getName().equalsIgnoreCase("Not selected")) {
            input.setVersion(null);
            return;
        }

        String type = ds.getDatasetType().getName();
        if (ds.getName() != null && ver == null && type.indexOf("External") < 0)
            throw new EmfException("Please select a dataset version.");

        input.setVersion(ver);
    }

    public void observe(InputFieldsPanelPresenter presenter) {
        this.presenter = presenter;
    }

    public CaseInput getInput() {
        return this.input;
    }

    private void clearMsgPanel() {
        this.messagePanel.clear();
    }

    public void validateFields() throws EmfException {
        Object selectedProg = program.getSelectedItem();
        if (inputName.getSelectedItem() == null)
            throw new EmfException("Please specify an input name.");

        if (selectedProg == null || selectedProg.toString().trim().equals(""))
            throw new EmfException("Please specify a program.");

        setFields();
    }

    public void viewOnly() {
        inputName.setEditable(false);
        envtVar.setEditable(false);
        program.setEditable(false);
        subDir.setEditable(false);
        required.setEnabled(false);
        localBox.setEnabled(false);
        datasetTxt.setPreferredSize(preferredSize);
        selectButton.setVisible(false);
    }

}
