package gov.epa.emissions.commons.gui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComboBox;

public class EditableComboBox extends JComboBox implements Changeable {
    private Changeables changeables;

    private boolean changed = false;

    public EditableComboBox(Object[] objects) {
        super(objects);
        setEditable(true);
    }

    private void addListeners() {
        addItemChangeListener();
        addKeyMotionListener();
    }

    private void addItemChangeListener() {
        addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                notifyChanges();
            }
        });
    }

    private void addKeyMotionListener() {
        this.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
                notifyChanges();
            }

            public void keyPressed(KeyEvent e) {
                // NO OP;
            }

            public void keyReleased(KeyEvent e) {
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
        addListeners();
    }
}
