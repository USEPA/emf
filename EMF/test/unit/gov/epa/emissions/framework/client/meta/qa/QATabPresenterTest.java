package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.security.User;
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

public class QATabPresenterTest extends EmfMockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws EmfException {
        Mock view = mock(QATabView.class);
        EmfDataset dataset = new EmfDataset();
        dataset.setId(6);

        Mock qaService = mock(QAService.class);
        QAStep[] steps = new QAStep[0];
        expects(qaService, 1, "getQASteps", new Constraint[] { same(dataset) }, steps);

        Mock session = mock(EmfSession.class);
        expects(session, 1, "qaService", qaService.proxy());

        ViewQATabPresenter presenter = new ViewQATabPresenterImpl((QATabView) view.proxy(), dataset,
                (EmfSession) session.proxy());

        expectsOnce(view, "display", steps);
        expectsOnce(view, "observe", presenter);

        presenter.display();
    }

    public void testShouldDisplayQAStepViewOnView() throws EmfException {
        EmfDataset dataset = new EmfDataset();
        dataset.setId(6);
        QAStep step = new QAStep();
        QAStepResult result = new QAStepResult();
        QAProgram[] programs = {};

        User user = new User();
        user.setUsername("emf");

        Mock qaService = mock(QAService.class);
        expects(qaService, 1, "getQAPrograms", programs);
        expects(qaService, 1, "getQAStepResult", new Constraint[] { same(step) }, result);

        Mock preferences = mock(UserPreference.class);
        expects(preferences, 1, "outputFolder", "");

        Mock session = mock(EmfSession.class);
        expects(session, 1, "qaService", qaService.proxy());
        expects(session, 1, "user", user);
        expects(session, 1, "preferences", preferences.proxy());

        ViewQATabPresenterImpl presenter = new ViewQATabPresenterImpl(null, dataset, (EmfSession) session.proxy());

        Mock view = mock(QAStepView.class);
        expects(view, 1, "setMostRecentUsedFolder", eq(""));
        expects(view, 1, "display", new Constraint[] { eq(step), eq(result), eq(programs), eq(dataset), eq(user),
                eq(step.getVersion() + "") });
        ViewQAStepPresenter qaStepPresenter = new ViewQAStepPresenter((QAStepView) view.proxy(), dataset,
                (EmfSession) session.proxy());
        expects(view, 1, "observe", same(qaStepPresenter));

        presenter.view(step, (QAStepView) view.proxy(), qaStepPresenter);
    }

}
