package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.cost.ControlMeasureMonth;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class MonthChooser extends JDialog {

    private ControlMeasureMonth[] allMonths;

    private ListWidget allMonthsListwidget;

    private ListWidget monthsListWidget;

    public MonthChooser(ControlMeasureMonth[] allMonths, ListWidget monthsListWidget, EmfConsole parentConsole) {
        super(parentConsole);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        
        setTitle("Select Months");
        this.allMonths = allMonths;
        this.monthsListWidget = monthsListWidget;
    }

    public void display() {
        JScrollPane pane = listWidget();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(pane);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(panel);

        pack();
        setSize(300, 300);
        setLocation(ScreenUtils.getPointToCenter(this));
        setModal(true);
        setVisible(true);
    }

    private JScrollPane listWidget() {
        allMonthsListwidget = new ListWidget(allMonths);
        JScrollPane pane = new JScrollPane(allMonthsListwidget);
        return pane;
    }

    private JPanel buttonPanel() {
        OKButton okButton = new OKButton(okAction());
        CancelButton cancelButton = new CancelButton(cancelAction());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                disposeView();
            }
        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setSelectedValues();
                disposeView();
            }
        };
    }

    private void disposeView() {
        dispose();
        setVisible(false);
    }

    private void setSelectedValues() {
        Object[] values = allMonthsListwidget.getSelectedValues();
        ControlMeasureMonth[] selectedValues = Arrays.asList(values).toArray(new ControlMeasureMonth[0]);
        addNewMonths(selectedValues);
    }

    private void addNewMonths(ControlMeasureMonth[] selected) {
        //see if "All Months" or "None" was choosen, if so, ignore all the rest of the months...
        for (int i = 0; i < selected.length; i++) {
            if (selected[i].getMonth() == 0 || selected[i].getMonth() == -1) {
                monthsListWidget.removeAllElements();
                monthsListWidget.addElement(selected[i]);
                return;
            }
        }
        //a month was choosen, get rid of the "All Months" and "None" item(s)
        Object[] items = monthsListWidget.getAllElements();
        for (int i = 0; i < items.length; i++) {
            if (((ControlMeasureMonth)items[i]).getMonth() == 0 
                    || ((ControlMeasureMonth)items[i]).getMonth() == -1) {
                monthsListWidget.removeElements(new Object[] {items[i]});
            }
        }
        for (int i = 0; i < selected.length; i++) {
            if (selected[i].getMonth() != 0 && !monthsListWidget.contains(selected[i]))
                monthsListWidget.addElement(selected[i]);
        }
    }
}
