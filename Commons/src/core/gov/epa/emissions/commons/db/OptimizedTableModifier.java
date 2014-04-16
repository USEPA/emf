package gov.epa.emissions.commons.db;

import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.importer.ImporterException;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;

public class OptimizedTableModifier extends TableModifier {

    private static final int BATCH_SIZE = 20000;

    private int counter;
    
    private int batchCount;

    // VERSIONS TABLE - completed. it should throw exception if datasource is emissions and table is versions
    public OptimizedTableModifier(Datasource datasource, String tableName) throws SQLException { 
        super(datasource, tableName);
        counter = 0;
        batchCount = 0;
    }

    public OptimizedTableModifier(Datasource datasource, String tableName, boolean stripPMPollutantPrimarySuffix) throws SQLException {
        this(datasource, tableName);
        this.stripPMPollutantPrimarySuffix = stripPMPollutantPrimarySuffix;
    }

    public void start() throws SQLException {
        connection.setAutoCommit(false);
        //System.out.println("Batch size = "+BATCH_SIZE);
    }

    public void insert(String[] data) throws Exception {
        if (data.length > columns.length)
            throw new Exception("Invalid number of data tokens - " + data.length + " on line "+counter+
                    ". Number of columns in the table: " + columns.length);
        
        // Check data types
        for (int i = 0; i < data.length; i++) {
            String dataValue= data[i].trim();
            //System.out.println("value of data: " +data[i] + ", name: "+ columns[i].getName()+", type: "+columns[i].sqlType());
            if ( dataValue!=null && !dataValue.isEmpty()){
                String type = columns[i].sqlType();
                if (type.toUpperCase().startsWith("VARCHAR")){         
                    int length = columns[i].width();
                    if (dataValue.length() > length)
                        throw new ImporterException("Error format for column[" + i +"], expected: " 
                                + type +"("+length+"), but was: " + dataValue+", line "+counter);
                }
                try {
                    if (type.toUpperCase().startsWith("DOUBLE") || type.toUpperCase().startsWith("FLOAT")){
                        Double.parseDouble(dataValue);
                        //System.out.println("value of column " +columns[i]+" is "+ dataValue);
                    }
                    if (type.toUpperCase().startsWith("INT")){
                        Integer.parseInt(dataValue);
                        //System.out.println("value of column " +cols[i]+" is "+ value);
                    }
                }catch (NumberFormatException nfe) {
                    throw new ImporterException("Error format for column[" + i +"], expected: " 
                            + type +", but was:" + dataValue + ", line "+ (++counter) );
                }
            }
        }
        insertRow(tableName, data, columns);
    }

    public void finish() throws SQLException {
        try {
            statement.executeBatch();// executing the last batch
        } catch (Exception e) {
            String msg = e.getMessage();
            String searchString = "Batch entry";
            int line = 0;
            
            if (msg != null && msg.contains(searchString)) {
                msg = msg.substring(searchString.length()).trim(); //msg should start with a number now
                msg = msg.substring(0, msg.indexOf(' ')); //msg should now only has a number
            } 
            
            try {
                line = NumberFormat.getInstance().parse(msg).intValue();
            } catch (ParseException e1) {
                line = 0;
            }
            e.printStackTrace();
            String exmsg = e.getMessage();
            if (e instanceof SQLException)
            {
                SQLException sqle = (SQLException)e;
                if (sqle.getNextException() != null)
                    exmsg = exmsg +"; "+sqle.getMessage();
                    sqle.printStackTrace();
            }
            throw new SQLException("Data line (" + (batchCount * BATCH_SIZE + line + 1) + ") has errors: " + exmsg);
        } finally {
            connection.commit();
            connection.setAutoCommit(true);
            counter = 0;
            batchCount = 0;
        }
    }

    private void insertRow(String table, String[] data, Column[] cols) throws SQLException {
        StringBuffer insert = createInsertStatement(table, data, cols);
        try {
            execute(insert.toString());
        } catch (SQLException e) {
            connection.rollback();
            connection.setAutoCommit(true);
            throw e;
        }
    }

    private void execute(String query) throws SQLException {
        if (counter < BATCH_SIZE) {
            statement.addBatch(query);
            counter++;
        } else {
            statement.addBatch(query);
            statement.executeBatch();
            counter = 0;
            batchCount++;
        }
    }
}
