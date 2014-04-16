package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.db.SqlDataTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Maps File Type to Generic Data Type Maps Name of a Column in a File to its
 * Width
 */
public class FileColumnsMetadata {// TODO: why ???

    private HashMap columnNameTypeMap;

    private HashMap columnNameWidthMap;

    private String tableName;

    private SqlDataTypes sqlTypeMapper;

    private List columns;

    public FileColumnsMetadata(String tableName, SqlDataTypes sqlTypeMapper) {
        this.tableName = tableName;
        this.sqlTypeMapper = sqlTypeMapper;
        columnNameTypeMap = new HashMap();
        columnNameWidthMap = new HashMap();
        this.columns = new ArrayList();
    }

    public void setType(String columnName, String columnType) throws Exception {
        if (!columns.contains(columnName))
            throw new Exception("Could not set type since name does not exist");

        columnNameTypeMap.put(columnName, columnType);
    }

    public void setWidth(String name, String width) throws Exception {
        if (!columns.contains(name))
            throw new Exception("Could not set type since name does not exist");

        columnNameWidthMap.put(name, width);
    }

    public String getType(String name) {
        String type = (String) columnNameTypeMap.get(name);

        return sqlTypeMapper.type(name, type, getWidth(name));
    }

    public int getWidth(String name) {
        int width = Integer.parseInt((String) columnNameWidthMap.get(name));
        return width;
    }

    public String[] getColumnNames() {
        String[] columnNames = new String[columns.size()];
        columnNames = (String[]) columns.toArray(columnNames);
        return columnNames;
    }

    public int[] getColumnWidths() {
        int[] columnWidths = new int[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            columnWidths[i] = getWidth(columns.get(i).toString());
        }
        return columnWidths;
    }

    public String[] getColumnTypes() {
        String[] columnTypes = new String[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            columnTypes[i] = getType(columns.get(i).toString());
        }
        return columnTypes;
    }

    public String getTableName() {
        return tableName;
    }

    public void addColumnName(String columnName) {
        columns.add(columnName);
    }
}
