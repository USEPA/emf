package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;

import org.jmock.Mock;
import org.jmock.core.constraint.IsInstanceOf;

public class CaseManagerPresenterTest extends EmfMockObjectTestCase {

    public void testShouldDisplayBrowserOnDisplay() throws EmfException {
        Mock browser = mock(CaseManagerView.class);
        Case[] cases = new Case[0];
        expects(browser, 1, "display", eq(cases));

        Mock service = mock(CaseService.class);
        stub(service, "getCases", cases);

        Mock session = mock(EmfSession.class);
        stub(session, "caseService", service.proxy());

        CaseManagerPresenter presenter = new CaseManagerPresenterImpl((EmfSession) session.proxy(),
                (CaseManagerView) browser.proxy());
        expects(browser, 1, "observe", same(presenter));

        presenter.swDisplay(cases);
    }

    public void testShouldRefreshBrowserOnRefresh() throws EmfException {
        Mock browser = mock(CaseManagerView.class);
        Case[] cases = new Case[0];
        expects(browser, 1, "refresh", eq(cases));

        Mock service = mock(CaseService.class);
        stub(service, "getCases", cases);

        Mock session = mock(EmfSession.class);
        stub(session, "caseService", service.proxy());

        CaseManagerPresenterImpl presenter = new CaseManagerPresenterImpl((EmfSession) session.proxy(),
                (CaseManagerView) browser.proxy());

        presenter.doRefresh();
    }

    public void testShouldRemoveCaseOnRemove() throws EmfException {
        Mock browser = mock(CaseManagerView.class);

        Mock service = mock(CaseService.class);
        Mock session = mock(EmfSession.class);
        stub(session, "caseService", service.proxy());

        Case caseObj = new Case();
        expects(service, 1, "removeCase", same(caseObj));

        CaseManagerPresenter presenter = new CaseManagerPresenterImpl((EmfSession) session.proxy(),
                (CaseManagerView) browser.proxy());

        presenter.doRemove(caseObj);
    }

    public void testShouldDisplayNewCaseViewOnNew() {
        CaseManagerPresenter presenter = new CaseManagerPresenterImpl(null, null);

        Mock view = mock(NewCaseView.class);
        expects(view, 1, "display");
        expects(view, 1, "observe", new IsInstanceOf(NewCasePresenter.class));

        presenter.doNew((NewCaseView) view.proxy());
    }

    public void testShouldDisplayCaseEditorOnEdit() throws EmfException {
        CaseManagerPresenterImpl presenter = new CaseManagerPresenterImpl(null, null);

        Mock editorPresenter = mock(CaseEditorPresenter.class);
        expects(editorPresenter, 1, "doDisplay");

        presenter.displayEditor((CaseEditorPresenter) editorPresenter.proxy());
    }

    public void testShouldCloseViewOnClose() {
        Mock browser = mock(CaseManagerView.class);
        expects(browser, 1, "disposeView");

        CaseManagerPresenter presenter = new CaseManagerPresenterImpl(null, (CaseManagerView) browser.proxy());

        presenter.doClose();
    }
}
