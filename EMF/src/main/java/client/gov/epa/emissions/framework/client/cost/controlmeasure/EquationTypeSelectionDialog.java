package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.cost.EquationType;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class EquationTypeSelectionDialog extends JDialog implements EquationTypeSelectionView {

    private Component dialogParent;

    private ComboBox equationTypeCombo;
    
    private EquationType equationType;

    private ManageChangeables changeables;
    
    public EquationTypeSelectionDialog(EmfConsole parent, Component dialogParent, ManageChangeables changeables) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        this.dialogParent = dialogParent;
        this.changeables = changeables;
        setModal(true);
    }

    public void display(EquationType[] equationTypes) {

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(buildEquationTypeCombo(equationTypes), BorderLayout.NORTH);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        contentPane.add(panel);

        setTitle("Select Equation Type");
        this.pack();
        this.setSize(400, 150);
        this.setLocation(ScreenUtils.getPointToCenter(dialogParent));
        this.setVisible(true);
    }

    private JPanel buildEquationTypeCombo(EquationType[] equationTypes) {
        JPanel panel = new JPanel(new BorderLayout());
        
        equationTypeCombo = new ComboBox("Choose an equation type", equationTypes);
  

        panel.add(equationTypeCombo, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return panel;
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new OKButton(okAction()));
        panel.add(new CancelButton(cancelAction()));
        return panel;
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //set Equation Type to null, they clicked cancel
                equationType = null;
                setVisible(false);
                dispose();
            }

        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //set Equation Type...
                equationType = (EquationType)equationTypeCombo.getSelectedItem();
                changeables.addChangeable(equationTypeCombo);
//                System.out.println("add changeables: " +changeables.toString().trim());
                setVisible(false);
                dispose();
            }
        };
    }

    public EquationType getEquationType() {
        return equationType;
    }

    public void observe(EquationTypeSelectionPresenter presenter) {
        //
    }
}