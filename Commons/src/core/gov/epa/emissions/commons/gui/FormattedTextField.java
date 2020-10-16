package gov.epa.emissions.commons.gui;

import java.text.Format;

import javax.swing.JFormattedTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

public class FormattedTextField extends JFormattedTextField implements Changeable {
    private boolean changed = false;

    private Changeables changeables;

    public FormattedTextField(String name, Object value, Format format, MessageBoard messagePanel) {
        super(format);
        super.setName(name);
        super.setValue(value);
        super.setColumns(10);

        super.setInputVerifier(new FormattedTextFieldVerifier(messagePanel));
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
