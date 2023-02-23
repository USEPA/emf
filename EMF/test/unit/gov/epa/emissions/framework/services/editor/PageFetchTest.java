package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.Page;

import org.hibernate.Session;
import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class PageFetchTest extends MockObjectTestCase {

    public void testShouldCalculateTotalSizeOfAllPreviousPages() throws Exception {
        Mock cache = mock(DataAccessCache.class);
        cache.stubs().method("defaultPageSize").will(returnValue(10));

        PageFetch fetch = new PageFetch((DataAccessCache) cache.proxy());

        DataAccessToken token = new DataAccessToken();
        Session session = (Session) mock(Session.class).proxy();

        Mock sets1 = mock(ChangeSets.class);
        sets1.stubs().method("netIncrease").will(returnValue(3));
        cache.stubs().method("changesets").with(same(token), eq(Integer.valueOf(1)), same(session)).will(
                returnValue(sets1.proxy()));

        Mock sets2 = mock(ChangeSets.class);
        sets2.stubs().method("netIncrease").will(returnValue(-5));
        cache.stubs().method("changesets").with(same(token), eq(Integer.valueOf(2)), same(session)).will(
                returnValue(sets2.proxy()));

        assertEquals(18, fetch.totalSizeOfPreviousPagesUpto(token, 2, session));
    }

    public void testShouldSetRangeBasedOnTotalRecordsOfPreviousPagesAndCurrentPageSize() throws Exception {
        Mock cache = mock(DataAccessCache.class);
        cache.stubs().method("defaultPageSize").will(returnValue(10));

        PageFetch fetch = new PageFetch((DataAccessCache) cache.proxy());

        DataAccessToken token = new DataAccessToken();
        Session session = (Session) mock(Session.class).proxy();

        Mock sets1 = mock(ChangeSets.class);
        sets1.stubs().method("netIncrease").will(returnValue(3));
        cache.stubs().method("changesets").with(same(token), eq(Integer.valueOf(1)), same(session)).will(
                returnValue(sets1.proxy()));

        Mock sets2 = mock(ChangeSets.class);
        sets2.stubs().method("netIncrease").will(returnValue(-5));
        cache.stubs().method("changesets").with(same(token), eq(Integer.valueOf(2)), same(session)).will(
                returnValue(sets2.proxy()));

        Page page = new Page();
        page.setNumber(3);
        fetch.setRange(page, token, session);
        assertEquals(18, page.getMin());
    }

    public void testShouldReturnPageByRecordNumber() throws Exception {
        Mock cache = mock(DataAccessCache.class);
        cache.stubs().method("pageSize").will(returnValue(10));

        PageFetch fetch = new PageFetch((DataAccessCache) cache.proxy());

        DataAccessToken token = new DataAccessToken();
        Session session = (Session) mock(Session.class).proxy();

        Mock sets1 = mock(ChangeSets.class);
        sets1.stubs().method("netIncrease").will(returnValue(3));// page 1 - 13 records
        cache.stubs().method("changesets").with(same(token), eq(Integer.valueOf(1)), same(session)).will(
                returnValue(sets1.proxy()));

        Mock sets2 = mock(ChangeSets.class);
        sets2.stubs().method("netIncrease").will(returnValue(-1));// page 2 - 9 records
        cache.stubs().method("changesets").with(same(token), eq(Integer.valueOf(2)), same(session)).will(
                returnValue(sets2.proxy()));

        assertEquals(1, fetch.pageNumber(token, 12, 2, session));
        assertEquals(2, fetch.pageNumber(token, 14, 2, session));
        assertEquals(1, fetch.pageNumber(token, 10, 2, session));
        assertEquals(2, fetch.pageNumber(token, 22, 2, session));
    }
}
