package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.exim.ExImService;

public class EMFPersistedTasksRestoreClient {
    private static final String DEFAULT_URL = "http://localhost:8080/emf/services";// default

//    private static UserService userAdmin;
    private static ExImService eximService;
    
    private static CaseService caseService;
//    private static DataService dataService;

    public static void main(String[] args) throws Exception {
        if (args.length == 1 && args[0].equalsIgnoreCase("Help")) {
            displayHelp();
            return;
        }

        run(args);

    }

    private static void displayHelp() {
        System.out
                .println("Usage\njava "
                        + EMFPersistedTasksRestoreClient.class.getName()
                        + " [url]"
                        + "\n\turl - location of EMF Services. Defaults to "
                        + DEFAULT_URL
                        + "\n\tspecify '-DUSER_PREFERENCES=<full path to EMFPrefs.txt>' to override location of User Preferences");
    }

    /**
     * 
     */
    private static void run(String[] args) throws Exception {

        try {
            String url = DEFAULT_URL;
            if (args.length == 1)
                url = args[0];

            System.out.println("Starting EMF Task Manager Status Client");
            ServiceLocator serviceLocator = new RemoteServiceLocator(url);

            caseService = serviceLocator.caseService();
            eximService = serviceLocator.eximService();
            
            String restoreStatus = caseService.restoreTaskManagers();
            System.out.println(restoreStatus);
            
            String caseJobTaskManagerStatus = caseService.printStatusCaseJobTaskManager();
            System.out.println(caseJobTaskManagerStatus);
     
            String exportTaskManagerStatus = eximService.printStatusExportTaskManager();
            System.out.println(exportTaskManagerStatus);
            
            String importTaskManagerStatus = eximService.printStatusImportTaskManager();
            System.out.println(importTaskManagerStatus);

        } catch (Exception exc) {
            System.out.println("Exception starting client: " + exc.getMessage());
            exc.printStackTrace();
            throw exc;
        }
    }

}
