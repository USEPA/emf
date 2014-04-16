package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.qa.QAService;

import org.jmock.Mock;
import org.jmock.core.Constraint;

public class SetQAStatusPresenterTest extends EmfMockObjectTestCase {

    public void testShouldRefreshTabViewAndCloseOnSave() throws EmfException {
        Mock tabView = mock(EditableQATabView.class);
        expects(tabView, "refresh");

        Mock view = mock(SetQAStatusView.class);
        expects(view,"save");
        expects(view, "disposeView");
        Mock session = mock(EmfSession.class);
        Mock qaService = mock(QAService.class);
        expects(session,1, "qaService", qaService.proxy());
        
        QAStep[] steps = new QAStep[]{};
        expectsOnce(qaService,"updateWitoutCheckingConstraints", steps);
        
        SetQAStatusPresenter presenter = new SetQAStatusPresenter((SetQAStatusView) view.proxy(), steps,
                (EditableQATabView) tabView.proxy(), (EmfSession) session.proxy());
        presenter.doSave();
    }

    public void testShouldCloseViewOnClose() {
        Mock view = mock(SetQAStatusView.class);
        expects(view, "disposeView");

        SetQAStatusPresenter presenter = new SetQAStatusPresenter((SetQAStatusView) view.proxy(), null, null, null);
        presenter.doClose();
    }

    public void testShouldObserverAndDisplayViewOnDisplay() {
        QAStep[] steps = {};
        User user = new User();

        Mock session = mock(EmfSession.class);
        stub(session, "user", user);

        Mock view = mock(SetQAStatusView.class);
        expectsOnce(view, "display", new Constraint[] { same(steps), same(user) });

        SetQAStatusPresenter presenter = new SetQAStatusPresenter((SetQAStatusView) view.proxy(), steps, null,
                (EmfSession) session.proxy());
        expects(view, "observe");

        presenter.display();
    }
}
