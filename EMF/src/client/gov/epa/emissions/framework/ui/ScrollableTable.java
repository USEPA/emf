package gov.epa.emissions.framework.ui;

import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class ScrollableTable extends JScrollPane {

    private JTable table;

    private Font tableCellFont;

    public ScrollableTable(EmfTableModel tableModel, Font font) {
        super(VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);

        this.tableCellFont = font;
        table = table(tableModel);
        super.setViewportView(table);
    }

    public ScrollableTable(JTable table, Font font) {
        super(VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.table = table;
        this.tableCellFont = font;
        table(table);
        super.setViewportView(table);
    }

    private void table(JTable table) {
        table.setRowHeight(18);

        if (tableCellFont != null)
            table.setFont(this.tableCellFont);

        enableScrolling(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
    }

    private JTable table(EmfTableModel tableModel) {
        JTable table = new JTable(tableModel);
        if (tableModel.getRowCount() == 1) {
            if ((tableModel.getColumnClass(0).isInstance(Boolean.TRUE))
                    && tableModel.getColumnName(0).equalsIgnoreCase("Select")) {
                tableModel.setValueAt(Boolean.TRUE, 0, 0);
            }
        }
        table(table);
        return table;
    }

    private void enableScrolling(JTable table) {
        // essential for horizontal scrolling
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    public void setColumnWidth(String colName, int width) {

        TableColumnModel model = table.getColumnModel();

        for (int i = 0; i < model.getColumnCount(); i++) {

            TableColumn col = model.getColumn(i);
            String headerValue = (String) col.getHeaderValue();
            if (headerValue.equals(colName)) {

                col.setMinWidth(width);
                col.setResizable(true);
                break;
            }
        }
        table.repaint();
    }

    public void setColWidthsBasedOnColNames() {
        TableColumnModel model = table.getColumnModel();

        for (int i = 0; i < model.getColumnCount(); i++) {
            TableColumn col = model.getColumn(i);
            String headerValue = (String) col.getHeaderValue();
            int width = headerValue.length() * 10;
            col.setMinWidth(width);
            col.setResizable(true);
        }
        table.repaint();
    }

    public void setMaxColWidth(String[] columns, Font font) {
        TableColumnModel model = table.getColumnModel();

        FontMetrics fontMetrics = table.getFontMetrics(font);
        for (int i = 0; i < model.getColumnCount(); i++) {
            TableColumn col = model.getColumn(i);
            String headerValue = (String) col.getHeaderValue();

            int width = fontMetrics.stringWidth(headerValue) + 8;
            if (contains(columns, headerValue)) {
                col.setMaxWidth(width);
            }
            else {
                col.setMinWidth(width);
            }

            col.setResizable(true);
        }

        table.repaint();
    }

    private boolean contains(String[] columns, String column) {
        for (String col : columns) {
            if (col.equalsIgnoreCase(column.trim()))
                return true;
        }
        return false;
    }

    public void autoScrolling() {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    public void disableScrolling() {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    }

    public void moveToBottom() {
        JScrollBar vertical = getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum() - 1);

        selectLastRow();
    }

    public void selectLastRow() {
        int total = table.getModel().getRowCount();
        table.setRowSelectionInterval(total - 1, total - 1);
    }

    public void resetTextFont(Font font) {
        table.setFont(font);
    }

    public JTable getTable() {
        return table;
    }
}
