package gov.epa.emissions.framework.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Label extends JLabel {

    public Label(String name, String label) {
        super(label);
        super.setName(toCanonicalName(name));
        super.getAccessibleContext().setAccessibleName(name);
        super.getAccessibleContext().setAccessibleDescription(label);
        super.setFocusable(true);

        super.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                setBorder(BorderFactory.createLineBorder(Color.BLACK));
            }

            @Override
            public void focusLost(FocusEvent e) {
                setBorder(null);
            }
        });
        super.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }

    public Label(String name, String label, String toolTipText, int preferredWidth) {
        this(name, label);
        super.setToolTipText(toolTipText);
        super.setPreferredSize(new Dimension(preferredWidth, 18));
    }

    public Label(String name, String label, String toolTipText, int preferredWidth, int preferredHeight) {
        this(name, label);
        super.setToolTipText(toolTipText);
        super.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
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
