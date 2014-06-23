package gov.epa.emissions.framework.client;

import javax.swing.JLabel;

public class Label extends JLabel {

    public Label(String name, String label) {
        super(label);
        super.setName(toCanonicalName(name));
    }

    public Label(String label) {
        this(label, label);
    }

    // FIXME: refactor to be reusable across components
    private String toCanonicalName(String name) {
        name = name.trim().replaceAll(" ", "");
        if (name.length() == 0)
            return "";

        return name.substring(0, 1).toLowerCase() + name.substring(1, name.length());
    }
}
