package gov.epa.emissions.commons.io.orl;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.clarkware.junitperf.TimedTest;

public class ORLImportTimedTests {

    public static Test suite() {
        int secs = 1000;
        int minute = 60 * secs;

        TestSuite suite = new TestSuite();

        Test smallFile = new TimedTest(new ORLImportPerformanceTest("testShouldImportASmallNonRoadFile"), (minute));
        suite.addTest(smallFile);

        Test fiveMbFile = new TimedTest(new ORLImportPerformanceTest("testShouldImportA5MBNonRoadFile"), (5 * minute));
        suite.addTest(fiveMbFile);

        return suite;
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
