package gov.epa.emissions.commons.io.importer;

import java.sql.SQLException;
import java.util.Random;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.TableCreator;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.TableFormat;

public class DataTable {

    private String name;

    private String colNameString;

    private String colTypeString;

    private TableCreator delegate;

    private int numOfCols;
    
    private Dataset dataset;

    public DataTable(Dataset dataset, Datasource datasource) {
        this.name = createName(dataset.getName()); // VERSIONS TABLE: this will not be a problem, the name never will be versions
        this.delegate = new TableCreator(datasource);
        this.dataset = dataset;
    }

    public String name() {
        return name;
    }

    public String createName(String name) {
        name = name.trim();
        String prefix = "DS_";
        String sufix = "_" + Math.abs(new Random().nextInt()); // to make name unique
        String table = prefix + name + sufix;

        if (table.length() > 63) { // postgresql table name max length is 64
            int space = table.length() - 63;
            table = prefix + name.substring(space + 1) + sufix;
        }

        for (int i = 0; i < table.length(); i++) {
            if (!Character.isLetterOrDigit(table.charAt(i))) {
                table = table.replace(table.charAt(i), '_');
            }
        }

        return table;
    }

    public static String encodeTableName(String tableName) {
        for (int i = 0; i < tableName.length(); i++) {
            if (!Character.isLetterOrDigit(tableName.charAt(i))) {
                tableName = tableName.replace(tableName.charAt(i), '_');
            }
        }

        if (Character.isDigit(tableName.charAt(0))) {
            tableName = tableName.replace(tableName.charAt(0), '_');
            tableName = "DS" + tableName;
        }
        return tableName.trim().replaceAll(" ", "_");
    }

    public void create(String table, TableFormat tableFormat) throws ImporterException {
        try {
            delegate.create(table, tableFormat);
        } catch (Exception e) {
            if (e instanceof SQLException) {
                String errStr = e.getMessage();
                int index = errStr.indexOf("ERROR:");
                throw new ImporterException(index >= 0 ? errStr.substring(index + 6) + "." : errStr);
            }
            
            throw new ImporterException("Note that CSV imports require a row column names before the data rows - " + e.getMessage());
        }
    }

    public void create(TableFormat tableFormat, int datasetId) throws ImporterException {
        try {
            delegate.create(name(), tableFormat, datasetId);
        } catch (Exception e) {
            e.printStackTrace();
           throw new ImporterException(e.getMessage());
        }
    }

    public void create(TableFormat tableFormat) throws ImporterException {
        create(name(), tableFormat);
    }

    public void drop(String table) throws ImporterException {
        try {
            delegate.drop(table);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ImporterException(
                    "could not drop table " + table + " after encountering error importing dataset", e);
        }
    }

    public void drop() throws ImporterException {
        drop(name());
    }

    public void rename(String oldName, String newName) throws ImporterException {
        try {
            delegate.rename(oldName, newName);
        } catch (Exception e) {
            throw new ImporterException("could not rename table " + name + ", " + e.getMessage());
        }
    }

    public boolean exists(String table) throws Exception {
        return delegate.exists(table);
    }

    public void addIndex(String table, String colList, boolean clustered)  {
        try {
            delegate.addIndex(table, colList, clustered);
        } catch (Exception e) {
            //suppress exceptions
//            throw new Exception("could not rename table " + name + ", " + e.getMessage());
        }
    }

    public void analyzeTable(String table) {
        try {
            delegate.analyzeTable(table);
        } catch (Exception e) {
//            throw new Exception("could not analyze table " + name + ", " + e.getMessage());
        }
    }

    public String createConsolidatedTable(TableFormat tableFormat) throws ImporterException {
        getTableColInfo(tableFormat);

        try {
            String consolidatedTable = delegate
                    .checkTableConsolidation(this.colNameString, this.colTypeString, dataset);

            if (consolidatedTable != null && !consolidatedTable.isEmpty())
                return consolidatedTable;
            
            create(tableFormat);
            delegate.addConsolidationItem(this.numOfCols, this.name, this.colNameString, this.colTypeString, dataset);
            return this.name;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ImporterException(e.getMessage());
        }
    }
    
    public void updateConsolidatedTable(int dsTypeId, String table) {
        try {
            delegate.updateConsolidationTable(dsTypeId, table);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getTableColInfo(TableFormat tableFormat) {
        StringBuffer colNames = new StringBuffer();
        StringBuffer colTypes = new StringBuffer();
        Column[] cols = tableFormat.cols();
        int count = 0;

        for (Column col : cols) {
            colNames.append(col.name() + ",");
            colTypes.append(col.sqlType() + ",");
            count++;
        }

        colNames.deleteCharAt(colNames.length() - 1);
        colTypes.deleteCharAt(colTypes.length() - 1);

        this.colNameString = colNames.toString();
        this.colTypeString = colTypes.toString();
        this.numOfCols = count;
    }

}
