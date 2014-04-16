package gov.epa.emissions.commons.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.KeyStroke;

public class Button extends JButton {

    public Button(String label, final Action action) {
        super(label);
        super.setName(toCanonicalName(label));
        
        addActionForEnterKeyPress(label, action);
        addActionListener(action);
    }

    protected void addActionListener(final Action action) {
        super.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                action.actionPerformed(event);
            }
        });
    }

    private String toCanonicalName(String name) {
        name = name.trim().replaceAll(" ", "");

        return name.substring(0, 1).toLowerCase() + name.substring(1, name.length());
    }

    private void addActionForEnterKeyPress(String name, Action action) {
        super.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), name);
        super.getActionMap().put(name, action);
    }

}
