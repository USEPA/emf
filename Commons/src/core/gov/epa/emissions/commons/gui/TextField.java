package gov.epa.emissions.commons.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

public class TextField extends JTextField implements Changeable {
    
    private Changeables changeables;

    private boolean changed = false;

    public TextField(String name, int size) {
        super(size);
        super.setName(name);
    }

    public TextField(String name, String value, int size) {
        this(name, size);
        super.setText(value != null ? value : "");
    }

    public TextField(String name, int size, Action action) {
        this(name, size);
        addActionListener(action);

    }

    protected void addActionListener(final Action action) {
        super.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                action.actionPerformed(event);
            }
        });
    }

    private void addTextListener() {
        Document nameDoc = this.getDocument();
        nameDoc.addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                notifyChanges();
            }

            public void insertUpdate(DocumentEvent e) {
                notifyChanges();
            }

            public void removeUpdate(DocumentEvent e) {
                notifyChanges();
            }
        });
    }

    private void addKeyListener() {
        this.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                notifyChanges();
            }
        });
    }

    public void clear() {
        this.changed = false;
    }

    protected void notifyChanges() {
        changed = true;
        if (changeables != null)
            changeables.onChanges();
    }

    public boolean hasChanges() {
        return this.changed;
    }

    public void observe(Changeables changeables) {
        this.changeables = changeables;
        addTextListener();
        addKeyListener();
    }

    public boolean isEmpty() {
        return getText().trim().length() == 0;
    }
}
