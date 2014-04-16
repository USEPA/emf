package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class VersionedImporter implements Importer {
    private Importer delegate;

    private Dataset dataset;

    private Datasource datasource;

    private Date lastModifiedDate;

    public VersionedImporter(Importer delegate, Dataset dataset, DbServer dbServer, Date lastModifiedDate) {
        this.delegate = delegate;
        this.dataset = dataset;
        this.datasource = dbServer.getEmissionsDatasource(); 
        // completed: datasource is not used anywhere else except addVersionZeroEntryToVersionsTable
        this.lastModifiedDate = lastModifiedDate;
    }

    //NOTE: need to access the importer to get external sources
    public Importer getWrappedImporter() {
        return delegate;
    }

    public void run() throws ImporterException {
        delegate.run();
        try {
            addVersionZeroEntryToVersionsTable(datasource, dataset);
        } catch (Exception e) {
            throw new ImporterException("Could not add Version Zero entry to the Versions Table." + e.getMessage());
        }
        //NOTE: should let the calling function to close it explicitly for ease of db management 2/13/2008
        //        } finally {
        //            try {
        //                this.dbServer.disconnect();
        //            } catch (Exception exc) {
        //                throw new ImporterException("Could not disconnect db server: " + exc.getMessage());
        //            }
        //        }
    }

    private void addVersionZeroEntryToVersionsTable(Datasource datasource, Dataset dataset) throws Exception {

        String sql =
            "INSERT INTO emf.versions " +
            "(dataset_id, version, name, path, final_version, date, description) " +
            "VALUES (?,?,?,?,?,?,?)";
        Connection connection = null;
        PreparedStatement insertStatement = null;
        try {
            connection = datasource.getConnection();
            insertStatement = connection.prepareStatement(sql);
            insertStatement.setInt(1, dataset.getId());
            insertStatement.setInt(2, 0);
            insertStatement.setString(3, "Initial Version");
            insertStatement.setString(4, "");
            insertStatement.setBoolean(5, true);
            insertStatement.setTimestamp(6, new Timestamp(lastModifiedDate.getTime()));
            insertStatement.setString(7, "");
            insertStatement.execute();
        } catch (SQLException e) {
            throw e;
        } finally {
            if ( insertStatement != null) {
                insertStatement.close();
            }
            if ( connection != null) {
                connection = null;
            }
        }
    }
}
