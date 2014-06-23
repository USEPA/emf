package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.commons.data.Pollutant;
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

public class TargetPollutantChooser extends JDialog {

    private Pollutant[] allPollutants;

    private ListWidget allPollutantsListwidget;

    private ListWidget pollutantsListWidget;

    public TargetPollutantChooser(Pollutant[] allPollutants, ListWidget pollutantsListWidget, EmfConsole parentConsole) {
        super(parentConsole);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        
        setTitle("Select Target Pollutants");
        this.allPollutants = allPollutants;
        this.pollutantsListWidget = pollutantsListWidget;
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
        allPollutantsListwidget = new ListWidget(allPollutants);
        JScrollPane pane = new JScrollPane(allPollutantsListwidget);
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
        Object[] values = allPollutantsListwidget.getSelectedValues();
        Pollutant[] selectedValues = Arrays.asList(values).toArray(new Pollutant[0]);
        addNewPollutants(selectedValues);
    }

    private void addNewPollutants(Pollutant[] selected) {
        for (int i = 0; i < selected.length; i++) {
            if (!pollutantsListWidget.contains(selected[i]))
                pollutantsListWidget.addElement(selected[i]);
        }
    }
}
