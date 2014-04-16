package gov.epa.emissions.framework.client.qa;

import org.jmock.Mock;
import org.jmock.core.Constraint;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.DefaultEmfSession;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.EmfException;

public class NewQAStepTemplatePresenterTest extends EmfMockObjectTestCase {

    public void testShouldAddQAStepTemplateToViewOnAdd() {
        Mock view = mock(QAStepTemplatesPanelView.class);
        Mock dialog = mock(NewQAStepTemplateView.class);
        QAStepTemplate stepTemplate = new QAStepTemplate();
        expectsOnce(view, "add", stepTemplate);
        
        NewQAStepTemplatePresenter presenter = new NewQAStepTemplatePresenter(
                (QAStepTemplatesPanelView) view.proxy(), (NewQAStepTemplateView)dialog.proxy());

        presenter.addNew(stepTemplate);
    }
    
    public void testShouldDisplayView() throws EmfException {
        Mock view = mock(QAStepTemplatesPanelView.class);
        Mock servlocator = mock(ServiceLocator.class);

        DatasetType type = new DatasetType();
        QAProgram[] programs = {};
        User user = new User("emf", "emf", "(999)123-4567", "xxx@xxx.com", "emf", "pass12345", true, true);
        EmfSession session = new DefaultEmfSession(user, (ServiceLocator)servlocator.proxy());
        
        Mock dialog = mock(NewQAStepTemplateView.class);
        expects(dialog, 1, "display", new Constraint[]{same(session), same(type),same(programs)});

        NewQAStepTemplatePresenter presenter = new NewQAStepTemplatePresenter(
                (QAStepTemplatesPanelView) view.proxy(), (NewQAStepTemplateView)dialog.proxy());

        expects(dialog, 1, "observe", same(presenter));
        
        presenter.display(type,programs, session);
    }
}
