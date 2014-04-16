package gov.epa.emissions.framework.services.persistence;

import java.sql.SQLException;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableDefinition;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;

public class VersionedTable {

    private Datasource datasource;

    private SqlDataTypes types;

    public VersionedTable(Datasource datasource, SqlDataTypes types) {
        this.datasource = datasource;
        this.types = types;
    }

    public void create(String table, Column[] cols) throws SQLException {
        TableDefinition tableDefinition = datasource.tableDefinition();
        tableDefinition.createTable(table, tableFormat(types, cols).cols());
    }

    private TableFormat tableFormat(final SqlDataTypes types, final Column[] cols) {
        FileFormat fileFormat = fileFormat(cols);
        return new VersionedTableFormat(fileFormat, types);
    }

    private FileFormat fileFormat(final Column[] cols) {
        FileFormat fileFormat = new FileFormat() {
            public String identify() {
                return "Record_Id";
            }

            public Column[] cols() {
                return cols;
            }
        };
        return fileFormat;
    }
}
