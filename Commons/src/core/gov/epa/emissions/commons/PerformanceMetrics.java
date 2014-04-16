package gov.epa.emissions.commons;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PerformanceMetrics {
    private long startMemory;

    private long startTime;

    private static Log LOG = LogFactory.getLog(PerformanceMetrics.class);

    public void startTracking(String desc) {
        startTracking();
        LOG.debug("Started tracking for: " + desc);
    }

    public void startTracking() {
        startMemory = usedMemory();
        startTime = time();
    }

    public void gc() {
        gc("");
    }

    public void gc(String prefix) {
        startTracking();

        LOG.debug("gc started....");
        doGc();
        LOG.debug("gc complete");

        dumpStats(prefix);
    }

    public void doGc() {
        System.gc();
    }

    public long time() {
        return new Date().getTime() / 1000;
    }

    public void dumpStats() {
        dumpStats("");
    }

    public void dumpStats(String prefix) {
        long end = usedMemory();
        LOG.debug(prefix + "\tGC Stats: Reduced memory by " + (startMemory - end) + " MB" + "(end:" + end + ", start: "
                + startMemory + ") in " + (time() - startTime) + " secs");
    }

    public long usedMemory() {
        return (totalMemory() - freeMemory());
    }

    public long freeMemory() {
        return Runtime.getRuntime().freeMemory() / megabyte();
    }

    private int megabyte() {
        return (1024 * 1024);
    }

    public long totalMemory() {
        return Runtime.getRuntime().totalMemory() / megabyte();
    }

}
