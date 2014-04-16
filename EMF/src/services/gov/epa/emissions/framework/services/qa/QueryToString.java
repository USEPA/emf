package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class QueryToString {

    private String sqlQuery;

    private Datasource datasource;

    private String delimiter;

    private StringBuffer output;

    protected String lineFeeder = System.getProperty("line.separator");

    private String columnNameList = "";
    
    private int rows; 

    public QueryToString(DbServer dbServer, String sqlQuery, String delimiter) throws ExporterException {
        this.sqlQuery = sqlQuery;
        this.datasource = dbServer.getEmissionsDatasource();
        this.delimiter = delimiter;
        this.output = new StringBuffer();
        //get column name list, used for sorting purposes...
        try {
            ResultSet rs = datasource.query().executeQuery(sqlQuery + " limit 0");
            ResultSetMetaData md = rs.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                this.columnNameList += (i > 1 ? "," : "") + "\"" + md.getColumnName(i) + "\"";
            }
            //System.out.println("ColumnNameList: "+columnNameList );
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExporterException(e.getMessage());
        }
    }

    public String toString() {
        try {
            writeToString();
        } catch (Exception e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        //System.out.println(output.toString());
        return output.toString();
    }

    private void writeToString() throws Exception {
        try {
            
            if (DebugLevels.DEBUG_0())
                System.out.println("\n query: " + sqlQuery);

            
            ResultSet rs = datasource.query().executeQuery(sqlQuery);
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            int startingColumn=1;
            rows = 0;
            writeHeaderRow(md, startingColumn, columnCount);

            String row = "";
            String value = "";
                       
            while (rs.next()) {
                rows = rows + 1;
                row = "";
                for (int i = startingColumn; i <= columnCount; i++) {
                    value = rs.getString(i);
                    String type = md.getColumnTypeName(i);
                    if (value != null) {
                        if (type.equals("varchar") || value.indexOf(",") > 0 || value.indexOf(";") > 0 || value.indexOf(" ") > 0) 
                        {
                            value = "\"" + value.replaceAll("\"", "\"\"").replaceAll("\n", " ") + "\"";
                        }
                        else if (value.length()==0) value="\"\"";
                    }
                    row += (i > startingColumn ? delimiter : "") + (!rs.wasNull() ? value : "\"\"");
                }
                //the analysis engine only supports reports with more than one column, so add a dummy column value for now.
                if (columnCount == 1) row += delimiter + "\"\"";
                output.append(row + lineFeeder);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExporterException("could not convert table to string ", e);
        }
    }

    private void writeHeaderRow(ResultSetMetaData md, int startingColumn, int columnCount) throws SQLException {
        String colTypes = "#COLUMN_TYPES=";
        String colNames = ""; 
        
        for (int i = startingColumn; i <= columnCount; i++) {
            colTypes += md.getColumnTypeName(i) + "(" + md.getPrecision(i) + ")" + (i < columnCount ? "|" : "");
            colNames += (i > startingColumn ? delimiter : "") + "\"" + md.getColumnName(i) + "\"";
        }
        //the analysis engine only supports reports with more than one column, so add a dummy column for now.
        if (columnCount == 1) {
            colTypes += "|varchar(1)";
            colNames += delimiter + "extra";
        }
        output.append(colTypes + lineFeeder + colNames + lineFeeder);
    }
    
    public long getRows(){
        return rows; 
    }
}
