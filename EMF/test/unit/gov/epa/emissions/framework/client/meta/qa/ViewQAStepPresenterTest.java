package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;
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

public class ViewQAStepPresenterTest extends EmfMockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws EmfException {

        QAStep step = new QAStep(new QAStepTemplate(), 1);
        QAStepResult qaStepResult = new QAStepResult();
        Mock view = mock(QAStepView.class);

        Mock qaService = mock(QAService.class);
        QAProgram[] programs = new QAProgram[] {};
        expects(qaService, 1, "getQAPrograms", programs);
        expects(qaService, 1, "getQAStepResult", new Constraint[] { eq(step) }, qaStepResult);

        Mock preferences = mock(UserPreference.class);
        expects(preferences, 1, "outputFolder", "");

        Mock session = mock(EmfSession.class);
        expects(session, 1, "qaService", qaService.proxy());
        expects(session, 1, "preferences", preferences.proxy());

        User user = new User();
        user.setUsername("emf");
        expects(session, 1, "user", user);

        EmfDataset dataset = new EmfDataset();
        ViewQAStepPresenter presenter = new ViewQAStepPresenter((QAStepView) view.proxy(), dataset,
                (EmfSession) session.proxy());

        String versionName = step.getVersion() + "";

        expects(view, 1, "display", new Constraint[] { eq(step), eq(qaStepResult), eq(programs), eq(dataset), eq(user),
                eq(versionName) });
        
        expects(view, 1, "setMostRecentUsedFolder", eq(""));
        presenter.display(step, versionName);
    }

}
