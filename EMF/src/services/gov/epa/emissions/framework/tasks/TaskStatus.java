package gov.epa.emissions.framework.tasks;

public interface TaskStatus {
    // Tagging interface
    public static final int NULL= 0;
    public static final int RUNNING=1;
    public static final int PENDING=2;
    public static final int COMPLETED=3;
    public static final int FAILED=4;
    public static final int CANCELED=5;
    
}
