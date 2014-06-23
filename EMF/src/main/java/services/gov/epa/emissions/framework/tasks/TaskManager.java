package gov.epa.emissions.framework.tasks;

public interface TaskManager {
//    int getSizeofTaskQueue();
//    int getSizeofWaitTable();
//    int getSizeofRunTable();
//    void shutDown() ;
//    void removeTask(Runnable task);
//    void removeTasks(ArrayList<?> tasks);
//    void registerTaskSubmitter(TaskSubmitter ts);
//    void deregisterSubmitter(TaskSubmitter ts);
//    void finalize() throws Throwable;

    // clone not supported needs to be added
    Object clone() throws CloneNotSupportedException;
}
