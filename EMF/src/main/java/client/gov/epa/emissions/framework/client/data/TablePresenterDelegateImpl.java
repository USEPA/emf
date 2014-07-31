package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.CommonDebugLevel;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.client.swingworker.GenericSwingWorker;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.editor.DataAccessService;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.editor.DataViewService;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.Container;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

public class TablePresenterDelegateImpl implements TablePresenterDelegate {

    private DataAccessService service;

    private TableMetadata tableMetadata;

    private TableView view;

    private TablePaginator paginator;

    private String rowFilter;

    private String sortOrder;

    private DatasetType datasetType;

    private Page page;

    private Container parentContainer;

    private MessagePanel messagePanel;

    private DataViewService dataViewService;

    public TablePresenterDelegateImpl(DatasetType datasetType, TablePaginator paginator, TableMetadata tableMetadata,
            TableView view, DataAccessService service, DataViewService dataViewService, Container parentContainer, MessagePanel messagePanel) {
        this.datasetType = datasetType;
        this.service = service;
        this.dataViewService = dataViewService;
        this.tableMetadata = tableMetadata;
        this.view = view;
        this.rowFilter = view.getRowFilter(); 
        this.paginator = paginator;
        this.parentContainer = parentContainer;
        this.messagePanel = messagePanel;
    }

    public void display() throws EmfException {
        
        new GenericSwingWorker<Void>(parentContainer, messagePanel) {
            @Override
            public Void doInBackground() throws EmfException {
//                dataViewService.openSession(token()); //cache data...
//                applyConstraints(rowFilter, datasetType.getDefaultSortOrder());
                return null;
            }
            
            @Override
            public void done() {
                try {
                    get();
                    paginator.doDisplayFirst();
                } catch (InterruptedException e1) {
                    messagePanel.setError(e1.getMessage());
//                    setErrorMsg(e1.getMessage());
                } catch (ExecutionException e1) {
                    messagePanel.setError(e1.getMessage());
//                    setErrorMsg(e1.getCause().getMessage());
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        }.execute();
        
    }

    public void reloadCurrent() throws EmfException {
        new GenericSwingWorker<Void>(parentContainer, messagePanel) {
            @Override
            public Void doInBackground() throws EmfException {
                applyConstraints(rowFilter, sortOrder);

                return null;
            }
            
            @Override
            public void done() {
                try {
                    get();
                    paginator.reloadCurrent();
                } catch (InterruptedException e1) {
                    messagePanel.setError(e1.getMessage());
//                    setErrorMsg(e1.getMessage());
                } catch (ExecutionException e1) {
                    messagePanel.setError(e1.getMessage());
//                    setErrorMsg(e1.getCause().getMessage());
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        }.execute();
    }

    public void doDisplayNext() throws EmfException {
        paginator.doDisplayNext();
    }

    public void doDisplayPrevious() throws EmfException {
        paginator.doDisplayPrevious();
    }

    public void doDisplay(int pageNumber) throws EmfException {
        paginator.doDisplay(pageNumber);
    }

    public void doDisplayFirst() throws EmfException {
        paginator.doDisplayFirst();
    }

    public void doDisplayLast() throws EmfException {
        paginator.doDisplayLast();
    }

    public void doDisplayPageWithRecord(int record) throws EmfException {
        if ((record == 0) || paginator.isCurrent(record))
            return;
        paginator.doDisplayPageWithRecord(record);
    }

    public int totalRecords() throws EmfException {
        return paginator.getTotalRecords();
    }

    public void updateFilteredCount(int totalRecords) throws EmfException {
        view.updateFilteredRecordsCount(totalRecords);
    }

    public DataAccessToken token() {
        return paginator.token();
    }

    public void doApplyConstraints(final String rowFilter, final String sortOrder) throws EmfException {
        new GenericSwingWorker<Void>(parentContainer, messagePanel) {
            private int totalRecords;

            @Override
            public Void doInBackground() throws EmfException {
                page = applyConstraints(rowFilter, sortOrder);
                if ( CommonDebugLevel.DEBUG_PAGE_2) {
                    System.out.println("doApplyConstraints");
                    page.print();
                }
                
                totalRecords = paginator.getTotalRecords();

                return null;
            }
            
            @Override
            public void done() {
                try {
                    get();
                    view.display(page);
                    updateFilteredCount(totalRecords);
                } catch (InterruptedException e1) {
                    messagePanel.setError(e1.getMessage());
//                    setErrorMsg(e1.getMessage());
                } catch (ExecutionException e1) {
                    messagePanel.setError(e1.getMessage());
//                    setErrorMsg(e1.getCause().getMessage());
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        }.execute();
    }

    public void doApplyFormat() {
        view.display(page);
    }

    Page applyConstraints(String rowFilter, String sortOrder) throws EmfException {
        validateColsInSortOrder(sortOrder);
        validateRowFilterFormat(rowFilter);
        setRowAndSortFilter(rowFilter, sortOrder);

System.out.println("s applyConstraints " + System.currentTimeMillis());
        Page applyConstraints = service.applyConstraints(token(), rowFilter, sortOrder);
System.out.println("e applyConstraints " + System.currentTimeMillis());
        return applyConstraints;
    }

    private void validateRowFilterFormat(String rowFilter) throws EmfException {
        if (rowFilter != null && rowFilter.contains("\""))
            throw new EmfException("Invalid Row Filter: Please use single quotes instead of double quotes.");
    }

    public void setRowAndSortFilter(String rowFilter, String sortOrder) {
        this.rowFilter = rowFilter;
        this.sortOrder = sortOrder;
    }

    private void validateColsInSortOrder(String sortOrder) throws EmfException {
        for (StringTokenizer tokenizer = new StringTokenizer(sortOrder.trim(), ","); tokenizer.hasMoreTokens();) {
            String col = tokenizer.nextToken().trim().toLowerCase();
            if (col.toUpperCase().endsWith(" DESC"))
            {
                col = col.substring(0,col.length()-5);
            }
            if (!tableMetadata.containsCol(col))
                throw new EmfException("Sort Order contains an invalid column: " + col);
        }
    }

    public int pageNumber() {
        return paginator.pageNumber();
    }


}
