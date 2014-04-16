package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.qa.QAService;

import org.jmock.Mock;
import org.jmock.core.Constraint;
import org.jmock.core.constraint.IsInstanceOf;

public class QAStepTemplatesPanelPresenterTest extends EmfMockObjectTestCase {

    public void testShouldObserveViewOnDisplay() {
        Mock view = mock(QAStepTemplatesPanelView.class);

        QAStepTemplatesPanelPresenter presenter = new QAStepTemplatesPanelPresenter(null, null,
                (QAStepTemplatesPanelView) view.proxy());
        expectsOnce(view, "observe", presenter);

        presenter.display();
    }

    public void testShouldDisplayEditQAStepTemplateWindow() throws EmfException {
        Mock view = mock(QAStepTemplatesPanelView.class);

        DatasetType type = new DatasetType();

        QAProgram[] programs = {};
        Mock qaService = mock(QAService.class);
        expects(qaService, 1, "getQAPrograms", programs);
        Mock session = mock(EmfSession.class);
        expects(session, 1, "qaService", qaService.proxy());

        QAStepTemplatesPanelPresenter presenter = new QAStepTemplatesPanelPresenter((EmfSession) session.proxy(), type,
                (QAStepTemplatesPanelView) view.proxy());

        QAStepTemplate template = new QAStepTemplate();
        Mock editor = mock(EditQAStepTemplateView.class);
        expects(editor, 1, "observe", new IsInstanceOf(EditQAStepTemplatesPresenter.class));
        expects(editor, 1, "display", new Constraint[] { same(type), same(programs), same(template), same(session.proxy()) });

        presenter.doEdit((EditQAStepTemplateView) editor.proxy(), template);
    }

}
