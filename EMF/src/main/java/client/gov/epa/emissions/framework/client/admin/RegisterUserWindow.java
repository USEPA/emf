package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.gui.ChangeObserver;
import gov.epa.emissions.commons.gui.Changeable;
import gov.epa.emissions.commons.gui.Changeables;
import gov.epa.emissions.commons.gui.DefaultChangeables;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.DefaultChangeObserver;
import gov.epa.emissions.framework.client.EmfFrame;
import gov.epa.emissions.framework.client.login.LaunchLoginOnCancelStrategy;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

public class RegisterUserWindow extends EmfFrame implements RegisterUserView, ChangeObserver, ManageChangeables {

    private RegisterUserPanel view;

    private LaunchLoginOnCancelStrategy onCancelStrategy;

    private ChangeObserver changeObserver;

    private Changeables changeablesList;
    
    public RegisterUserWindow(ServiceLocator serviceLocator, PostRegisterStrategy postRegisterStrategy) {    
        super("RegisterUser", "Register New User");
        onCancelStrategy = new LaunchLoginOnCancelStrategy(serviceLocator);
        changeablesList = new DefaultChangeables(this);
        changeObserver = new DefaultChangeObserver(this);
        view = new RegisterUserPanel(postRegisterStrategy, onCancelStrategy, this, this, null);
        this.getContentPane().add(view);

        this.setSize(view.getSize());
        this.setLocation(ScreenUtils.getPointToCenter(this));
        display();
    }

    public RegisterUserWindow(ServiceLocator serviceLocator, PostRegisterStrategy postRegisterStrategy, User user) {    
        super("Reset Password", "Reset Password");
        onCancelStrategy = new LaunchLoginOnCancelStrategy(serviceLocator);
        changeablesList = new DefaultChangeables(this);
        changeObserver = new DefaultChangeObserver(this);
        view = new RegisterUserPanel(postRegisterStrategy, onCancelStrategy, this, this, user);
        this.getContentPane().add(view);

        this.setSize(view.getSize());
        this.setLocation(ScreenUtils.getPointToCenter(this));
        display();
    }

    protected void doClose() {
        RegisterUserPresenter presenter = view.getPresenter();
        onCancelStrategy.execute(presenter);
    }

    public void windowClosing() {
        view.closeWindow();
    }

    public void observe(RegisterUserPresenter presenter) {
        view.observe(presenter);
    }

    public void signalChanges() {
        changeObserver.signalChanges();
    }

    public void signalSaved() {
        changeObserver.signalSaved();
    }

    public void addChangeable(Changeable changeable) {
        changeablesList.add(changeable);
    }

    public void resetChanges() {
        // NOTE Auto-generated method stub
        
    }

}
