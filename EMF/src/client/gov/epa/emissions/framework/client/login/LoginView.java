package gov.epa.emissions.framework.client.login;

public interface LoginView {
    void observe(LoginPresenter presenter);

    void disposeView();

    void display();
    
    void ssoLogin();

}
