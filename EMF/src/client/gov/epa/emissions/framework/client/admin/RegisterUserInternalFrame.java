package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;

public class RegisterUserInternalFrame extends DisposableInteralFrame implements RegisterUserDesktopView {

    private RegisterUserPanel view;

    public RegisterUserInternalFrame(PostRegisterStrategy postRegisterStrategy, DesktopManager desktopManager) {
        super("Register New User", desktopManager);
        super.setName("registerNewUser");
        view = new RegisterUserPanel(postRegisterStrategy, new CloseViewOnCancelStrategy(), this, new AddAdminOption(),
                this, null);

        super.dimensions(view.getSize());
        super.getContentPane().add(view);
    }

    public void observe(RegisterUserPresenter presenter) {
        view.observe(presenter);
    }

    public void windowClosing() {
        view.closeWindow();
    }

}
