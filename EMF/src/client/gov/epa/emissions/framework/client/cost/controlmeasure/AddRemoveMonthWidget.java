package gov.epa.emissions.framework.client.cost.controlmeasure;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.cost.ControlMeasureMonth;
import gov.epa.emissions.framework.ui.ListWidget;

public class AddRemoveMonthWidget extends JPanel {

    private ListWidget monthsList;

    private ControlMeasureMonth[] allMonths;

    private EmfConsole parentConsole;
    
    private Button addButton;
    private Button removeButton;

    public AddRemoveMonthWidget(ControlMeasureMonth[] allMonths, ManageChangeables changeables, EmfConsole parentConsole) {
        this.allMonths = allMonths;
        this.parentConsole = parentConsole;
        setupLayout(changeables);

    }

    public void setMonths(ControlMeasureMonth[] months) {
        //if brand new measure then default to all months,
        if (months != null && months.length > 0) {
            for (int i = 0; i < months.length; i++) {
                monthsList.addElement(months[i]);
            }
        } else {
            ControlMeasureMonth month = new ControlMeasureMonth();
            month.setMonth((short)0);
            monthsList.addElement(month);
        }
    }

    public ControlMeasureMonth[] getMonths() {
        return Arrays.asList(monthsList.getAllElements()).toArray(new ControlMeasureMonth[0]);
    }

    private void setupLayout(ManageChangeables changeables) {
        this.monthsList = new ListWidget(new Sector[0]);
        changeables.addChangeable(monthsList);
        
        JScrollPane pane = new JScrollPane(monthsList);
        JPanel buttonPanel = addRemoveButtonPanel();

        this.setLayout(new BorderLayout(1, 1));
        this.add(pane);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel addRemoveButtonPanel() {
        JPanel panel = new JPanel();
        addButton = new AddButton("Add", addAction());
        addButton.setMnemonic('d');
        removeButton = new RemoveButton("Remove", removeAction());
        removeButton.setMnemonic('e');
        addButton.setMargin(new Insets(1, 2, 1, 2));      
        removeButton.setMargin(new Insets(1, 2, 1, 2));
        panel.add(addButton);
        panel.add(removeButton);

        return panel;
    }

    private Action removeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                removeMonths();
            }
        };
    }

    private Action addAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                addMonths();
            }
        };
    }

    private void addMonths() {
        MonthChooser monthSelector = new MonthChooser(allMonths, monthsList, parentConsole);
        monthSelector.display();
    }

    private void removeMonths() {
        Object[] removeValues = monthsList.getSelectedValues();
        //if trying to remove "All Months" don't do it, we need to keep atleast one item...
        if (removeValues.length == 1 && ((ControlMeasureMonth)removeValues[0]).getMonth() == 0)
            return;
        monthsList.removeElements(removeValues);
        //make sure there is always a "All Months" item...
        if (monthsList.getAllElements().length == 0)
            monthsList.addElement(new ControlMeasureMonth());        
    }

    public void viewOnly() {
        addButton.setVisible(false);
        removeButton.setVisible(false);  
    }
}
