package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorPresenter;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorPresenterImpl;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;

import org.jmock.Mock;

public class CaseEditorPresenterTest extends EmfMockObjectTestCase {

    public void testShouldObserveLockCaseAndDisplayViewOnDisplay() throws EmfException {
        Mock view = mock(CaseEditorView.class);
        Mock caseObj = mock(Case.class);
        expects(view, 1, "display", same(caseObj.proxy()));
        stub(caseObj, "isLocked", Boolean.TRUE);

        Mock service = mock(CaseService.class);
        Mock session = mock(EmfSession.class);
        stub(session, "caseService", service.proxy());
        stub(session, "user", new User());
        expects(service, 1, "obtainLocked", caseObj.proxy());

        CaseEditorPresenter p = new CaseEditorPresenterImpl((Case) caseObj.proxy(), (EmfSession) session.proxy(),
                (CaseEditorView) view.proxy(), null);
        expects(view, 1, "observe", same(p));

        p.doDisplay();
    }

    public void testShouldCloseViewOnClose() throws EmfException {
        Mock view = mock(CaseEditorView.class);
        expects(view, 1, "disposeView");

        Mock service = mock(CaseService.class);
        Mock session = mock(EmfSession.class);
        stub(session, "caseService", service.proxy());
        expects(service, 1, "releaseLocked");

        CaseEditorPresenter p = new CaseEditorPresenterImpl(null, (EmfSession) session.proxy(), (CaseEditorView) view
                .proxy(), null);

        p.doClose();
    }

    public void testShouldSaveCaseAndCloseViewOnSave() throws EmfException {
        Mock view = mock(CaseEditorView.class);
        expects(view, 1, "disposeView");

        Mock service = mock(CaseService.class);
        Case caseObj = new Case("name");
        expects(service, 1, "updateCase", same(caseObj));
        stub(service, "getCases", new Case[0]);
        
        Mock user = mock(User.class);

        Mock session = mock(EmfSession.class);
        stub(session, "caseService", service.proxy());
        stub(session, "user", user.proxy());

        Mock managerPresenter = mock(CaseManagerPresenter.class);
        expects(managerPresenter, 1, "doRefresh");

        CaseEditorPresenter p = new CaseEditorPresenterImpl(caseObj, (EmfSession) session.proxy(),
                (CaseEditorView) view.proxy(), (CaseManagerPresenter) managerPresenter.proxy());

        p.doSave();
    }

    public void testShouldRaiseErrorIfDuplicateCaseNameOnSave() {
        Mock service = mock(CaseService.class);
        
        Case duplicateCase = new Case("case2");
        duplicateCase.setId(1243);
        Case caseObj = new Case("case2");
        caseObj.setId(9324);
        Case[] cases = new Case[] { new Case("case1"), duplicateCase, caseObj };
        stub(service, "getCases", cases);

        Mock session = mock(EmfSession.class);
        stub(session, "caseService", service.proxy());

        CaseEditorPresenter p = new CaseEditorPresenterImpl(caseObj, (EmfSession) session.proxy(), null, null);

        try {
            p.doSave();
        } catch (EmfException e) {
            assertEquals("Duplicate name - 'case2'.", e.getMessage());
            return;
        }

        fail("Should have raised an error if name is duplicate");
    }
}
