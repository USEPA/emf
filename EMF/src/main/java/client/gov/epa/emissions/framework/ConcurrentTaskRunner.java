package gov.epa.emissions.framework;

public class ConcurrentTaskRunner implements TaskRunner {

    private final Object mutex = new Object();

    private boolean alive = false;

    private TaskDelegate delegate;

    // FIXME: use jdk 1.5 concurrent utlities for synchronization
    public void start(Runnable task) {
        alive = true;
        this.delegate = new TaskDelegate(task);
        Thread thread = new Thread(delegate);// FIXME: use thread pool or does it matter ?
        thread.start();
    }

    public void stop() {
        synchronized (mutex) {
            this.alive = false;
            mutex.notify();
        }
    }

    public class TaskDelegate implements Runnable {

        private Runnable task;

        public TaskDelegate(Runnable task) {
            this.task = task;
        }

        public void run() {
            synchronized (mutex) {
                while (alive) {
                    try {
                        // FIXME: what's a reasonable polling time ?
                        long pollInterval = 1 * 60 * 1000;
                        mutex.wait(pollInterval);
                    } catch (InterruptedException e) {
                        alive = false;
                    }
                    
                    task.run();
                }
            }
        }

    }
}
