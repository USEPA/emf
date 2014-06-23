package gov.epa.emissions.framework.services.editor;

import java.sql.SQLException;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.version.DefaultVersionedRecordsWriter;
import gov.epa.emissions.commons.db.version.VersionedRecordsWriter;

public class DefaultVersionedRecordsWriterFactory implements VersionedRecordsWriterFactory {

    public VersionedRecordsWriter create(Datasource datasource, String table, SqlDataTypes sqlTypes)
            throws SQLException {
        return new DefaultVersionedRecordsWriter(datasource, table);
    }

}
