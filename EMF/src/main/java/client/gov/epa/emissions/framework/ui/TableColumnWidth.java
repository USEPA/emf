package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetadata;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class TableColumnWidth {

    private JTable table;

    private static final int MAX_WIDTH = 300;

    private TableMetadata tableMetadata;
    
    // factor(10) is selected by trial and error
    private static final int STRING_FACTOR = 10;

    public TableColumnWidth(JTable table, TableMetadata tableMetadata) {
        this.table = table;
        this.tableMetadata = tableMetadata;
    }

    public void columnWidths() {
        TableColumnModel columnModel = table.getColumnModel();
        int columnCount = columnModel.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            String columnName = table.getColumnName(i);
            if (exist(columnName, tableMetadata) != null) {
                TableColumn tableColumn = columnModel.getColumn(i);
                width(tableColumn);
            }
        }
        table.repaint();
    }

    private void width(TableColumn tableColumn) {
        String header = (String) tableColumn.getHeaderValue();
        int headerWidth = header.length() * STRING_FACTOR;

        int modelIndex = tableColumn.getModelIndex();
        int valueWidth = this.getMaxValueLength(modelIndex) * STRING_FACTOR;
                        
        valueWidth = (valueWidth < MAX_WIDTH) ? valueWidth : MAX_WIDTH;

        int preferedWidth = (headerWidth > valueWidth) ? headerWidth : valueWidth;
        tableColumn.setPreferredWidth(preferedWidth);
    }

    /**
     * Finds the length of the longest value (in terms of characters) in the
     * column at the given index.
     */
    private int getMaxValueLength(int column) {

        TableModel model = this.table.getModel();

        int rowCount = model.getRowCount();
        int maxLength = Integer.MIN_VALUE;
        for (int i = 0; i < rowCount; i++) {

            Object value = model.getValueAt(i, column);

            int length = 0;
            if (value != null) {
                length = value.toString().length();
            }

            maxLength = Math.max(length, maxLength);
        }

        return maxLength;
    }
    
    private ColumnMetaData exist(String columnName, TableMetadata tableMetadata) {
        ColumnMetaData[] cols = tableMetadata.getCols();
        for (int i = 0; i < cols.length; i++) {
            if (columnName.equalsIgnoreCase(cols[i].getName())) {
                return cols[i];
            }
        }
        return null;
    }

}
