package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.commons.security.PasswordGenerator;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.UserService;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class LoginPresenterTest extends MockObjectTestCase {

    private Mock view;

    private LoginPresenter presenter;

    protected void setUp() {
        view = mock(LoginView.class);

        presenter = new LoginPresenter(null);
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").withNoArguments();
        
        presenter.display((LoginView) view.proxy());
    }

    public void testShouldAuthenticateWithEmfUserAdminOnNotifyLogin() throws Exception {
        User user = new User();
        user.setUsername("joey");
        user.setPassword("joeymoey12");

        Mock userAdmin = mock(UserService.class);
        userAdmin.expects(once()).method("authenticate").with(eq(user.getUsername()), eq(user.getEncryptedPassword()));
        userAdmin.expects(once()).method("getUser").with(eq(user.getUsername())).will(returnValue(user));

        LoginPresenter presenter = new LoginPresenter((UserService) userAdmin.proxy());

        assertSame(user, presenter.doLogin("joey", "joeymoey12"));
    }

    public void testShouldFailIfAuthenticateFailsOnNotifyLogin() throws Exception {
        Mock userAdmin = mock(UserService.class);
        Throwable exception = new EmfException("authentication failure");
        String encryptedPassword = new PasswordGenerator().encrypt("password");
        userAdmin.expects(once()).method("authenticate").with(eq("username"), eq(encryptedPassword)).will(
                throwException(exception));

        LoginPresenter presenter = new LoginPresenter((UserService) userAdmin.proxy());

        try {
            presenter.doLogin("username", "password");
        } catch (EmfException e) {
            assertSame(exception, e);
            return;
        }

        fail("should have raised an exception on authentication failure");
    }

    public void testShouldCloseViewOnNotifyCancel() {
        view.expects(once()).method("disposeView").withNoArguments();

        presenter.doCancel();
    }

}
