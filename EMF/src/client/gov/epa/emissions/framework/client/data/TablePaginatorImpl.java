package gov.epa.emissions.framework.client.data;

import java.awt.Container;
import java.util.concurrent.ExecutionException;

import gov.epa.emissions.commons.CommonDebugLevel;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.swingworker.GenericSwingWorker;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.editor.DataAccessService;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.ui.MessagePanel;

public class TablePaginatorImpl implements TablePaginator {

    private DataAccessService service;

    private Page page;

    private TableView view;

    private DataAccessToken token;

    private Container parentContainer;

    private MessagePanel messagePanel;

    private Integer pageCount;

    TablePaginatorImpl(Version version, String table, TableView view, DataAccessService service,
            Container parentContainer, MessagePanel messagePanel) {
        this(new DataAccessToken(version, table), view, service, parentContainer, messagePanel);
    }

    public TablePaginatorImpl(DataAccessToken token, TableView view, DataAccessService service,
            Container parentContainer, MessagePanel messagePanel) {
        page = new Page();// page 0, uninitialized

        this.token = token;
        this.view = view;
        this.service = service;
        this.parentContainer = parentContainer;
        this.messagePanel = messagePanel;
    }

    private class TablePaginatorSwingWorker extends GenericSwingWorker<Void> {
        public TablePaginatorSwingWorker(Container parentContainer, MessagePanel messagePanel, int pageNumber) {
            super(parentContainer, messagePanel);
            this.pageNumber = pageNumber;
            view.clear();
        }

        private int pageNumber;

         private Page page;

        @Override
        public Void doInBackground() throws EmfException {
            //just need to populate once
            if (pageCount == null)
                pageCount = pageCount();
            
            // fix bad page numbers
            if (pageNumber > pageCount)
                pageNumber = pageCount;
            if (pageNumber <= 0)
                pageNumber = 1;

            page = service.getPage(token(), pageNumber);
            TablePaginatorImpl.this.page = page;

            if (CommonDebugLevel.DEBUG_PAGE) {
                System.out.println("loadPage");
                page.print();
            }

            return null;
        }

        @Override
        public void done() {
            try {
                // perform background processing...
                get();

                // display page in table
                view.display(page);

                // if paging to last page, then scroll to page end
                if (pageNumber == pageCount) {
                    view.scrollToPageEnd();
                }

            } catch (InterruptedException e1) {
                messagePanel.setError(e1.getMessage());
                // setErrorMsg(e1.getMessage());
            } catch (ExecutionException e1) {
                messagePanel.setError(e1.getMessage());
                // setErrorMsg(e1.getCause().getMessage());
            } finally {
                finalize();
            }
        }
    }

    public void doDisplayNext() throws EmfException {
        new TablePaginatorSwingWorker(parentContainer, messagePanel, pageNumber() + 1).execute();
        // doDisplay(pageNumber);
    }

    public void doDisplayPrevious() throws EmfException {
        new TablePaginatorSwingWorker(parentContainer, messagePanel, pageNumber() - 1).execute();
    }

    public int pageNumber() {
        return page.getNumber();
    }

    public void doDisplay(int pageNumber) throws EmfException {
//        if (pageNumber() == pageNumber)
//            return;

        // loadPage(pageNumber);
        new TablePaginatorSwingWorker(parentContainer, messagePanel, pageNumber).execute();
    }

    public void reloadCurrent() throws EmfException {
        // loadPage(pageNumber());
        new TablePaginatorSwingWorker(parentContainer, messagePanel, pageNumber()).execute();
    }

    // private void loadPage(int pageNumber) throws EmfException {
    // page = service.getPage(token(), pageNumber);
    //
    // if ( CommonDebugLevel.DEBUG_PAGE) {
    // System.out.println("loadPage");
    // page.print();
    // }
    //
    // view.display(page);
    // }
    //
    public void doDisplayFirst() throws EmfException {
        int pageNumber = pageNumber();
        if (pageNumber != 1)
            new TablePaginatorSwingWorker(parentContainer, messagePanel, 1).execute();
    }

    @Override
    public void clear() {
        view.clear();
    }
    
    public void doDisplayLast() throws EmfException {
        new TablePaginatorSwingWorker(parentContainer, messagePanel, pageCount).execute();
    }

    private int pageCount() throws EmfException {
        return service.getPageCount(token());
    }

    public void doDisplayPageWithRecord(final int record) throws EmfException {
        if (isCurrent(record))
            return;

        class DisplayPageWithRecordSwingWorker extends GenericSwingWorker<Void> {
            
            public DisplayPageWithRecordSwingWorker(Container parentContainer, MessagePanel messagePanel) {
                super(parentContainer, messagePanel);
                view.clear();
            }
//
            @Override
            public Void doInBackground() throws EmfException {

                page = service.getPageWithRecord(token(), record);

                if (CommonDebugLevel.DEBUG_PAGE) {
                    System.out.println("doDisplayPageWithRecord");
                    page.print();
                }

                return null;
            }

            @Override
            public void done() {
                try {
                    // perform background processing...
                    get();

                    // display page in table
                    view.display(page);

                } catch (InterruptedException e1) {
                    messagePanel.setError(e1.getMessage());
                    // setErrorMsg(e1.getMessage());
                } catch (ExecutionException e1) {
                    messagePanel.setError(e1.getMessage());
                    // setErrorMsg(e1.getCause().getMessage());
                } finally {
                    finalize();
                }
            }
        }
        new DisplayPageWithRecordSwingWorker(parentContainer, messagePanel).execute();
    }

    private Integer totalRecords;
    
    public int getTotalRecords() throws EmfException {
//        System.out.println("TablPginatorImpl.totalRecords token() = " + token());
//        if (totalRecords == null)
            totalRecords = service.getTotalRecords(token());
        return totalRecords;
    }

    public DataAccessToken token() {
        return token;
    }

    public boolean isCurrent(int record) {
        return page.contains(record);
    }

}
