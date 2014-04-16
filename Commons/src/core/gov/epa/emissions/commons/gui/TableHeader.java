package gov.epa.emissions.commons.gui;

public class TableHeader {

    private String[] columnNames;

    public TableHeader(String[] columnNames) {
        this.columnNames = columnNames;
    }

    public Class getColumnClass(int column) {//All columns assumed to be of type - String
        return String.class;
    }

    public int columnsSize() {
        return columnNames.length;
    }

    public String columnName(int i) {
        return columnNames[i];
    }
}