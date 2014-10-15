package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.CommonDebugLevel;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.editor.DataAccessService;
import gov.epa.emissions.framework.services.editor.DataAccessToken;

public class TablePaginatorImpl implements TablePaginator {

    private DataAccessService service;

    private Page page;

    private TableView view;

    private DataAccessToken token;

    TablePaginatorImpl(Version version, String table, TableView view, DataAccessService service) {
        this(new DataAccessToken(version, table), view, service);
    }

    public TablePaginatorImpl(DataAccessToken token, TableView view, DataAccessService service) {
        page = new Page();// page 0, uninitialized

        this.token = token;
        this.view = view;
        this.service = service;
    }

    public void doDisplayNext() throws EmfException {
        int pageNumber = pageNumber();
        if (pageNumber < pageCount())
            pageNumber++;

        doDisplay(pageNumber);
    }

    public void doDisplayPrevious() throws EmfException {
        int pageNumber = pageNumber();
        if (pageNumber > 1)
            pageNumber--;

        doDisplay(pageNumber);
    }

    public int pageNumber() {
        return page.getNumber();
    }

    public void doDisplay(int pageNumber) throws EmfException {
        if (pageNumber() == pageNumber)
            return;

        loadPage(pageNumber);
    }

    public void reloadCurrent() throws EmfException {
        loadPage(pageNumber());
    }

    private void loadPage(int pageNumber) throws EmfException {
        page = service.getPage(token(), pageNumber);
        
        if ( CommonDebugLevel.DEBUG_PAGE) {
            System.out.println("loadPage");
            page.print();
        }
        
        view.display(page);
    }

    public void doDisplayFirst() throws EmfException {
        if (pageNumber() != 1)
            doDisplay(1);
    }

    public void doDisplayLast() throws EmfException {
        int pageCount = pageCount();
        if (pageNumber() != pageCount) {
            doDisplay(pageCount);
            view.scrollToPageEnd();
        }
    }

    private int pageCount() throws EmfException {
        return service.getPageCount(token());
    }

    public void doDisplayPageWithRecord(int record) throws EmfException {
        if (isCurrent(record))
            return;

        page = service.getPageWithRecord(token(), record);
        
        if ( CommonDebugLevel.DEBUG_PAGE) {
            System.out.println("doDisplayPageWithRecord");
            page.print();
        }
        
        view.display(page);
    }

    public int totalRecords() throws EmfException {
        return service.getTotalRecords(token());
    }

    public DataAccessToken token() {
        return token;
    }

    public boolean isCurrent(int record) {
        return page.contains(record);
    }

}
