package gov.epa.emissions.commons.gui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JPasswordField;

public class PasswordField extends JPasswordField implements Changeable {
    private Changeables changeables;

    private boolean changed = false;

    public PasswordField(String name, int size) {
        super(size);
        super.setName(name);
    }

    private void addKeyListener() {
        addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
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

    public void observe(Changeables list) {
        this.changeables = list;
        addKeyListener();
    }

}
