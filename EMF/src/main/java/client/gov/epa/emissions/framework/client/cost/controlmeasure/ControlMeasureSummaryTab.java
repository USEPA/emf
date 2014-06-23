package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.CoSTConstants;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.Utils;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.casemanagement.editor.AddRemoveSectorWidget;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.ControlTechnologies;
import gov.epa.emissions.framework.client.data.SourceGroups;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.ControlMeasureMonth;
import gov.epa.emissions.framework.services.cost.ControlMeasureNEIDevice;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.NumberFieldVerifier;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.util.Date;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class ControlMeasureSummaryTab extends JPanel implements ControlMeasureTabView {

    protected ControlMeasure measure;

    protected TextField name;

    protected TextArea description;

    private JLabel creator;

    protected ComboBox majorPollutant;

    protected EditableComboBox sourceGroup;

    protected EditableComboBox controlTechnology;

    protected TextField deviceCode;

    protected TextField dateReviewed;

    protected TextField equipmentLife;

    protected TextField costYear;

    protected TextField abbreviation;

    protected TextField lastModifiedTime;

    protected TextField lastModifiedBy;

    protected ComboBox cmClass;

    private AddRemoveSectorWidget sectorsWidget;

    private AddRemoveMonthWidget monthsWidget;
    
//    protected TextField dataSources;

    protected MessagePanel messagePanel;

    private ManageChangeables changeablesList;

    protected EmfSession session;

    private ControlMeasureClass[] allClasses;

    protected int deviceId, year;

    protected float cost, life, effectivness, penetration, minUnctrldEmiss, maxUnctrldEmiss;

    protected Pollutant[] allPollutants;

    private NumberFieldVerifier verifier;

    private EmfConsole parentConsole;

    public ControlMeasureSummaryTab(ControlMeasure measure, EmfSession session, MessagePanel messagePanel,
            ManageChangeables changeablesList, EmfConsole parentConsole) {
        super.setName("summary");
        this.measure = measure;
        this.messagePanel = messagePanel;
        this.changeablesList = changeablesList;
        this.session = session;
        this.parentConsole = parentConsole;

        super.setLayout(new BorderLayout());
        super.add(createOverviewSection(), BorderLayout.PAGE_START);
        super.add(createAttributeSection(), BorderLayout.CENTER);
        super.add(createSectorMonthSection(), BorderLayout.PAGE_END);
        this.verifier = new NumberFieldVerifier("Summary tab: ");
    }

    protected void populateFields() {
        String cmName = measure.getName();
        Date modifiedTime = measure.getLastModifiedTime();
        name.setText(getText(cmName));
        description.setText(getText(measure.getDescription()));
        creator.setText(getText(measure.getCreator().getName()));
        majorPollutant.setSelectedItem(measure.getMajorPollutant());
        sourceGroup.setSelectedItem(measure.getSourceGroup());
        controlTechnology.setSelectedItem(measure.getControlTechnology());
        cmClass.setSelectedItem(measure.getCmClass());
//        cmClass.setSelectedItem(getText(measure.getCmClass()));
        // costYear.setText(measure.getCostYear() + "");
        deviceCode.setText(getNEIDevices());
        equipmentLife.setText(measure.getEquipmentLife() == null ? "" : measure.getEquipmentLife()+"");
        if (modifiedTime != null)
            lastModifiedTime.setText(CustomDateFormat.format_YYYY_MM_DD_HH_MM(modifiedTime));
        lastModifiedBy.setText(measure.getLastModifiedBy() + "");
        abbreviation.setText(getText(measure.getAbbreviation()));
        dateReviewed.setText(formatDateReviewed());
//        dataSources.setText(getText(measure.getDataSouce()));
        sectorsWidget.setSectors(measure.getSectors());
        monthsWidget.setMonths(measure.getMonths());
    }

    private String getNEIDevices() {
        String neiDeviceList = "";
        ControlMeasureNEIDevice[] neiDevices = measure.getNeiDevices();
        if (neiDevices != null && neiDevices.length > 0) {
            for (ControlMeasureNEIDevice neiDevice : neiDevices) 
                if (neiDeviceList.length() > 0) 
                    neiDeviceList += ", " + neiDevice.getNeiDeviceCode();
                else
                    neiDeviceList = neiDevice.getNeiDeviceCode() + "";
        }
        return neiDeviceList;
    }
    
    private String formatDateReviewed() {
        return CustomDateFormat.format_MM_DD_YYYY(measure.getDateReviewed());
    }

    private String getText(String value) {
        return (value != null) ? value : "";
    }

    private JPanel createOverviewSection() {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addWidgetPair(createLeftOverview(), createRightOverview(), panel);
        widgetLayout(1, 2, 5, 5, 5, 5, layoutGenerator, panel);

        return panel;
    }

    private JPanel createLeftOverview() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("Control measure name", 27);
        changeablesList.addChangeable(name);
        layoutGenerator.addLabelWidgetPair("Name:", name, panel);

        // description
        description = new TextArea("description", measure.getDescription());
        changeablesList.addChangeable(description);
        ScrollableComponent descPane = new ScrollableComponent(description);
        descPane.setPreferredSize(new Dimension(300, 65));//50));
        layoutGenerator.addLabelWidgetPair("Description:", descPane, panel);

        widgetLayout(2, 2, 5, 5, 5, 5, layoutGenerator, panel);
        return panel;
    }

    private JPanel createRightOverview() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        abbreviation = new TextField("Abbreviation", 12);
        changeablesList.addChangeable(abbreviation);
        layoutGenerator.addLabelWidgetPair("Abbreviation:", abbreviation, panel);

        creator = new JLabel(session.user().getName());
        layoutGenerator.addLabelWidgetPair("Creator:", creator, panel);

        lastModifiedTime = new TextField("Last Modified Time", 15);
        changeablesList.addChangeable(lastModifiedTime);
        lastModifiedTime.setEnabled(false);
        lastModifiedTime.setOpaque(false);
        lastModifiedTime.setDisabledTextColor(Color.BLACK);
        lastModifiedTime.setBorder(BorderFactory.createEmptyBorder());
        layoutGenerator.addLabelWidgetPair("Last Modified Time:", lastModifiedTime, panel);

        lastModifiedBy = new TextField("Last Modified By", 15);
        changeablesList.addChangeable(lastModifiedBy);
        lastModifiedBy.setEnabled(false);
        lastModifiedBy.setOpaque(false);
        lastModifiedBy.setDisabledTextColor(Color.BLACK);
        lastModifiedBy.setBorder(BorderFactory.createEmptyBorder());
        layoutGenerator.addLabelWidgetPair("Last Modified By:", lastModifiedBy, panel);

        widgetLayout(4, 2, 5, 5, 5, 5, layoutGenerator, panel);

        return panel;
    }

//    private JPanel tempPanel(int width, int height) {
//        JPanel tempPanel = new JPanel();
//        tempPanel.setPreferredSize(new Dimension(width, height));
//        return tempPanel;
//    }

//    private JPanel createAttributeSection() {
//
//        JPanel container = new JPanel(new BorderLayout());
////        container.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
//
//        container.add(createLeftPanel(), BorderLayout.WEST);
//        container.add(createRightPanel(), BorderLayout.EAST);
//
//        return container;
//    }
    
    private JPanel createAttributeSection() {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY));

  //    panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addWidgetPair(createLeftPanel(), createRightPanel(), panel);
        widgetLayout(1, 2, 5, 5, 5, 8, layoutGenerator, panel);

        return panel;
    }

    private JPanel createLeftPanel() {
        JPanel container = new JPanel(new BorderLayout());
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        try {
            allPollutants = session.dataCommonsService().getPollutants();
            majorPollutant = new ComboBox("Choose a pollutant", allPollutants);
        } catch (EmfException e1) {
            messagePanel.setError("Could not retrieve Pollutants");
        }
        changeablesList.addChangeable(majorPollutant);
        layoutGenerator.addLabelWidgetPair("Major Pollutant:", majorPollutant, panel);

        try {
            controlTechnology = new EditableComboBox(getControlTechnologies());
            controlTechnology.setPreferredSize(new Dimension(250, 25));
        } catch (EmfException e) {
            messagePanel.setError("Could not retrieve all Control Technologies");
        }
        changeablesList.addChangeable(controlTechnology);
        layoutGenerator.addLabelWidgetPair("Control Technology:", controlTechnology, panel);

        try {
            sourceGroup = new EditableComboBox(getSourceGroups());
            sourceGroup.setPreferredSize(new Dimension(250, 25));
        } catch (EmfException e) {
            messagePanel.setError("Could not retrieve Source Groups");
        }

        changeablesList.addChangeable(sourceGroup);
        layoutGenerator.addLabelWidgetPair("Source Group:", sourceGroup, panel);

        deviceCode = new TextField("NEI Device code", 20);
        deviceCode.setEditable(false);
        changeablesList.addChangeable(deviceCode);
        layoutGenerator.addLabelWidgetPair("NEI Device code(s):", deviceCode, panel);
        
//       layoutGenerator.addLabelWidgetPair("Sectors:", sectors(), panel);

//        layoutGenerator.addLabelWidgetPair("", tempPanel(20, 20), panel);
        widgetLayout(4, 2, 5, 5, 8, 5, layoutGenerator, panel);
        container.add(panel, BorderLayout.NORTH);
        return container;
    }
    
    protected boolean checkIfSuperUser() {
        try {
            User currentUser = session.user();
            String costSUs = session.controlStrategyService().getCoSTSUs(); //presenter.getCoSTSUs();
            //if this is found, then every one is considered an SU (really used for State Installations....)
            if (costSUs.equals("ALL_USERS")) return true;
            StringTokenizer st = new StringTokenizer(costSUs,"|");
            while ( st.hasMoreTokens()) {
                String token = st.nextToken();
                if ( token.equals( currentUser.getUsername())) {
                    return true;
                }
            }
            return false;

        } catch (EmfException e1) {
            // NOTE Auto-generated catch block
            e1.printStackTrace();
            messagePanel.setMessage(e1.getMessage());
            return false;
        }        
    }    

    private JPanel createRightPanel() {
        JPanel container = new JPanel(new BorderLayout());
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        try {
            allClasses = session.controlMeasureService().getMeasureClasses();
            cmClass = new ComboBox("Choose a class", allClasses);
        } catch (EmfException e1) {
            messagePanel.setError("Could not retrieve control measure classes");
        }
        changeablesList.addChangeable(cmClass);
        layoutGenerator.addLabelWidgetPair("Class:", cmClass, panel);

        equipmentLife = new TextField("Equipment life", 15);
        changeablesList.addChangeable(equipmentLife);
        layoutGenerator.addLabelWidgetPair("Equipment life (yrs):", equipmentLife, panel);

        dateReviewed = new TextField("Date Reviewed", 15);
        changeablesList.addChangeable(dateReviewed);
        layoutGenerator.addLabelWidgetPair("Date Reviewed:", dateReviewed, panel);

//        dataSources = new TextField("Data Sources:", 15);
//        layoutGenerator.addLabelWidgetPair("Data Sources:", dataSources, panel);
        
        widgetLayout(3, 2, 5, 5, 5, 8, layoutGenerator, panel);

        container.add(panel, BorderLayout.NORTH);
        return container;
    }
    
    
    private JPanel createSectorMonthSection() {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addWidgetPair(createSector(), createMonth(), panel);
        widgetLayout(1, 2, 5, 5, 5, 5, layoutGenerator, panel);

        return panel;
    }
    
    private JPanel createSector() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Sectors: \n", sectors(), panel);

        widgetLayout(1, 2, 5, 5, 5, 5, layoutGenerator, panel);
        return panel;
    }
    
    private JPanel createMonth() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Months: \n", months(), panel);

        widgetLayout(1, 2, 5, 5, 10, 10, layoutGenerator, panel);
        return panel;
    }

    private JPanel sectors() {
        sectorsWidget = new AddRemoveSectorWidget(getAllSectors(), changeablesList, parentConsole);
        sectorsWidget.setPreferredSize(new Dimension(250, 100));
        return sectorsWidget;
    }

    private JPanel months() {
        monthsWidget = new AddRemoveMonthWidget(getAllMonths(), changeablesList, parentConsole);
        monthsWidget.setPreferredSize(new Dimension(150, 100));
        return monthsWidget;
    }

    private void widgetLayout(int rows, int cols, int initX, int initY, int xPad, int yPad,
            SpringLayoutGenerator layoutGenerator, JPanel panel) {
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, rows, cols, // rows, cols
                initX, initY, // initialX, initialY
                xPad, yPad);// xPad, yPad
    }

    public void save(ControlMeasure measure) throws EmfException {
        messagePanel.clear();
        validateFields();
        measure.setName(name.getText());
        measure.setDescription(description.getText());
        measure.setCreator(session.user());
        //leave untouched for now, these are not editable for now...
        measure.setNeiDevices(measure.getNeiDevices());
        if (equipmentLife.getText().trim().length() > 0)
            measure.setEquipmentLife(life);
        else 
            measure.setEquipmentLife(null);
        updatePollutant();
        updateControlTechnology();
        updateSourceGroup();
        updateDateReviewed(measure);
        updateClass();
//        measure.setCmClass(selectedClass(cmClass.getSelectedItem()));
        measure.setLastModifiedTime(new Date());
        measure.setLastModifiedBy(session.user().getName());
        measure.setAbbreviation(abbreviation.getText());
//        measure.setDataSouce(dataSources.getText());
        measure.setSectors(sectorsWidget.getSectors());
        if (monthsWidget.getMonths() == null || monthsWidget.getMonths().length == 0)
            throw new EmfException("Summary tab: The months for the measure is missing.");
        measure.setMonths(monthsWidget.getMonths());
        
        // save items modified in efficiency tab
    }

    private void updateDateReviewed(ControlMeasure measure) throws EmfException {

        String dateStr = this.dateReviewed.getText().trim();
        String fieldName = "Date Reviewed";
        Utils.validateDate(dateStr, fieldName);
        try {
            measure.setDateReviewed(CustomDateFormat.parse_MMddyyyy(dateStr));
        } catch (ParseException e) {
            throw new EmfException("Error while parsing date '" + dateStr + "' for '" + fieldName + "'.");
        }
    }

//    private String selectedClass(Object selectedItem) {
//        return selectedItem == null ? "" : selectedItem + "";
//    }

    private void updateControlTechnology() throws EmfException {
        Object selected = controlTechnology.getSelectedItem();
        if (selected instanceof String) {
            String controltechnologyName = (String) selected;
            if (controltechnologyName.length() == 0) {
                measure.setControlTechnology(null);
                return;
            }
            ControlTechnology controltechnology = controltechnology(controltechnologyName);// checking for
            // duplicates
            measure.setControlTechnology(controltechnology);
        } else if (selected instanceof ControlTechnology) {
            measure.setControlTechnology((ControlTechnology) selected);
        }
    }

    private ControlTechnology controltechnology(String name) throws EmfException {
        return new ControlTechnologies(getControlTechnologies()).get(name);
    }
    
    private ControlTechnology[] getControlTechnologies() throws EmfException {
        return session.controlMeasureService().getControlTechnologies();
    }

    private void updateSourceGroup() throws EmfException {
        Object selected = sourceGroup.getSelectedItem();

        if (selected instanceof String) {
            String sourcegroupName = (String) selected;
            if (sourcegroupName.length() == 0) {
                measure.setSourceGroup(null);
                return;
            }
            SourceGroup sourcegroup = sourcegroup(sourcegroupName);// checking for duplicates
            measure.setSourceGroup(sourcegroup);

        } else if (selected instanceof SourceGroup) {
            measure.setSourceGroup((SourceGroup) selected);
        }
    }

    private SourceGroup sourcegroup(String name) throws EmfException {
        return new SourceGroups(getSourceGroups()).get(name);
    }

   private SourceGroup[] getSourceGroups() throws EmfException {
       return session.dataCommonsService().getSourceGroups();
   }
    
    private void updatePollutant() {
        Object selected = majorPollutant.getSelectedItem();
        measure.setMajorPollutant((Pollutant) selected);
    }

    private void updateClass() {
        Object selected = cmClass.getSelectedItem();
        measure.setCmClass((ControlMeasureClass) selected);
    }

    private void validateFields() throws EmfException {
        messagePanel.clear();

        if (name.getText().trim().length() == 0 && abbreviation.getText().trim().length() == 0)
            throw new EmfException("Summary tab: A name and abbreviation must be specified");

        if (name.getText().trim().length() == 0)
            throw new EmfException("Summary tab: A name must be specified");

        if (abbreviation.getText().trim().length() == 0) {
            throw new EmfException("Summary tab: An abbreviation must be specified");
        }

        //make sure its not longer than 10 characters
        if (abbreviation.getText().trim().length() > CoSTConstants.CM_ABBREV_LEN) { //10) { 
            throw new EmfException("Summary tab: An abbreviation must not be longer than " + CoSTConstants.CM_ABBREV_LEN + " characters");
        }

        //make sure its does not contain a space
        if (abbreviation.getText().trim().indexOf(" ") > 0) {
            throw new EmfException("Summary tab: An abbreviation can not contain a space");
        }

        if (majorPollutant.getSelectedItem() == null)
            throw new EmfException("Summary tab: Please select a major pollutant");

        if (cmClass.getSelectedItem() == null)
            throw new EmfException("Summary tab: Please select a class");

//        if (deviceCode.getText().trim().length() > 0)
//            deviceId = verifier.parseInteger(deviceCode);

        if (equipmentLife.getText() != null && equipmentLife.getText().trim().equals("0"))
            throw new EmfException("Summary tab: Please input a non-zero value for equipment life or leave the field blank.");
        
        if (equipmentLife.getText().trim().length() > 0)
            life = verifier.parseFloat(equipmentLife);

        Utils.validateDate(this.dateReviewed.getText().trim(), "Date Reviewed");
    }

    private Sector[] getAllSectors() {
        try {
            return session.dataCommonsService().getSectors();
        } catch (EmfException e) {
            messagePanel.setError("Could not get all the sectors");
        }
        return null;
    }

    //A month of Zero indicates "All Months"
    private ControlMeasureMonth[] getAllMonths() {
        ControlMeasureMonth[] months = new ControlMeasureMonth[14];
        for (int i = -1; i < 13; i++) {
            ControlMeasureMonth month = new ControlMeasureMonth();
            month.setMonth((short)i);
            months[i + 1] = month;
        }
        return months;
    }

    public void refresh(ControlMeasure measure) {
        this.measure = measure;
    }

    public void modify() {
        populateLastModifiedFields();
    }

    private void populateLastModifiedFields() {
        lastModifiedTime.setText(CustomDateFormat.format_YYYY_MM_DD_HH_MM(new Date()));
        lastModifiedBy.setText(session.user().getName());
    }

    public void viewOnly() {
        
        name.setEditable(false);
        description.setEditable(false);
        abbreviation.setEditable(false);
        majorPollutant.setEditable(false);
        controlTechnology.setEditable(false);
        equipmentLife.setEditable(false);
//        dataSources.setEditable(false);
        dateReviewed.setEditable(false);
        sourceGroup.setEditable(false);
        cmClass.setEditable(false);
        sectorsWidget.viewOnly();
        monthsWidget.viewOnly();
    
        this.disableComboBoxChanges();
    }
    
    protected void setTextFieldCaretPosition() {
        name.setCaretPosition(0);
        ((JTextField)((JComboBox)sourceGroup).getEditor().getEditorComponent()).setCaretPosition(0);
        ((JTextField)((JComboBox)controlTechnology).getEditor().getEditorComponent()).setCaretPosition(0);
        description.setCaretPosition(0);
    }
    
    private void disableComboBoxChanges() {

        this.majorPollutant.addItemListener(new ComboBoxResetListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                majorPollutant.setSelectedItem(measure.getMajorPollutant());
            }
        }));

        this.sourceGroup.addItemListener(new ComboBoxResetListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                sourceGroup.setSelectedItem(measure.getSourceGroup());
            }
        }));

        this.controlTechnology.addItemListener(new ComboBoxResetListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                controlTechnology.setSelectedItem(measure.getControlTechnology());
            }
        }));

        this.cmClass.addItemListener(new ComboBoxResetListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                cmClass.setSelectedItem(measure.getCmClass());
            }
        }));
    }
}
