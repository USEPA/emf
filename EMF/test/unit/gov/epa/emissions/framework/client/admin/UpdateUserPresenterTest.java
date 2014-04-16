package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.basic.UserService;

import java.util.Date;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class UpdateUserPresenterTest extends MockObjectTestCase {

    private Mock service;

    private Mock view;

    protected void setUp() {
        service = mock(UserService.class);
        view = mock(UpdatableUserView.class);
    }

    public void testShouldUpdateUserOnSave() throws Exception {
        User user = new User();
        user.setUsername("joey");
        user.setName("Joey Moey");

        UpdateUserPresenter presenter = new UpdateUserPresenterImpl(null, user, (UserService) service.proxy());
        service.expects(once()).method("updateUser").with(eq(user));

        presenter.doSave();
    }

    public void testShouldDisplayViewAfterObtainingLockOnDisplay() throws Exception {
        UpdateUserPresenter presenter = displayablePresenter();

        presenter.display((UpdatableUserView) view.proxy(), null);
    }

    public void testShouldShowNonEditViewAfterFailingToObtainLockOfUserOnDisplay() throws Exception {
        User user = new User();// no lock
        user.setUsername("user");

        User owner = new User();
        owner.setUsername("owner");

        service.expects(once()).method("obtainLocked").with(same(owner), same(user)).will(returnValue(user));

        Mock session = mock(EmfSession.class);
        session.stubs().method("user").withNoArguments().will(returnValue(owner));

        Mock userView = mock(UserView.class);
        userView.expects(once()).method("display").with(same(user));

        UpdateUserPresenter presenter = new UpdateUserPresenterImpl((EmfSession) session.proxy(), user,
                (UserService) service.proxy());
        userView.expects(once()).method("observe").with(new IsInstanceOf(ViewUserPresenter.class));

        Mock updatable = mock(UpdatableUserView.class);
        
        presenter.display((UpdatableUserView) updatable.proxy(), (UserView) userView.proxy());
    }

    public void testShouldCloseViewOnCloseActionWithNoEdits() throws Exception {
        User user = new User();
        user.setUsername("user");

        UpdateUserPresenter presenter = displayablePresenter(user);
        presenter.display((UpdatableUserView) view.proxy(), null);

        view.expects(once()).method("disposeView").withNoArguments();
        service.expects(once()).method("releaseLocked").with(same(user)).will(returnValue(user));

        presenter.doClose();
    }

    public void testShouldConfirmLosingChangesOnCloseAfterEdits() throws Exception {
        UpdateUserPresenter presenter = displayablePresenter();
        presenter.display((UpdatableUserView) view.proxy(), null);

        view.expects(once()).method("closeOnConfirmLosingChanges").withNoArguments();

        presenter.onChange();
        presenter.doClose();
    }

    public void testShouldCloseWithNoPromptsOnSaveFollowedByClose() throws Exception {
        User user = new User();
        user.setUsername("joey");
        user.setName("Joey Moey");

        UpdateUserPresenter presenter = displayablePresenter(user);
        presenter.display((UpdatableUserView) view.proxy(), null);

        service.expects(once()).method("updateUser").with(eq(user));
        presenter.doSave();

        view.expects(once()).method("disposeView").withNoArguments();
        service.expects(once()).method("releaseLocked").with(same(user)).will(returnValue(user));
        presenter.doClose();
    }

    private UpdateUserPresenter displayablePresenter(User user) {
        User owner = new User();
        owner.setUsername("owner");
        user.setLockOwner(owner.getUsername());
        user.setLockDate(new Date());

        Mock session = mock(EmfSession.class);
        session.stubs().method("user").withNoArguments().will(returnValue(owner));

        service.expects(once()).method("obtainLocked").with(same(owner), same(user)).will(returnValue(user));

        UpdateUserPresenter presenter = new UpdateUserPresenterImpl((EmfSession) session.proxy(), user,
                (UserService) service.proxy());
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(same(user));

        return presenter;
    }

    private UpdateUserPresenter displayablePresenter() throws Exception {
        User user = new User();
        user.setUsername("user");

        return displayablePresenter(user);
    }
}
