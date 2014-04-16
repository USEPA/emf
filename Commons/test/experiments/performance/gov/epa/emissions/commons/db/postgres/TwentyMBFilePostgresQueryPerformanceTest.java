package gov.epa.emissions.commons.db.postgres;

import java.sql.ResultSet;

import gov.epa.emissions.commons.PerformanceTestCase;

public class TwentyMBFilePostgresQueryPerformanceTest extends PerformanceTestCase {

    public TwentyMBFilePostgresQueryPerformanceTest(String name) {
        super(name);
    }

    public void testTrackMemory() throws Exception {
        startTracking();

        int count = 143385;
        int pageSize = 50000;

        OptimizedTestQuery runner = new OptimizedTestQuery(emissions().getConnection());
        runner.init("SELECT * FROM emissions.test_onroad_twenty_mb", pageSize);

        for (int i = 0; i < count;) {
            ResultSet rs = runner.execute();
            while(rs.next()){
                rs.getObject(1);
            }
            rs.close();

            i += pageSize;
        }
        runner.close();

        dumpStats();
    }

}
