package gov.epa.emissions.commons.db.version;

import gov.epa.emissions.commons.db.DbColumn;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;

public class VersionDataColumns {

    private SqlDataTypes types;

    public VersionDataColumns(SqlDataTypes types) {
        this.types = types;
    }

    public DbColumn[] get() {
        DbColumn recordId = new Column("record_id", types.intType());
        DbColumn datasetId = new Column("dataset_id", types.intType());
        DbColumn version = new Column("version", types.intType());
        DbColumn deleteVersion = new Column("  delete_versions", types.stringType(255));
        DbColumn param1 = new Column("param1", types.stringType(255));
        DbColumn param2 = new Column("param2", types.stringType(255));

        return new DbColumn[] { recordId, datasetId, version, deleteVersion, param1, param2 };
    }

}
