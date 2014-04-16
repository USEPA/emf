package gov.epa.emissions.commons;

import gov.epa.emissions.commons.io.importer.PersistenceTestCase;
import gov.epa.emissions.commons.util.AnalyzePerformance;

public abstract class PerformanceTestCase extends PersistenceTestCase {

    private AnalyzePerformance analyzePerformance;

    public PerformanceTestCase(String name) {
        super(name);
        this.analyzePerformance = new AnalyzePerformance();
    }

    protected void doTearDown() throws Exception {
        // TODO Auto-generated method stub
    }

    protected void dumpMemory() {
        analyzePerformance.dumpMemory();
    }

    protected long usedMemory() {
        return analyzePerformance.usedMemory();
    }

    protected long maxMemory() {
        return analyzePerformance.maxMemory();
    }

    protected long freeMemory() {
        return analyzePerformance.freeMemory();
    }

    protected long totalMemory() {
        return analyzePerformance.totalMemory();
    }

    protected long time() {
        return analyzePerformance.time();
    }

    protected void startTracking() {
        analyzePerformance.startTracking();
    }

    protected void dumpStats() {
        analyzePerformance.dumpStats();
    }

}
