package gov.epa.emissions.framework.client.cost.controlprogram.editor;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.BorderlessButton;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.client.data.viewer.DataViewPresenter;
import gov.epa.emissions.framework.client.data.viewer.DataViewer;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlProgramType;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ControlProgramSummaryTab extends JPanel implements ControlProgramTabView {

    private ControlProgramSummaryTabPresenter presenter;

    private ControlProgram controlProgram;

    private ManageChangeables changeablesList;

    private TextField name, startDate, endDate;

    private TextArea description;

    private EmfSession session;

    private MessagePanel messagePanel;

    protected EmfConsole parentConsole;

    private ComboBox controlProgramTypeCombo;

//    private DecimalFormat decFormat;
//
//    private NumberFieldVerifier verifier;
    
    private TextField dataset;

    protected ComboBox version;

    private ComboBox dsType;
    
    private Dimension preferredSize = new Dimension(450, 25);

    private Button selectButton;
    
    private DesktopManager desktopManager;

    public ControlProgramSummaryTab(ControlProgram controlProgram, EmfSession session, 
            ManageChangeables changeablesList, MessagePanel messagePanel, 
            EmfConsole parentConsole, DesktopManager desktopManager) {
        super.setName("summary");
        this.controlProgram = controlProgram;
        this.session = session;
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManager;
//        this.decFormat = new DecimalFormat("0.###E0");
//        this.verifier = new NumberFieldVerifier("Summary tab: ");
//        setLayout();
    }

    public void display(ControlProgram controlProgram) throws EmfException {
        setLayout();
    }

    private void setLayout() throws EmfException {
        super.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getBorderedPanel(createmMainSection(), ""), BorderLayout.CENTER);
        super.add(panel, BorderLayout.CENTER);
    }

    private JPanel createmMainSection() throws EmfException {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JPanel panelTop = new JPanel(new SpringLayout());
        // panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Name:", name(), panelTop);
        layoutGenerator.addLabelWidgetPair("Description:", new ScrollableComponent(description()), panelTop);
        
        layoutGenerator.addLabelWidgetPair("Start Date:", start(), panelTop);
        layoutGenerator.addLabelWidgetPair("End Date:", end(), panelTop);
        layoutGenerator.addLabelWidgetPair("Last Modified Date:", lastModifiedDate(), panelTop);
        layoutGenerator.addLabelWidgetPair("Creator:", creator(), panelTop);
        layoutGenerator.makeCompactGrid(panelTop, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad
        panel.add(panelTop);
        
        JPanel panelBottom = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator1 = new SpringLayoutGenerator();

        layoutGenerator1.addLabelWidgetPair("Type of Control Program:", typeOfAnalysis(), panelBottom);
//        layoutGenerator1.addLabelWidgetPair("Dataset:", start(), panelBottom);
//        layoutGenerator1.addLabelWidgetPair("Dataset Version:", end(), panelBottom);
        
        
        dsType = new ComboBox(presenter.getDatasetTypes());
        if (controlProgram.getDataset() != null) dsType.setSelectedItem(controlProgram.getDataset().getDatasetType());
        dsType.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                dataset.setText("");
                fillVersions(null, new Version[] {});
            }
        });
        changeablesList.addChangeable(dsType);
        dsType.setPreferredSize(preferredSize);
        layoutGenerator.addLabelWidgetPair("Dataset Type:", dsType, panelBottom);

        layoutGenerator.addLabelWidgetPair("Dataset:", datasetPanel(), panelBottom);
        Version[] versions = getVersions(controlProgram.getDataset());
        version = new ComboBox(versions);
        fillVersions(controlProgram.getDataset(), versions);
        
        if (controlProgram.getDatasetVersion() != null) {
            for (Version v : versions) {
                if (v.getVersion() == controlProgram.getDatasetVersion())
                    version.setSelectedItem(v);
            }
        }
        
        changeablesList.addChangeable(version);
        version.setPreferredSize(preferredSize);
        layoutGenerator.addLabelWidgetPair("Version:", version, panelBottom);

        
        
        // Lay out the panel.
        layoutGenerator1.makeCompactGrid(panelBottom, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        panel.add(panelBottom);
        return panel;
    }

    private JPanel datasetPanel() {

        dataset = new TextField("dataset", 35);
        dataset.setEditable(false);
        EmfDataset inputDataset = controlProgram.getDataset();
        if(inputDataset!= null )
            dataset.setText(controlProgram.getDataset().getName());

        changeablesList.addChangeable(dataset);
        dataset.setToolTipText("Press select button to choose from a dataset list.");
        selectButton = new AddButton("Select", selectAction());
        selectButton.setMargin(new Insets(1, 2, 1, 2));
        Button viewButton = new BorderlessButton("View", viewDatasetAction()); 
        Button viewDataButton = new BorderlessButton("View Data", viewDataAction()); 
        JPanel invPanel = new JPanel(new BorderLayout(5,0));
        JPanel subPanel = new JPanel(new BorderLayout(5,0));

        invPanel.add(dataset, BorderLayout.LINE_START);
        subPanel.add(selectButton, BorderLayout.LINE_START);
        subPanel.add(viewDataButton, BorderLayout.LINE_END);
        invPanel.add(subPanel);
        invPanel.add(viewButton, BorderLayout.LINE_END );
        return invPanel;
    }
    
    private ComboBox typeOfAnalysis() throws EmfException {
        ControlProgramType[] types = session.controlProgramService().getControlProgramTypes();
        controlProgramTypeCombo = new ComboBox("Choose a control program type", types);
        controlProgramTypeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                ControlProgramType controlProgramType = (ControlProgramType)controlProgramTypeCombo.getSelectedItem();
                presenter.doChangeControlProgramType(controlProgramType);
            }
        });
        if (controlProgram.getControlProgramType() != null) controlProgramTypeCombo.setSelectedItem(controlProgram.getControlProgramType());
        changeablesList.addChangeable(controlProgramTypeCombo);
        
        return controlProgramTypeCombo;
    }

    private JPanel getBorderedPanel(JPanel component, String border) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new Border(border));
        panel.add(component);

        return panel;
    }


    private JLabel lastModifiedDate() {
        return createLeftAlignedLabel(controlProgram.getLastModifiedDate() != null ? CustomDateFormat.format_MM_DD_YYYY_HH_mm(controlProgram.getLastModifiedDate()) : "");
    }

    private JLabel creator() {
        return createLeftAlignedLabel(controlProgram.getCreator() != null ? controlProgram.getCreator().getName() : "");
    }

    private TextArea description() {
        description = new TextArea("description", controlProgram.getDescription() != null ? controlProgram.getDescription() : "", 40, 3);
        changeablesList.addChangeable(description);

        return description;
    }

    private TextField name() {
        name = new TextField("name", 40);
        name.setText(controlProgram.getLastModifiedDate() != null ? controlProgram.getName() : "");
        name.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(name);

        return name;
    }

    private TextField start() {
        startDate = new TextField("start", 40);
        startDate.setText(controlProgram.getStartDate() != null ? CustomDateFormat.format_MM_DD_YYYY(controlProgram.getStartDate()) : "");
        startDate.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(startDate);

        return startDate;
    }

    private TextField end() {
        endDate = new TextField("end", 40);
        endDate.setText(controlProgram.getEndDate() != null ? CustomDateFormat.format_MM_DD_YYYY(controlProgram.getEndDate()) : "");
        endDate.setToolTipText("Optional, the date efter which the control program no longer applies.");
        endDate.setMaximumSize(new Dimension(300, 15));
        changeablesList.addChangeable(endDate);

        return endDate;
    }

    private JLabel createLeftAlignedLabel(String name) {
        JLabel label = new JLabel(name);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        return label;
    }

    public void save(ControlProgram controlProgram) throws EmfException {
        messagePanel.clear();
        if (name.getText().trim().length() ==0)
            throw new EmfException("The name is missing.");

        if (controlProgram.getDataset() == null)
            throw new EmfException("The control program dataset is missing.");

        if (controlProgram.getControlProgramType() == null)
            throw new EmfException("The control program type is missing.");

        controlProgram.setName(name.getText());
        controlProgram.setDescription(description.getText());
        Version v = (Version)version.getSelectedItem();
        if (v != null)
            controlProgram.setDatasetVersion(v.getVersion());
        else
            controlProgram.setDatasetVersion(null);
        controlProgram.setControlProgramType((ControlProgramType)controlProgramTypeCombo.getSelectedItem());
        controlProgram.setStartDate(getDate(startDate, true));
        controlProgram.setEndDate(getDate(endDate, false));
    }

    private Date getDate(TextField date, boolean required) throws EmfException {
        String dateAsString = date.getText().trim();
        if (required && dateAsString.length() == 0) {
            throw new EmfException("The " + date.getName() + " date is missing.");
        }
        try {
            if (dateAsString.length() == 0) {
                return null;
            }
            return CustomDateFormat.parse_MMddyyyy(dateAsString);
        } catch (Exception e) {
            throw new EmfException("Please Correct the Date Format(MM/dd/yyyy)");
        }
    }

    public void notifyControlProgramTypeChange(ControlProgramType controlProgramType) {
        // NOTE Auto-generated method stub
        
    }
    
    public void observe(ControlProgramSummaryTabPresenter presenter) {
        this.presenter = presenter;
    }

    private Version[] getVersions(EmfDataset dataset) {
        Version[] versions = new Version[] {};
        try {
            versions = presenter.getVersions(dataset);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
        return versions;
    }

    protected void fillVersions(EmfDataset dataset, Version[] versions) {
        version.setEnabled(true);
        version.removeAllItems();
        version.setModel(new DefaultComboBoxModel(versions));
        version.revalidate();
        if (versions.length > 0)
            version.setSelectedIndex(getDefaultVersionIndex(versions, dataset));
    }

    private int getDefaultVersionIndex(Version[] versions, EmfDataset dataset) {
        int defaultversion = dataset.getDefaultVersion();

        for (int i = 0; i < versions.length; i++)
            if (defaultversion == versions[i].getVersion())
                return i;

        return 0;
    }

    private Action selectAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    messagePanel.clear();
                    doAddWindow();
                } catch (Exception e1) {
                    messagePanel.setError(e1.getMessage());
                }
            }
        };
    }
    
    private Action viewDatasetAction() {
        return new AbstractAction(){
            public void actionPerformed(ActionEvent event) {
                try {
                    viewAction();
                } catch (EmfException e) {
                    messagePanel.setError("Error viewing dataset: " + e.getMessage());
                }
            }
        };
    }
    
    private Action viewDataAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                viewDataSetData();
            }
        };
    }

    private void viewDataSetData() {
        EmfDataset dataset = controlProgram.getDataset();
        if (dataset == null) {
            messagePanel.setMessage("Dataset is not available.");
            return;
        }

        Version[] versions = getVersions(controlProgram.getDataset());
        Version version = null;
        
        if (controlProgram.getDatasetVersion() != null) {
            for (Version v : versions) {
                if (v.getVersion() == controlProgram.getDatasetVersion())
                    version = v;
            }
        }

        showDatasetDataViewer(dataset, version);
    }
    
    private void showDatasetDataViewer(EmfDataset dataset, Version version) {
        try {
            DataViewer dataViewerView = new DataViewer(dataset, parentConsole, desktopManager);
            DataViewPresenter dataViewPresenter = new DataViewPresenter(dataset, version, getTableName(dataset), dataViewerView, session);
            dataViewPresenter.display();
        } catch (EmfException e) {
//            displayError(e.getMessage());
        }
    }
    protected String getTableName(Dataset dataset) {
        InternalSource[] internalSources = dataset.getInternalSources();
        String tableName = "";
        if (internalSources.length > 0)
            tableName = internalSources[0].getTable();
        return tableName;
    }
    private void doAddWindow() throws Exception {
        DatasetType type = (DatasetType) dsType.getSelectedItem();
        
        if (type == null)
            throw new EmfException("Please select a valid dataset type.");
        
        DatasetType[] datasetTypes = new DatasetType[]{type};
        InputDatasetSelectionDialog view = new InputDatasetSelectionDialog (parentConsole);
        InputDatasetSelectionPresenter presenter = new InputDatasetSelectionPresenter(view, session, datasetTypes);
        if (datasetTypes.length == 1)
            presenter.display(datasetTypes[0], true);
        else
            presenter.display(null, true);
        if (view.shouldCreate())
            setDatasets(presenter.getDatasets());
    }
    
    private void setDatasets(EmfDataset [] datasets) {
        dataset.setText(datasets[0].getName());
        controlProgram.setDataset(datasets[0]);
        fillVersions(datasets[0], getVersions(datasets[0]));
    }
    
    protected void viewAction() throws EmfException {
        messagePanel.clear();

        if (controlProgram.getDataset() == null) {
            messagePanel.setMessage("Dataset is not available.");
            return;
        }

        PropertiesViewPresenter datasetViewPresenter = new PropertiesViewPresenter(
                presenter.getDataset(controlProgram.getDataset().getId()), session);
        DatasetPropertiesViewer view = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
        datasetViewPresenter.doDisplay(view);
    }
    public void clearMsgPanel() {
        this.messagePanel.clear();
    }

}
