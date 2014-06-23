package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureProperty;
import gov.epa.emissions.framework.services.cost.ControlMeasurePropertyCategory;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class MeasurePropertyWindow extends DisposableInteralFrame {

    protected ControlMeasure measure;

    protected EmfSession session;

    protected MessagePanel messagePanel;

    protected TextField name;

    protected TextField dataType;

    protected TextField dbFieldName;

    protected TextArea value;

    protected TextField units;

    protected EditableComboBox category;

    protected ControlMeasureProperty property;

    protected ControlMeasurePropertyCategory[] allCategories;

    protected TextField lastModifiedTime;

    protected TextField lastModifiedBy;
    
    static int counter = 0;
    
    protected SaveButton saveButton;
    
    protected Button cancelButton;
    
    protected MeasurePropertyPresenter presenter;

    private boolean newProperty = false;
    
    private ManageChangeables changeablesList;

    private boolean viewOnly;
    
    public MeasurePropertyWindow(String title, ManageChangeables changeablesList, 
            DesktopManager desktopManager, EmfSession session) {
        super(title, new Dimension(675, 500), desktopManager);
        super.setMinimumSize(new Dimension(675,460));
        this.session = session;
        this.changeablesList = changeablesList;
    }

    public void save() {
        try {
            messagePanel.clear();
            doSave();
            if (!newProperty)
                presenter.refresh();
            else
                presenter.add(property);
            disposeView();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
            return;
        }
    }

    public void display(ControlMeasure measure, ControlMeasureProperty property) {
        String name = measure.getName();
        if (name == null)
            name = "New Control Measure";
        super.setLabel(super.getTitle() + " " + (counter++) + " for " + name);
        JPanel layout = createLayout();
        super.getContentPane().add(layout);
        super.display();
        this.property = property;
        populateFields();
        resetChanges();

    }

    //use this method when adding a new property
    public void display(ControlMeasure measure) {
        display(measure, new ControlMeasureProperty());
        newProperty = true;
    }

    private void populateFields() {
        name.setText((property.getName() != null ? property.getName() : ""));
        category.setSelectedItem(property.getCategory());
        units.setText((property.getUnits() != null ? property.getUnits() : ""));
        dataType.setText((property.getDataType() != null ? property.getDataType() : ""));
        dbFieldName.setText((property.getDbFieldName() != null ? property.getDbFieldName() : ""));
        value.setText((property.getValue() != null ? property.getValue() : ""));
    }

    public void observe(MeasurePropertyPresenter presenter) {
        this.presenter = presenter;

    }
    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        panel.add(recordPanel());

 //       panel.add(detailPanel());
        panel.add(buttonsPanel());

        return panel;
    }


    private JPanel recordPanel() {
  
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();

        container.add(LeftRecordPanel());
        panel.add(container, BorderLayout.NORTH);//LINE_START);

        return panel;
    }

    private Component LeftRecordPanel() {
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        JPanel panel = new JPanel(new SpringLayout());

        name = new TextField("Name", 50);
        this.addChangeable(name);
        changeablesList.addChangeable(name);
        layoutGenerator.addLabelWidgetPair("Name:", name, panel);

        try {
            allCategories = session.controlMeasureService().getPropertyCategories();
            //category = new EditableComboBox("Select One", allCategories);
            category = new EditableComboBox(allCategories);
            category.setPreferredSize(new Dimension(113, 30));
        } catch (EmfException e) {
            messagePanel.setError("Could not retrieve measure property categories");
        }
        this.addChangeable(category);
        changeablesList.addChangeable(category);
        layoutGenerator.addLabelWidgetPair("Category:", category, panel);

        units = new TextField("Units", 25);
        this.addChangeable(units);
        changeablesList.addChangeable(units);
        layoutGenerator.addLabelWidgetPair("Units:", units, panel);

        dataType = new TextField("Data Type", 25);
        this.addChangeable(dataType);
        changeablesList.addChangeable(dataType);
        layoutGenerator.addLabelWidgetPair("Data Type:", dataType, panel);

        dbFieldName = new TextField("DB Field Name", 25);
        this.addChangeable(dbFieldName);
        changeablesList.addChangeable(dbFieldName);
        layoutGenerator.addLabelWidgetPair("DB Field Name:", dbFieldName, panel);

        value = new TextArea("Value", "", 50, 12);
        ScrollableComponent detailPane = new ScrollableComponent(value);
//      detailPane.setPreferredSize(new Dimension(540, 50));

        this.addChangeable(value);
        changeablesList.addChangeable(value);
        layoutGenerator.addLabelWidgetPair("Value:", detailPane, panel);
//        layoutGenerator.makeCompactGrid(detailContainer, 1, 2, 30, 5, 10, 10);
        
        widgetLayout(6, 2, 5, 5, 10, 10, layoutGenerator, panel);

        return panel;
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        saveButton = new SaveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        getRootPane().setDefaultButton(saveButton);
        panel.add(saveButton);

        cancelButton = new CancelButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                closeWindow();
            }
        });
        panel.add(cancelButton);

        return panel;
    }

    protected void doSave() throws EmfException {
        //lets validate the inputs...
        String exception = "";
        String propertyName = name.getText().trim();
        if (propertyName.length() == 0)
            exception = "Missing name. ";
        String propertyCategory = "";
        Object selected = category.getSelectedItem();
        if (selected instanceof String) 
            propertyCategory = ((String) selected).trim();
        else if (selected instanceof ControlMeasurePropertyCategory) 
            propertyCategory = ((ControlMeasurePropertyCategory)selected).getName().trim();
        if (propertyCategory.length() == 0)
            exception += "Missing category. ";
        String propertyUnits = units.getText().trim();
//        if (propertyUnits.length() == 0)
//            exception += "Missing units. ";
        String propertyDataType = dataType.getText().trim();
        if (propertyDataType.length() == 0)
            exception += "Missing data type. ";
        String propertyValue = value.getText().trim();
        if (propertyValue.length() == 0)
            exception += "Missing value. ";
        
        if (exception.length() > 0)
            throw new EmfException(exception);

        property.setName(propertyName);
        updateCategory();
        property.setUnits(propertyUnits);
        property.setDataType(propertyDataType);
        property.setDbFieldName(dbFieldName.getText().trim());  //this field is optional
        property.setValue(propertyValue);
    }

    private void widgetLayout(int rows, int cols, int initX, int initY, int xPad, int yPad,
            SpringLayoutGenerator layoutGenerator, JPanel panel) {
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, rows, cols, // rows, cols
                initX, initY, // initialX, initialY
                xPad, yPad);// xPad, yPad
    }
    
    private void closeWindow() {
        if (shouldDiscardChanges())
            disposeView();
    }

    public void viewOnly() {
        
        this.viewOnly = true;
        
        saveButton.setVisible(false);
        cancelButton.setText("Close");

        this.name.setEditable(false);
        this.dataType.setEditable(false);
        this.dbFieldName.setEditable(false);
        this.value.setEditable(false);
        this.units.setEditable(false);
        this.category.setEditable(false);
        this.disableComboBoxChanges();
    }
    
    private void disableComboBoxChanges() {

        this.category.addItemListener(new ComboBoxResetListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                category.setSelectedItem(property.getCategory());
            }
        }));

    }

    private void updateCategory() throws EmfException {
        Object selected = category.getSelectedItem();
        if (selected instanceof String) {
            String categoryName = ((String) selected).trim();
            if (categoryName.length() > 0) {
                ControlMeasurePropertyCategory propertyCategory = category(categoryName);// checking for duplicates
                property.setCategory(propertyCategory);
            }
        } else if (selected instanceof ControlMeasurePropertyCategory) {
            property.setCategory((ControlMeasurePropertyCategory) selected);
        }
    }

    private ControlMeasurePropertyCategory category(String categoryName) throws EmfException {
        return new ControlMeasurePropertyCategories(session.controlMeasureService().getPropertyCategories(), session).get(categoryName);
    }

    @Override
    public boolean shouldDiscardChanges() {
        return this.viewOnly || super.shouldDiscardChanges();
    }    
}