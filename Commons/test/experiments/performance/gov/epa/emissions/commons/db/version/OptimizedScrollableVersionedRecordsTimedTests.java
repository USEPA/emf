package gov.epa.emissions.commons.db.version;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.clarkware.junitperf.LoadTest;
import com.clarkware.junitperf.TimedTest;

public class OptimizedScrollableVersionedRecordsTimedTests {

    public static Test suite() {
        int secs = 1000;
        int minute = 60 * secs;

        TestSuite suite = new TestSuite();

        Test rowCount = new TimedTest(new ScrollableVersionedRecordsFiveMBPerformanceTest("testSimpleQuery"),
                (5 * minute));
        Test repeatedTest = new LoadTest(rowCount, 15);
        suite.addTest(repeatedTest);

        return suite;
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
