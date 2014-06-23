package gov.epa.emissions.framework;

public interface TaskRunner {

    void start(Runnable runnable);

    void stop();

}
