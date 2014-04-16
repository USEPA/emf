package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class ViewUserPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewAfterObtainingLockOnDisplay() throws Exception {
        User user = new User();
        user.setUsername("user");

        ViewUserPresenter presenter = new ViewUserPresenterImpl(user);

        Mock view = mock(UserView.class);
        view.expects(once()).method("display").with(same(user));
        view.expects(once()).method("observe").with(same(presenter));

        presenter.display((UserView) view.proxy());
    }

    public void testShouldCloseViewOnClose() throws Exception {
        User user = new User();
        user.setUsername("user");

        ViewUserPresenter presenter = new ViewUserPresenterImpl(user);

        Mock view = mock(UserView.class);
        view.expects(once()).method("display").with(same(user));
        view.expects(once()).method("observe").with(same(presenter));
        presenter.display((UserView) view.proxy());

        view.expects(once()).method("disposeView").withNoArguments();
        presenter.doClose();
    }

}
