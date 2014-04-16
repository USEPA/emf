package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.DefaultEmfSession;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.EmfException;

import org.jmock.Mock;
import org.jmock.core.Constraint;
import org.jmock.core.constraint.IsInstanceOf;

public class EditQAStepTemplatesPresenterTest extends EmfMockObjectTestCase {

    public void testShouldObserveViewOnDisplay() throws EmfException {
        Mock view = mock(QAStepTemplatesPanelView.class);
        Mock editor = mock(EditQAStepTemplateView.class);
        Mock servlocator = mock(ServiceLocator.class);
        
        User user = new User("emf", "emf", "(999)123-4567", "xxx@xxx.com", "emf", "pass12345", true, true);
        EmfSession session = new DefaultEmfSession(user, (ServiceLocator)servlocator.proxy());
        DatasetType type = new DatasetType();
        QAProgram[] programs = {new QAProgram("program1")};
        QAStepTemplate template = new QAStepTemplate();
        
        
        EditQAStepTemplatesPresenter presenter = new EditQAStepTemplatesPresenterImpl((EditQAStepTemplateView)editor.proxy(), (QAStepTemplatesPanelView) view
                .proxy(), session);        
        expects(editor, 1, "observe", new IsInstanceOf(EditQAStepTemplatesPresenter.class));
        expects(editor, 1, "display", new Constraint[]{same(type),same(programs),same(template),same(session)});

        presenter.display(type,programs, template);
    }

    public void testShouldDoEdit() throws EmfException {
        Mock view = mock(QAStepTemplatesPanelView.class);
        Mock editor = mock(EditQAStepTemplateView.class);
        EditQAStepTemplatesPresenter presenter = new EditQAStepTemplatesPresenterImpl((EditQAStepTemplateView)editor.proxy(), (QAStepTemplatesPanelView) view
                .proxy(), null);  
        
        expects(view, 1, "refresh");
        expects(editor, 1, "loadTemplate");
        
        presenter.doEdit();
    }

}
