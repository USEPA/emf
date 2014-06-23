package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.PerformanceMetrics;

/**
 * enforces garbage collection after running the delegate task.
 */
public class GCEnforcerTask implements Runnable {

    private Runnable delegate;

    private String desc;

    public GCEnforcerTask(String desc, Runnable delegate) {
        this.desc = desc;
        this.delegate = delegate;
    }

    public void run() {
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.startTracking(desc);
        try {
            delegate.run();
        } finally {
            metrics.doGc();
            metrics.dumpStats("Shutting down " + desc);
        }
    }

}
