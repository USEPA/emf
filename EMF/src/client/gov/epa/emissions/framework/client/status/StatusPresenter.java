package gov.epa.emissions.framework.client.status;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.TaskRunner;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.ui.RefreshObserver;

public class StatusPresenter implements RefreshObserver {

    private DataCommonsService service;

    private StatusView view;

    private User user;

    private StatusMonitor monitor;

    private TaskRunner runner;

    public StatusPresenter(User user, DataCommonsService servoce, TaskRunner runner) {
        this.user = user;
        this.service = servoce;
        this.runner = runner;

        this.monitor = new StatusMonitor();
    }

    public void stop() {
        this.runner.stop();
    }

    public class StatusMonitor implements Runnable {
        public void run() {
            doRefresh();
        }
    }

    public void display(StatusView view) {
        this.view = view;
        view.observe(this);
        view.display();

        runner.start(monitor);
    }

    public void doRefresh() {
        try {
            Status[] statuses = service.getStatuses(user.getUsername());
            view.update(statuses);
        } catch (EmfException e) {
            view.notifyError(e.getMessage());
        }
    }

    public void doClear() {
        view.clear();
    }

    public void close() {
        runner.stop();
    }

}
