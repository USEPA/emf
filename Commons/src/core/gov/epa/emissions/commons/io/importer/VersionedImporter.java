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

    private int targetVersion;

    public VersionedImporter(Importer delegate, Dataset dataset, DbServer dbServer, Date lastModifiedDate) {
        this(delegate, dataset, dbServer, lastModifiedDate, 0);
    }

    public VersionedImporter(Importer delegate, Dataset dataset, DbServer dbServer, Date lastModifiedDate, int targetVersion) {
        this.delegate = delegate;
        this.dataset = dataset;
        this.datasource = dbServer.getEmissionsDatasource(); 
        // completed: datasource is not used anywhere else except addVersionZeroEntryToVersionsTable
        this.lastModifiedDate = lastModifiedDate;
        this.targetVersion = targetVersion;
    }

    //NOTE: need to access the importer to get external sources
    public Importer getWrappedImporter() {
        return delegate;
    }

    public void run() throws ImporterException {
        delegate.run();
        try {
            addVersionEntryToVersionsTable(datasource, dataset, 0);
            if (targetVersion != 0) {
                addVersionEntryToVersionsTable(datasource, dataset, targetVersion);
            }
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

    private void addVersionEntryToVersionsTable(Datasource datasource, Dataset dataset, int versionNum) throws Exception {

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
            insertStatement.setInt(2, versionNum);
            if (versionNum == 0) {
                insertStatement.setString(3, "Initial Version");
                insertStatement.setString(4, "");
            } else {
                insertStatement.setString(3, "Import Version");
                insertStatement.setString(4, "0");
            }
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
