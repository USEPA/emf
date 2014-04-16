package gov.epa.emissions.commons.db.postgres;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.clarkware.junitperf.ConstantTimer;
import com.clarkware.junitperf.LoadTest;
import com.clarkware.junitperf.Timer;

public class TwentyAndFiftyMBFilesMultiUserPerformanceTests {

    public static Test suite() {
        TestSuite suite = new TestSuite();

        Timer timer = new ConstantTimer(1000);

        Test fifty = new LoadTest(new FiftyMBFilePostgresQueryPerformanceTest("testTrackMemory"), 3, timer);
        suite.addTest(fifty);

        Test test = new LoadTest(new TwentyMBFilePostgresQueryPerformanceTest("testTrackMemory"), 4, timer);
        suite.addTest(test);

        return suite;
    }

    public static void main(String[] args) {
        long start = new Date().getTime() / 1000;
        junit.textui.TestRunner.run(suite());
        long end = new Date().getTime() / 1000;
        
        System.out.println("total time: " + (end - start));
    }
}
