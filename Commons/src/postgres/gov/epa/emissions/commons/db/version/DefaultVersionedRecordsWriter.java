package gov.epa.emissions.commons.db.version;

import gov.epa.emissions.commons.db.DataModifier;
import gov.epa.emissions.commons.db.Datasource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DefaultVersionedRecordsWriter implements VersionedRecordsWriter {

    private PreparedStatement updateStatement;

    private String table;

    private Datasource datasource;

    public DefaultVersionedRecordsWriter(Datasource datasource, String table) throws SQLException {
        this.datasource = datasource;
        this.table = table;

        updateStatement = updateStatement(datasource, table);
    }

    private PreparedStatement updateStatement(Datasource datasource, String table) throws SQLException {
        String dataUpdate = "UPDATE " + datasource.getName() + "." + table + " SET delete_versions=? WHERE record_id=?";
        Connection connection = datasource.getConnection();
        return connection.prepareStatement(dataUpdate);
    }

    /**
     * ChangeSet contains adds, deletes, and updates. An update is treated as a combination of 'delete' and 'add'. In
     * effect, the ChangeSet is written as a list of 'delete' and 'add' operations.
     */
    public void update(ChangeSet changeset) throws Exception {
        convertUpdatedRecords(changeset);
        writeData(changeset);
    }

    public void close() throws SQLException {
        updateStatement.close();
    }

    private void convertUpdatedRecords(ChangeSet changeset) {
        VersionedRecord[] updatedRecords = changeset.getUpdatedRecords();

        for (int i = 0; i < updatedRecords.length; i++) {
            VersionedRecord deleteRec = updatedRecords[i];
            deleteRec.setDeleteVersions(deleteRec.getDeleteVersions() + "," + changeset.getVersion().getVersion());
            changeset.addDeleted(deleteRec);

            VersionedRecord insertRec = updatedRecords[i];
            changeset.addNew(insertRec);
        }
    }

    private void writeData(ChangeSet changeset) throws Exception {
        insertData(changeset.getNewRecords(), changeset.getVersion());
        deleteData(changeset.getDeletedRecords(), changeset.getVersion());
    }

    private void insertData(VersionedRecord[] records, Version version) throws Exception {
        DataModifier modifier = datasource.dataModifier();
        for (int i = 0; i < records.length; i++) {
            String[] data = records[i].dataForInsertion(version);
            modifier.insertRow(table, data);
        }
    }

    private void deleteData(VersionedRecord[] records, Version version) throws SQLException {
        for (int i = 0; i < records.length; i++) {
            String delVer = records[i].getDeleteVersions();
            
            if (delVer == null || delVer.trim().isEmpty())
                delVer = version.getVersion() + "";
            else
                delVer = delVer.trim() + "," + version.getVersion();
            
            updateStatement.setString(1, delVer);
            updateStatement.setInt(2, records[i].getRecordId());
            updateStatement.execute();
        }
    }

}
