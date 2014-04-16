package gov.epa.emissions.framework.client.cost.controlmeasure;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Action;

public class ComboBoxResetListener implements ItemListener {
    
    private boolean first = true;

    private Action action;

    public ComboBoxResetListener(Action action) {
        this.action = action;
    }

    public void itemStateChanged(ItemEvent e) {

        if (this.first) {

            this.first = false;
            this.action.actionPerformed(null);
            this.first = true;
        }
    }
}