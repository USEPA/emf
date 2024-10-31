package gov.epa.emissions.framework.tasks;

import javax.persistence.EntityManagerFactory;

public class TaskManagerFactory {
	private static TaskManagerFactory ref;
	
	private TaskManagerFactory(){
		System.out.println();
	}
	
	// Singleton factory method
	public static synchronized TaskManagerFactory getTaskManagerFactory() {
		if (ref == null)
			// it's ok, we can call this constructor
			ref = new TaskManagerFactory();
		return ref;
	}

	public static synchronized ExportTaskManager getExportTaskManager(){
		return ExportTaskManager.getExportTaskManager();
	}
	
    public static synchronized CaseJobTaskManager getCaseJobTaskManager(EntityManagerFactory entityManagerFactory){
        return CaseJobTaskManager.getCaseJobTaskManager(entityManagerFactory);
    }

    public static synchronized ImportTaskManager getImportTaskManager() {
        return ImportTaskManager.getImportTaskManager();
    }
    
    public static synchronized CopyTaskManager getCopyTaskManager() {
        return CopyTaskManager.getCopyTaskManager();
    }
	
}
