package gov.epa.emissions.framework.client.data.sector;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.data.DataCommonsService;

import java.util.Date;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class EditableSectorPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewAfterObtainingLockOnDisplay() throws Exception {
        Sector sector = new Sector();
        Mock view = mock(EditableSectorView.class);

        User owner = new User();
        owner.setUsername("name");
        sector.setLockOwner(owner.getUsername());
        sector.setLockDate(new Date());

        Mock service = mock(DataCommonsService.class);
        service.expects(once()).method("obtainLockedSector").with(same(owner), same(sector)).will(returnValue(sector));

        Mock session = session(owner, service.proxy());

        EditableSectorPresenter presenter = new EditableSectorPresenterImpl((EmfSession) session.proxy(),
                (EditableSectorView) view.proxy(), null, sector);
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(eq(sector));

        presenter.doDisplay();
    }

    private Mock session(User user, Object dataCommonsProxy) {
        Mock session = mock(EmfSession.class);
        session.stubs().method("user").withNoArguments().will(returnValue(user));
        session.stubs().method("dataCommonsService").withNoArguments().will(returnValue(dataCommonsProxy));

        return session;
    }

    public void testShouldShowNonEditViewAfterFailingToObtainLockForSectorOnDisplay() throws Exception {
        Sector sector = new Sector();// no lock
        User user = new User();
        user.setUsername("name");

        Mock service = mock(DataCommonsService.class);
        service.expects(once()).method("obtainLockedSector").with(same(user), same(sector)).will(returnValue(sector));

        Mock session = session(user, service.proxy());

        Mock view = mock(ViewableSectorView.class);
        view.expects(once()).method("observe").with(new IsInstanceOf(ViewableSectorPresenterImpl.class));
        view.expects(once()).method("display").with(eq(sector));

        EditableSectorPresenter presenter = new EditableSectorPresenterImpl((EmfSession) session.proxy(), null,
                (ViewableSectorView) view.proxy(), sector);

        presenter.doDisplay();
    }

    public void testShouldCloseViewOnClose() throws Exception {
        Sector sector = new Sector();
        Mock view = mock(EditableSectorView.class);
        view.expects(once()).method("disposeView");

        User user = new User();
        user.setUsername("user");
        Mock service = mock(DataCommonsService.class);
        service.expects(once()).method("releaseLockedSector").with(same(sector)).will(returnValue(sector));

        Mock session = session(user, service.proxy());

        EditableSectorPresenter presenter = new EditableSectorPresenterImpl((EmfSession) session.proxy(),
                (EditableSectorView) view.proxy(), null, sector);

        presenter.doClose();
    }

    public void testShouldUpdateSectorReleaseLockAndCloseOnSave() throws Exception {
        Sector sector = new Sector();

        Mock view = mock(EditableSectorView.class);
        view.expects(once()).method("disposeView");

        User user = new User();
        Mock service = mock(DataCommonsService.class);
        service.expects(once()).method("updateSector").with(same(sector)).will(returnValue(sector));

        Mock session = session(user, service.proxy());

        EditableSectorPresenter presenter = new EditableSectorPresenterImpl((EmfSession) session.proxy(),
                (EditableSectorView) view.proxy(), null, sector);

        presenter.doSave();
    }

}
