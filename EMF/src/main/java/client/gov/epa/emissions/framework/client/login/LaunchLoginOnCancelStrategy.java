package gov.epa.emissions.framework.client.login;

import javax.swing.JFrame;

import gov.epa.emissions.framework.client.admin.RegisterCancelStrategy;
import gov.epa.emissions.framework.client.admin.RegisterUserPresenter;
import gov.epa.emissions.framework.client.transport.ServiceLocator;

public class LaunchLoginOnCancelStrategy implements RegisterCancelStrategy {

    private ServiceLocator serviceLocator;

    public LaunchLoginOnCancelStrategy(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    public void execute(RegisterUserPresenter presenter) {
        presenter.doCancel();

        launchLoginWindow();
    }

    private void launchLoginWindow() {
        LoginWindow view = new LoginWindow(serviceLocator);
        view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        LoginPresenter loginPresenter = new LoginPresenter(serviceLocator.userService());
        loginPresenter.display(view);
    }

}
