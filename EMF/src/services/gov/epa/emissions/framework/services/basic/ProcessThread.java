package gov.epa.emissions.framework.services.basic;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProcessThread extends Thread {
    private static Log LOG = LogFactory.getLog(ProcessThread.class);
    
    private boolean threadDone = false;
    
    private Process p;
    
    private String cmd;
    
    private long sleep;
    
    private String errorMsg;
    
    public ProcessThread (Process p, String cmd, long sleep) {
        this.p = p;
        this.cmd = cmd;
        this.sleep = sleep;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
    
    public void done() {
        threadDone = true;
    }

    public void run() {
        long start = new Date().getTime();
        long end = new Date().getTime();
        
        while (!threadDone && end - start < sleep) {//wait for  long enough time
            try {
                Thread.sleep(30000); //wait for 30 seconds
            } catch (InterruptedException e) {
                //
            }
            
            end = new Date().getTime();
        }
        
        if (!threadDone) p.destroy();
        
        if (end - start >= sleep) {
            errorMsg = "There has been no response for " + (sleep/60000) + " minutes since the command was submitted.\n" + cmd;
            LOG.error(errorMsg);
        }
    }
}
