package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.preference.UserPreference;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.qa.QAService;

import org.jmock.Mock;
import org.jmock.core.Constraint;

public class EditQAStepPresenterTest extends EmfMockObjectTestCase {

    public void testShouldRefreshTabViewAndCloseOnEdit() throws EmfException {
        Mock tabView = mock(EditableQATabView.class);
        expects(tabView, "refresh");

        Mock view = mock(EditQAStepView.class);
        expects(view, "disposeView");
        QAStep step = new QAStep();
        expects(view, 1, "save", step);

        Mock qaService = mock(QAService.class);
        qaService.expects(once()).method("updateWitoutCheckingConstraints").with(eq(new QAStep[]{step}));
        
        Mock session = mock(EmfSession.class);
        expects(session, 1,"qaService", qaService.proxy());

        EditQAStepPresenter presenter = new EditQAStepPresenter((EditQAStepView) view.proxy(), null,
                (EditableQATabView) tabView.proxy(), (EmfSession) session.proxy());
        presenter.save();
    }

    public void testShouldCloseViewOnClose() {
        Mock view = mock(EditQAStepView.class);
        expects(view, "disposeView");

        EditQAStepPresenter presenter = new EditQAStepPresenter((EditQAStepView) view.proxy(), null, null, null);
        presenter.close();
    }

    public void testShouldObserverAndDisplayViewOnDisplay() throws EmfException {
        QAStep step = new QAStep();
        QAStepResult result = new QAStepResult();

        EmfDataset dataset = new EmfDataset();
        //User user = new User();
        QAProgram[] programs = {};
        
        Mock qaService = mock(QAService.class);
        expects(qaService, 1, "getQAPrograms", programs);
        qaService.expects(once()).method("getQAStepResult").with(same(step)).will(returnValue(result));

        Mock session = mock(EmfSession.class);
        //expects(session, 1, "user", user);
        expects(session, 1, "qaService", qaService.proxy());
        setPreferences(session);

        Mock view = mock(EditQAStepView.class);
        expectsOnce(view, "display", new Constraint[] { same(step), same(result), same(programs), same(dataset),
                same(""), same(session.proxy()) });
        expectsOnce(view, "setMostRecentUsedFolder", "");

        EditQAStepPresenter presenter = new EditQAStepPresenter((EditQAStepView) view.proxy(), dataset, null,
                (EmfSession) session.proxy());
        expects(view, "observe");

        presenter.display(step, "");
    }

    private void setPreferences(Mock session) {
        Mock prefs = mock(UserPreference.class);
        session.stubs().method("preferences").will(returnValue(prefs.proxy()));
        prefs.stubs().method("mapLocalOutputPathToRemote").will(returnValue(""));
        prefs.stubs().method("outputFolder").will(returnValue(""));
    }
}
