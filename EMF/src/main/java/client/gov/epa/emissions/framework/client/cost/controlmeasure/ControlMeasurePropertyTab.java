package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.EditButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.commons.gui.buttons.ViewButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureProperty;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

//import java.util.ArrayList;
//import java.util.List;

public class ControlMeasurePropertyTab extends JPanel implements ControlMeasureTabView {

    private MessagePanel messagePanel;

//    private ControlMeasure measure;
    private ControlMeasurePresenter controlMeasurePresenter;
    private EmfConsole parent;
    private EmfSession session;
    
    private ControlMeasure measure;
    private Button addButton;
    private Button editButton;
    private Button viewButton;
    private Button removeButton;
    private JPanel mainPanel;
    private JPanel tablePanel;

    
    private CMPropertiesTableData tableData;
    private ManageChangeables changeables;
    private DesktopManager desktopManager;
    
    private SelectableSortFilterWrapper table;

    public ControlMeasurePropertyTab(ControlMeasure measure, EmfSession session, 
            ManageChangeables changeables, MessagePanel messagePanel, 
            EmfConsole parent, ControlMeasurePresenter controlMeasurePresenter, 
            DesktopManager desktopManager) {
        
        this.mainPanel = new JPanel(new BorderLayout());
        this.parent = parent;
       
        this.session = session; 
        this.messagePanel = messagePanel;
        this.measure = measure;
        this.changeables = changeables;
        this.controlMeasurePresenter = controlMeasurePresenter;
        this.desktopManager = desktopManager;
     
        this.tablePanel = new JPanel(new BorderLayout());
        doLayout(measure);
        
        super.setName("CMProperty tab");
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
    }
 

    private void doLayout(ControlMeasure measure) {
        updateMainPanel(measure.getProperties());
        mainPanel.setBorder(BorderFactory.createTitledBorder("Property"));
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        this.add(mainPanel, BorderLayout.CENTER);
        this.add(buttonPanel(), BorderLayout.SOUTH);
    }

    private void updateMainPanel(ControlMeasureProperty[] properties) {
        tablePanel.removeAll();
        tableData = new CMPropertiesTableData(properties);
        table = new SelectableSortFilterWrapper(parent, tableData, null);
        tablePanel.add(table);
        tablePanel.validate();
    }
    
    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        addButton = new AddButton(addAction());
        panel.add(addButton);
        editButton = new EditButton(editAction());
        panel.add(editButton);
        viewButton = new ViewButton(viewAction());
        viewButton.setVisible(false);
        panel.add(viewButton);
        removeButton = new RemoveButton(removeAction());
        panel.add(removeButton);

        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.LINE_START);

        return container;
    }

    private Action addAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doAdd();
            }
        };
    }

    private Action editAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                //if this is a new measure, make sure we save it before we proceed.
                //the efficiency editor needs to have a measure in the db first!
                if (measure.getId() == 0) {
                    try {
                        controlMeasurePresenter.doSave(true);
                    } catch (EmfException e1) {
                        messagePanel.setError("Cannot save control measure: " + e1.getMessage());
                        return;
                    }
                }


                List list = table.selected();
                
                if (list.size() == 0) {
                    messagePanel.setError("Please select an item to edit.");
                    return;
                }
                
                ControlMeasureProperty[] properties = (ControlMeasureProperty[]) list.toArray(new ControlMeasureProperty[0]);

                for (ControlMeasureProperty property : properties) {
                    doEdit(property);
                }
            }
        };
    }

    private Action viewAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                List list = table.selected();
                
                if (list.size() == 0) {
                    messagePanel.setError("Please select an item to view.");
                    return;
                }
                
                ControlMeasureProperty[] properties = (ControlMeasureProperty[]) list.toArray(new ControlMeasureProperty[0]);

                for (ControlMeasureProperty property : properties) {
                    doView(property);
                }
            }
        };
    }

    protected void doAdd() {
        messagePanel.clear();
        MeasurePropertyWindow view = new MeasurePropertyWindow("Add Property", changeables, 
                desktopManager, session);
        MeasurePropertyPresenter presenter = new MeasurePropertyPresenter(this, view);
        
        presenter.display(measure);
    }

    protected void doEdit(ControlMeasureProperty property) {
        messagePanel.clear();
        MeasurePropertyWindow view = new MeasurePropertyWindow("Edit Property", changeables, 
                desktopManager, session);
        MeasurePropertyPresenter presenter = new MeasurePropertyPresenter(this, view);
        
        presenter.display(measure, property);
    }

    protected void doView(ControlMeasureProperty property) {
        messagePanel.clear();
        MeasurePropertyWindow view = new MeasurePropertyWindow("View Property", changeables, 
                desktopManager, session);
        MeasurePropertyPresenter presenter = new MeasurePropertyPresenter(this, view);
        
        presenter.display(measure, property);
        view.viewOnly();
    }

    private Action removeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
               
                try {
                    messagePanel.clear();
                    doRemove(); 
                    
                } catch (Exception e1) {
                    messagePanel.setError("Could not remove equation type");
                }
            

            }
        };
    }
    private void doRemove() {
        ControlMeasureProperty[] selectedProperties = table.selected().toArray(new ControlMeasureProperty[0]);
        
        //nothing was selected so show a message
        if (selectedProperties.length == 0) {
            messagePanel.setError("Please select an item to remove.");
            return;
        }
        
        String title = "Warning";
        String message = "Are you sure you want to remove the properties?";
        int selection = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            modify();
            tableData.remove(selectedProperties);
            refresh();
        }
    }

    public void modify() {
        controlMeasurePresenter.doModify();
    }

    public void viewOnly() {
        addButton.setVisible(false);
        editButton.setVisible(false);
        removeButton.setVisible(false);
        viewButton.setVisible(true);
    }


    public void refresh() {
        tableData.refresh();
        table.refresh(tableData);
        tablePanel.removeAll();
        tablePanel.add(table);
        super.validate();
    }


    public void refresh(ControlMeasure measure) {
        
//        measure.setEquationTypes(cmEquationTypes);
       
    }


    public void add(ControlMeasureProperty property) {
        tableData.add(property);
        refresh();
        modify();
    }

    public void save(ControlMeasure measure) {
        measure.setProperties(tableData.sources());
    }
}


