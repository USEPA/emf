package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.UserService;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class RegisterUserPresenterTest extends MockObjectTestCase {

    private RegisterUserPresenter presenter;

    private Mock view;

    private Mock userServices;

    protected void setUp() {
        view = mock(RegisterUserView.class);
        userServices = mock(UserService.class);

        presenter = new RegisterUserPresenter((UserService) userServices.proxy());
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").withNoArguments();
        
        presenter.display((RegisterUserView) view.proxy());
    }

    public void testShouldCreateUserAndLoginOnNotifyCreateUser() throws Exception {
        User user = new User();
        user.setUsername("joey");
        user.setPassword("passwd234");

        userServices.expects(once()).method("createUser").with(eq(user)).will(returnValue(user));
        userServices.expects(once()).method("authenticate").with(eq("joey"), eq(user.getEncryptedPassword()));

        presenter.doRegister(user, true);
    }

    public void testShouldCloseViewOnCancelAction() {
        view.expects(once()).method("disposeView").withNoArguments();

        presenter.doCancel();
    }

}
