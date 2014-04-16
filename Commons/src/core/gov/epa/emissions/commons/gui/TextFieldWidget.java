package gov.epa.emissions.commons.gui;

import javax.swing.JComponent;

public class TextFieldWidget implements Widget {
    private TextField textfield;

    public TextFieldWidget(String name, String value, int size) {
        textfield = new TextField(name, size);
        textfield.setText(value);
    }

    public JComponent element() {
        return textfield;
    }

    public String value() {
        return textfield.getText();
    }

}
