package gov.epa.emissions.framework.tasks;

import gov.epa.emissions.framework.services.EmfException;

import java.util.Date;
import java.util.TimerTask;

public class WaitDelay extends TimerTask {

    public WaitDelay() {
        super();
    }

    public void run() {
        if (DebugLevels.DEBUG_9())
            System.out.println("Timer timed out and invoking the CaseJobTaskManager processQueue "
                    + new Date().getTime());
        try {
            CaseJobTaskManager.processTaskQueue();
        } catch (EmfException e) {
            e.printStackTrace();
            throw new RuntimeException("Error in CaseJobTaskManager: processQueue");
        }
    }

}
