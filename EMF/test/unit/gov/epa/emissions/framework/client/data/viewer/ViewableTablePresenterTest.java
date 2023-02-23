package gov.epa.emissions.framework.client.data.viewer;

import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.data.TablePresenterDelegate;
import gov.epa.emissions.framework.services.EmfException;

import org.jmock.Mock;

public class ViewableTablePresenterTest extends EmfMockObjectTestCase {

    public void testShouldDelegateOnApplyConstraints() throws EmfException {
        Mock delegate = mock(TablePresenterDelegate.class);
        expects(delegate, 1, "doApplyConstraints");

        TablePresenter p = new ViewableTablePresenter((TablePresenterDelegate) delegate.proxy(), null);

        String rowFilter = "rowFilter";
        String sortOrder = "col2";
        p.doApplyConstraints(rowFilter, sortOrder);
    }

    public void testShoulObserveAndDisplayDelegateOnDisplay() throws Exception {
        Mock view = mock(ViewerPanelView.class);

        Mock delegate = mock(TablePresenterDelegate.class);
        expects(delegate, 1, "display");

        ViewableTablePresenter p = new ViewableTablePresenter((TablePresenterDelegate) delegate.proxy(),
                (ViewerPanelView) view.proxy());
        expects(view, 1, "observe", same(p));

        p.display();
    }

    public void testShouldDelegateOnTotalRecords() throws Exception {
        Mock delegate = mock(TablePresenterDelegate.class);
        stub(delegate, "totalRecords", Integer.valueOf(28));

        TablePresenter p = new ViewableTablePresenter((TablePresenterDelegate) delegate.proxy(), null);

        assertEquals(28, p.totalRecords());
    }

    public void testShouldDelegateDisplaySpecifiedPage() throws Exception {
        Mock delegate = mock(TablePresenterDelegate.class);
        expects(delegate, 1, "doDisplay", eq(Integer.valueOf(21)));

        TablePresenter p = new ViewableTablePresenter((TablePresenterDelegate) delegate.proxy(), null);
        p.doDisplay(21);
    }

    public void testShouldDelegateOnDisplayPageWithRecord() throws Exception {
        Mock delegate = mock(TablePresenterDelegate.class);
        expects(delegate, 1, "doDisplayPageWithRecord", eq(Integer.valueOf(21)));

        TablePresenter p = new ViewableTablePresenter((TablePresenterDelegate) delegate.proxy(), null);
        p.doDisplayPageWithRecord(21);
    }

    public void testShouldDelegateOnDisplayFirstPage() throws Exception {
        Mock delegate = mock(TablePresenterDelegate.class);
        expects(delegate, 1, "doDisplayFirst");

        TablePresenter p = new ViewableTablePresenter((TablePresenterDelegate) delegate.proxy(), null);
        p.doDisplayFirst();
    }

    public void testShouldDelegateOnDisplayPreviousPage() throws Exception {
        Mock delegate = mock(TablePresenterDelegate.class);
        expects(delegate, 1, "doDisplayPrevious");

        TablePresenter p = new ViewableTablePresenter((TablePresenterDelegate) delegate.proxy(), null);
        p.doDisplayPrevious();
    }

    public void testShouldDelegateOnDisplayNextPage() throws Exception {
        Mock delegate = mock(TablePresenterDelegate.class);
        expects(delegate, 1, "doDisplayNext");

        TablePresenter p = new ViewableTablePresenter((TablePresenterDelegate) delegate.proxy(), null);
        p.doDisplayNext();
    }

    public void testShouldDelegateDisplayLastPage() throws Exception {
        Mock delegate = mock(TablePresenterDelegate.class);
        expects(delegate, 1, "doDisplayLast");

        TablePresenter p = new ViewableTablePresenter((TablePresenterDelegate) delegate.proxy(), null);
        p.doDisplayLast();
    }

}
