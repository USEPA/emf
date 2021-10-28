package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.gui.Changeable;
import gov.epa.emissions.commons.gui.Changeables;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;

import javax.swing.Action;
import javax.swing.JFormattedTextField;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

public class NumberFormattedTextField extends JFormattedTextField implements Changeable {

    private Changeables changeables;

    private boolean changed = false;

    public NumberFormattedTextField(int min, int max, int size, Action action) {
        super.setFormatterFactory(new DefaultFormatterFactory(integerFormatter(min, max)));
        super.setValue(new Integer(min));
        super.setColumns(size);

        addActionForEnterKeyPress(action);
    }

    public NumberFormattedTextField(double min, double max, int size, Action action) {
        super.setFormatterFactory(new DefaultFormatterFactory(doubleFormatter(min, max)));
        super.setColumns(size);

        addActionForEnterKeyPress(action);
    }

    public NumberFormattedTextField(int size, Action action) {
        this(Double.MIN_VALUE, Double.MAX_VALUE, size, action);
    }

    public NumberFormattedTextField(int size) {
        super.setFormatterFactory(new DefaultFormatterFactory(doubleFormatter(Double.MIN_VALUE, Double.MAX_VALUE)));
        super.setColumns(size);
    }

    private void addActionForEnterKeyPress(Action action) {
        super.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "check");
        super.getActionMap().put("check", action);
    }

    private NumberFormatter integerFormatter(int min, int max) {
        NumberFormatter formatter = new NumberFormatter(NumberFormat.getIntegerInstance());
        formatter.setMinimum(new Integer(min));
        formatter.setMaximum(new Integer(max));

        return formatter;
    }

    private NumberFormatter doubleFormatter(double min, double max) {
        NumberFormatter formatter = new NumberFormatter(NumberFormat.getNumberInstance());
        formatter.setMinimum(new Double(min));
        formatter.setMaximum(new Double(max));

        return formatter;
    }

    public void setRange(int min, int max) {
        super.setFormatter(integerFormatter(min, max));
    }
    
    public int getInt() {
        return Integer.parseInt(getText().trim());
    }
    
    public double getDouble() {
        return Double.parseDouble(getText().trim());
    }

    public boolean isEmpty() {
        return getText().trim().length() == 0;
    }

    private void addEditTrackingListener() {
        this.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                notifyChanges();
            }
        });
    }

    public void clear() {
        this.changed = false;
    }

    private void notifyChanges() {
        this.changed = true;
        if (changeables != null)
            changeables.onChanges();
    }

    public boolean hasChanges() {
        return this.changed;
    }

    public void observe(Changeables changeables) {
        this.changeables = changeables;
        addEditTrackingListener();
    }

}
