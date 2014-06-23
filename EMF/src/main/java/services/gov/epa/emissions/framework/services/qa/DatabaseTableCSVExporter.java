package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.io.ExporterException;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;


//This class is only useful for exporting NON VERSIONED tables.
//Another approach will be needed to deal with versioned tables...
//This exporter use the Postgres COPY statement to go directly from the table to a file 
//This exporter will not handle custom SQL statements, but could be easily expanded to support this.
public class DatabaseTableCSVExporter implements Exporter {

    private Datasource datasource;

    private String qualifiedTableName;
    
    private long exportedLinesCount = 0;

    private boolean windowsOS = false;

    private String rowFilter;

    public DatabaseTableCSVExporter(String qualifiedTableName, Datasource datasource) {
        this.qualifiedTableName = qualifiedTableName;
        this.datasource = datasource;
        if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS"))
            windowsOS = true;
    }

    public DatabaseTableCSVExporter(String qualifiedTableName, Datasource datasource, String rowFilter) {
        this.qualifiedTableName = qualifiedTableName;
        this.datasource = datasource;
        if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS"))
            windowsOS = true;
        this.rowFilter = rowFilter;
    }

    public void export(File file) throws ExporterException {
        Connection connection = null;

        try {
            //build COPY SQL statement
            String writeQuery = getWriteQueryString(file.getPath());

            connection = datasource.getConnection();

            //execute COPY SQL statement
            executeQuery(connection, writeQuery);

        } catch (Exception e) {
            e.printStackTrace();
            // NOTE: this closes the db server for other exporters
            // try {
            // if ((connection != null) && !connection.isClosed())
            // connection.close();
            // } catch (Exception ex) {
            // ex.printStackTrace();
            // throw new ExporterException(ex.getMessage());
            // }
            throw new ExporterException(e.getMessage());
        } finally {
            //
        }
    }

    public long getExportedLinesCount() {
        return this.exportedLinesCount;
    }

    private void executeQuery(Connection connection, String writeQuery) throws SQLException {
        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        statement.execute(writeQuery);
        statement.close();
    }

    private String getWriteQueryString(String dataFile) {
        String withClause = " WITH NULL '' CSV HEADER FORCE QUOTE " + getNeedQuotesCols();

        return "COPY " + (rowFilter == null || rowFilter.isEmpty() ? qualifiedTableName : "(select * from " + qualifiedTableName + " where " + rowFilter + ")") + " to '" + putEscape(dataFile) + "'" + withClause;
    }
    private String getNeedQuotesCols() {
        ResultSet rs = null;
        String colNames = "";
        try {
            rs = datasource.query().executeQuery("select * from " + qualifiedTableName + " where 1 = 0");
            ResultSetMetaData md = rs.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                String colType = md.getColumnTypeName(i).toUpperCase();

                if (colType.startsWith("VARCHAR") || colType.startsWith("TEXT"))
                    colNames += "\"" + md.getColumnName(i) + "\",";
                
            }
        } catch (SQLException e) {
            //
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    //
                }
        }
        return (colNames.length() > 0) ? colNames.substring(0, colNames.length() - 1) : colNames;
    }

    private String putEscape(String path) {
        if (windowsOS)
            return path.replaceAll("\\\\", "\\\\\\\\");

        return path;
    }
}
