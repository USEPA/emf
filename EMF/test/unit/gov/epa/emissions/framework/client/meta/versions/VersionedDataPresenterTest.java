package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.data.EmfDataset;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class VersionedDataPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() {
        Mock view = mock(VersionedDataView.class);
        EmfDataset dataset = new EmfDataset();

        view.expects(once()).method("display").with(same(dataset), new IsInstanceOf(EditVersionsPresenter.class));

        Mock session = mockSession();

        VersionedDataPresenter p = new VersionedDataPresenter(dataset, (EmfSession) session.proxy());
        view.expects(once()).method("observe").with(same(p));

        p.display((VersionedDataView) view.proxy());
    }

    private Mock mockSession() {
        return mock(EmfSession.class);
    }

    public void testShouldCloseViewOnClose() {
        Mock view = mock(VersionedDataView.class);
        VersionedDataPresenter p = displayPresenter(view);

        view.expects(once()).method("disposeView").withNoArguments();
        p.doClose();
    }

    private VersionedDataPresenter displayPresenter(Mock view) {
        EmfDataset dataset = new EmfDataset();

        view.expects(once()).method("display").with(same(dataset), new IsInstanceOf(EditVersionsPresenter.class));

        VersionedDataPresenter p = new VersionedDataPresenter(dataset, (EmfSession) mockSession().proxy());
        view.expects(once()).method("observe").with(same(p));

        p.display((VersionedDataView) view.proxy());

        return p;
    }
}
