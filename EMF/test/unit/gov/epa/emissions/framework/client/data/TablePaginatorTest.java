package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.services.editor.DataAccessService;

import org.jmock.Mock;
import org.jmock.core.matcher.InvokeCountMatcher;

public class TablePaginatorTest extends EmfMockObjectTestCase {

    public void testShouldNotDisplayPageIfRecordRequestedExistsOnCurrentPage() throws Exception {
        Mock service = mock(DataAccessService.class);

        Page page = new Page();
        page.setMin(20);
        page.add(new VersionedRecord());
        page.add(new VersionedRecord());
        page.add(new VersionedRecord());
        stub(service, "getPage", page);

        Mock view = mock(TableView.class);
        view.expects(once()).method("display");

        TablePaginator paginator = new TablePaginatorImpl(null, null, (TableView) view.proxy(), (DataAccessService) service
                .proxy());
        paginator.doDisplayFirst();

        paginator.doDisplayPageWithRecord(20);
        paginator.doDisplayPageWithRecord(21);
        paginator.doDisplayPageWithRecord(22);
    }

    public void testShouldDisplaySpecificPage() throws Exception {
        Mock service = mock(DataAccessService.class);

        Page page = new Page();
        page.setNumber(3);
        service.stubs().method("getPage").with(ANYTHING, eq(new Integer(3))).will(returnValue(page));

        Mock view = mock(TableView.class);
        view.expects(once()).method("display");

        TablePaginator paginator = new TablePaginatorImpl(null, null, (TableView) view.proxy(), (DataAccessService) service
                .proxy());

        paginator.doDisplay(3);
    }

    public void testShouldReloadExistingPage() throws Exception {
        Mock service = mock(DataAccessService.class);

        Page page = new Page();
        page.setNumber(3);
        service.stubs().method("getPage").with(ANYTHING, eq(new Integer(3))).will(returnValue(page));

        Mock view = mock(TableView.class);
        view.expects(new InvokeCountMatcher(2)).method("display");

        TablePaginator paginator = new TablePaginatorImpl(null, null, (TableView) view.proxy(), (DataAccessService) service
                .proxy());

        paginator.doDisplay(3);
        paginator.reloadCurrent();
    }

    public void testShouldDisplayFirstPage() throws Exception {
        Mock service = mock(DataAccessService.class);

        Page page = new Page();
        page.setNumber(1);
        service.stubs().method("getPage").with(ANYTHING, eq(new Integer(1))).will(returnValue(page));

        Mock view = mock(TableView.class);
        view.expects(once()).method("display");

        TablePaginator paginator = new TablePaginatorImpl(null, null, (TableView) view.proxy(), (DataAccessService) service
                .proxy());

        paginator.doDisplayFirst();
    }

    public void testShouldDisplayNextPage() throws Exception {
        Mock service = mock(DataAccessService.class);

        Page page = new Page();
        page.setNumber(1);
        service.stubs().method("getPage").with(ANYTHING, eq(new Integer(1))).will(returnValue(page));
        stub(service, "getPageCount", new Integer(3));

        Mock view = mock(TableView.class);
        view.expects(once()).method("display");

        TablePaginator paginator = new TablePaginatorImpl(null, null, (TableView) view.proxy(), (DataAccessService) service
                .proxy());

        paginator.doDisplayNext();
    }

    public void testShouldDisplayLastPage() throws Exception {
        Mock service = mock(DataAccessService.class);

        Page page = new Page();
        page.setNumber(1);
        service.stubs().method("getPage").with(ANYTHING, eq(new Integer(7))).will(returnValue(page));
        stub(service, "getPageCount", new Integer(7));

        Mock view = mock(TableView.class);
        view.expects(once()).method("display");
        view.expects(once()).method("scrollToPageEnd");

        TablePaginator paginator = new TablePaginatorImpl(null, null, (TableView) view.proxy(), (DataAccessService) service
                .proxy());

        paginator.doDisplayLast();
    }

    public void testShouldIgnoreIfCurrentPageIsLastPageOnDisplayLastPage() throws Exception {
        Mock service = mock(DataAccessService.class);

        Page page = new Page();
        page.setNumber(92);
        service.stubs().method("getPage").with(ANYTHING, eq(new Integer(92))).will(returnValue(page));
        stub(service, "getPageCount", new Integer(92));

        Mock view = mock(TableView.class);
        view.expects(once()).method("display");
        view.expects(once()).method("scrollToPageEnd");

        TablePaginator paginator = new TablePaginatorImpl(null, null, (TableView) view.proxy(), (DataAccessService) service
                .proxy());

        paginator.doDisplayLast();
        paginator.doDisplayLast();
    }

    public void testShouldIgnoreIfCurrentPageIsPageOneDisplayFirstPage() throws Exception {
        Mock service = mock(DataAccessService.class);

        Page page = new Page();
        page.setNumber(1);
        service.stubs().method("getPage").with(ANYTHING, eq(new Integer(1))).will(returnValue(page));

        Mock view = mock(TableView.class);
        view.expects(once()).method("display");

        TablePaginator paginator = new TablePaginatorImpl(null, null, (TableView) view.proxy(), (DataAccessService) service
                .proxy());

        paginator.doDisplayFirst();
        paginator.doDisplayFirst();
    }
}
