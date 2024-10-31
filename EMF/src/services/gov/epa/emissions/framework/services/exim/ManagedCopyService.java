package gov.epa.emissions.framework.services.exim;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.LoggingServiceImpl;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.ManagedCaseService;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.tasks.CopyCaseSubmitter;
import gov.epa.emissions.framework.tasks.DebugLevels;
import gov.epa.emissions.framework.tasks.TaskManagerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ManagedCopyService {
    private static Log log = LogFactory.getLog(ManagedCopyService.class);

    private static int numOfRunningThread = 0;

    //private static Thread runningThread = null;

    private EntityManagerFactory entityManagerFactory;

    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MMddyy_HHmmss");

//    private ImportSubmitter importClientSubmitter = null;
//
    private ArrayList<Runnable>  copyTasks = new ArrayList<Runnable>();
//
//    private ArrayList<Runnable> importOutputTasks = new ArrayList<Runnable>();

    private static int svcCount = 0;

    private String svcLabel = null;

    protected DbServerFactory dbServerFactory;
    
    private CopyCaseSubmitter copyCaseSubmitter;
    
    //private ManagedCaseService caseService;

    public synchronized String myTag() {
        if (svcLabel == null) {
            svcCount++;
            this.svcLabel = "#" + svcCount + "-" + getClass().getName() + "-" + new Date().getTime();
        }
        log.info(svcLabel);

        return "For label: " + svcLabel + " # of active objects of this type= " + svcCount;
    }

    public ManagedCopyService(EntityManagerFactory entityManagerFactory) {
        this(DbServerFactory.get(), entityManagerFactory);
    }

    public ManagedCopyService(DbServerFactory dbServerFactory, EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        this.dbServerFactory = dbServerFactory;

        if (DebugLevels.DEBUG_17())
            System.out.println("ManagedCopyService: At the class initialization -- numOfRunningThread: "
                    + numOfRunningThread);
    }

    protected Services services() {
        Services services = new Services();
        services.setLoggingService(new LoggingServiceImpl(entityManagerFactory));
        services.setStatusService(new StatusDAO(entityManagerFactory));
        services.setDataService(new DataServiceImpl(entityManagerFactory));

        return services;
    }

    public synchronized String copyCases(User user, int[] toCopy, ManagedCaseService caseService) throws EmfException {
        if ( copyCaseSubmitter == null ){
            copyCaseSubmitter = new CopyCaseSubmitter();        
            TaskManagerFactory.getCopyTaskManager().registerTaskSubmitter(copyCaseSubmitter);
        }
        //this.caseService = caseService;
        Services services = services();
        try {
            for (int i = 0; i < toCopy.length; i++) {
                Case caseToCopy = caseService.getCase(toCopy[i]);
                //Case caseToCopy = getCase(toCopy[0]);
                CopyTask copyTask = new CopyTask(caseToCopy, user, services, dbServerFactory, entityManagerFactory, caseService);
                copyTasks.add(copyTask);
                copyCaseSubmitter.addTasksToSubmitter(copyTasks);
                copyTasks.removeAll(copyTasks);
            }
        }catch (Exception e) {
            e.printStackTrace();
            //log.error("ERROR copying case : " + caseToCopy.getName());
            throw new EmfException(e.getMessage());
        }
        return copyCaseSubmitter.getSubmitterId();
    }   
   
}
