package gov.epa.emissions.framework.tasks;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class TaskManagerFactory {
	private static TaskManagerFactory ref;
	
	private ExportTaskManager exportTaskManager;
	
    public void setExportTaskManager(ExportTaskManager exportTaskManager){
        this.exportTaskManager = exportTaskManager;
    }
    
	public TaskManagerFactory(){
		System.out.println();
	}
	
//	// Singleton factory method
	public synchronized TaskManagerFactory getTaskManagerFactory() {
        return this;
//		if (ref == null)
//			// it's ok, we can call this constructor
//			ref = new TaskManagerFactory();
//		return ref;
	}

    public synchronized ExportTaskManager getExportTaskManager(){
        return this.exportTaskManager;
    }
    
    public static synchronized CaseJobTaskManager getCaseJobTaskManager(HibernateSessionFactory sessionFactory){
        return CaseJobTaskManager.getCaseJobTaskManager(sessionFactory);
    }

    public static synchronized ImportTaskManager getImportTaskManager() {
        return ImportTaskManager.getImportTaskManager();
    }
    
    public static synchronized CopyTaskManager getCopyTaskManager() {
        return CopyTaskManager.getCopyTaskManager();
    }
	
}
