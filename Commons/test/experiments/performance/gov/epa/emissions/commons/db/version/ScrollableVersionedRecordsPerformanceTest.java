package gov.epa.emissions.commons.db.version;

import gov.epa.emissions.commons.PerformanceTestCase;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormatWithOptionalCols;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;

import java.sql.SQLException;
import java.util.List;

public abstract class ScrollableVersionedRecordsPerformanceTest extends PerformanceTestCase {

    protected Datasource datasource;

    private String query;

    private String whereClause;

    public ScrollableVersionedRecordsPerformanceTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        doSetup(table(), datasetId());
    }

    abstract protected String table();

    abstract protected int datasetId();

    private void doSetup(String dataTable, int datasetId) throws Exception {
        datasource = emissions();

        whereClause = " WHERE dataset_id = " + datasetId + " AND version IN (0)";
//        whereClause = " WHERE dataset_id = " + datasetId + " AND version IN (0) AND  "
//                + "delete_versions NOT SIMILAR TO '(0|0,%|%,0,%|%,0)'";
        query = "SELECT * FROM " + dataTable + whereClause + " ORDER BY record_id";
    }

    protected SimpleScrollableVersionedRecords executeSimpleQuery() throws SQLException {
        return new SimpleScrollableVersionedRecords(emissions(), query);
    }

    protected void doTearDown() throws Exception {
        super.doTearDown();
        System.out.println("---------------------------------");
    }

    protected OptimizedScrollableVersionedRecords executeOptimizedQuery() throws SQLException {
        return new OptimizedScrollableVersionedRecords(emissions(), 10000, 300,query, table(), whereClause);
    }

    protected TableFormat tableFormat(final SqlDataTypes types) {
        FileFormatWithOptionalCols fileFormat = new FileFormatWithOptionalCols() {
            public Column[] optionalCols() {
                return new Column[0];
            }

            public Column[] minCols() {
                Column p1 = new Column("p1", types.text());
                Column p2 = new Column("p2", types.text());
                Column p3 = new Column("p3", types.intType());
                Column p4 = new Column("p4", types.realType());

                return new Column[] { p1, p2, p3, p4 };
            }

            public String identify() {
                return "Record_Id";
            }

            public Column[] cols() {
                return minCols();
            }

            public void fillDefaults(List data, long datasetId) {// ignore
            }
        };
        return new VersionedTableFormat(fileFormat, types);
    }

}
