package gov.epa.emissions.commons.gui;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

public class TextArea extends JTextArea implements Changeable {
    private Changeables changeables;

    private boolean changed = false;

    public TextArea(String name, String value) {
        this(name, value, 40, name);
    }

    public TextArea(String name, String value, String toolTipText) {
        this(name, value, 40, toolTipText);
    }

    public TextArea(String name, String value, int columns) {
        this(name, value, columns, 4, name);
    }

    public TextArea(String name, String value, int columns, String toolTipText) {
        this(name, value, columns, 4, toolTipText);
    }

    public TextArea(String name, String value, int columns, int rows) {
        this(name, value, columns, rows, name);
    }

    public TextArea(String name, String value, int columns, int rows, String toolTipText) {
        super.setName(name);
        super.setText(value);
        super.setRows(rows);
        super.setLineWrap(true);
        super.setCaretPosition(0);
        super.setColumns(columns);
        super.setWrapStyleWord(true);
        super.setToolTipText(toolTipText);
        addKeyListener();
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
                if (!(e.getKeyChar() == KeyEvent.VK_TAB)
                    && !(e.getKeyCode() == KeyEvent.VK_TAB &&  e.isShiftDown()))
                    notifyChanges();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB)
                {
                    //System.out.println("keyPressed " + e.getKeyCode());
                    e.consume();
                    KeyboardFocusManager.
                            getCurrentKeyboardFocusManager().focusNextComponent();
                }

                if (e.getKeyCode() == KeyEvent.VK_TAB
                        &&  e.isShiftDown())
                {
                    //System.out.println("keyPressed " + e.getKeyCode());
                    e.consume();
                    KeyboardFocusManager.
                            getCurrentKeyboardFocusManager().focusPreviousComponent();
                }

//                if (e.getKeyCode() == KeyEvent.VK_TAB) {
//                    if (e.getModifiersEx() > 0) {
//                        this.transferFocusBackward();
//                    } else {
//                        this.transferFocus();
//                    }
//                    e.consume();
//                }
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
        addTextListener();
//        addKeyListener();
    }

    public boolean isEmpty() {
        return getText().trim().length() == 0;
    }

}
