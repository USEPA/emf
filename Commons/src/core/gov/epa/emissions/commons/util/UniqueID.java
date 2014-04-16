package gov.epa.emissions.commons.util;

import java.util.concurrent.atomic.AtomicInteger;

public class UniqueID {
    private static long startTime = System.currentTimeMillis();
    private static long id = 0;
    static AtomicInteger aID = new AtomicInteger(0);

    public static synchronized String getUniqueID() {
        return "" + startTime + "_" + id++;
    }
    
    public static String getAtomicID() {
        return "" + startTime + "_" + aID.incrementAndGet(); 
    }
}
