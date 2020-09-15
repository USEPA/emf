package gov.epa.emissions.commons.gui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.Format;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class FormattedTextField extends JFormattedTextField implements Changeable {
    private boolean changed = false;

    private Changeables changeables;
    protected String formattedValue = "";

    public FormattedTextField(String name, Object value, Format format, MessageBoard messagePanel) {
        super(format);
        super.setName(name);
        super.setValue(value);
        super.setColumns(10);
        super.setInputVerifier(new FormattedTextFieldVerifier(messagePanel));
        this.formattedValue = super.getText();
    }

    private void addTextListener() {
        Document nameDoc = this.getDocument();
        nameDoc.addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
//                notifyChanges();
            }

            public void insertUpdate(DocumentEvent e) {
                if (!formattedValue.equals(getText()))
                    notifyChanges();
            }

            public void removeUpdate(DocumentEvent e) {
                if (getText().length() > 0 && !formattedValue.equals(getText()))
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
        changeables = list;
        addTextListener();
    }

}
