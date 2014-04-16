package gov.epa.emissions.framework.client.data;

import java.awt.Component;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
public class DoubleRenderer extends DefaultTableCellRenderer {

    private int decimalPlaces = -1;

    private NumberFormat format = new DecimalFormat(NO_GROUP_PATTERN);

    private static final String GROUP_PATTERN = "#,###";

    private static final String NO_GROUP_PATTERN = "####";

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        String text = (value == null) ? "" : value.toString();

        if (decimalPlaces == -1 || text.isEmpty()) {
            label.setText(text);
        } else {

            try {
                double doubleValue = Double.valueOf(text);
                double roundedDoubleValue = this.roundDecimalPaces(doubleValue, this.decimalPlaces);

                label.setText(format.format(roundedDoubleValue));
            } catch (Exception e) {
                label.setText(text);
            }

        }

        return label;
    }

    private double roundDecimalPaces(double d, int digits) {

        double pow = Math.pow(10, digits);
        return Math.round(d * pow) / pow;
    }

    public void setDecimalPlaces(int decimalPlaces) {

        this.decimalPlaces = decimalPlaces;

        String newPattern = this.format.isGroupingUsed() ? GROUP_PATTERN : NO_GROUP_PATTERN;
        if (this.decimalPlaces > 0) {

            newPattern += ".";
            for (int i = 0; i < decimalPlaces; i++) {
                newPattern += "0";
            }
        }

        this.format = new DecimalFormat(newPattern);
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public void setGroup(boolean group) {
        this.format.setGroupingUsed(group);
    }

    public boolean isGroup() {
        return this.format.isGroupingUsed();
    }
}
