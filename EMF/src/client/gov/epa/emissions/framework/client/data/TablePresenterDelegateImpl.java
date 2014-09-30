package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.CommonDebugLevel;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.editor.DataAccessService;
import gov.epa.emissions.framework.services.editor.DataAccessToken;

import java.util.StringTokenizer;

public class TablePresenterDelegateImpl implements TablePresenterDelegate {

    private DataAccessService service;

    private TableMetadata tableMetadata;

    private TableView view;

    private TablePaginator paginator;

    private String rowFilter;

    private String sortOrder;

    private DatasetType datasetType;

    private Page page;

    public TablePresenterDelegateImpl(DatasetType datasetType, TablePaginator paginator, TableMetadata tableMetadata,
            TableView view, DataAccessService service) {
        this.datasetType = datasetType;
        this.service = service;
        this.tableMetadata = tableMetadata;
        this.view = view;
        this.rowFilter = view.getRowFilter(); 
        this.paginator = paginator;
    }

    public void display() throws EmfException {
        applyConstraints(rowFilter, datasetType.getDefaultSortOrder());
        paginator.doDisplayFirst();
    }

    public void reloadCurrent() throws EmfException {
        applyConstraints(rowFilter, sortOrder);
        paginator.reloadCurrent();
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
        return paginator.totalRecords();
    }

    public void updateFilteredCount() throws EmfException {
        view.updateFilteredRecordsCount(paginator.totalRecords());
    }

    public DataAccessToken token() {
        return paginator.token();
    }

    public void doApplyConstraints(String rowFilter, String sortOrder) throws EmfException {
        page = applyConstraints(rowFilter, sortOrder);
        
        if ( CommonDebugLevel.DEBUG_PAGE_2) {
            System.out.println("doApplyConstraints");
            page.print();
        }
        
        view.display(page);
        updateFilteredCount();
    }

    public void doApplyFormat() {
        view.display(page);
    }

    Page applyConstraints(String rowFilter, String sortOrder) throws EmfException {
        validateColsInSortOrder(sortOrder);
        validateRowFilterFormat(rowFilter);
        setRowAndSortFilter(rowFilter, sortOrder);

        return service.applyConstraints(token(), rowFilter, sortOrder);
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
