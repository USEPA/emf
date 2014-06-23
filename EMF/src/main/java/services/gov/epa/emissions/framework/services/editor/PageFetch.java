package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.CommonDebugLevel;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.PageReader;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.editor.ChangeSets.ChangeSetsIterator;
import gov.epa.emissions.framework.tasks.DebugLevels;

import org.hibernate.Session;

public class PageFetch {

    private DataAccessCache cache;

    private RecordsFilter filter;

    public PageFetch(DataAccessCache cache) {
        this.cache = cache;
        filter = new RecordsFilter();
    }

    public Page getPage(DataAccessToken token, int pageNumber, Session session) throws Exception {
        Page page = filteredPage(token, pageNumber, session);
        setRange(page, token, session);

        if ( CommonDebugLevel.DEBUG_PAGE) {
            System.out.println("------\n------\n------\nPageFetch:getPage()\n------\n------\n------");
            page.print();
        }
        
        return page;
    }

    void setRange(Page page, DataAccessToken token, Session session) throws Exception {
        if (DebugLevels.DEBUG_19()) {
            System.out.println("PageFetch:setRange():Page null ? " + (page == null));
            if (page != null)
                System.out.println("\tPage number: " + page.getNumber() + " max: " + page.getMax() + " min: " + page.getMin());
        }
        
        int previousPage = page.getNumber() - 1;
        int previousPagesTotal = totalSizeOfPreviousPagesUpto(token, previousPage, session);
        int min = page.count() == 0 ? previousPagesTotal : previousPagesTotal + 1;

        page.setMin(min);
    }

    private Page filteredPage(DataAccessToken token, int pageNumber, Session session) throws Exception {
        PageReader reader = cache.reader(token);
        Page page = reader.page(pageNumber);
        
        if ( CommonDebugLevel.DEBUG_PAGE) {
            System.out.println("------\n------\n------\nPageFetch:getPage()\n------\n------\n------");
            page.print();
        }
        
        ChangeSets changesets = cache.changesets(token, pageNumber, session);
        
        if (DebugLevels.DEBUG_19()) {
            System.out.println("PageFetch:filteredPage():Page null from PageReader? " + (page == null));
            if (page != null)
                System.out.println("\tPage number: " + page.getNumber());
            
            System.out.println("\tchangesets null from cache? " + (changesets == null));
            if (changesets != null)
                System.out.println("\tNumber of changesets: " + changesets.size() + " has changes? " + changesets.hasChanges() + " net increase: " + changesets.netIncrease());
        }

        return filter.filter(page, changesets);
    }

    public int getPageCount(DataAccessToken token) throws Exception {
        PageReader reader = cache.reader(token);
        return reader.totalPages();
    }

    public Page getPageWithRecord(DataAccessToken token, int record, Session session) throws Exception {
        int pageCount = getPageCount(token);
        int pageNumber = pageNumber(token, record, pageCount, session);
        
        return getPage(token, pageNumber, session);
    }

    int pageNumber(DataAccessToken token, int record, int pageCount, Session session) throws Exception {
        int pageSize = cache.pageSize(token);
        int low = 0;
        int high = low;
        for (int i = 1; i <= pageCount; i++) {
            ChangeSets sets = cache.changesets(token, i, session);
            int pageMax = pageSize + sets.netIncrease();
            high = low + pageMax;

            if ((low < record) && (record <= high))
                return i;

            low += pageMax;
        }
        if(record>high) //record id is in the last page
            return pageCount;

        throw new EmfException("invalid record id-"+record);
    }

    public int getTotalRecords(DataAccessToken token, Session session) throws Exception {
        PageReader reader = cache.reader(token);
        return reader.totalRecords() + netRecordCountIncreaseDueToChanges(token, session);
    }

    private int netRecordCountIncreaseDueToChanges(DataAccessToken token, Session session) throws Exception {
        int total;
        ChangeSets changesets = cache.changesets(token, session);
        total = 0;
        for (ChangeSetsIterator iter = changesets.iterator(); iter.hasNext();) {
            ChangeSet element = iter.next();
            total += element.netIncrease();
        }

        return total;
    }

    public int defaultPageSize(Session session) {
        return cache.defaultPageSize(session);
    }

    int totalSizeOfPreviousPagesUpto(DataAccessToken token, int last, Session session) throws Exception {
        int result = 0;
        int pageSize = defaultPageSize(session);

        for (int i = 1; i <= last; i++) {
            ChangeSets sets = cache.changesets(token, i, session);
            result += pageSize + sets.netIncrease();
        }

        return result;
    }

}
