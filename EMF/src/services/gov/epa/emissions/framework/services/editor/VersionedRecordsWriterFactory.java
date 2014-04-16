package gov.epa.emissions.framework.services.editor;

import java.sql.SQLException;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.version.VersionedRecordsWriter;

public interface VersionedRecordsWriterFactory {
    VersionedRecordsWriter create(Datasource datasource, String table, SqlDataTypes sqlTypes) throws SQLException;
}
