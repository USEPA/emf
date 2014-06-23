package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.BorderlessButton;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.Changeable;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EmptyStrings;
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
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class SetInputFieldsPanel extends InputFieldsPanel{

    private TextField datasetTxt;
    
    private Dimension preferredSize = new Dimension(380, 20);
    
    public SetInputFieldsPanel(MessagePanel messagePanel, ManageChangeables changeablesList,
            EmfConsole parentConsole, DesktopManager desktopManager) {
        super(messagePanel, changeablesList, parentConsole, desktopManager);
    }

    public void display(CaseInput input, JComponent container, int modelToRunId, EmfSession session) throws EmfException {
        this.input = input;
        this.session=session; 
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        String width = EmptyStrings.create(125);
        JLabel inputName = new JLabel(input.getInputName().toString());
        layoutGenerator.addLabelWidgetPair("Input Name:", inputName, panel);

        JLabel envtVar = new JLabel(input.getEnvtVars()==null? "":input.getEnvtVars().toString());
        layoutGenerator.addLabelWidgetPair("Envt. Variable:", envtVar, panel);

        JLabel sector = new JLabel(input.getSector()==null? "All jobs for sector" :input.getSector().toString());
        layoutGenerator.addLabelWidgetPair("Sector:", sector, panel);
        
        JLabel jobName = new JLabel(presenter.getJobName(input.getCaseJobID()));
        layoutGenerator.addLabelWidgetPair("Job:", jobName, panel);
        
        JLabel dsType = new JLabel(input.getDatasetType()==null? "":input.getDatasetType().toString());
        layoutGenerator.addLabelWidgetPair("Dataset Type:", dsType, panel);
        
        layoutGenerator.addLabelWidgetPair("Dataset:", datasetPanel(), panel);

        version = new ComboBox(new Version[]  { input.getVersion() });
        fillVersions(input.getDataset());
        
        if (input.getVersion() != null)
            version.setSelectedItem(input.getVersion());
        
        changeablesList.addChangeable(version);
        version.setPreferredSize(preferredSize);
        version.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Version:", version, panel);
        

        JLabel required = new JLabel(input.isRequired()? "True" : "False" );
        layoutGenerator.addLabelWidgetPair("Required?", required, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 8, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        container.add(panel);
    }

    private JPanel datasetPanel() {

        datasetTxt = new TextField("dataset", 26);
        datasetTxt.setEditable(false);
        EmfDataset inputDataset = input.getDataset();
        if(inputDataset!= null )
            datasetTxt.setText(input.getDataset().getName());
        
        changeablesList.addChangeable(datasetTxt);
        datasetTxt.setToolTipText("Press select button to choose from a dataset list.");
        Button selectButton = new AddButton("Select", selectAction());
        selectButton.setMargin(new Insets(1, 2, 1, 2));
        Button viewButton = new BorderlessButton("View", viewDatasetAction());
        JPanel invPanel = new JPanel(new BorderLayout(5,0));

        invPanel.add(datasetTxt, BorderLayout.LINE_START);
        invPanel.add(selectButton);
        invPanel.add(viewButton, BorderLayout.LINE_END );
        return invPanel;
    }
    
    private Action selectAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
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
    
    protected void viewAction() throws EmfException {
        messagePanel.clear();

        if (input.getDataset() == null) {
            messagePanel.setMessage("Dataset is not available.");
            return;
        }

        presenter.viewDataset(input.getDataset().getId(), parentConsole, desktopManager);
    }
    private void doAddWindow() throws Exception {
        DatasetType type = input.getDatasetType();
        if (type == null)
            throw new EmfException("Dataset Type doesn't exist. ");
        DatasetType[] datasetTypes = new DatasetType[]{type};
        InputDatasetSelectionDialog view = new InputDatasetSelectionDialog (parentConsole);
        InputDatasetSelectionPresenter presenter = new InputDatasetSelectionPresenter(view, session, datasetTypes);
        presenter.display(datasetTypes[0], true);
        if (view.shouldCreate())
            setDatasets(presenter.getDatasets());
    }
    
    private void setDatasets(EmfDataset [] datasets) {
        datasetTxt.setText(datasets[0].getName());
        updateDataset(datasets[0]);
        fillVersions(datasets[0]);
    }

    public CaseInput setFields() throws EmfException {
        //updateDataset();
        updateVersion();
        input.setLastModifiedDate(new Date());
        return input;
    }

    public CaseInput getInput() {
        return this.input;
    }

    public void addChangeable(Changeable changeable) {
        // NOTE Auto-generated method stub
        
    }

}
