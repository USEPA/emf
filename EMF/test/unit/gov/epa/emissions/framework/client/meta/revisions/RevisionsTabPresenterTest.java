package gov.epa.emissions.framework.client.meta.revisions;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.Revision;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class RevisionsTabPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws Exception {
        Revision[] revisions = new Revision[0];
        Mock view = mock(RevisionsTabView.class);

        Mock service = mock(DataCommonsService.class);
        Mock session = mock(EmfSession.class);
        EmfDataset dataset = new EmfDataset();
        dataset.setId(2);
        service.stubs().method("getRevisions").with(eq(dataset.getId())).will(returnValue(revisions));

        //RevisionsTabPresenter presenter = new RevisionsTabPresenter(dataset, (DataCommonsService) service.proxy());
        RevisionsTabPresenter presenter = new RevisionsTabPresenter(dataset, (EmfSession) session.proxy());
        view.expects(once()).method("display").with(eq(revisions), same(presenter));

        presenter.display((RevisionsTabView) view.proxy());
    }

    public void testShouldDisplayNoteViewOnDisplayNote() throws Exception {
        EmfDataset dataset = new EmfDataset();
        RevisionsTabPresenter presenter = new RevisionsTabPresenter(dataset, null);

        Revision revision = new Revision();
        Mock view = mock(RevisionView.class);
        view.expects(once()).method("display").with(same(revision), same(dataset));

        presenter.doViewRevision(revision, (RevisionView) view.proxy());
    }
}
