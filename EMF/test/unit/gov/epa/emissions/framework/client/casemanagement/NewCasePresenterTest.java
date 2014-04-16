package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;

import org.jmock.Mock;

public class NewCasePresenterTest extends EmfMockObjectTestCase {

    public void testShouldObserveAndDisplayViewOnDisplay() {
        Mock view = mock(NewCaseView.class);
        expects(view, 1, "display");

        NewCasePresenter p = new NewCasePresenter(null, (NewCaseView) view.proxy(), null);
        expects(view, 1, "observe", same(p));

        p.doDisplay();
    }

    public void testShouldCloseViewOnClose() {
        Mock view = mock(NewCaseView.class);
        expects(view, 1, "disposeView");

        NewCasePresenter p = new NewCasePresenter(null, (NewCaseView) view.proxy(), null);

        p.doClose();
    }

    public void testShouldSaveCaseAndCloseViewOnSave() throws EmfException {
        Mock view = mock(NewCaseView.class);
        expects(view, 1, "disposeView");

        Mock service = mock(CaseService.class);
        Case newCase = new Case();
        expects(service, 1, "addCase", same(newCase));
        stub(service, "getCases", new Case[0]);

        Mock session = mock(EmfSession.class);
        stub(session, "caseService", service.proxy());
        User user = new User();
        stub(session, "user", user);

        Mock managerPresenter = mock(CaseManagerPresenter.class);
        expects(managerPresenter, 1, "doRefresh");

        NewCasePresenter p = new NewCasePresenter((EmfSession) session.proxy(), (NewCaseView) view.proxy(),
                (CaseManagerPresenter) managerPresenter.proxy());

        p.doSave(newCase);
        
        assertSame(user , newCase.getLastModifiedBy());
        assertNotNull(newCase.getLastModifiedDate());
    }

    public void testShouldRaiseErrorIfDuplicateCaseNameOnSave() {
        Mock view = mock(NewCaseView.class);

        Mock service = mock(CaseService.class);
        Case newCase = new Case("test-case");
        Case[] cases = new Case[] { new Case("test-case") };
        stub(service, "getCases", cases);

        Mock session = mock(EmfSession.class);
        stub(session, "caseService", service.proxy());

        NewCasePresenter p = new NewCasePresenter((EmfSession) session.proxy(), (NewCaseView) view.proxy(), null);

        try {
            p.doSave(newCase);
        } catch (EmfException e) {
            assertEquals("A Case named 'test-case' already exists.", e.getMessage());
            return;
        }

        fail("Should have raised an error if name is duplicate");
    }
}
