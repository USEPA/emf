package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class OutputFieldsPanel extends JPanel implements OutputFieldsPanelView {

    private TextField outputName;
    
    private ComboBox jobCombo;

    private JLabel dsTypeLabel;

    private TextField datasetLabel;
    
    private JLabel sector;
    
    private JLabel datasetCreationDate, datasetCreater; 
    
    private TextField execName;

    private TextField status;

    private TextArea message;

    private String[] datasetValues; 
    
    //private EmfDataset dataset;

    private MessagePanel messagePanel;

    private ManageChangeables changeablesList;

    private OutputFieldsPanelPresenter presenter;
    
    private Dimension preferredSize = new Dimension(480, 20);

    private CaseOutput output;
    
    private Button selectButton;
    
    private EmfSession session;

    private EmfConsole parentConsole;  
    
    public OutputFieldsPanel(MessagePanel messagePanel, ManageChangeables changeablesList, 
            EmfConsole parentConsole) {
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.parentConsole = parentConsole;
    }

    public void display(CaseOutput output, JComponent container, EmfSession session) throws EmfException {
        this.output = output;
        this.session = session;
        this.datasetValues = presenter.getDatasetValues();
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        outputName = new TextField("name", output.getName(), 43);
        //outputName.setEditable(true);
        outputName.setPreferredSize(preferredSize);
        changeablesList.addChangeable(outputName);
        layoutGenerator.addLabelWidgetPair("Output Name:", outputName, panel);
        CaseJob[] jobArray = presenter.getCaseJobs();
        jobCombo= new ComboBox(jobArray);
        jobCombo.setSelectedIndex(presenter.getJobIndex(output.getJobId(), jobArray));
        changeablesList.addChangeable(jobCombo);
        jobCombo.setPreferredSize(preferredSize);
        jobCombo.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                updateSectorName((CaseJob)jobCombo.getSelectedItem());
            }
        });
        layoutGenerator.addLabelWidgetPair("Job:", jobCombo, panel);

        dsTypeLabel = new JLabel("");
        layoutGenerator.addLabelWidgetPair("Dataset Type:", dsTypeLabel, panel);

        // fill in dataset
        layoutGenerator.addLabelWidgetPair("Dataset:", datasetPanel(), panel);
        
        sector=new JLabel("");
        datasetCreationDate=new JLabel("");
        datasetCreater=new JLabel("");
        
        execName = new TextField("program", output.getExecName(), 43);
        //execName.setEditable(true);
        execName.setPreferredSize(preferredSize);
        changeablesList.addChangeable(execName);
        
        status = new TextField("status", output.getStatus(), 43);
        //status.setEditable(true);
        status.setPreferredSize(preferredSize);
        changeablesList.addChangeable(status);
  
        layoutGenerator.addLabelWidgetPair("Sector:", sector, panel);
        layoutGenerator.addLabelWidgetPair("Dataset Creator:", datasetCreater, panel);
        layoutGenerator.addLabelWidgetPair("Creation Date:", datasetCreationDate, panel);
        layoutGenerator.addLabelWidgetPair("Exec name:", execName, panel);
        layoutGenerator.addLabelWidgetPair("Status:", status, panel);
        layoutGenerator.addLabelWidgetPair("Message:", message(), panel);
        
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 10, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        container.add(panel);
        
        updateSectorName((CaseJob)jobCombo.getSelectedItem());
        datasetLabels();
    }
    
    private JPanel datasetPanel() {

        datasetLabel = new TextField("dataset", 38);
        datasetLabel.setEditable(false);
        //datasetLabel.setText( getDatasetProperty("name"));
        changeablesList.addChangeable(datasetLabel);
        datasetLabel.setToolTipText("Press select button to choose from a dataset list.");
        selectButton = new AddButton("Select", selectAction());
        selectButton.setMargin(new Insets(1, 2, 1, 2));

        JPanel invPanel = new JPanel(new BorderLayout(5, 0));

        invPanel.add(datasetLabel, BorderLayout.LINE_START);
        invPanel.add(selectButton);
        return invPanel;
    }

    private void datasetLabels() {
        String dsTypeText ="";
        String dateText="";
        String createrText="";
        String datasetName = "";

        if (datasetValues != null ){
            datasetName=getDatasetProperty("name");
            if (datasetName.trim().length()>0){
                dateText=getDatasetProperty("createdDateTime").substring(0, 16);
                createrText=getDatasetProperty("creator");
                dsTypeText=getDatasetProperty("datasetType");  
            }
        }
        datasetLabel.setText(datasetName);
        datasetCreationDate.setText(dateText);
        datasetCreater.setText(createrText);
        dsTypeLabel.setText(dsTypeText);
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
    
    protected void updateSectorName(CaseJob job) {
        String sectorText="";
        Sector sector1=job.getSector();
        if (sector1!=null)
            sectorText=sector1.getName();
        
        sector.setText(sectorText);
    }
    
    private ScrollableComponent message() {
        message = new TextArea("message", output.getMessage(), 40, 3 );
        changeablesList.addChangeable(message);
        //message.setPreferredSize(new Dimension(450, 80));

        ScrollableComponent descScrollableTextArea = new ScrollableComponent(message);
        descScrollableTextArea.setPreferredSize(new Dimension(480, 80));
        return descScrollableTextArea;
    }
    
    private void doAddWindow() throws Exception {
        DatasetType[] dsTypeArray = presenter.getDSTypes();
        DatasetType type = presenter.getDatasetType(dsTypeLabel.getText());
        //DatasetType[] datasetTypes = new DatasetType[] { type };
        InputDatasetSelectionDialog view = new InputDatasetSelectionDialog(parentConsole);
        InputDatasetSelectionPresenter datasePresenter = new InputDatasetSelectionPresenter(view, session, dsTypeArray);
        if (type != null)
            datasePresenter.display(type, true);
        else
            datasePresenter.display(null, true);

        setDatasets(datasePresenter.getDatasets());
    }
    
    private void setDatasets(EmfDataset[] datasets) {
        if (datasets == null || datasets.length == 0) {
            updateDataset(null);
            datasetValues=presenter.getDatasetValues();
            datasetLabels();
            return; 
        }
        updateDataset(datasets[0]);
        datasetValues=presenter.getDatasetValues();
        datasetLabels();
    }

    public CaseOutput setFields() {
        updateOutputName();
        updateJob();
        output.setDatasetType(dsTypeLabel.getText());
//        System.out.println(output.getDatasetType());
        //updateDataset();
        updateMessage();
        updateExecName();
        updateStatus();
        return output;
    }

//    private void updateDatasetType() {
//         output.setDatasetType(dsTypeCombo.getSelectedItem().toString());
//    }
    
   private void updateJob() {
        CaseJob job = (CaseJob) jobCombo.getSelectedItem();
        if (job==null || job.getName().equalsIgnoreCase(OutputFieldsPanelPresenter.ALL_FOR_SECTOR)) {
            output.setJobId(0);
            return;
        }
        output.setJobId(job.getId());
    }

    private void updateOutputName() {
        output.setName(outputName.getText().trim());
    }
    
    private void updateExecName() {
        output.setExecName(execName.getText().trim());
    }
    
    private void updateStatus() {
        output.setStatus(status.getText().trim());
    }
    
    private void updateMessage() {
        output.setMessage(message.getText());
    }
    
    private void updateDataset(EmfDataset dataset) {
        if (dataset == null ) {
            output.setDatasetId(0);
            return;
        }
        output.setDatasetId(dataset.getId());
    }

    public void observe(OutputFieldsPanelPresenter presenter) {
        this.presenter = presenter;
    }

    public CaseOutput getOutput() {
        return this.output;
    }

    public void validateFields() throws EmfException {
        if (outputName.getText().trim().equalsIgnoreCase(""))
            throw new EmfException("Please specify an output name.");
        if (((CaseJob)jobCombo.getSelectedItem()).getId()==0)
            throw new EmfException("Please choose a valid job.");
    }

    private String getDatasetProperty(String property) {
        if (datasetValues == null)
            return null;
        
        String value = null;

        for (String values : datasetValues) {
            if (values.startsWith(property))
                value = values.substring(values.indexOf(",") + 1);
        }
        return value;
    }
    
    public void viewOnly(){
        outputName.setEditable(false);
        execName.setEditable(false);
        status.setEditable(false);
        message.setEditable(false);
    }
}
