package gov.epa.emissions.framework.client.admin;

public class CloseViewOnCancelStrategy implements RegisterCancelStrategy {

    public void execute(RegisterUserPresenter presenter) {
        presenter.doCancel();
    }

}
