package gov.epa.emissions.commons.db.postgres;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.ExporterException;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PostgresCOPYExport {

    Log log = LogFactory.getLog(PostgresCOPYExport.class);

    private Datasource datasource;

    private boolean windowsOS = false;
    
    public PostgresCOPYExport(DbServer dbServer) {
        this.datasource = dbServer.getEmissionsDatasource();
        
        if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS"))
            windowsOS = true;
    }

    public void export(String selectQuery, String filePath) throws ExporterException {
        Connection connection = null;

        try {
            File dataFile = new File(filePath);
            createNewFile(dataFile);

            String exportQuery = getWriteQueryString(filePath, selectQuery);

            log.warn(exportQuery);

            connection = datasource.getConnection();

            executeQuery(connection, exportQuery);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ExporterException(e.getMessage());
        } finally {
            //
        }
    }

    private String putEscape(String path) {
        if (windowsOS)
            return path.replaceAll("\\\\", "\\\\\\\\");

        return path;
    }

    private void executeQuery(Connection connection, String writeQuery) throws SQLException {
        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        statement.execute(writeQuery);
        statement.close();
    }

    private String getWriteQueryString(String filePath, String query) {
        String columnsNeedingQuotes = getNeedQuotesCols(query);
        String withClause = " WITH HEADER NULL '' CSV" + (columnsNeedingQuotes.length() > 0 ? " FORCE QUOTE " + columnsNeedingQuotes : "");

        return "COPY (" + query + ") to '" + putEscape(filePath) + "'" + withClause;
    }

    private void createNewFile(File file) throws Exception {
        try {
            if (windowsOS) {
                // AME: Updates for EPA's system
                file.createNewFile();
                Runtime.getRuntime().exec("CACLS " + file.getAbsolutePath() + " /E /G \"Users\":W");
                file.setWritable(true, false);
                Thread.sleep(1000); // for the system to refresh the file access permissions
            }
            // for now, do nothing from Linux
        } catch (IOException e) {
            throw new ExporterException("Could not create export file: " + file.getAbsolutePath());
        }
    }

    private String getNeedQuotesCols(String selectQuery) {
        String colNames = "";
        ResultSet rs = null;
        String sql = selectQuery + (selectQuery.toLowerCase().indexOf("where") > 0 ? " and " : " where ") + "1 = 0";
        try {
            rs = datasource.query().executeQuery(sql);
            ResultSetMetaData md = rs.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++)
                if (md.getColumnTypeName(i).toUpperCase().startsWith("VARCHAR") || md.getColumnTypeName(i).toUpperCase().startsWith("TEXT"))
                    colNames += "\"" + md.getColumnLabel(i) + "\",";
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
}
