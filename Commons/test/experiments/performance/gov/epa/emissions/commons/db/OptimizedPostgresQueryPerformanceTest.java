package gov.epa.emissions.commons.db;

import gov.epa.emissions.commons.PerformanceTestCase;
import gov.epa.emissions.commons.db.postgres.OptimizedPostgresQuery;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OptimizedPostgresQueryPerformanceTest extends PerformanceTestCase {

    public OptimizedPostgresQueryPerformanceTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testShouldFetch20MBDataset() throws Exception {
        String table = "emissions.test_onroad_twenty_mb";
        int count = runQuery(table);
        assertEquals(15, count);
    }

    public void testShouldFetch100MBDataset() throws Exception {
        String table = "emissions.test_onroad_hundred_mb";
        int count = runQuery(table);
        assertEquals(72, count);
    }

    private int runQuery(String table) throws SQLException {
        startTracking();

        String query = "SELECT * FROM " + table;
        int fetchSize = 10000;
        OptimizedPostgresQuery runner = new OptimizedPostgresQuery(emissions().getConnection(), query, fetchSize);

        int count = 0;
        while (runner.execute()) {
            ResultSet rs = runner.getResultSet();
            dumpStats();
            rs.close();
            ++count;
            startTracking();
        }

        runner.close();
        return count;
    }

}
