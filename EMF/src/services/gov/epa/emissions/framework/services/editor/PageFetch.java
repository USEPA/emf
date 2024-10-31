package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.CommonDebugLevel;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.PageReader;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.editor.ChangeSets.ChangeSetsIterator;
import gov.epa.emissions.framework.tasks.DebugLevels;

import javax.persistence.EntityManager;

public class PageFetch {

    private DataAccessCache cache;

    private RecordsFilter filter;

    public PageFetch(DataAccessCache cache) {
        this.cache = cache;
        filter = new RecordsFilter();
    }

    public Page getPage(DataAccessToken token, int pageNumber, EntityManager entityManager) throws Exception {
        Page page = filteredPage(token, pageNumber, entityManager);
        setRange(page, token, entityManager);

        if ( CommonDebugLevel.DEBUG_PAGE) {
            System.out.println("------\n------\n------\nPageFetch:getPage()\n------\n------\n------");
            page.print();
        }
        
        return page;
    }

    void setRange(Page page, DataAccessToken token, EntityManager entityManager) throws Exception {
        if (DebugLevels.DEBUG_19()) {
            System.out.println("PageFetch:setRange():Page null ? " + (page == null));
            if (page != null)
                System.out.println("\tPage number: " + page.getNumber() + " max: " + page.getMax() + " min: " + page.getMin());
        }
        
        int previousPage = page.getNumber() - 1;
        int previousPagesTotal = totalSizeOfPreviousPagesUpto(token, previousPage, entityManager);
        int min = page.count() == 0 ? previousPagesTotal : previousPagesTotal + 1;

        page.setMin(min);
    }

    private Page filteredPage(DataAccessToken token, int pageNumber, EntityManager entityManager) throws Exception {
        PageReader reader = cache.reader(token);
        Page page = reader.page(pageNumber);
        
        if ( CommonDebugLevel.DEBUG_PAGE) {
            System.out.println("------\n------\n------\nPageFetch:getPage()\n------\n------\n------");
            page.print();
        }
        
        ChangeSets changesets = cache.changesets(token, pageNumber, entityManager);
        
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

    public Page getPageWithRecord(DataAccessToken token, int record, EntityManager entityManager) throws Exception {
        int pageCount = getPageCount(token);
        int pageNumber = pageNumber(token, record, pageCount, entityManager);
        
        return getPage(token, pageNumber, entityManager);
    }

    int pageNumber(DataAccessToken token, int record, int pageCount, EntityManager entityManager) throws Exception {
        int pageSize = cache.pageSize(token);
        int low = 0;
        int high = low;
        for (int i = 1; i <= pageCount; i++) {
            ChangeSets sets = cache.changesets(token, i, entityManager);
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

    public int getTotalRecords(DataAccessToken token, EntityManager entityManager) throws Exception {
        PageReader reader = cache.reader(token);
        return reader.totalRecords() + netRecordCountIncreaseDueToChanges(token, entityManager);
    }

    private int netRecordCountIncreaseDueToChanges(DataAccessToken token, EntityManager entityManager) throws Exception {
        int total;
        ChangeSets changesets = cache.changesets(token, entityManager);
        total = 0;
        for (ChangeSetsIterator iter = changesets.iterator(); iter.hasNext();) {
            ChangeSet element = iter.next();
            total += element.netIncrease();
        }

        return total;
    }

    public int defaultPageSize(EntityManager entityManager) {
        return cache.defaultPageSize(entityManager);
    }

    int totalSizeOfPreviousPagesUpto(DataAccessToken token, int last, EntityManager entityManager) throws Exception {
        int result = 0;
        int pageSize = defaultPageSize(entityManager);

        for (int i = 1; i <= last; i++) {
            ChangeSets sets = cache.changesets(token, i, entityManager);
            result += pageSize + sets.netIncrease();
        }

        return result;
    }

}
