package gov.epa.emissions.commons.db.postgres;

import java.sql.ResultSet;

import gov.epa.emissions.commons.PerformanceTestCase;

public class HundredMBFilePostgresQueryPerformanceTest extends PerformanceTestCase {

    public HundredMBFilePostgresQueryPerformanceTest(String name) {
        super(name);
    }

    public void testTrackMemory() throws Exception {
        startTracking();

        int count = 716920;
        int pageSize = 50000;

        OptimizedTestQuery runner = new OptimizedTestQuery(emissions().getConnection());
        runner.init("SELECT * FROM emissions.test_onroad_hundred_mb", pageSize);

        for (int i = 0; i < count;) {
            ResultSet rs = runner.execute();
            while(rs.next()){
                rs.getObject(1);
            }
            
            i += pageSize;
        }

        dumpStats();
    }

}
