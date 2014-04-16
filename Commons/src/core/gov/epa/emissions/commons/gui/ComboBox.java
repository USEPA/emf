package gov.epa.emissions.commons.gui;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

public class ComboBox extends JComboBox implements Changeable {
    private Changeables changeables;

    private boolean changed = false;

    private String defaultLabel;

    public ComboBox(String defaultLabel, Object[] values) {
        this.defaultLabel = defaultLabel;
        List list = new ArrayList(Arrays.asList(values));
        if (!list.contains(defaultLabel))
            list.add(0, defaultLabel);

        setModel(new DefaultComboBoxModel(list.toArray()));
        setRenderer(new ComboBoxRenderer(defaultLabel));
    }

    public ComboBox(Object[] values) {
        super(values);
    }

    public ComboBox() {
        super();
    }
    
    public void resetModel(Object[] values) {
        List list = new ArrayList(Arrays.asList(values));
        if (!list.contains(defaultLabel))
            list.add(0, defaultLabel);

        setModel(new DefaultComboBoxModel(list.toArray()));
        setRenderer(new ComboBoxRenderer(defaultLabel));
    }

    public Object getSelectedItem() {
        if (defaultLabel == null) {
            return super.getSelectedItem();
        }
        int index = super.getSelectedIndex();
        return (index > 0) ? super.getSelectedItem() : null;
    }

    class ComboBoxRenderer extends BasicComboBoxRenderer {

        private String defaultString;

        ComboBoxRenderer(String defaultString) {
            this.defaultString = defaultString;
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setFont(list.getFont());

            if (value instanceof Icon) {
                setIcon((Icon) value);
            }

            if (value == null) {
                setText(defaultString);
            } else {
                setText(value.toString());
            }
            return ComboBoxRenderer.this;
        }
    }

    private void addItemChangeListener() {
        addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
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
        addItemChangeListener();
    }
}
