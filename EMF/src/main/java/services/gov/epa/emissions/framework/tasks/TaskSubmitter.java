package gov.epa.emissions.framework.tasks;
import java.util.ArrayList;

public interface TaskSubmitter {
	public void updateStatus(Runnable task);
	public void cancelTasks(ArrayList<Runnable> tasks);
	public void submitTasksToTaskManager(String submitterId, ArrayList<Runnable> tasks);
	//public void registerTaskManager(TaskManager tm);
	public void addTasksToSubmitter(ArrayList<Runnable> tasksForSubmitter);
	public void callbackFromTaskManager(String taskId, String status, String mesg);
	public String getSubmitterId();
	
    public void deregisterSubmitterFromRunManager(TaskSubmitter ts);
    public int getTaskCount();


}
