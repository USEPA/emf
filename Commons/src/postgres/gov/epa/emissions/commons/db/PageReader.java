package gov.epa.emissions.commons.db;

import gov.epa.emissions.commons.CommonDebugLevel;
import gov.epa.emissions.commons.db.version.ScrollableVersionedRecords;
import gov.epa.emissions.commons.db.version.VersionedRecord;

import java.sql.SQLException;

public class PageReader {
    private int pageSize;

    private ScrollableVersionedRecords scrollableRecords;
    
    public PageReader(int pageSize, ScrollableVersionedRecords scrollableRecords) {
        this.pageSize = pageSize;
        this.scrollableRecords = scrollableRecords;
    }

    public int totalRecords() throws SQLException {
        return scrollableRecords.total();
    }

    public int totalPages() throws SQLException {
        return identifyPage(scrollableRecords.total());
    }

    /**
     * @param record
     *            starts at index '1' through n (total records)
     */
    public Page pageByRecord(int record) throws SQLException {
        return page(identifyPage(record));
    }

    private int identifyPage(int record) {
        float val = (float) record / pageSize;
        return (int) Math.ceil(val);
    }

    /**
     * 
     * @param pageNumber
     *            starts at index '1' through n (total pages)
     */
    public Page page(int pageNumber) throws SQLException {
        int actualPage = pageNumber - 1; // page '1' maps to page '0'
        
        if (actualPage >= totalPages())
            actualPage = 0;

        int start = actualPage * pageSize;
        int end = start + pageSize - 1;// since, end is inclusive in the range
        
        VersionedRecord[] records = scrollableRecords.range(start, end);
        
        // debug
        if ( CommonDebugLevel.DEBUG_PAGE) 
        {
            for ( VersionedRecord record : records) {
                if ( record !=null) {
                    Object[] objs = record.getTokens();
                    System.out.println("PageReader.page(int)");
                    if ( objs !=null)
                        for ( int i=0; i<objs.length; i++) {
                            if ( objs[i] == null) {
                                System.out.println(i+ "> null");
                            } else {
                                System.out.println(i+ "> class: " + objs[i].getClass() + ", value: " + objs[i]);
                            }
                        }
                }
            }
        }
        // end of debug

        Page page = new Page(pageNumber);
        page.setRecords(records);

        return page;
    }

    public void close() throws SQLException {
        scrollableRecords.close();
    }

    public int pageSize() {
        return pageSize;
    }
}
