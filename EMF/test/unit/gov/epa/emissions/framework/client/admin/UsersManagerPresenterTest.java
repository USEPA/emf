package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.UserService;

import java.util.Date;

import org.jmock.Mock;
import org.jmock.core.constraint.IsInstanceOf;

public class UsersManagerPresenterTest extends EmfMockObjectTestCase {

    private Mock view;

    private UsersManagerPresenter presenter;

    private Mock session;

    private Mock service;

    protected void setUp() throws EmfException {
        session = mock(EmfSession.class);
        service = mock(UserService.class);
        presenter = new UsersManagerPresenter((EmfSession) session.proxy(), (UserService) service.proxy());

        view = mock(UsersManagerView.class);

        view.expects(once()).method("observe").with(eq(presenter));

        User[] users = new User[0];
        stub(service, "getUsers", users);
        expectsOnce(view, "display", users);

        presenter.display((UsersManagerView) view.proxy());
    }

    public void testShouldCloseViewOnClickOfCloseButton() {
        view.expects(once()).method("disposeView").withNoArguments();
        presenter.doClose();
    }

    public void testShouldDeleteUserAfterObtainingLockOnNotifyDelete() throws Exception {
        Mock owner = mock(User.class);
        stub(owner, "getUsername", "joe");

        User ownerProxy = (User) owner.proxy();
        stub(session, "user", ownerProxy);

        Mock user = mock(User.class);
        User userProxy = (User) user.proxy();
        stub(user, "getUsername", "matts");
        stub(user, "isLocked", ownerProxy, new Boolean(true));

        service.expects(once()).method("deleteUser").with(same(userProxy));
        service.expects(once()).method("obtainLocked").with(same(ownerProxy), same(userProxy)).will(
                returnValue(userProxy));

        User[] users = new User[] { userProxy };
        view.expects(once()).method("refresh");

        presenter.doDelete(users);
    }

    public void testShouldAbortWhenFailedToObtainLockOnNotifyDelete() throws Exception {
        Mock owner = mock(User.class);
        stub(owner, "getUsername", "joe");

        User ownerProxy = (User) owner.proxy();
        stub(session, "user", ownerProxy);

        Mock user = mock(User.class);
        User userProxy = (User) user.proxy();
        stub(user, "getUsername", "matts");
        stub(user, "getLockOwner", null);
        stub(user, "getLockDate", new Date());
        stub(user, "isLocked", ownerProxy, new Boolean(false));

        service.expects(once()).method("obtainLocked").with(same(ownerProxy), same(userProxy)).will(
                returnValue(userProxy));

        try {
            presenter.doDelete(new User[] { userProxy });
        } catch (EmfException e) {
            return;
        }

        fail("Should have raised an error if failed to delete user");
    }

    public void testShouldDisplayRegisterNewUserViewOnDisplayRegisterNewUser() {
        Mock registerUserView = mock(RegisterUserDesktopView.class);
        registerUserView.expects(once()).method("observe").with(new IsInstanceOf(RegisterUserPresenter.class));
        registerUserView.expects(once()).method("display");

        RegisterUserDesktopView viewProxy = (RegisterUserDesktopView) registerUserView.proxy();
        view.expects(once()).method("refresh").withNoArguments();

        presenter.doRegisterNewUser(viewProxy);
    }

    public void testShouldDisplayUpdateUserViewOnDisplayUpdateUser() throws Exception {
        Mock updateUserView = mock(UpdatableUserView.class);

        UpdatableUserView viewProxy = (UpdatableUserView) updateUserView.proxy();
        view.expects(once()).method("refresh").withNoArguments();

        User user = new User();
        user.setUsername("name");

        Mock updatePresenter = mock(UpdateUserPresenter.class);
        updatePresenter.expects(once()).method("display").with(same(viewProxy), eq(null));

        presenter.updateUser(viewProxy, null, (UpdateUserPresenter) updatePresenter.proxy());
    }

    public void testShouldRefreshViewOnClickOfRefreshButton() throws EmfException {
        User[] users = new User[0];
        view.expects(once()).method("refresh").with(same(users));
        service.stubs().method("getUsers").will(returnValue(users));

        presenter.doRefresh();
    }

    public void testShouldNotDeleteCurrentlyLoggedInUserOnNotifyDelete() throws Exception {
        User user = new User();
        user.setUsername("joe");
        stub(session, "user", user);

        try {
            presenter.doDelete(new User[] { user });
        } catch (EmfException e) {
            assertEquals("Cannot delete yourself - '" + user.getUsername() + "'", e.getMessage());
            return;
        }

        fail("should not have allowed deletion of currently logged in user");
    }

    public void testShouldNotDeleteAdminOnNotifyDelete() throws Exception {
        User admin = new User();
        admin.setUsername("admin");
        try {
            presenter.doDelete(new User[] { admin });
        } catch (EmfException e) {
            assertEquals("Cannot delete EMF super user - 'admin'", e.getMessage());
            return;
        }

        fail("should not have allowed deletion of EMF super user");
    }

}
