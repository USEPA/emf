package gov.epa.emissions.commons.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AnalyzePerformance {

    private long startMemory;

    private long startTime;

    private SimpleDateFormat dateFormatter;

    public AnalyzePerformance() {
        dateFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    }

    public void dumpMemory() {
        System.out.println(usedMemory() + " MB");
    }

    public long usedMemory() {
        return (totalMemory() - freeMemory());
    }

    public long maxMemory() {
        return (Runtime.getRuntime().maxMemory() / megabyte());
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

    public long time() {
        return new Date().getTime() / 1000;
    }

    public void startTracking() {
        startMemory = usedMemory();
        startTime = time();
    }

    public void dumpStats() {
        long current = usedMemory();
        System.out.println("Current Time: "+dateFormatter.format(new Date()));
        System.out.println("Time Took: " + (time() - startTime) + " secs using " + (current - startMemory) + " MB memory "
                + "(current:" + current + ", start: " + startMemory + ")");
    }

}
