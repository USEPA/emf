package gov.epa.emissions.commons.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

public class CheckBox extends JCheckBox implements Changeable {

    private Changeables changeables;

    private boolean changed = false;

    public CheckBox(String title) {
        this(title, false);
    }

    public CheckBox(String title, boolean selected) {
        super(title, selected);
    }

    private void addActionListener() {
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                notifyChanges();
            }
        });
    }

    public void clear() {
        this.changed = false;
    }

    void notifyChanges() {
        changed = true;
        if (changeables != null)
            changeables.onChanges();
    }

    public boolean hasChanges() {
        return this.changed;
    }

    public void observe(Changeables changeables) {
        this.changeables = changeables;
        addActionListener();
    }

}
