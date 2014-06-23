package gov.epa.emissions.framework.client.casemanagement.parameters;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.EmptyStrings;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.casemanagement.parameters.ValueType;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.Dimension;
import java.util.Date;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class ParameterFieldsPanel extends JPanel implements ParameterFieldsPanelView {

    private EditableComboBox parameterName;

    private EditableComboBox program;
    
    private ComboBox region;

    private ComboBox sector;

    private EditableComboBox envtVar;

    private CheckBox required;

    private CheckBox local;

    private ManageChangeables changeablesList;

    private ParameterFieldsPanelPresenter presenter;

    private CaseParameter parameter;

    private ComboBox jobs;

    private ComboBox varTypes;

    private TextArea notes;

    private TextField envValue;

    private TextArea purpose;

    private TextField order;

    private MessagePanel messagePanel;
    
    private int model_id; 

    public ParameterFieldsPanel(MessagePanel messagePanel, ManageChangeables changeablesList) {
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
    }

    public void display(CaseParameter param, int model_id, JComponent container) throws EmfException {
        this.parameter = param;
        this.model_id = model_id;
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        String width = EmptyStrings.create(65);
        Dimension preferredSize = new Dimension(300, 20);

        parameterName = new EditableComboBox(presenter.getParameterNames(model_id));
        addPopupMenuListener(parameterName, "parameternames");
        parameterName.setSelectedItem(param.getParameterName());
        parameterName.setPreferredSize(preferredSize);
        changeablesList.addChangeable(parameterName);
        parameterName.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Parameter Name:", parameterName, panel);

        program = new EditableComboBox(presenter.getPrograms(model_id));
        addPopupMenuListener(program, "programs");
        program.setSelectedItem(param.getProgram());
        program.setPreferredSize(preferredSize);
        changeablesList.addChangeable(program);
        program.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Program:", program, panel);

        envtVar = new EditableComboBox(presenter.getEnvtVars(model_id));
        addPopupMenuListener(envtVar, "envtvars");
        envtVar.setSelectedItem(param.getEnvVar());
        envtVar.setPreferredSize(preferredSize);
        changeablesList.addChangeable(envtVar);
        envtVar.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Envt. Variable:", envtVar, panel);

        varTypes = new ComboBox(presenter.getValueTypes());
        varTypes.setSelectedItem(param.getType());
        varTypes.setPreferredSize(preferredSize);
        addPopupMenuListener(varTypes, "vartypes");
        changeablesList.addChangeable(varTypes);
        varTypes.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Type:", varTypes, panel);

        envValue = new TextField("value", param.getValue(),27);
        envValue.setPreferredSize(preferredSize);
        changeablesList.addChangeable(envValue);
        layoutGenerator.addLabelWidgetPair("Value:", envValue, panel);
        
        region = new ComboBox(presenter.getGeoRegions());
        region.setSelectedItem(param.getRegion() == null ? region.getItemAt(0) : param.getRegion());
        addPopupMenuListener(region, "grids");
        region.setPreferredSize(preferredSize);
        changeablesList.addChangeable(region);
        region.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Region:", region, panel);

        sector = new ComboBox(presenter.getSectors());
        sector.setSelectedItem(param.getSector() == null ? sector.getItemAt(0) : param.getSector());
        sector.setPreferredSize(preferredSize);
        addPopupMenuListener(sector, "sectors");
        changeablesList.addChangeable(sector);
        sector.setPrototypeDisplayValue(width);
        layoutGenerator.addLabelWidgetPair("Sector:", sector, panel);

        setJob(param);
        changeablesList.addChangeable(jobs);
        jobs.setPrototypeDisplayValue(width);
        jobs.setPreferredSize(preferredSize);
        layoutGenerator.addLabelWidgetPair("Job:", jobs, panel);

        purpose = new TextArea("purpose", param.getPurpose());
        changeablesList.addChangeable(purpose);
        ScrollableComponent scrolpane = new ScrollableComponent(purpose);
        scrolpane.setPreferredSize(new Dimension(300, 80));
        layoutGenerator.addLabelWidgetPair("Purpose:", scrolpane, panel);

        order = new TextField("order", param.getOrder() + "",27);
        order.setPreferredSize(preferredSize);
        changeablesList.addChangeable(order);
        layoutGenerator.addLabelWidgetPair("Order:", order, panel);

        local = new CheckBox("");
        changeablesList.addChangeable(local);

        required = new CheckBox("");
        changeablesList.addChangeable(required);

        JPanel checkBoxPanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layout = new SpringLayoutGenerator();
        JPanel showPanel = new JPanel();
        showPanel.add(new JLabel(EmptyStrings.create(25)));
        showPanel.add(new JLabel("Local?"));
        showPanel.add(new JLabel(EmptyStrings.create(20)));
        showPanel.add(local);
        layout.addWidgetPair(required, showPanel, checkBoxPanel);
        layout.makeCompactGrid(checkBoxPanel, 1, 2, 0, 0, 0, 0);
        layoutGenerator.addLabelWidgetPair("Required?", checkBoxPanel, panel);

        notes = new TextArea("notes", param.getNotes());
        changeablesList.addChangeable(notes);
        ScrollableComponent notes_scrollpane = new ScrollableComponent(notes);
        notes_scrollpane.setPreferredSize(new Dimension(300, 80));
        layoutGenerator.addLabelWidgetPair("Notes:", notes_scrollpane, panel);
        
        layoutGenerator.addLabelWidgetPair("Parent case ID:", new JLabel("" + this.parameter.getParentCaseId()), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 13, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 8);// xPad, yPad

        populateFields(parameter);
        container.add(panel);
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
        if (toget.equals("parameternames"))
            return presenter.getParameterNames(model_id);

        if (toget.equals("programs"))
            return presenter.getPrograms(model_id);

        if (toget.equals("envtvars"))
            return presenter.getEnvtVars(model_id);

        if (toget.equals("vartypes"))
            return presenter.getValueTypes();
        
        if (toget.equals("grids"))
            return presenter.getGeoRegions();

        if (toget.equals("sectors"))
            return presenter.getSectors();

        return null;
    }

    private void setJob(CaseParameter param) throws EmfException {
        jobs = new ComboBox(presenter.getCaseJobs());
        jobs.setSelectedIndex(presenter.getJobIndex(param.getJobId()));
    }

    private void populateFields(CaseParameter param) {
        required.setSelected(param.isRequired());
        local.setSelected(param.isLocal());
    }

    public CaseParameter setFields() throws EmfException {
        updateParameterName();
        updateProgram();
        updateEnvtVar();
        updateRegion(); 
        updateSector();
        parameter.setRequired(required.isSelected());
        parameter.setLocal(local.isSelected());
        parameter.setLastModifiedDate(new Date());
        updateJob();
        parameter.setType((ValueType) varTypes.getSelectedItem());
        parameter.setValue(envValue.getText() == null ? "" : envValue.getText().trim());
        parameter.setPurpose(purpose.getText());
        parameter.setNotes(notes.getText());
        parameter.setOrder(Float.parseFloat(order.getText()));

        return parameter;
    }

    private void updateJob() {
        Object job = jobs.getSelectedItem();

        if (job == null)
            return;

        parameter.setJobId(presenter.getJobId((CaseJob) job));
    }

    private void updateParameterName() throws EmfException {
        Object selected = parameterName.getSelectedItem();
        parameter.setParameterName(presenter.getParameterName(selected, model_id));
        //parameter.getParameterName().setModelToRunId(model_id);
    }

    private void updateProgram() throws EmfException {
        Object selected = program.getSelectedItem();
        if (selected == null) {
            parameter.setProgram(null);
            return;
        }

        parameter.setProgram(presenter.getCaseProgram(selected, model_id));
    }

    private void updateEnvtVar() throws EmfException {
        Object selected = envtVar.getSelectedItem();
        if (selected == null) {
            parameter.setEnvVar(null);
            return;
        }

        parameter.setEnvVar(presenter.getParameterEnvtVar(selected, model_id));
    }
    
    private void updateRegion() {
        GeoRegion selected = (GeoRegion) region.getSelectedItem();

        if (selected.getName().equalsIgnoreCase("")) {
            parameter.setRegion(null);
            return;
        }

        parameter.setRegion(selected);
    }

    private void updateSector() {
        Sector selected = (Sector) sector.getSelectedItem();

        if (selected.getName().equalsIgnoreCase("All sectors")) {
            parameter.setSector(null);
            return;
        }

        parameter.setSector(selected);
    }

    public void observe(ParameterFieldsPanelPresenter presenter) {
        this.presenter = presenter;
    }

    public void validateFields() throws EmfException {
        Object selectedProg = program.getSelectedItem();
        if (parameterName.getSelectedItem() == null)
            throw new EmfException("Please specify an parameter name.");

        if (selectedProg == null || selectedProg.toString().trim().equals(""))
            throw new EmfException("Please specify a program.");

        try {
            Float.parseFloat(order.getText());
        } catch (NumberFormatException e) {
            throw new EmfException("Please put a float number in Order field.");
        }
    }

    public CaseParameter getParameter() {
        return this.parameter;
    }
    
    public void viewOnly(){
        parameterName.setEditable(false);
        program.setEditable(false);
        envtVar.setEditable(false);
        notes.setEditable(false);
        envValue.setEditable(false);
        purpose.setEditable(false);
        order.setEditable(false);
        required.setEnabled(false);
        local.setEnabled(false);
    }

}
